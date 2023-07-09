package net.requef.flesh.model

import net.requef.flesh.Flesh
import net.requef.flesh.entity.Archer

class ArcherModel : ZombieModel<Archer>() {
    override fun getModelResource(animatable: Archer) = Flesh.identifier("geo/zombie.geo.json")

    override fun getTextureResource(animatable: Archer) = Flesh.identifier("textures/entity/archer/archer.png")

    override fun getAnimationResource(animatable: Archer) = Flesh.identifier("animations/zombie.animation.json")
}