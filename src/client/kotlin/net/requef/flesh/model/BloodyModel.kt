package net.requef.flesh.model

import net.requef.flesh.Flesh
import net.requef.flesh.entity.Bloody

class BloodyModel : ZombieModel<Bloody>() {
    override fun getModelResource(animatable: Bloody) =
        Flesh.identifier("geo/${
            when (animatable.type) {
                Bloody.Type.SOMEWHAT_MUTILATED -> "bloody1.geo.json"
                Bloody.Type.MUTILATED -> "bloody2.geo.json"
                Bloody.Type.EXTREMELY_MUTILATED -> "bloody3.geo.json" 
            }
        }")

    override fun getTextureResource(animatable: Bloody) =
        Flesh.identifier("textures/entity/bloody/${
            when (animatable.type) {
                Bloody.Type.SOMEWHAT_MUTILATED -> "variant1.png"
                Bloody.Type.MUTILATED -> "variant2.png"
                Bloody.Type.EXTREMELY_MUTILATED -> "variant3.png"
            }
        }")
}