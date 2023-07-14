package net.requef.flesh

import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.biome.v1.ModificationPhase
import net.fabricmc.fabric.impl.biome.modification.BiomeModificationImpl
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.SpawnRestriction
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.world.Heightmap
import net.minecraft.world.biome.SpawnSettings.SpawnEntry
import net.requef.flesh.entity.Entities
import kotlin.math.max
import kotlin.math.roundToInt

object EntitySpawns {
    private val biomeModificationInstance = BiomeModificationImpl.INSTANCE

    fun registerEntitySpawns() {
        addOverworld(Entities.zombie, 200, 4, 12)
        SpawnRestriction.register(Entities.zombie, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark)

        addOverworld(Entities.archer, 40, 1, 4)
        SpawnRestriction.register(Entities.archer, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark)

        addOverworld(Entities.bloody, 30, 1, 4)
        SpawnRestriction.register(Entities.bloody, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark)

        addOverworld(Entities.overgrown, 25, 1, 4)
        SpawnRestriction.register(Entities.overgrown, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark)
    }

    fun removeVanillaZombieSpawns() {
        removeSpawn(EntityType.ZOMBIE)
        removeSpawn(EntityType.ZOMBIE_VILLAGER)
    }

    /**
     * Reduces the 'weight' of a spawn entry for all biomes for most monster entities,
     * which are effectively replaced by their zombie counterparts.
     */
    fun reduceVanillaMonstersSpawns(reductionFactor: Float) {
        reduceSpawn(EntityType.SPIDER, reductionFactor)
        reduceSpawn(EntityType.SKELETON, reductionFactor)
        reduceSpawn(EntityType.CREEPER, reductionFactor)
        reduceSpawn(EntityType.SLIME, reductionFactor)
        reduceSpawn(EntityType.WITCH, reductionFactor)
    }

    private fun <T : Entity> addOverworld(entityType: EntityType<T>, weight: Int, minGroupSize: Int, maxGroupSize: Int) =
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, entityType, weight, minGroupSize, maxGroupSize)

    private fun <T : Entity> removeSpawn(entityType: EntityType<T>) =
        biomeModificationInstance.addModifier(EntityType.getId(entityType), ModificationPhase.REMOVALS, { true }) {
            ctx -> ctx.spawnSettings.removeSpawnsOfEntityType(entityType)
        }

    private fun <T : Entity> reduceSpawn(entityType: EntityType<T>, reductionFactor: Float) =
        biomeModificationInstance.addModifier(EntityType.getId(entityType), ModificationPhase.POST_PROCESSING,
            // For all biomes that have this entityType in a spawn entry...
            { ctx -> ctx.biome.spawnSettings.getSpawnEntries(entityType.spawnGroup).entries.any { entry -> entry.type == entityType } }) {
            // ...reduce that spawn entry weight by reductionFactor.
            ctx, _ -> ctx.biome.spawnSettings.getSpawnEntries(entityType.spawnGroup).entries.filter {
                entry -> entry.type == entityType }.map {
                    entry ->
                    // Keep the weight at least 1, so that the entities would have some chance to spawn.
                    val newWeight = max((reductionFactor * entry.weight.value).roundToInt(), 1)
                    Flesh.logger.info("Reducing $entityType spawn weight in ${ctx.biomeKey.value} from ${entry.weight.value} to $newWeight")
                    SpawnEntry(entry.type, newWeight, entry.minGroupSize, entry.maxGroupSize)
                }
        }
}