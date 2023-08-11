package net.requef.flesh.render

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.requef.flesh.entity.Entities
import net.requef.flesh.model.ArcherModel
import net.requef.flesh.model.BloodyModel
import net.requef.flesh.model.OvergrownModel

@Environment(EnvType.CLIENT)
object Renderers {
    private fun <T : Entity> register(type: EntityType<T>, factory: EntityRendererFactory<T>) =
        EntityRendererRegistry.register(type, factory)

    fun register() {
        register(Entities.zombie, ::ZombieRenderer)
        register(Entities.archer) { ctx -> ZombieRenderer(ctx, ArcherModel()) }
        register(Entities.overgrown) { ctx -> ZombieRenderer(ctx, OvergrownModel()) }
        register(Entities.bloody) { ctx -> ZombieRenderer(ctx, BloodyModel()) }
        register(Entities.bomber) { ctx -> BomberRenderer(ctx) }

        register(Entities.netherZombie) { ctx -> ZombieRenderer(ctx, netherEyes = true) }
        register(Entities.netherOvergrown) { ctx -> ZombieRenderer(ctx, OvergrownModel(), true) }
        register(Entities.netherArcher) { ctx -> ZombieRenderer(ctx, ArcherModel(), true) }
        register(Entities.netherBloody) { ctx -> ZombieRenderer(ctx, BloodyModel(), true) }
        register(Entities.netherBomber) { ctx -> BomberRenderer(ctx, netherEyes = true) }
    }
}