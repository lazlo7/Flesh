package net.requef.flesh.render

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.model.Dilation
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.requef.flesh.Flesh
import net.requef.flesh.mob.MobRegistry

@Environment(EnvType.CLIENT)
object Renderers {
    private val modelOvergrownLayer = EntityModelLayer(Flesh.identifier("overgrown"), "main")

    fun register() {
        EntityRendererRegistry.register(MobRegistry.overgrown) { ctx -> OvergrownRenderer(ctx) }
        EntityModelLayerRegistry.registerModelLayer(modelOvergrownLayer) {
            TexturedModelData.of(
                BipedEntityModel.getModelData(
                    Dilation.NONE, 0.0f
                ), 64, 64
            )
        }
    }
}