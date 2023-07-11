package net.requef.flesh.entity

import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.RangedAttackMob
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.goal.ZombieAttackGoal
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.item.BowItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.RangedWeaponItem
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.random.Random
import net.minecraft.world.LocalDifficulty
import net.minecraft.world.World
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.core.animation.*
import software.bernie.geckolib.core.`object`.PlayState
import java.util.*
import kotlin.math.sqrt

class Archer(entityType: EntityType<out ZombieEntity>, world: World)
    : Zombie(entityType, world), RangedAttackMob {
    companion object {
        fun createArcherAttributes(): DefaultAttributeContainer.Builder = createFleshZombieAttributes()
            .add(EntityAttributes.GENERIC_ARMOR, 6.0)
    }

    private val bowRange = 30.0f

    override fun registerControllers(registrar: AnimatableManager.ControllerRegistrar) {
        super.registerControllers(registrar)
        registrar.add(AnimationController(this, "bowAttackController", 0, ::attackPredicate))
    }

    private fun <T> attackPredicate(state: AnimationState<T>): PlayState where T: GeoAnimatable {
        if (isUsingItem && state.controller.animationState == AnimationController.State.STOPPED) {
            val path = "animation.zombie.bow_${if (isLeftHanded) "left" else "right"}_hand_attack"
            state.resetCurrentAnimation()

            return state.setAndContinue(RawAnimation.begin()
                .then(path, Animation.LoopType.HOLD_ON_LAST_FRAME))
        }

        if (!isUsingItem) {
            state.resetCurrentAnimation()
            return PlayState.STOP
        }

        return PlayState.CONTINUE
    }

    override fun initGoals() {
        super.initGoals()
        // Remove melee zombie attack goal because this is a ranged zombie.
        goalSelector.clear { goal -> goal is ZombieAttackGoal }
        goalSelector.add(2, ArcherBowAttackGoal(this, 1.15, 25, 40, bowRange))
    }

    override fun initEquipment(random: Random, localDifficulty: LocalDifficulty) {
        super.initEquipment(random, localDifficulty)
        equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.BOW))
    }

    override fun attack(target: LivingEntity, pullProgress: Float) {
        val arrow = FireArrow(world, x, eyeY - 0.1, z)
        arrow.owner = this

        val dx = target.x - x
        val dy = target.getBodyY(1.0 / 3.0) - arrow.y
        val dz = target.z - z

        val dist = sqrt(dx * dx + dz * dz)
        var vy = dy + dist * 0.17
        // Adjust for long-range horizontal shooting
        if (dist > bowRange * 0.7) {
            vy += dist * dist * 0.0045
        }
        // Adjust for long-range upward shooting
        if (dy >= 8) {
            vy *= 1.2
        }

        val skeletonDivergence = 14 - world.difficulty.id * 4

        arrow.setVelocity(dx, vy, dz, 1.7f, 0.15f * skeletonDivergence)
        playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f / (getRandom().nextFloat() * 0.4f + 0.8f))

        world.spawnEntity(arrow)
    }

    override fun canUseRangedWeapon(weapon: RangedWeaponItem) = weapon is BowItem

    class ArcherBowAttackGoal<T>(
        private val actor: T,
        private val speed: Double,
        private val arrowCooldownTicks: Int,
        private val aimingDurationTicks: Int,
        range: Float
    ) : Goal() where T: HostileEntity, T: RangedAttackMob {
        private val squaredRange: Float
        private var arrowCooldown = 0
        private var targetSeeingTicker = 0
        private var combatTicks = -1
        private var movingToLeft = false
        private var backward = false

        init {
            squaredRange = range * range
            controls = EnumSet.of(Control.MOVE, Control.LOOK)
        }

        override fun canStart() = actor.target != null && isHoldingBow()

        private fun isHoldingBow() = actor.isHolding(Items.BOW)

        override fun shouldContinue() = isHoldingBow() && (canStart() || !actor.navigation.isIdle)

        override fun start() {
            super.start()
            actor.isAttacking = true
        }

        override fun stop() {
            super.stop()
            actor.isAttacking = false
            targetSeeingTicker = 0
            arrowCooldown = -1
            actor.clearActiveItem()
        }

        override fun shouldRunEveryTick() = true

        override fun tick() {
            val target = actor.target ?: return
            val dist = actor.squaredDistanceTo(target.x, target.y, target.z)

            val seeingTarget = actor.visibilityCache.canSee(target)
            val seenTargetEnough = targetSeeingTicker > 0
            if (seeingTarget != seenTargetEnough) {
                targetSeeingTicker = 0
            }

            targetSeeingTicker += if (seeingTarget) 1 else -1

            if (dist > squaredRange.toDouble() || targetSeeingTicker < 20) {
                actor.navigation.startMovingTo(target, speed)
                combatTicks = -1
            } else {
                actor.navigation.stop()
                combatTicks++
            }

            if (combatTicks >= 20) {
                if (actor.random.nextFloat().toDouble() < 0.3) {
                    movingToLeft xor true
                }

                if (actor.random.nextFloat().toDouble() < 0.3) {
                    backward xor true
                }

                combatTicks = 0
            }

            if (combatTicks > -1) {
                backward = dist < 0.25 * squaredRange

                actor.moveControl.strafeTo(
                    if (backward) -0.5f else 0.5f,
                    if (movingToLeft) 0.5f else -0.5f
                )

                val vehicle = actor.controllingVehicle
                if (vehicle is MobEntity) {
                    vehicle.lookAtEntity(target, 30.0f, 30.0f)
                }

                actor.lookAtEntity(target, 30.0f, 30.0f)
            } else {
                actor.lookControl.lookAt(target, 30.0f, 30.0f)
            }

            if (actor.isUsingItem) {
                if (!seeingTarget && targetSeeingTicker < -60) {
                    actor.clearActiveItem()
                } else if (seeingTarget && actor.itemUseTime >= aimingDurationTicks) {
                    actor.attack(target, BowItem.getPullProgress(actor.itemUseTime))
                    actor.clearActiveItem()
                    arrowCooldown = arrowCooldownTicks
                }
            } else if (arrowCooldown-- <= 0 && targetSeeingTicker >= -60) {
                actor.setCurrentHand(ProjectileUtil.getHandPossiblyHolding(actor, Items.BOW))
            }
        }
    }
}