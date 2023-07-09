package net.requef.flesh.entity

import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.requef.flesh.Flesh

object Entities {
    val overgrown: EntityType<Overgrown> = Registry.register(Registries.ENTITY_TYPE,
        Flesh.identifier("overgrown"),
        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ::Overgrown)
            .dimensions(EntityDimensions.fixed(0.75f, 2.6f))
            .build()
    )

    val archer: EntityType<Archer> = Registry.register(Registries.ENTITY_TYPE,
        Flesh.identifier("archer"),
        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ::Archer)
            .dimensions(EntityDimensions.fixed(0.5f, 2.0f))
            .build())

    fun registerAttributes() {
        FabricDefaultAttributeRegistry.register(overgrown, Overgrown.createOvergrownAttributes())
        FabricDefaultAttributeRegistry.register(archer, Archer.createArcherAttributes())
    }
}