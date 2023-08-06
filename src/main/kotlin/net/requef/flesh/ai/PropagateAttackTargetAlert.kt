package net.requef.flesh.ai

import com.mojang.datafixers.util.Pair
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.entity.ai.brain.MemoryModuleState
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.mob.MobEntity
import net.tslat.smartbrainlib.util.BrainUtils

class PropagateAttackTargetAlert<T : MobEntity> : AlertAboutAttackTarget<T>() {
    companion object {
        private val memoryRequirements: List<Pair<MemoryModuleType<*>, MemoryModuleState>> =
            ObjectArrayList.of(
                Pair.of(FleshMemoryModuleTypes.attackTargetAlert, MemoryModuleState.VALUE_PRESENT),
                Pair.of(FleshMemoryModuleTypes.alertAboutAttackTargetCooldown, MemoryModuleState.VALUE_ABSENT)
            )
    }

    override fun getMemoryRequirements() = PropagateAttackTargetAlert.memoryRequirements

    override fun getBroadcastedPrioritizedTarget(broadcaster: T) =
        BrainUtils.getMemory(broadcaster, FleshMemoryModuleTypes.attackTargetAlert)!!
}