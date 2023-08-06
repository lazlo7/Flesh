package net.requef.flesh.ai

import com.mojang.serialization.Codec
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.requef.flesh.Flesh
import java.util.*

object FleshMemoryModuleTypes {
    val alertAboutAttackTargetCooldown: MemoryModuleType<MinecraftUnit> = registerCooldown("alert_about_attack_target_cooldown")
    val attackTargetAlert: MemoryModuleType<PrioritizedTarget> = register("attack_target_alert")

    private fun <T> register(id: String, memoryModuleType: MemoryModuleType<T>) =
        Registry.register(Registries.MEMORY_MODULE_TYPE, Flesh.identifier(id), memoryModuleType)

    private fun <T> register(id: String, codec: Optional<Codec<T>> = Optional.empty()) =
        register(id, MemoryModuleType(codec))

    private fun <T> register(id: String, codec: Codec<T>) =
        register(id, Optional.of(codec))

    private fun registerCooldown(id: String) =
        register(id, Codec.unit(MinecraftUnit.INSTANCE))

    fun initialize() {}
}

// To avoid conflict with kotlin 'Unit'.
typealias MinecraftUnit = net.minecraft.util.Unit