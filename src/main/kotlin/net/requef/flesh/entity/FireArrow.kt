package net.requef.flesh.entity

import net.minecraft.block.FireBlock
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FireArrow(world: World, x: Double, y: Double, z: Double) : ArrowEntity(world, x, y, z) {
    private var ticksToSetBlockOnFire = 0
    private var blockPosToSetOnFire: BlockPos? = null

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
        if (fireTicks > 0 && (!inGround || inGroundTime % 10 == 0)) {
            spawnFireParticles()
        }
        if (ticksToSetBlockOnFire > 0 && inGround) {
            ticksToSetBlockOnFire--
            if (ticksToSetBlockOnFire == 0) {
                world.setBlockState(blockPosToSetOnFire, FireBlock.getState(world, blockPosToSetOnFire))
            }
        }
    }

    override fun onBlockHit(blockHitResult: BlockHitResult) {
        super.onBlockHit(blockHitResult)
        if (fireTicks > 0) {
            ticksToSetBlockOnFire = 20 * random.nextBetween(5, 10)
            blockPosToSetOnFire = blockHitResult.blockPos.add(blockHitResult.side.vector)
        }
    }
}