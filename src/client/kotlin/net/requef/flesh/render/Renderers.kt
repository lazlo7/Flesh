package net.requef.flesh.render

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.requef.flesh.entity.Entities
import net.requef.flesh.model.ArcherModel
import net.requef.flesh.model.BloodyModel
import net.requef.flesh.model.OvergrownModel

@Environment(EnvType.CLIENT)
object Renderers {
    fun register() {
        EntityRendererRegistry.register(Entities.zombie, ::ZombieRenderer)
        EntityRendererRegistry.register(Entities.overgrown) { ctx -> ZombieRenderer(ctx, OvergrownModel()) }
        EntityRendererRegistry.register(Entities.archer) { ctx -> ZombieRenderer(ctx, ArcherModel()) }
        EntityRendererRegistry.register(Entities.bloody) { ctx -> ZombieRenderer(ctx, BloodyModel()) }
        EntityRendererRegistry.register(Entities.bomber) { ctx -> BomberRenderer(ctx) }
    }
}