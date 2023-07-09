package net.requef.flesh.entity

import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.world.World

class FireArrow(world: World, x: Double, y: Double, z: Double) : ArrowEntity(world, x, y, z) {
    init {
        fireTicks = 20 * 30
    }

    private fun spawnFireParticles() {
        world.addImportantParticle(
            ParticleTypes.FALLING_DRIPSTONE_LAVA,
            true,
            x,
            y,
            z,
            0.0,
            0.0,
            0.0
        )
        world.addImportantParticle(
            ParticleTypes.LAVA,
            true,
            x,
            y,
            z,
            0.0,
            0.0,
            0.0
        )
    }

    override fun tick() {
        super.tick()
        if (inGround) {
            if (inGroundTime % 5 == 0) {
                spawnFireParticles()
            }
        } else {
            spawnFireParticles()
        }
    }
}