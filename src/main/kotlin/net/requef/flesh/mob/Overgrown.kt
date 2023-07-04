package net.requef.flesh.mob

import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.world.World
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache
import software.bernie.geckolib.core.animation.*
import software.bernie.geckolib.core.`object`.PlayState

class Overgrown(entityType: EntityType<out ZombieEntity>, world: World) : ZombieEntity(entityType, world), GeoEntity {
    private var cache = SingletonAnimatableInstanceCache(this)

    override fun registerControllers(registrar: AnimatableManager.ControllerRegistrar) {
        registrar.add(AnimationController(this, "controller", 0, ::predicate))
    }

    override fun getAnimatableInstanceCache() = cache

    private fun <T> predicate(state: AnimationState<T>): PlayState where T: GeoAnimatable {
        state.controller.setAnimation(RawAnimation.begin()
            .then("animation.overgrown.idle", Animation.LoopType.LOOP))
        return PlayState.CONTINUE
    }
}