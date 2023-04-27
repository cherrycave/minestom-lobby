package net.cherrycave.minestom.lobby.handlers

import net.kyori.adventure.key.Key
import net.minestom.server.instance.block.BlockHandler
import net.minestom.server.tag.Tag
import net.minestom.server.utils.NamespaceID

object SkullHandler : BlockHandler {
    override fun getNamespaceId(): NamespaceID {
        return NamespaceID.from(Key.key("minecraft:skull"))
    }

    override fun getBlockEntityTags(): MutableCollection<Tag<*>> {
        return mutableListOf(
            Tag.String("ExtraType"),
            Tag.NBT("SkullOwner")
        )
    }
}