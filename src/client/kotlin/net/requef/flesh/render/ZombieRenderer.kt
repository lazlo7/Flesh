package net.requef.flesh.render

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.mob.MobEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.RotationAxis
import net.requef.flesh.Flesh
import net.requef.flesh.model.ZombieModel
import software.bernie.geckolib.cache.`object`.GeoBone
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoEntityRenderer
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer

open class ZombieRenderer<T>(
    ctx: EntityRendererFactory.Context,
    model: GeoModel<T> = ZombieModel(),
    netherEyes: Boolean = false
) : GeoEntityRenderer<T>(ctx, model) where T: MobEntity, T: GeoAnimatable {
    companion object {
        private val normalEyesTextureResource = Flesh.identifier("textures/entity/zombie_eyes.png")
        private val netherEyesTextureResource = Flesh.identifier("textures/entity/nether_zombie_eyes.png")
    }

    val armorRightBootName = "right_leg_boots"
    val armorLeftBootName = "left_leg_boots"

    val armorRightLegName = "right_leg_armor"
    val armorLeftLegName = "left_leg_armor"

    val armorRightArmName = "right_arm"
    val armorLeftArmName = "left_arm"
    val armorBodyName = "body_armor"

    val armorHeadName = "head_armor"

    val rightHeldItemName = "right_arm_held_item"
    val leftHeldItemName = "left_arm_held_item"

    init {
        renderLayers.addLayer(object : ItemArmorGeoLayer<T>(this) {
            override fun getArmorItemForBone(bone: GeoBone, animatable: T) = when(bone.name) {
                armorRightBootName, armorLeftBootName -> bootsStack
                armorRightLegName, armorLeftLegName -> leggingsStack
                armorBodyName, armorRightArmName, armorLeftArmName -> chestplateStack
                armorHeadName -> helmetStack
                else -> null
            }

            override fun getEquipmentSlotForBone(
                bone: GeoBone,
                stack: ItemStack?,
                animatable: T
            ): EquipmentSlot = when(bone.name) {
                armorRightBootName, armorLeftBootName -> EquipmentSlot.FEET
                armorRightLegName, armorLeftLegName -> EquipmentSlot.LEGS
                armorRightArmName -> if (animatable.isLeftHanded) EquipmentSlot.OFFHAND else EquipmentSlot.MAINHAND
                armorLeftArmName -> if (animatable.isLeftHanded) EquipmentSlot.MAINHAND else EquipmentSlot.OFFHAND
                armorBodyName -> EquipmentSlot.CHEST
                armorHeadName -> EquipmentSlot.HEAD
                else -> super.getEquipmentSlotForBone(bone, stack, animatable)
            }

            override fun getModelPartForBone(
                bone: GeoBone,
                slot: EquipmentSlot?,
                stack: ItemStack?,
                animatable: T,
                baseModel: BipedEntityModel<*>
            ) = when (bone.name) {
                armorRightBootName, armorRightLegName -> baseModel.rightLeg
                armorLeftBootName, armorLeftLegName -> baseModel.leftLeg
                armorRightArmName -> baseModel.rightArm
                armorLeftArmName -> baseModel.leftArm
                armorBodyName -> baseModel.body
                armorHeadName -> baseModel.head
                else -> super.getModelPartForBone(bone, slot, stack, animatable, baseModel)
            }
        })

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

        if (netherEyes) {
            renderLayers.addLayer(EyesGeoRenderLayer(netherEyesTextureResource, this, true))
        } else {
            renderLayers.addLayer(EyesGeoRenderLayer(normalEyesTextureResource, this))
        }
    }
}