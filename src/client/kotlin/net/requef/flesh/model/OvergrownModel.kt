package net.requef.flesh.model

import net.minecraft.client.model.*
import net.minecraft.client.render.entity.model.AbstractZombieModel
import net.minecraft.client.render.entity.model.EntityModelPartNames
import net.requef.flesh.mob.Overgrown


class OvergrownModel(modelPart: ModelPart) : AbstractZombieModel<Overgrown>(modelPart) {
    companion object {
        fun getTexturedModelData(): TexturedModelData {
            val modelData = ModelData()
            val modelPartData = modelData.root

            modelPartData.addChild(
                EntityModelPartNames.HEAD,
                ModelPartBuilder.create()
                    .uv(0, 0)
                    .cuboid(-4.0F, -17.0F, -4.0F, 8.0F, 8.0F, 8.0F, Dilation(0.0F)),
                ModelTransform.pivot(0.0F, -13.0f, 0.0F)
            )
            modelPartData.addChild(
                EntityModelPartNames.HAT,
                ModelPartBuilder.create()
                    .uv(32, 0)
                    .cuboid(-4.0F, -17.0F, -4.0F, 8.0F, 8.0F, 8.0F, Dilation(0.0f)),
                ModelTransform.pivot(0.0f, -13.0f, 0.0f)
            )
            modelPartData.addChild(
                EntityModelPartNames.BODY,
                ModelPartBuilder.create()
                    .uv(16, 16)
                    .cuboid(-4.0F, -9.0F, -2.0F, 8.0F, 12.0F, 4.0F, Dilation(0.0f)),
                ModelTransform.pivot(0.0f, -14.0f, 0.0f)
            )
            modelPartData.addChild(
                EntityModelPartNames.RIGHT_ARM,
                ModelPartBuilder.create()
                    .uv(40, 16)
                    .mirrored()
                    .cuboid(8.0F, -15.0F, -6.0F, 4.0F, 20.0F, 4.0F, Dilation(0.0f))
                    .mirrored(false),
                ModelTransform.of(5.0F, -17.0F, 0.0F, 0.0F, 0.0F, 0.1F)
            )
            modelPartData.addChild(
                EntityModelPartNames.LEFT_ARM,
                ModelPartBuilder.create()
                    .uv(40, 16)
                    .cuboid(-12.0F, -15.0F, -6.0F, 4.0F, 20.0F, 4.0F, Dilation(0.0f)),
                ModelTransform.of(-5.0F, -17.0F, 0.0F, 0.0F, 0.0F, 0.1F)
            )
            modelPartData.addChild(
                EntityModelPartNames.RIGHT_LEG,
                ModelPartBuilder.create()
                    .uv(0, 16)
                    .cuboid(-2.0F, -9.0F, -2.0F, 4.0F, 21.0F, 4.0F, Dilation(0.0f)),
                ModelTransform.pivot(-2.0f, -5.0f, 0.0f)
            )
            modelPartData.addChild(
                EntityModelPartNames.LEFT_LEG,
                ModelPartBuilder.create()
                    .uv(0, 16)
                    .mirrored()
                    .cuboid(-2.0F, -9.0F, -2.0F, 4.0F, 21.0F, 4.0F, Dilation(0.0f))
                    .mirrored(false),
                ModelTransform.pivot(2.0f, -5.0f, 0.0f)
            )

            return TexturedModelData.of(modelData, 64, 64)
        }
    }

    override fun isAttacking(entity: Overgrown) = entity.isAttacking
}