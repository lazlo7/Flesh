package net.requef.flesh

import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import net.requef.flesh.mob.Mobs
import org.slf4j.LoggerFactory

object Flesh : ModInitializer {
    private val logger = LoggerFactory.getLogger("flesh")

	override fun onInitialize() {
		Mobs.registerAttributes()
	}

	fun identifier(path: String) = Identifier("flesh", path)
}