package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasReader
import dev.dexsr.gmod.palworld.trainer.ue.gvas.OpenGvasDict
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Connector

sealed class ConnectorDict : OpenGvasDict()

class ConnectorData(
    val supportedLevel: Int,
    val connect: ConnectorConnect,
    val otherConnectors: ArrayList<ConnectorOtherConnector>?
) : ConnectorDict()

class ConnectorConnect(
    val index: Byte,
    val anyPlace: ArrayList<ConnectorConnectInfo>
)

class ConnectorConnectInfo(
    val connectToModelInstanceId: String,
    val index: Byte
)

class ConnectorOtherConnector(
    val index: Byte,
    val connect: ArrayList<ConnectorConnectInfo>
)

class ConnectorRawData(
    val values: ByteArray
) : ConnectorDict()

fun Connector.decodeBytes(
    parentReader: GvasReader,
    bytes: ByteArray
) : ConnectorDict {
    if (bytes.isEmpty()) {
        return ConnectorRawData(bytes)
    }

    val reader = parentReader.copy(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))

    val data = ConnectorData(
        supportedLevel = reader.readInt(),
        connect = ConnectorConnect(
            index = reader.readByte(),
            anyPlace = reader.readArray {
                ConnectorConnectInfo(
                    connectToModelInstanceId = reader.uuid().toString(),
                    index = reader.readByte()
                )
            },
        ),
        otherConnectors = if (!reader.isEof()) {
            ArrayList<ConnectorOtherConnector>()
                .apply {
                    while (!reader.isEof()) {
                        add(ConnectorOtherConnector(reader.readByte(), reader.readArray {
                            ConnectorConnectInfo(
                                connectToModelInstanceId = reader.uuid().toString(),
                                index = reader.readByte()
                            )
                        }))
                    }
                }
        } else null
    )

    return data
}