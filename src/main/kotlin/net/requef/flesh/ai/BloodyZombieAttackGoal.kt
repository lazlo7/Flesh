package net.requef.flesh.ai

import net.minecraft.entity.ai.goal.ZombieAttackGoal
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

class BloodyZombieAttackGoal(zombie: ZombieEntity, speed: Double, private val lungeSpeed: Double)
    : ZombieAttackGoal(zombie, speed, false) {
    private val lungeCooldownTicksMin = 5 * 20
    private val lungeCooldownTicksMax = 11 * 20
    private var lungeCooldownTicks = 0

    override fun tick() {
        super.tick()

        if (lungeCooldownTicks > 0) {
            lungeCooldownTicks--
        } else if (mob.target != null && isFarFromAttackRange()) {
            lunge(mob.target!!.pos)
            lungeCooldownTicks = mob.random.nextBetween(lungeCooldownTicksMin, lungeCooldownTicksMax)
        }
    }

    private fun isFarFromAttackRange() =
        mob.getSquaredDistanceToAttackPosOf(mob.target) >= 1.5 * getSquaredMaxAttackDistance(mob.target)

    private fun lunge(towards: Vec3d) {
        val vel = towards.subtract(mob.pos)
            .normalize()
            .multiply(lungeSpeed, 1.0, lungeSpeed)
            .add(0.0, 0.3, 0.0)
            .let { v -> Vec3d(
                MathHelper.clamp(v.x, 0.0, 1.25),
                MathHelper.clamp(v.y, 0.0, 0.7),
                MathHelper.clamp(v.z, 0.0, 1.25)
            ) }
        mob.addVelocity(vel)
    }
}