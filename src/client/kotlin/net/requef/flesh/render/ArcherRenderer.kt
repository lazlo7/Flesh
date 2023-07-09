package net.requef.flesh.render

import net.minecraft.client.render.entity.EntityRendererFactory
import net.requef.flesh.entity.Archer
import net.requef.flesh.model.ArcherModel

class ArcherRenderer(ctx: EntityRendererFactory.Context) : ZombieRenderer<Archer>(ctx, ArcherModel())