package net.requef.flesh.render

import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.util.Identifier
import software.bernie.geckolib.cache.`object`.BakedGeoModel
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.renderer.GeoRenderer
import software.bernie.geckolib.renderer.layer.GeoRenderLayer

class EyesGeoRenderLayer<T>(
    private val eyesTextureResource: Identifier,
    renderer: GeoRenderer<T>
) : GeoRenderLayer<T>(renderer) where T: GeoAnimatable, T: Entity {
    override fun render(
        poseStack: MatrixStack,
        animatable: T,
        bakedModel: BakedGeoModel?,
        renderType: RenderLayer?,
        bufferSource: VertexConsumerProvider,
        buffer: VertexConsumer?,
        partialTick: Float,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val armorRenderType = RenderLayer.getArmorCutoutNoCull(eyesTextureResource)
        getRenderer().reRender(
            getDefaultBakedModel(animatable),
            poseStack,
            bufferSource,
            animatable,
            armorRenderType,
            bufferSource.getBuffer(armorRenderType),
            partialTick,
            packedLight,
            OverlayTexture.DEFAULT_UV,
            1f,
            1f,
            1f,
            1f
        )
    }
}