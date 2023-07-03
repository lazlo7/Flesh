package net.requef.flesh.render

import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.requef.flesh.Flesh
import net.requef.flesh.mob.Overgrown
import net.requef.flesh.model.OvergrownModel

class OvergrownRenderer(ctx: EntityRendererFactory.Context)
    : MobEntityRenderer<Overgrown, BipedEntityModel<Overgrown>>(ctx, OvergrownModel(ctx.getPart(Renderers.modelOvergrownLayer)), 0.8f) {
    override fun getTexture(entity: Overgrown) = Flesh.identifier("textures/entity/overgrown/overgrown.png")
}