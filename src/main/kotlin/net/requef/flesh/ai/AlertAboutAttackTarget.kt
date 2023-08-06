package net.requef.flesh.ai

import com.mojang.datafixers.util.Pair
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.entity.ai.brain.MemoryModuleState
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.mob.MobEntity
import net.minecraft.util.TypeFilter
import net.requef.flesh.Flesh
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour
import net.tslat.smartbrainlib.util.BrainUtils

/**
 * Behaves like TargetOrRetaliate::alertAllies, but doesn't depend on mob's follow range.
 * Alerts allies (mobs of the same type or derived type)
 * by setting their attackTargetAlert to broadcaster's target.
 */
open class AlertAboutAttackTarget<T : MobEntity> : ExtendedBehaviour<T>() {
    companion object {
        private val memoryRequirements: List<Pair<MemoryModuleType<*>, MemoryModuleState>> =
            ObjectArrayList.of(
                Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT),
                Pair.of(FleshMemoryModuleTypes.alertAboutAttackTargetCooldown, MemoryModuleState.VALUE_ABSENT)
            )
    }

    private var range = 45.0
    private var alertCooldown = 10 * 20
    private var alertMemoryDuration = 10 * 20
    private var targetGrader: TargetGrader = { 0 }

    fun range(range: Double): AlertAboutAttackTarget<T> {
        this.range = range
        return this
    }

    fun alertCooldown(cooldown: Int): AlertAboutAttackTarget<T> {
        this.alertCooldown = cooldown
        return this
    }

    fun gradeTarget(grader: TargetGrader): AlertAboutAttackTarget<T> {
        this.targetGrader = grader
        return this
    }

    override fun getMemoryRequirements() = AlertAboutAttackTarget.memoryRequirements

    override fun start(entity: T) {
        alertOthers(entity)
    }

    protected open fun getBroadcastedPrioritizedTarget(broadcaster: T): PrioritizedTarget {
        val target = BrainUtils.getTargetOfEntity(broadcaster)!!
        return PrioritizedTarget(target, targetGrader(target))
    }

    private fun alertOthers(broadcaster: T) {
        BrainUtils.setForgettableMemory(
            broadcaster,
            FleshMemoryModuleTypes.alertAboutAttackTargetCooldown,
            MinecraftUnit.INSTANCE,
            alertCooldown
        )

        val prioritizedTarget = getBroadcastedPrioritizedTarget(broadcaster)

        val responders = broadcaster.world.getEntitiesByType(
            TypeFilter.instanceOf(broadcaster.javaClass),
            broadcaster.boundingBox.expand(range)) {
            entity -> !BrainUtils.hasMemory(entity, FleshMemoryModuleTypes.alertAboutAttackTargetCooldown)
        }

        responders.forEach {
            val currentAlert = BrainUtils.getMemory(it, FleshMemoryModuleTypes.attackTargetAlert)
            if (currentAlert == null || prioritizedTarget.priority > currentAlert.priority) {
                BrainUtils.setForgettableMemory(
                    it,
                    FleshMemoryModuleTypes.attackTargetAlert,
                    prioritizedTarget,
                    alertMemoryDuration
                )
            }
        }
    }
}