package net.requef.flesh.model

import software.bernie.geckolib.constant.DataTickets
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.core.animation.AnimationState
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.model.data.EntityModelData

abstract class ZombieModel<T> : GeoModel<T>() where T: GeoAnimatable {
    override fun setCustomAnimations(
        animatable: T,
        instanceId: Long,
        animationState: AnimationState<T>?
    ) {
        super.setCustomAnimations(animatable, instanceId, animationState)

        if (animationState == null) {
            return
        }

        val head = animationProcessor.getBone("head")
        if (head != null) {
            val extraData = animationState.extraData[DataTickets.ENTITY_MODEL_DATA] as EntityModelData
            head.rotX = extraData.headPitch * Math.PI.toFloat() / 180.0f
            head.rotY = extraData.netHeadYaw * Math.PI.toFloat() / 180.0f
        }
    }
}