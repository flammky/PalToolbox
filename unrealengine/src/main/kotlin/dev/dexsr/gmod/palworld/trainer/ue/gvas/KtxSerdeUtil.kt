package dev.dexsr.gmod.palworld.trainer.ue.gvas

// denote that the given dictionary properties should be embedded to the parent
// TODO: use this
class EmbeddedGvasDict <T: GvasDict>(val v: T)

class EmbeddedProperty <T: Any>(val v: T)