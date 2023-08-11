package net.requef.flesh.render

import net.minecraft.client.render.entity.EntityRendererFactory
import net.requef.flesh.entity.Bomber
import net.requef.flesh.model.BomberModel
import software.bernie.geckolib.cache.`object`.GeoBone
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer

class BomberRenderer(
    ctx: EntityRendererFactory.Context,
    model: GeoModel<Bomber> = BomberModel(),
    netherEyes: Boolean = false
) : ZombieRenderer<Bomber>(ctx, model, netherEyes) {
    init {
        renderLayers.addLayer(object : BlockAndItemGeoLayer<Bomber>(this) {
            override fun getBlockForBone(bone: GeoBone, animatable: Bomber) = when (bone.name) {
                leftHeldItemName -> animatable.type.leftCarriedBlock
                rightHeldItemName -> animatable.type.rightCarriedBlock
                else -> super.getBlockForBone(bone, animatable)
            }
        })
    }
}