package net.requef.flesh.entity

import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import net.requef.flesh.Flesh
import net.requef.flesh.ai.PrioritizedTargetOrRetaliate
import net.tslat.smartbrainlib.api.core.BrainActivityGroup
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget
import net.tslat.smartbrainlib.util.BrainUtils
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.core.animation.Animation
import software.bernie.geckolib.core.animation.AnimationState
import software.bernie.geckolib.core.animation.RawAnimation
import software.bernie.geckolib.core.`object`.PlayState

class Bomber(entityType: EntityType<out ZombieEntity>, world: World) : Zombie(entityType, world) {
    companion object {
        fun createBomberAttributes() = createFleshZombieAttributes()
    }

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

    override fun getFightTasks(): BrainActivityGroup<out Zombie> = BrainActivityGroup.fightTasks(
        InvalidateAttackTarget<Bomber>(),
        PrioritizedTargetOrRetaliate<Bomber>().gradeTarget(::gradeTarget).cooldownFor { 20 },
        SetWalkTargetToAttackTarget<Bomber>(),
        BomberMeleeAttack<Bomber>(4.0f, 20)
    )

    class BomberMeleeAttack<T : MobEntity>(
        private val explosionPower: Float,
        explodeDelayTicks: Int
    ) : AnimatableMeleeAttack<T>(explodeDelayTicks) {
        override fun doDelayedAction(entity: T) {
            BrainUtils.setForgettableMemory(
                entity,
                MemoryModuleType.ATTACK_COOLING_DOWN,
                true,
                attackIntervalSupplier.apply(entity)
            )

            if (target == null
                || !entity.visibilityCache.canSee(target)
                || !entity.isInAttackRange(target)) return

            val serverWorld = entity.world
            if (serverWorld !is ServerWorld) return

            Flesh.logger.info("Bomber exploding")

            serverWorld.createExplosion(
                entity,
                null,
                null,
                entity.x,
                entity.y,
                entity.z,
                explosionPower,
                false,
                World.ExplosionSourceType.MOB
            )

            entity.discard()
        }
    }
}