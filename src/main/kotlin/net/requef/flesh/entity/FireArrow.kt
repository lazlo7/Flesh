package net.requef.flesh.entity

import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

class FireArrow(world: World, x: Double, y: Double, z: Double) : ArrowEntity(world, x, y, z) {
    init {
        fireTicks = 20 * 30
    }

    private fun spawnFireParticles() {
        val serverWorld = world
        if (serverWorld !is ServerWorld) {
            return
        }

        serverWorld.spawnParticles(
            ParticleTypes.FALLING_DRIPSTONE_LAVA,
            x,
            y,
            z,
            1,
            0.125,
            0.125,
            0.125,
            1.0
        )

        serverWorld.spawnParticles(
            ParticleTypes.LAVA,
            x,
            y,
            z,
            1,
            0.125,
            0.125,
            0.125,
            1.0
        )
    }

    override fun tick() {
        super.tick()
        if (fireTicks > 0 && (!inGround || inGroundTime % 5 == 0)) {
            spawnFireParticles()
        }
    }
}