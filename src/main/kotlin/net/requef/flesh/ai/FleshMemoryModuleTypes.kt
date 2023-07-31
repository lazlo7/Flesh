package net.requef.flesh.ai

import com.mojang.serialization.Codec
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.requef.flesh.Flesh
import java.util.*

object FleshMemoryModuleTypes {
    val alertAboutAttackTargetCooldown: MemoryModuleType<MinecraftUnit> = registerCooldown("alert_about_attack_target_cooldown")

    private fun <T> register(id: String, memoryModuleType: MemoryModuleType<T>) =
        Registry.register(Registries.MEMORY_MODULE_TYPE, Flesh.identifier(id), memoryModuleType)

    private fun registerCooldown(id: String) =
        register(id, MemoryModuleType(Optional.of(Codec.unit(MinecraftUnit.INSTANCE))))

    fun initialize() {}
}

// To avoid conflict with kotlin 'Unit'.
typealias MinecraftUnit = net.minecraft.util.Unit