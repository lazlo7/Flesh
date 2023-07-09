package net.requef.flesh.model

import net.requef.flesh.Flesh
import net.requef.flesh.entity.Overgrown

class OvergrownModel : ZombieModel<Overgrown>() {
    override fun getModelResource(animatable: Overgrown) = Flesh.identifier("geo/overgrown.geo.json")

    override fun getTextureResource(animatable: Overgrown) = Flesh.identifier("textures/entity/overgrown/overgrown.png")

    override fun getAnimationResource(animatable: Overgrown) = Flesh.identifier("animations/overgrown.animation.json")
}