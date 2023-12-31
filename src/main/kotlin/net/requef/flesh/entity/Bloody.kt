package net.requef.flesh.entity


import com.mojang.datafixers.util.Pair
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.entity.EntityData
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.ai.brain.MemoryModuleState
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.floatprovider.ConstantFloatProvider
import net.minecraft.world.LocalDifficulty
import net.minecraft.world.ServerWorldAccess
import net.minecraft.world.World
import net.requef.flesh.ai.PrioritizedTargetOrRetaliate
import net.tslat.smartbrainlib.api.core.BrainActivityGroup
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget
import net.tslat.smartbrainlib.util.BrainUtils
import java.util.*
import java.util.function.BiPredicate
import kotlin.math.pow


class Bloody(entityType: EntityType<out ZombieEntity>, world: World) : Zombie(entityType, world) {
    companion object {
        private val maxHealthAttributeUUID = UUID.fromString("923adb56-2723-47cf-a53f-08fe859a4149")
        private val movementSpeedAttributeUUID = UUID.fromString("add6df5b-7c38-4887-8067-57b7dab58bd2")
        private val attackDamageAttributeUUID = UUID.fromString("0ec01a0b-ca1d-40e4-aa80-9fff9896af8a")

        private val typeTracker = DataTracker.registerData(Bloody::class.java, TrackedDataHandlerRegistry.INTEGER)
        private fun intToType(int: Int) = Type.values()[int]
        private fun typeToInt(type: Type) = type.ordinal
        fun createBloodyAttributes() = createFleshZombieAttributes()
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
            in 0.5f..1.0f -> Type.SOMEWHAT_MUTILATED
            in 0.15f..0.5f -> Type.MUTILATED
            else -> Type.EXTREMELY_MUTILATED
        }
        return result
    }

    override fun onTrackedDataSet(data: TrackedData<*>?) {
        super.onTrackedDataSet(data)
        if (world.isClient) return
        if (data == typeTracker) updateTypeAttributes()
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

    override fun applyAttributeModifiers(chanceMultiplier: Float) {
        super.applyAttributeModifiers(chanceMultiplier)
        updateTypeAttributes()
    }

    private fun updateTypeAttributes() {
        val remove = { uuid: UUID, attribute: EntityAttribute ->
            getAttributeInstance(attribute)?.tryRemoveModifier(uuid)
        }

        val apply = { uuid: UUID, attribute: EntityAttribute, multiplier: Double ->
            getAttributeInstance(attribute)?.addPersistentModifier(
                EntityAttributeModifier(
                    uuid,
                    type.attributeBonusName,
                    multiplier,
                    EntityAttributeModifier.Operation.ADDITION
                )
            )
        }

        remove(maxHealthAttributeUUID, EntityAttributes.GENERIC_MAX_HEALTH)
        remove(movementSpeedAttributeUUID, EntityAttributes.GENERIC_MOVEMENT_SPEED)
        remove(attackDamageAttributeUUID, EntityAttributes.GENERIC_ATTACK_DAMAGE)

        apply(maxHealthAttributeUUID, EntityAttributes.GENERIC_MAX_HEALTH, type.healthIncrease)
        health = getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH).toFloat()
        apply(movementSpeedAttributeUUID, EntityAttributes.GENERIC_MOVEMENT_SPEED, type.speedIncrease)
        apply(attackDamageAttributeUUID, EntityAttributes.GENERIC_ATTACK_DAMAGE, type.attackDamageIncrease)
    }

    override fun onLanding() {
        super.onLanding()
        if (target != null) {
            navigation.recalculatePath()
            lookControl.lookAt(target)
        }
    }

    override fun getIdleTasks(): BrainActivityGroup<out Zombie> = BrainActivityGroup.idleTasks(
        PrioritizedTargetOrRetaliate<Zombie>().gradeTarget(::gradeTarget).cooldownFor { 20 },
        SetRandomLookTarget<Bloody>()
            .lookChance(ConstantFloatProvider.create(0.2f))
            .lookTime { entity -> entity.random.nextInt(10) + 10 },
        OneRandomBehaviour(
            SetRandomWalkTarget<Bloody>().setRadius(1.0).speedModifier(1.25f),
            OneRandomBehaviour(
                SetRandomWalkTarget<Bloody>().setRadius(15.0),
                Idle<Bloody>().runFor { entity -> entity.random.nextBetween(20, 50) }
            )
        )
    )

    override fun getFightTasks(): BrainActivityGroup<out Zombie> = BrainActivityGroup.fightTasks(
        InvalidateAttackTarget<Bloody>(),
        PrioritizedTargetOrRetaliate<Zombie>().gradeTarget(::gradeTarget).cooldownFor { 20 },
        SetWalkTargetToAttackTarget<Bloody>(),
        AnimatableMeleeAttack<Bloody>(0),
        LungeTowardsAttackTarget<Bloody>(0.3)
            .lungePredicate { entity, target ->
                // From nms: MeleeAttackGoal
                val maxAttackDistanceSqr = (entity.width * 2.0).pow(2) + target.width
                entity.getSquaredDistanceToAttackPosOf(target) > 1.5 * maxAttackDistanceSqr }
            .cooldownFor { 8 * 20 }
    )

    enum class Type(
        val attributeBonusName: String,
        val healthIncrease: Double,
        val speedIncrease: Double,
        val attackDamageIncrease: Double
    ) {
        SOMEWHAT_MUTILATED(
            "Bloodlust I",
            5.0,
            0.02,
            1.0
        ),
        MUTILATED(
            "Bloodlust II",
            12.0,
            0.0575,
            1.8
        ),
        EXTREMELY_MUTILATED(
            "Bloodlust III",
            20.0,
            0.115,
            3.05
        )
    }

    class LungeTowardsAttackTarget<T : MobEntity>(private val lungeYSpeed: Double) : ExtendedBehaviour<T>() {
        companion object {
            private val memoryRequirements: List<Pair<MemoryModuleType<*>, MemoryModuleState>> =
                ObjectArrayList.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT))
        }

        private var lungePredicate: BiPredicate<in MobEntity, LivingEntity> = BiPredicate { _, _ -> true }

        fun lungePredicate(predicate: BiPredicate<in MobEntity, LivingEntity>): LungeTowardsAttackTarget<T> {
            lungePredicate = predicate
            return this
        }

        override fun getMemoryRequirements() = LungeTowardsAttackTarget.memoryRequirements

        override fun shouldRun(level: ServerWorld, entity: T) = lungePredicate.test(
            // Memory requirements have been already satisfied at this point,
            // so we can use ATTACK_TARGET memory here.
            entity, BrainUtils.getMemory(entity, MemoryModuleType.ATTACK_TARGET)!!
        )

        override fun start(entity: T) {
            val attackTarget = BrainUtils.getMemory(entity, MemoryModuleType.ATTACK_TARGET)

            if (attackTarget == null) {
                BrainUtils.clearMemory(entity, MemoryModuleType.ATTACK_TARGET)
                return
            }

            val targetPos = attackTarget.pos
            val vel = targetPos.subtract(entity.pos)
                .add(0.0, lungeYSpeed, 0.0)
                .normalize()
            entity.addVelocity(vel)
        }
    }
}