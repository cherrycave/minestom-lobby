package net.cherrycave.lobby.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import net.minestom.server.coordinate.Pos

typealias SerializablePos = @Serializable(with = PositionSerializer::class) Pos

class PositionSerializer : KSerializer<Pos> {
    override val descriptor: SerialDescriptor = SerializablePos.serializer().descriptor
    override fun deserialize(decoder: Decoder): Pos {
        val input = decoder.decodeSerializableValue(SerializablePos.serializer())
        return Pos(input.x, input.y, input.z)
    }

    override fun serialize(encoder: Encoder, value: Pos) {
        encoder.encodeStructure(SerializablePos.serializer().descriptor) {
            encodeDoubleElement(SerializablePos.serializer().descriptor, 0, value.x)
            encodeDoubleElement(SerializablePos.serializer().descriptor, 1, value.y)
            encodeDoubleElement(SerializablePos.serializer().descriptor, 2, value.z)
        }
    }

    @Serializable
    data class SerializablePos(val x: Double, val y: Double, val z: Double)
}