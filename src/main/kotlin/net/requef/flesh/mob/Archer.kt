package net.requef.flesh.mob

import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.RangedAttackMob
import net.minecraft.entity.ai.goal.BowAttackGoal
import net.minecraft.entity.ai.goal.ZombieAttackGoal
import net.minecraft.entity.attribute.DefaultAttributeContainer
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
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache
import software.bernie.geckolib.core.animation.*
import software.bernie.geckolib.core.`object`.PlayState
import kotlin.math.sqrt

class Archer(entityType: EntityType<out ZombieEntity>, world: World)
    : ZombieEntity(entityType, world), GeoEntity, RangedAttackMob {
    companion object {
        fun createArcherAttributes(): DefaultAttributeContainer.Builder = createZombieAttributes()
    }

    private var cache = SingletonAnimatableInstanceCache(this)

    override fun registerControllers(registrar: AnimatableManager.ControllerRegistrar) {
        registrar.add(AnimationController(this, "controller", 0, ::predicate))
        registrar.add(AnimationController(this, "bowAttackController", 0, ::attackPredicate))
    }

    override fun getAnimatableInstanceCache() = cache

    private fun <T> predicate(state: AnimationState<T>): PlayState where T: GeoAnimatable {
        if (state.isMoving) {
            return state.setAndContinue(RawAnimation.begin()
                .then("animation.zombie.walk", Animation.LoopType.LOOP))
        }

        return state.setAndContinue(RawAnimation.begin()
            .then("animation.zombie.idle", Animation.LoopType.LOOP))
    }

    private fun <T> attackPredicate(state: AnimationState<T>): PlayState where T: GeoAnimatable {
        if (isAttacking && state.controller.animationState == AnimationController.State.STOPPED) {
            val path = "animation.zombie.bow_${if (isLeftHanded) "left" else "right"}_hand_attack"
            state.resetCurrentAnimation()
            return state.setAndContinue(RawAnimation.begin()
                .then(path, Animation.LoopType.LOOP))
        }

        if (!isAttacking) {
            state.resetCurrentAnimation()
            return PlayState.STOP
        }

        return PlayState.CONTINUE
    }

    override fun initGoals() {
        super.initGoals()
        // Remove melee zombie attack goal because this is a ranged zombie.
        goalSelector.clear { goal -> goal is ZombieAttackGoal }
        goalSelector.add(2, BowAttackGoal(this, 1.0, 20, 15.0f))
    }

    override fun initEquipment(random: Random, localDifficulty: LocalDifficulty) {
        super.initEquipment(random, localDifficulty)
        equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.BOW))
    }

    override fun attack(target: LivingEntity, pullProgress: Float) {
        val arrowItem = getProjectileType(getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW)))
        val arrowEntity = ProjectileUtil.createArrowProjectile(this, arrowItem, pullProgress)

        val dx = target.x - this.x
        val dy = target.getBodyY(0.3333333333333333) - arrowEntity.y
        val dz = target.z - this.z
        val dist = sqrt(dx * dx + dz * dz)

        arrowEntity.setVelocity(dx, dy + dist * 0.2, dz, 1.6f, (14 - world.difficulty.id * 4).toFloat())
        playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f / (getRandom().nextFloat() * 0.4f + 0.8f))

        world.spawnEntity(arrowEntity)
    }

    override fun canUseRangedWeapon(weapon: RangedWeaponItem) = weapon is BowItem
}