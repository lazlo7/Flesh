package net.requef.flesh

import net.fabricmc.api.ClientModInitializer
import net.requef.flesh.render.Renderers

object FleshClient : ClientModInitializer {
	override fun onInitializeClient() {
		Renderers.register()
	}
}