package net.requef.flesh.render

import net.minecraft.client.render.entity.EntityRendererFactory
import net.requef.flesh.mob.Archer
import net.requef.flesh.model.ArcherModel
import software.bernie.geckolib.renderer.GeoEntityRenderer

class ArcherRenderer(ctx: EntityRendererFactory.Context) : GeoEntityRenderer<Archer>(ctx, ArcherModel())