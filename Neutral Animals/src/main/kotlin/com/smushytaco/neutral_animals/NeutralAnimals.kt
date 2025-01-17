package com.smushytaco.neutral_animals
import com.smushytaco.neutral_animals.angerable_defaults.DefaultAngerable
import com.smushytaco.neutral_animals.configuration_support.ModConfiguration
import com.smushytaco.neutral_animals.mixins.PlayerHitTimerAccessor
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer
import net.fabricmc.api.ModInitializer
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.attribute.EntityAttributeInstance
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.predicate.entity.EntityPredicates
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.TimeHelper
import net.minecraft.util.math.Box
import net.minecraft.util.math.intprovider.UniformIntProvider
import java.util.*
object NeutralAnimals : ModInitializer {
    const val MOD_ID = "neutral_animals"
    lateinit var config: ModConfiguration
        private set
    private val ATTACKING_SPEED_BOOST_ID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718")
    private val ATTACKING_SPEED_BOOST = EntityAttributeModifier(ATTACKING_SPEED_BOOST_ID, "Attacking speed boost", 0.05, EntityAttributeModifier.Operation.ADDITION)
    val RANDOM = Random()
    val ANGER_TIME_RANGE: UniformIntProvider = TimeHelper.betweenSeconds(20, 39)
    val ANGER_PASSING_COOLDOWN_RANGE: UniformIntProvider = TimeHelper.betweenSeconds(4, 6)
    private fun <T: AnimalEntity> angerNearbyAnimals(animalEntity: T) {
        val followRange = animalEntity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE)
        val box = Box.from(animalEntity.pos).expand(followRange, 10.0, followRange)
        animalEntity.world.getEntitiesByClass(animalEntity.javaClass, box, EntityPredicates.EXCEPT_SPECTATOR).stream()
            .filter { it !== animalEntity }
            .filter { it.target == null }
            .filter { it.isTeammate(animalEntity.target) }
            .forEach { it.target = animalEntity.target }
    }
    private fun <T> tickAngerPassing(animalEntity: T) where T : AnimalEntity, T : DefaultAngerable {
        if (animalEntity.defaultAngerableValues.angerPassingCooldown > 0) {
            --animalEntity.defaultAngerableValues.angerPassingCooldown
        } else {
            if (animalEntity.visibilityCache.canSee((animalEntity as PathAwareEntity).target)) angerNearbyAnimals(
                animalEntity
            )
            animalEntity.defaultAngerableValues.angerPassingCooldown = ANGER_PASSING_COOLDOWN_RANGE.get(animalEntity.random)
        }
    }
    fun <T> mobTickLogic(animalEntity: T) where T : AnimalEntity, T : DefaultAngerable {
        val entityAttributeInstance: EntityAttributeInstance = animalEntity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED) ?: return
        if (animalEntity.hasAngerTime()) {
            if (!entityAttributeInstance.hasModifier(ATTACKING_SPEED_BOOST)) {
                entityAttributeInstance.addTemporaryModifier(ATTACKING_SPEED_BOOST)
            }
        } else if (entityAttributeInstance.hasModifier(ATTACKING_SPEED_BOOST)) {
            entityAttributeInstance.removeModifier(ATTACKING_SPEED_BOOST)
        }
        animalEntity.tickAngerLogic(animalEntity.world as ServerWorld, true)
        if (animalEntity.getTarget() != null) tickAngerPassing(animalEntity)
        if (animalEntity.hasAngerTime()) {
            (animalEntity as PlayerHitTimerAccessor).setPlayerHitTimer(animalEntity.age)
        }
    }
    fun <T> neutralAnimalGoalAndTargets(goalSelector: GoalSelector, targetSelector: GoalSelector, animalEntity: T) where T : AnimalEntity, T: DefaultAngerable {
        goalSelector.add(0, MeleeAttackGoal(animalEntity, 1.0, false))
        targetSelector.add(0, RevengeGoal(animalEntity).setGroupRevenge())
        targetSelector.add(0, ActiveTargetGoal(animalEntity, PlayerEntity::class.java, 10, true, false, animalEntity::shouldAngerAt))
        targetSelector.add(0, UniversalAngerGoal(animalEntity, true))
    }
    override fun onInitialize() {
        AutoConfig.register(ModConfiguration::class.java) { definition: Config, configClass: Class<ModConfiguration> ->
            GsonConfigSerializer(definition, configClass)
        }
        config = AutoConfig.getConfigHolder(ModConfiguration::class.java).config
    }
}