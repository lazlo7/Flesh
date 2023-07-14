package net.requef.flesh.entity

import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.world.World

class Bloody(val type: Type, entityType: EntityType<out ZombieEntity>, world: World) : Zombie(entityType, world) {
    companion object {
        fun createBloodyZombie(entityType: EntityType<out ZombieEntity>, world: World) = when (world.random.nextFloat()) {
            in 0.6f..1.0f -> Bloody(Type.SOMEWHAT_MUTILATED, entityType, world)
            in 0.25f..0.6f -> Bloody(Type.MUTILATED, entityType, world)
            in 0.0f..0.25f -> Bloody(Type.EXTREMELY_MUTILATED, entityType, world)
            else -> throw AssertionError("Unreachable.")
        }

        fun createBloodyAttributes() = createFleshZombieAttributes()
    }

    override fun applyAttributeModifiers(chanceMultiplier: Float) {
        super.applyAttributeModifiers(chanceMultiplier)

        val apply = { attribute: EntityAttribute, multiplier: Double ->
            getAttributeInstance(attribute)?.addPersistentModifier(
                EntityAttributeModifier(
                    type.attributeBonusName,
                    multiplier,
                    EntityAttributeModifier.Operation.MULTIPLY_BASE
                )
            )
        }

        apply(EntityAttributes.GENERIC_MAX_HEALTH, type.healthMultiplier)
        apply(EntityAttributes.GENERIC_MOVEMENT_SPEED, type.speedMultiplier)
        apply(EntityAttributes.GENERIC_ATTACK_DAMAGE, type.attackDamageMultiplier)
    }

    enum class Type(
        val textureName: String,
        val attributeBonusName: String,
        val healthMultiplier: Double,
        val speedMultiplier: Double,
        val attackDamageMultiplier: Double
    ) {
        SOMEWHAT_MUTILATED(
            "variant1.png",
            "Bloodlust I",
            1.25,
            1.087,
            1.333
        ),
        MUTILATED(
            "variant2.png",
            "Bloodlust II",
            1.6,
            1.25,
            1.6
        ),
        EXTREMELY_MUTILATED(
            "variant3.png",
            "Bloodlust III",
            2.0,
            1.5,
            2.05
        )
    }
}