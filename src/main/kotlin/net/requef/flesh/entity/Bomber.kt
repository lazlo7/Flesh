package net.requef.flesh.entity

import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.core.animation.Animation
import software.bernie.geckolib.core.animation.AnimationController
import software.bernie.geckolib.core.animation.AnimationState
import software.bernie.geckolib.core.animation.RawAnimation
import software.bernie.geckolib.core.`object`.PlayState

class Bomber(entityType: EntityType<out ZombieEntity>, world: World) : Zombie(entityType, world) {
    companion object {
        fun createBomberAttributes() = createFleshZombieAttributes()
    }

    private var attackStarted = false

    override fun canTarget(target: LivingEntity): Boolean {
        // Only target players.
        return super.canTarget(target) && target is PlayerEntity
    }

    override val attackAnimationName: String
        get() = "animation.bomber.attack"

    override fun <T> predicate(state: AnimationState<T>): PlayState where T: GeoAnimatable {
        if (state.isMoving) {
            return state.setAndContinue(RawAnimation.begin()
                .then("animation.bomber.walk", Animation.LoopType.LOOP))
        }

        return state.setAndContinue(RawAnimation.begin()
            .then("animation.bomber.idle", Animation.LoopType.LOOP))
    }

    override fun <T> attackPredicate(state: AnimationState<T>): PlayState where T: GeoAnimatable {
        if (attackStarted && state.controller.animationState == AnimationController.State.STOPPED) {
            explode()
        }

        if (handSwinging && state.controller.animationState == AnimationController.State.STOPPED) {
            state.resetCurrentAnimation()
            state.setAnimation(RawAnimation.begin()
                .then(attackAnimationName, Animation.LoopType.PLAY_ONCE))
            handSwinging = false
            attackStarted = true
        }

        return PlayState.CONTINUE
    }

    private fun explode() {
        val serverWorld = world
        if (serverWorld !is ServerWorld) return

        serverWorld.createExplosion(
            this,
            null,
            null,
            x,
            getBodyY(0.0625),
            z,
            4.0f,
            false,
            World.ExplosionSourceType.MOB
        )

        discard()
    }
}