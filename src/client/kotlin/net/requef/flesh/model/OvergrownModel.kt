package net.requef.flesh.model

import net.requef.flesh.Flesh
import net.requef.flesh.mob.Overgrown
import software.bernie.geckolib.constant.DataTickets
import software.bernie.geckolib.core.animation.AnimationState
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.model.data.EntityModelData

class OvergrownModel : GeoModel<Overgrown>() {
    override fun getModelResource(animatable: Overgrown) = Flesh.identifier("geo/overgrown.geo.json")

    override fun getTextureResource(animatable: Overgrown) = Flesh.identifier("textures/entity/overgrown/overgrown.png")

    override fun getAnimationResource(animatable: Overgrown) = Flesh.identifier("animations/overgrown.animation.json")

    override fun setCustomAnimations(
        animatable: Overgrown?,
        instanceId: Long,
        animationState: AnimationState<Overgrown>?
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