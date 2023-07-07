package net.requef.flesh.render

import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.entity.mob.MobEntity
import net.minecraft.item.ItemStack
import software.bernie.geckolib.cache.`object`.GeoBone
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoEntityRenderer
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer

open class ZombieRenderer<T>(ctx: EntityRendererFactory.Context, model: GeoModel<T>)
    : GeoEntityRenderer<T>(ctx, model) where T: MobEntity, T: GeoAnimatable {
    val leftArmBoneName = "left_arm"
    val rightArmBoneName = "right_arm"

    init {
        renderLayers.addLayer(object : BlockAndItemGeoLayer<T>(this) {
            override fun getStackForBone(bone: GeoBone, animatable: T): ItemStack? = when(bone.name) {
                leftArmBoneName -> if (animatable.isLeftHanded) animatable.mainHandStack else animatable.offHandStack
                rightArmBoneName -> if (animatable.isLeftHanded) animatable.offHandStack else animatable.mainHandStack
                else -> null
            }

            override fun getTransformTypeForStack(
                bone: GeoBone,
                stack: ItemStack?,
                animatable: T
            ): ModelTransformationMode = when (bone.name) {
                leftArmBoneName, rightArmBoneName -> ModelTransformationMode.THIRD_PERSON_RIGHT_HAND
                else -> ModelTransformationMode.NONE
            }
        })
    }
}