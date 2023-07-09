package net.requef.flesh.render

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.requef.flesh.entity.Mobs

@Environment(EnvType.CLIENT)
object Renderers {
    fun register() {
        EntityRendererRegistry.register(Mobs.overgrown, ::OvergrownRenderer)
        EntityRendererRegistry.register(Mobs.archer, ::ArcherRenderer)
    }
}