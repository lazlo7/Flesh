package net.requef.flesh.entity

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityData
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.LocalDifficulty
import net.minecraft.world.ServerWorldAccess
import net.minecraft.world.World
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

        private val typeTracker = DataTracker.registerData(Bomber::class.java, TrackedDataHandlerRegistry.INTEGER)
        private fun intToType(int: Int) = Type.values()[int]
        private fun typeToInt(type: Type) = type.ordinal
    }

    var type: Type
        get() = intToType(dataTracker.get(typeTracker))
        set(value) = dataTracker.set(typeTracker, typeToInt(value))

    override fun initialize(
        world: ServerWorldAccess,
        difficulty: LocalDifficulty,
        spawnReason: SpawnReason?,
        entityData: EntityData?,
        entityNbt: NbtCompound?
    ): EntityData? {
        val result = super.initialize(world, difficulty, spawnReason, entityData, entityNbt)
        type = when (world.random.nextFloat()) {
            in 0.6f..1.0f -> Type.RIGHT_CARRIED
            in 0.2f..0.6f -> Type.LEFT_CARRIED
            else -> Type.BOTH_CARRIED
        }
        return result
    }

    override fun initDataTracker() {
        super.initDataTracker()
        dataTracker.startTracking(typeTracker, 0)
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        nbt.putInt("Type", typeToInt(type))
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        type = intToType(nbt.getInt("Type"))
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
        BomberMeleeAttack<Bomber>(type.power, 20)
    )

    enum class Type(
        val power: Float,
        val rightCarriedBlock: BlockState?,
        val leftCarriedBlock: BlockState?,
    ) {
        RIGHT_CARRIED(3.0f, Blocks.TNT.defaultState, null),
        LEFT_CARRIED(3.0f, null, Blocks.TNT.defaultState),
        BOTH_CARRIED(4.0f, Blocks.TNT.defaultState, Blocks.TNT.defaultState)
    }

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