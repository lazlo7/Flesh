package net.requef.flesh.render

import net.minecraft.client.render.entity.EntityRendererFactory
import net.requef.flesh.mob.Overgrown
import net.requef.flesh.model.OvergrownModel
import software.bernie.geckolib.renderer.GeoEntityRenderer

class OvergrownRenderer(ctx: EntityRendererFactory.Context) : GeoEntityRenderer<Overgrown>(ctx, OvergrownModel())