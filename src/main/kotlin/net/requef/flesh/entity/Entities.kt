package net.requef.flesh.entity

import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.EntityType.EntityFactory
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.requef.flesh.Flesh

object Entities {
    private fun <T : Entity> register(
        id: String,
        entity: EntityFactory<T>,
        builder: (FabricEntityTypeBuilder<T>) -> FabricEntityTypeBuilder<T> = { b -> b }
    ) = Registry.register(
            Registries.ENTITY_TYPE,
            Flesh.identifier(id),
            builder(FabricEntityTypeBuilder.Mob.create(SpawnGroup.MONSTER, entity)
                .spawnableFarFromPlayer()
                .trackRangeChunks(8)
                .dimensions(EntityDimensions.fixed(0.5f, 2.0f))
            ).build()
    )

    private fun <T : Entity> registerNether(
        id: String,
        entity: EntityFactory<T>,
        builder: (FabricEntityTypeBuilder<T>) -> FabricEntityTypeBuilder<T> = { b -> b }
    ) = register(id, entity) { builder(it.fireImmune()) }

    val zombie: EntityType<Zombie> = register("zombie", ::Zombie)

    val overgrown: EntityType<Overgrown> = register("overgrown", ::Overgrown) {
        builder -> builder.dimensions(EntityDimensions.fixed(0.75f, 2.6f))
    }

    val archer: EntityType<Archer> = register("archer", ::Archer)

    val bloody: EntityType<Bloody> = register("bloody", ::Bloody)

    val bomber: EntityType<Bomber> = register("bomber", ::Bomber)

    val netherZombie: EntityType<Zombie> = registerNether("nether_zombie", ::Zombie)

    val netherOvergrown: EntityType<Overgrown> = registerNether("nether_overgrown", ::Overgrown) {
        it.dimensions(EntityDimensions.fixed(0.75f, 2.6f))
    }

    val netherArcher: EntityType<Archer> = registerNether("nether_archer", ::Archer)

    val netherBloody: EntityType<Bloody> = registerNether("nether_bloody", ::Bloody)

    val netherBomber: EntityType<Bomber> = registerNether("nether_bomber", ::Bomber)

    fun registerAttributes() {
        FabricDefaultAttributeRegistry.register(zombie, Zombie.createFleshZombieAttributes())
        FabricDefaultAttributeRegistry.register(overgrown, Overgrown.createOvergrownAttributes())
        FabricDefaultAttributeRegistry.register(archer, Archer.createArcherAttributes())
        FabricDefaultAttributeRegistry.register(bloody, Bloody.createBloodyAttributes())
        FabricDefaultAttributeRegistry.register(bomber, Bomber.createBomberAttributes())

        FabricDefaultAttributeRegistry.register(netherZombie, Zombie.createFleshZombieAttributes())
        FabricDefaultAttributeRegistry.register(netherOvergrown, Overgrown.createOvergrownAttributes())
        FabricDefaultAttributeRegistry.register(netherArcher, Archer.createArcherAttributes())
        FabricDefaultAttributeRegistry.register(netherBloody, Bloody.createBloodyAttributes())
        FabricDefaultAttributeRegistry.register(netherBomber, Bomber.createBomberAttributes())
    }
}