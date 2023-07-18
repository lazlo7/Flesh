package net.requef.flesh.entity

import net.minecraft.entity.EntityData
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.ai.brain.Brain
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.world.LocalDifficulty
import net.minecraft.world.ServerWorldAccess
import net.minecraft.world.World
import net.tslat.smartbrainlib.api.SmartBrainOwner
import net.tslat.smartbrainlib.api.core.SmartBrainProvider
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache
import software.bernie.geckolib.core.animation.*
import software.bernie.geckolib.core.`object`.PlayState

open class Zombie(entityType: EntityType<out ZombieEntity>, world: World)
    : ZombieEntity(entityType, world), GeoEntity, SmartBrainOwner<Zombie> {
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

    // Clear any goals - use SmartBrainLib's system instead.
    override fun initGoals() { }

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

    @Suppress("KotlinConstantConditions")
    // Since Brain.Profile is a final class, kotlin will issue a warning about an always-fail cast.
    // However, the cast will proceed as normal because of an SBL's access widener,
    // which will make Brain.Profile extensible during run-time.
    override fun createBrainProfile() = SmartBrainProvider(this) as Brain.Profile<*>

    override fun mobTick() {
        super.mobTick()
        tickBrain(this)
    }

    override fun getSensors() = ArrayList<ExtendedSensor<Zombie>>()
}