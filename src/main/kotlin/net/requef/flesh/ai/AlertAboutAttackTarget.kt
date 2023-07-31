package net.requef.flesh.ai

import com.mojang.datafixers.util.Pair
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.entity.LivingEntity
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
 * by setting their ATTACK_TARGET to given ATTACK_TARGET using given range.
 * Alerted allies further propagate the alert.
 */
class AlertAboutAttackTarget<T : MobEntity> : ExtendedBehaviour<T>() {
    companion object {
        private val memoryRequirements: List<Pair<MemoryModuleType<*>, MemoryModuleState>> =
            ObjectArrayList.of(
                Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT),
                Pair.of(FleshMemoryModuleTypes.alertAboutAttackTargetCooldown, MemoryModuleState.VALUE_ABSENT)
            )
    }

    private var range = 30.0
    private var alertCooldown = 30 * 20

    fun range(range: Double): AlertAboutAttackTarget<T> {
        this.range = range
        return this
    }

    fun alertCooldown(cooldown: Int): AlertAboutAttackTarget<T> {
        this.alertCooldown = cooldown
        return this
    }

    override fun getMemoryRequirements() = AlertAboutAttackTarget.memoryRequirements

    override fun start(entity: T) {
        setCooldownMemory(entity)
        alertOthers(entity)
    }

    private fun setCooldownMemory(entity: LivingEntity) = BrainUtils.setForgettableMemory(
        entity,
        FleshMemoryModuleTypes.alertAboutAttackTargetCooldown,
        MinecraftUnit.INSTANCE,
        alertCooldown
    )

    private fun alertOthers(broadcaster: T) {
        val responders = broadcaster.world.getEntitiesByType(
            TypeFilter.instanceOf(broadcaster.javaClass),
            broadcaster.boundingBox.expand(range)
        ) {
            entity -> !BrainUtils.hasMemory(entity, FleshMemoryModuleTypes.alertAboutAttackTargetCooldown)
                && !BrainUtils.hasMemory(entity, MemoryModuleType.ATTACK_TARGET)
        }

        responders.forEach {
            Flesh.logger.info("[AlertAboutAttackTarget] ${broadcaster.id} alerted ${it.id}")
            BrainUtils.setMemory(it, MemoryModuleType.ATTACK_TARGET, broadcaster.target)
        }
    }
}