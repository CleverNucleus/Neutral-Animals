package com.smushytaco.neutral_animals.mixin;

import com.smushytaco.neutral_animals.NeutralAnimals;
import com.smushytaco.neutral_animals.angerable_defaults.DefaultAngerable;
import com.smushytaco.neutral_animals.angerable_defaults.DefaultAngerableValues;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(PigEntity.class)
public abstract class PigEntityToNeutral extends AnimalEntity implements DefaultAngerable {
    DefaultAngerableValues defaultAngerableValues = new DefaultAngerableValues();
    protected PigEntityToNeutral(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }
    @Inject(method = "initGoals", at = @At("RETURN"))
    private void hookInitGoals(CallbackInfo ci) {
        NeutralAnimals.INSTANCE.neutralAnimalGoalAndTargets(goalSelector, targetSelector, this);
    }
    @Inject(method = "createPigAttributes", at = @At("HEAD"), cancellable = true)
    private static void hookCreatePigAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.setReturnValue(MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0D));
    }
    @NotNull
    @Override
    public DefaultAngerableValues getDefaultAngerableValues() { return defaultAngerableValues; }
    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    private void hookWriteCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        angerToTag(tag);
    }
    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    private void hookReadCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        angerFromTag((ServerWorld) world, tag);
    }
    @Override
    protected void mobTick() {
        NeutralAnimals.INSTANCE.mobTickLogic(this);
        super.mobTick();
    }
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (getTarget() == null && target != null) {
            defaultAngerableValues.setField_25608(NeutralAnimals.INSTANCE.getFIELD_25609().choose(random));
        }
        if (target instanceof PlayerEntity) {
            setAttacking((PlayerEntity) target);
        }
        super.setTarget(target);
    }
}