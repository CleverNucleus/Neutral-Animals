package com.smushytaco.neutral_animals.mixin;
import com.smushytaco.neutral_animals.angerable_defaults.DefaultAngerable;
import com.smushytaco.neutral_animals.angerable_defaults.DefaultAngerableValues;
import com.smushytaco.neutral_animals.NeutralAnimals;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(CowEntity.class)
public abstract class CowEntityToNeutral extends AnimalEntity implements DefaultAngerable {
    DefaultAngerableValues defaultAngerableValues = new DefaultAngerableValues();
    protected CowEntityToNeutral(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }
    @Inject(method = "initGoals", at = @At("RETURN"))
    private void hookInitGoals(CallbackInfo ci) {
        NeutralAnimals.INSTANCE.neutralAnimalGoalAndTargets(goalSelector, targetSelector, this);
    }
    @Inject(method = "createCowAttributes", at = @At("HEAD"), cancellable = true)
    private static void hookCreateCowAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.setReturnValue(MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.20000000298023224D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0D));
    }
    @NotNull
    @Override
    public DefaultAngerableValues getDefaultAngerableValues() { return defaultAngerableValues; }
}