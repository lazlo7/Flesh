package net.requef.flesh.model

import net.requef.flesh.Flesh
import net.requef.flesh.entity.Bomber

class BomberModel : ZombieModel<Bomber>() {
    override fun getAnimationResource(animatable: Bomber) = Flesh.identifier("animations/bomber.animation.json")
}