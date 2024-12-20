package net.cherrycave.lobby.handlers

import net.kyori.adventure.key.Key
import net.minestom.server.instance.block.BlockHandler
import net.minestom.server.tag.Tag
import net.minestom.server.utils.NamespaceID

object BannerHandler : BlockHandler {

    override fun getNamespaceId(): NamespaceID {
        return NamespaceID.from(Key.key("minecraft:banner"))
    }

    override fun getBlockEntityTags(): MutableCollection<Tag<*>> {
        return mutableListOf(
            Tag.String("CustomName"),
            Tag.NBT("Patterns")
        )
    }

}