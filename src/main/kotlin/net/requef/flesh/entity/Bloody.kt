package net.requef.flesh.entity

import com.mojang.datafixers.util.Pair
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.brain.MemoryModuleState
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.floatprovider.ConstantFloatProvider
import net.minecraft.world.World
import net.requef.flesh.ai.AlertAboutAttackTarget
import net.tslat.smartbrainlib.api.core.BrainActivityGroup
import net.tslat.smartbrainlib.api.core.behaviour.AllApplicableBehaviours
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour
import net.tslat.smartbrainlib.api.core.behaviour.SequentialBehaviour
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate
import net.tslat.smartbrainlib.util.BrainUtils
import java.util.function.BiPredicate
import kotlin.math.pow


class Bloody(val type: Type, entityType: EntityType<out ZombieEntity>, world: World) : Zombie(entityType, world) {
    companion object {
        fun createBloodyZombie(entityType: EntityType<out ZombieEntity>, world: World) = when (world.random.nextFloat()) {
            in 0.5f..1.0f -> Bloody(Type.SOMEWHAT_MUTILATED, entityType, world)
            in 0.15f..0.5f -> Bloody(Type.MUTILATED, entityType, world)
            in 0.0f..0.15f -> Bloody(Type.EXTREMELY_MUTILATED, entityType, world)
            else -> throw AssertionError("Unreachable.")
        }

        fun createBloodyAttributes() = createFleshZombieAttributes()
    }

    override fun applyAttributeModifiers(chanceMultiplier: Float) {
        super.applyAttributeModifiers(chanceMultiplier)

        val apply = { attribute: EntityAttribute, multiplier: Double ->
            getAttributeInstance(attribute)?.addPersistentModifier(
                EntityAttributeModifier(
                    type.attributeBonusName,
                    multiplier,
                    EntityAttributeModifier.Operation.ADDITION
                )
            )
        }

        apply(EntityAttributes.GENERIC_MAX_HEALTH, type.healthIncrease)
        health = getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH).toFloat()
        apply(EntityAttributes.GENERIC_MOVEMENT_SPEED, type.speedIncrease)
        apply(EntityAttributes.GENERIC_ATTACK_DAMAGE, type.attackDamageIncrease)
    }

    override fun onLanding() {
        super.onLanding()
        if (target != null) {
            navigation.recalculatePath()
            lookControl.lookAt(target)
        }
    }

    override fun getIdleTasks(): BrainActivityGroup<out Zombie> = BrainActivityGroup.idleTasks(
        FirstApplicableBehaviour(
            AllApplicableBehaviours(
                TargetOrRetaliate<Zombie>()
                    .attackablePredicate { entity -> entity !is Zombie },
                AlertAboutAttackTarget()
            ),
            SetRandomLookTarget<Bloody>()
                .lookChance(ConstantFloatProvider.create(0.2f))
                .lookTime { entity -> entity.random.nextInt(10) + 10 }
        ),
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
        val textureName: String,
        val attributeBonusName: String,
        val healthIncrease: Double,
        val speedIncrease: Double,
        val attackDamageIncrease: Double
    ) {
        SOMEWHAT_MUTILATED(
            "variant1.png",
            "Bloodlust I",
            5.0,
            0.02,
            1.0
        ),
        MUTILATED(
            "variant2.png",
            "Bloodlust II",
            12.0,
            0.0575,
            1.8
        ),
        EXTREMELY_MUTILATED(
            "variant3.png",
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