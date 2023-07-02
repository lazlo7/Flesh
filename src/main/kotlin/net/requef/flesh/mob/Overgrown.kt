package net.requef.flesh.mob

import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.world.World

class Overgrown(entityType: EntityType<out ZombieEntity>, world: World) : ZombieEntity(entityType, world)