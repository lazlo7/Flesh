package net.requef.flesh.model

import net.requef.flesh.Flesh
import net.requef.flesh.entity.Bloody

class BloodyModel : ZombieModel<Bloody>() {
    override fun getModelResource(animatable: Bloody) = when (animatable.type) {
        Bloody.Type.SOMEWHAT_MUTILATED -> Flesh.identifier("geo/bloody.geo.json")
        else -> super.getModelResource(animatable)
    }

    override fun getTextureResource(animatable: Bloody) =
        Flesh.identifier("textures/entity/bloody/${animatable.type.textureName}")
}