package net.requef.flesh.entity

import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.world.World
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache
import software.bernie.geckolib.core.animation.*
import software.bernie.geckolib.core.`object`.PlayState

class Overgrown(entityType: EntityType<out ZombieEntity>, world: World) : ZombieEntity(entityType, world), GeoEntity {
    companion object {
        fun createOvergrownAttributes(): DefaultAttributeContainer.Builder = createZombieAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2645)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.3)
            .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 3.0)
    }

    private var cache = SingletonAnimatableInstanceCache(this)

    override fun registerControllers(registrar: AnimatableManager.ControllerRegistrar) {
        registrar.add(AnimationController(this, "controller", 0, ::predicate))
        registrar.add(AnimationController(this, "attackController", 0, ::attackPredicate))
    }

    override fun getAnimatableInstanceCache() = cache

    private fun <T> predicate(state: AnimationState<T>): PlayState where T: GeoAnimatable {
        if (state.isMoving) {
            return state.setAndContinue(RawAnimation.begin()
                .then("animation.overgrown.walk", Animation.LoopType.LOOP))
        }

        return state.setAndContinue(RawAnimation.begin()
            .then("animation.overgrown.idle", Animation.LoopType.LOOP))
    }

    private fun <T> attackPredicate(state: AnimationState<T>): PlayState where T: GeoAnimatable {
        if (handSwinging && state.controller.animationState == AnimationController.State.STOPPED) {
            state.resetCurrentAnimation()
            state.setAnimation(RawAnimation.begin()
                .then("animation.overgrown.attack", Animation.LoopType.PLAY_ONCE))
            handSwinging = false
        }

        return PlayState.CONTINUE
    }
}