package net.requef.flesh.entity

import net.minecraft.entity.EntityData
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.entity.mob.ZombifiedPiglinEntity
import net.minecraft.entity.passive.IronGolemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.world.LocalDifficulty
import net.minecraft.world.ServerWorldAccess
import net.minecraft.world.World
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache
import software.bernie.geckolib.core.animation.*
import software.bernie.geckolib.core.`object`.PlayState

open class Zombie(entityType: EntityType<out ZombieEntity>, world: World) : ZombieEntity(entityType, world), GeoEntity {
    companion object {
        fun createFleshZombieAttributes(): DefaultAttributeContainer.Builder = createZombieAttributes()
    }

    protected open val attackAnimationName: String
        get() = when (random.nextBetween(0, 1)) {
            0 -> "animation.zombie.vertical_swing"
            else -> "animation.zombie.horizontal_swing"
        }

    protected var cache = SingletonAnimatableInstanceCache(this)

    override fun registerControllers(registrar: AnimatableManager.ControllerRegistrar) {
        registrar.add(AnimationController(this, "controller", 0, ::predicate))
        registrar.add(AnimationController(this, "attackController", 0, ::attackPredicate))
    }

    protected open fun <T> predicate(state: AnimationState<T>): PlayState where T: GeoAnimatable {
        if (state.isMoving) {
            return state.setAndContinue(
                RawAnimation.begin()
                .then("animation.zombie.walk", Animation.LoopType.LOOP))
        }

        return state.setAndContinue(
            RawAnimation.begin()
            .then("animation.zombie.idle", Animation.LoopType.LOOP))
    }

    protected open fun <T> attackPredicate(state: AnimationState<T>): PlayState where T: GeoAnimatable {
        if (handSwinging && state.controller.animationState == AnimationController.State.STOPPED) {
            state.resetCurrentAnimation()
            state.setAnimation(RawAnimation.begin()
                .then(attackAnimationName, Animation.LoopType.PLAY_ONCE))
            handSwinging = false
        }

        return PlayState.CONTINUE
    }

    override fun getAnimatableInstanceCache() = cache

    override fun initGoals() {
        targetSelector.add(1, RevengeGoal(this, javaClass).setGroupRevenge(ZombifiedPiglinEntity::class.java))
        targetSelector.add(2, ActiveTargetGoal(this, PlayerEntity::class.java, true))
        targetSelector.add(3, ActiveTargetGoal(this, IronGolemEntity::class.java, true))

        goalSelector.add(2, ZombieAttackGoal(this, 1.0, false))
        goalSelector.add(6, MoveThroughVillageGoal(this, 1.0, true, 4) { canBreakDoors() })
        goalSelector.add(7, WanderAroundFarGoal(this, 1.0))
        goalSelector.add(8, LookAtEntityGoal(this, PlayerEntity::class.java, 8.0f))
        goalSelector.add(8, LookAroundGoal(this))
    }

    override fun initialize(
        world: ServerWorldAccess?,
        difficulty: LocalDifficulty,
        spawnReason: SpawnReason?,
        entityData: EntityData?,
        entityNbt: NbtCompound?
    ): EntityData? {
        // Minecraft zombie class tries to make the zombie a baby in the super-method.
        // It only does it if the provided entity data is an instance of ZombieData.
        // Flesh zombies can't be babies - by providing a plain entity data object,
        // the super-method skips the attempt to make the entity a baby.
        val result = super.initialize(world, difficulty, spawnReason, object : EntityData {}, entityNbt)

        // However, we still need to call other methods that go after the baby-setting code.
        setCanBreakDoors(shouldBreakDoors() && random.nextFloat() < difficulty.clampedLocalDifficulty * 0.1f)
        initEquipment(random, difficulty)
        updateEnchantments(random, difficulty)

        return result
    }
}