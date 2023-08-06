package net.requef.flesh.entity

import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.RangedAttackMob
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.item.BowItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.RangedWeaponItem
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.random.Random
import net.minecraft.world.LocalDifficulty
import net.minecraft.world.World
import net.requef.flesh.ai.PrioritizedTargetOrRetaliate
import net.tslat.smartbrainlib.api.core.BrainActivityGroup
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.BowAttack
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.AvoidSun
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.EscapeSun
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.StrafeTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget
import net.tslat.smartbrainlib.util.BrainUtils
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.core.animation.*
import software.bernie.geckolib.core.`object`.PlayState
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

    override fun <T> attackPredicate(state: AnimationState<T>): PlayState where T: GeoAnimatable {
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

    override fun getCoreTasks(): BrainActivityGroup<out Zombie> = BrainActivityGroup.coreTasks(
        AvoidSun<Archer>(),
        EscapeSun<Archer>(),
        LookAtTarget<Archer>(),
        StrafeTarget<Archer>()
            .speedMod(1.15f)
            .stopStrafingWhen {entity -> !isHoldingBow(entity) }
            .startCondition(::isHoldingBow),
        MoveToWalkTarget<Archer>(),
        PrioritizedTargetOrRetaliate<Archer>().gradeTarget(::gradeTarget).cooldownFor { 20 },
    )

    override fun getFightTasks(): BrainActivityGroup<out Zombie> = BrainActivityGroup.fightTasks(
        InvalidateAttackTarget<Archer>(),
        // Zombie archer can't melee attack by design.
        ArcherBowAttack<Archer>(25, 40)
            .attackInterval { _ -> 65 }
            .attackRadius(bowRange)
    )

    private fun isHoldingBow(entity: LivingEntity) = entity.isHolding {stack -> stack.item is BowItem }

    class ArcherBowAttack<T>(
        arrowCooldownTicks: Int,
        private val aimingDurationTicks: Int,
    ) : BowAttack<T>(arrowCooldownTicks) where T: LivingEntity, T: RangedAttackMob {
        override fun doDelayedAction(entity: T) {
            if (target == null
                || !BrainUtils.canSee(entity, target)
                || entity.squaredDistanceTo(target) > attackRadius
                || entity.itemUseTime < aimingDurationTicks) return

            entity.attack(target, BowItem.getPullProgress(entity.itemUseTime))
            entity.clearActiveItem()

            BrainUtils.setForgettableMemory(
                entity,
                MemoryModuleType.ATTACK_COOLING_DOWN,
                true,
                attackIntervalSupplier.apply(entity)
            )
        }
    }
}