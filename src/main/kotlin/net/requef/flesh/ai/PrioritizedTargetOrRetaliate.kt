package net.requef.flesh.ai

import com.mojang.datafixers.util.Pair
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.brain.MemoryModuleState
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.mob.MobEntity
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour
import net.tslat.smartbrainlib.util.BrainUtils

/**
 * Sets ATTACK_TARGET memory after using priority function.
 * Sources potential targets from: NEAREST_ATTACKABLE, attackTargetAlerts, HURT_BY_ENTITY, VISIBLE_MOBS.
 * Priority function grades targets from 0 (not a target) to infinity, greater values denoting higher priority targets.
 * Bears similarities with TargetOrRetaliate.
 */
class PrioritizedTargetOrRetaliate<T : MobEntity> : ExtendedBehaviour<T>() {
    companion object {
        private val memoryRequirements: List<Pair<MemoryModuleType<*>, MemoryModuleState>> =
            ObjectArrayList.of(
                Pair.of(MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleState.REGISTERED),
                Pair.of(FleshMemoryModuleTypes.attackTargetAlert, MemoryModuleState.REGISTERED),
                Pair.of(MemoryModuleType.HURT_BY_ENTITY, MemoryModuleState.REGISTERED),
                Pair.of(MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.REGISTERED)
            )
    }

    private var targetGrader: TargetGrader = { 0 }

    fun gradeTarget(grader: TargetGrader): PrioritizedTargetOrRetaliate<T> {
        targetGrader = grader
        return this
    }

    override fun getMemoryRequirements() = PrioritizedTargetOrRetaliate.memoryRequirements

    override fun start(entity: T) {
        var potentialTarget = BrainUtils.getMemory(entity, MemoryModuleType.NEAREST_ATTACKABLE)

        potentialTarget = pickTarget(potentialTarget, BrainUtils.getMemory(entity, FleshMemoryModuleTypes.attackTargetAlert)?.target)
        potentialTarget = pickTarget(potentialTarget, BrainUtils.getMemory(entity, MemoryModuleType.HURT_BY_ENTITY))
        potentialTarget = BrainUtils.getMemory(entity, MemoryModuleType.VISIBLE_MOBS)
            ?.iterate { true }?.fold(potentialTarget, ::pickTarget)

        val currentTarget = BrainUtils.getTargetOfEntity(entity)
        val target = pickTarget(potentialTarget, currentTarget)

        if (target != currentTarget) {
            BrainUtils.setTargetOfEntity(entity, target)
        }
    }

    private fun pickTarget(firstTarget: LivingEntity?, secondTarget: LivingEntity?): LivingEntity? {
        val bestTarget = when {
            firstTarget != null && secondTarget != null -> if (targetGrader(firstTarget) >= targetGrader(secondTarget))
                firstTarget else secondTarget
            firstTarget != null && secondTarget == null -> firstTarget
            firstTarget == null && secondTarget != null -> secondTarget
            else -> null
        } ?: return null

        return if (targetGrader(bestTarget) > 0) bestTarget else null
    }
}

typealias TargetGrader = (LivingEntity) -> Int