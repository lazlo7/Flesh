package net.requef.flesh

import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import net.requef.flesh.ai.FleshMemoryModuleTypes
import net.requef.flesh.entity.Entities
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Flesh : ModInitializer {
    val logger: Logger = LoggerFactory.getLogger("flesh")

	override fun onInitialize() {
		Entities.registerAttributes()

		EntitySpawns.removeVanillaZombieSpawns()
		EntitySpawns.reduceVanillaMonstersSpawns(0.15f)
		EntitySpawns.registerEntitySpawns()

		FleshMemoryModuleTypes.initialize()
	}

	fun identifier(path: String) = Identifier("flesh", path)
}