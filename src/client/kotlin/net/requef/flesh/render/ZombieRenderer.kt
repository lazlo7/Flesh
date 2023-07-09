package net.requef.flesh.render

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.mob.MobEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.RotationAxis
import software.bernie.geckolib.cache.`object`.GeoBone
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoEntityRenderer
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer

open class ZombieRenderer<T>(ctx: EntityRendererFactory.Context, model: GeoModel<T>)
    : GeoEntityRenderer<T>(ctx, model) where T: MobEntity, T: GeoAnimatable {
    val rightHeldItemName = "right_arm_held_item"
    val leftHeldItemName = "left_arm_held_item"

    init {
        renderLayers.addLayer(object : BlockAndItemGeoLayer<T>(this) {
            override fun getStackForBone(bone: GeoBone, animatable: T): ItemStack? = when(bone.name) {
                leftHeldItemName -> if (animatable.isLeftHanded) animatable.mainHandStack else animatable.offHandStack
                rightHeldItemName -> if (animatable.isLeftHanded) animatable.offHandStack else animatable.mainHandStack
                else -> null
            }

            override fun getTransformTypeForStack(
                bone: GeoBone,
                stack: ItemStack?,
                animatable: T
            ): ModelTransformationMode = when (bone.name) {
                leftHeldItemName, rightHeldItemName -> ModelTransformationMode.THIRD_PERSON_RIGHT_HAND
                else -> ModelTransformationMode.NONE
            }

            override fun renderStackForBone(
                poseStack: MatrixStack,
                bone: GeoBone?,
                stack: ItemStack,
                animatable: T,
                bufferSource: VertexConsumerProvider?,
                partialTick: Float,
                packedLight: Int,
                packedOverlay: Int
            ) {
                poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f))
                super.renderStackForBone(
                    poseStack,
                    bone,
                    stack,
                    animatable,
                    bufferSource,
                    partialTick,
                    packedLight,
                    packedOverlay
                )
            }
        })
    }
}