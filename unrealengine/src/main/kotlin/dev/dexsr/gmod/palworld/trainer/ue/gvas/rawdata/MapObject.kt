package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast

object MapObject

sealed class MapObjectDict

fun MapObject.decode(
    reader: GvasReader,
    typeName: String,
    size: Int,
    path: String
): GvasProperty {
    require(typeName == "ArrayProperty") {
        "Expected ArrayProperty, got=$typeName"
    }

    val value = reader.property(typeName, size, path, nestedCallerPath = path)

    val arrayDict = value
        .value
        .cast<GvasArrayDict>()

    for (mapObject in arrayDict.value.cast<GvasStructArrayPropertyValue>().values) {
        /*
            # Decode Model
        map_object["Model"]["value"]["RawData"]["value"] = map_model.decode_bytes(
            reader, map_object["Model"]["value"]["RawData"]["value"]["values"]
        )
         */
        val rawDataProp = mapObject
            .cast<GvasStructMap>().v["Model"]
            .cast<GvasProperty>().value
            .cast<GvasStructDict>().value
            .cast<GvasStructMap>().v["RawData"]
            .cast<GvasProperty>()
        rawDataProp.value = GvasArrayDict(
            arrayType = arrayDict.arrayType,
            id = arrayDict.id,
            value = GvasTransformedArrayValue(
                value = MapModel.decodeBytes(
                    parentReader = reader,
                    bytes = rawDataProp.value.cast<GvasArrayDict>().value
                        .cast<GvasAnyArrayPropertyValue>().values
                        .cast<GvasByteArrayValue>().value
                )
            )
        )

        val connectorProp = mapObject
            .cast<GvasStructMap>().v["Model"]
            .cast<GvasProperty>().value
            .cast<GvasStructDict>().value
            .cast<GvasStructMap>().v["Connector"]
            .cast<GvasProperty>()
        val connectorRawDataProp = connectorProp.value
            .cast<GvasStructDict>().value
            .cast<GvasStructMap>().v["RawData"]
            .cast<GvasProperty>()
        connectorRawDataProp.value = GvasArrayDict(
            arrayType = arrayDict.arrayType,
            id = arrayDict.id,
            value = GvasTransformedArrayValue(
                value = Connector.decodeBytes(
                    parentReader = reader,
                    bytes = connectorRawDataProp.value.cast<GvasArrayDict>().value
                        .cast<GvasAnyArrayPropertyValue>().values
                        .cast<GvasByteArrayValue>().value
                )
            )
        )

        val buildProcessProp = mapObject
            .cast<GvasStructMap>().v["Model"]
            .cast<GvasProperty>().value
            .cast<GvasStructDict>().value
            .cast<GvasStructMap>().v["BuildProcess"]
            .cast<GvasProperty>()
        val buildProcessRawDataProp = buildProcessProp.value
            .cast<GvasStructDict>().value
            .cast<GvasStructMap>().v["RawData"]
            .cast<GvasProperty>()
        buildProcessRawDataProp.value = GvasArrayDict(
            arrayType = arrayDict.arrayType,
            id = arrayDict.id,
            value = GvasTransformedArrayValue(
                value = BuildProcess.decodeBytes(
                    parentReader = reader,
                    bytes = buildProcessRawDataProp.value.cast<GvasArrayDict>().value
                        .cast<GvasAnyArrayPropertyValue>().values
                        .cast<GvasByteArrayValue>().value
                )
            )
        )

        val concreteModelId = mapObject
            .cast<GvasStructMap>().v["MapObjectId"]
            .cast<GvasProperty>().value
            .cast<GvasNameDict>().value
        val concreteModelProp  = mapObject
            .cast<GvasStructMap>().v["ConcreteModel"]
            .cast<GvasProperty>()
        val concreteModelRawDataProp = concreteModelProp.value
            .cast<GvasStructDict>().value
            .cast<GvasStructMap>().v["RawData"]
            .cast<GvasProperty>()
        concreteModelRawDataProp.value = GvasArrayDict(
            arrayType = arrayDict.arrayType,
            id = arrayDict.id,
            value = GvasTransformedArrayValue(
                value = MapConcreteModel.decodeBytes(
                    parentReader = reader,
                    bytes = concreteModelRawDataProp.value.cast<GvasArrayDict>().value
                        .cast<GvasAnyArrayPropertyValue>().values
                        .cast<GvasByteArrayValue>().value,
                    objectId = concreteModelId
                )
            )
        )

        for (module in concreteModelProp.value.cast<GvasStructDict>().value.cast<GvasStructMap>().v["ModuleMap"].cast<GvasProperty>().value.cast<GvasMapDict>().value) {
            val moduleType = module["key"]
                .cast<String>()
            val moduleRawDataProp = module["value"].cast<GvasStructMap>().v["RawData"].cast<GvasProperty>()
            val moduleBytes = moduleRawDataProp.value.cast<GvasArrayDict>().value
                .cast<GvasAnyArrayPropertyValue>().values
                .cast<GvasByteArrayValue>().value
            moduleRawDataProp.value = GvasArrayDict(
                arrayType = arrayDict.arrayType,
                id = arrayDict.id,
                value = GvasTransformedArrayValue(
                    value = MapConcreteModelModule.decodeBytes(
                        parentReader = reader,
                        bytes = moduleBytes,
                        moduleType = moduleType
                    )
                )
            )
        }
    }

    value.value = CustomByteArrayRawData(
        customType = path,
        id = arrayDict.id,
        value = value.value
    )

    return value
}

fun MapObject.encode(
    writer: GvasWriter,
    typeName: String,
    data: GvasProperty
): Int {
    TODO()
}