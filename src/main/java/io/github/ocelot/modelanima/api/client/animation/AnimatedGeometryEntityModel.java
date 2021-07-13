package io.github.ocelot.modelanima.api.client.animation;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModel;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModelManager;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModelRenderer;
import io.github.ocelot.modelanima.api.common.animation.AnimationData;
import io.github.ocelot.modelanima.api.common.molang.MolangException;
import io.github.ocelot.modelanima.api.common.molang.MolangRuntime;
import io.github.ocelot.modelanima.api.common.molang.MolangVariableProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.ElderGuardianEntity;
import net.minecraft.entity.monster.PatrollerEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.animation.Animation;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * <p>A basic implementation of {@link EntityModel} for {@link GeometryModel}.</p>
 *
 * @param <T> The type of entity this model is rendering
 * @author Ocelot
 * @since 1.0.0
 */
public class AnimatedGeometryEntityModel<T extends Entity> extends EntityModel<T>
{
    private final ResourceLocation model;
    private ResourceLocation texture;
    private ResourceLocation[] animations;
    private MolangVariableProvider variableProvider;

    public AnimatedGeometryEntityModel(ResourceLocation model)
    {
        this.model = model;
        this.texture = null;
        this.animations = new ResourceLocation[0];
        this.variableProvider = null;
    }

    private MolangRuntime.Builder createRuntime(T entity, float limbSwing, float limbSwingAmount, float yaw, float pitch)
    {
        float partialTicks = Animation.getPartialTickTime();
        MolangRuntime.Builder builder = MolangRuntime.runtime();
        // Skip above_top_solid
        // Skip actor_count
        // Anim Time handled by AnimatedModel
        builder.setQuery("approx_eq", -1, context ->
        {
            if (context.getParameters() <= 1)
                return 1.0F;

            float first = context.resolve(0);
            for (int i = 1; i < context.getParameters(); i++)
                if (Math.abs(context.resolve(i) - first) > 0.0000001)
                    return 0.0F;
            return 1.0F;
        });
        builder.setQuery("armor_color_slot", 1, context ->
        {
            if (!(entity instanceof LivingEntity))
                return -1F;

            int index = (int) context.resolve(0);
            if (index < 0 || index >= 4)
                return -1F;

            LivingEntity livingEntity = (LivingEntity) entity;
            ItemStack stack = livingEntity.getItemBySlot(EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, index));
            if (stack.isEmpty() || !(stack.getItem() instanceof IDyeableArmorItem))
                return -1F;
            return (float) ((IDyeableArmorItem) stack.getItem()).getColor(stack);
        });
        // Skip armor_material_slot
        // Skip armor_texture_slot
        builder.setQuery("average_frame_time", () ->
        {
            FrameTimer frameTimer = Minecraft.getInstance().getFrameTimer();
            return (float) frameTimer.getLog()[frameTimer.getLogEnd()] / 1_000_000_000F; // ns to s
        });
        builder.setQuery("average_frame_time", 1, context ->
        {
            int duration = (int) Math.min(context.resolve(0), 240); // Extended from 30 to 240 since that's what FrameTimer stores
            if (duration <= 0)
                throw new MolangException("Invalid argument for average_frame_time(): " + duration);
            FrameTimer frameTimer = Minecraft.getInstance().getFrameTimer();
            return (float) IntStream.range(0, duration).mapToLong(i ->
            {
                int wrappedIndex = frameTimer.getLogEnd() - i;
                while (wrappedIndex < 0)
                    wrappedIndex += 240;
                return frameTimer.getLog()[wrappedIndex];
            }).sum() / duration / 1_000_000_000F; // ns to s
        });
        builder.setQuery("block_face", 6.0F); // Undefined
        builder.setQuery("blocking", () -> entity.canBeCollidedWith() ? 1.0F : 0.0F);
        builder.setQuery("body_x_rotation", 0.0F);
        if (entity instanceof LivingEntity)
            builder.setQuery("body_y_rotation", () -> MathHelper.lerp(partialTicks, ((LivingEntity) entity).yBodyRotO, ((LivingEntity) entity).yBodyRot));
        builder.setQuery("camera_distance_range_lerp", 2, context ->
        {
            float first = context.resolve(0);
            float second = context.resolve(1);
            if (first == second)
                return 1.0F;

            float smaller = Math.min(first, second);
            float larger = Math.max(first, second);

            double distance = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceTo(entity.position());
            if (distance <= smaller)
                return smaller;
            if (distance >= larger)
                return larger;
            return (float) ((distance - smaller) / (larger - smaller));
        });
        builder.setQuery("camera_rotation", 1, ctx ->
        {
            int param = (int) ctx.resolve(0);
            if (param < 0 || param >= 2)
                throw new MolangException("Invalid argument for camera_rotation: " + param);
            ActiveRenderInfo camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            return param == 0 ? camera.getXRot() : camera.getYRot();
        });
        // Skip can_climb
        // Skip can_damage_nearby_mobs
        // Skip can_fly
        if (entity instanceof IJumpingMount)
            builder.setQuery("can_power_jump", () -> ((IJumpingMount) entity).canJump() ? 1.0F : 0.0F);
        // Skip can_swim
        // Skip can_walk
        // Skip cape_flap_amount
        builder.setQuery("cardinal_block_face_placed_on", 6.0F); // Undefined
        builder.setQuery("cardinal_facing", () -> (float) Direction.orderedByNearest(entity)[0].get3DDataValue());
        builder.setQuery("cardinal_facing_2d", () -> (float) Direction.orderedByNearest(entity)[0].get2DDataValue());
        if (entity instanceof PlayerEntity)
            builder.setQuery("cardinal_player_facing", () -> (float) Direction.orderedByNearest(entity)[0].get3DDataValue());
        // Skip combine_entities
        // Skip count
        if (entity instanceof SlimeEntity)
            builder.setQuery("current_squish_value", () -> MathHelper.lerp(partialTicks, ((SlimeEntity) entity).oSquish, ((SlimeEntity) entity).squish));
        builder.setQuery("day", () -> (float) (entity.level.getDayTime() / 24000L + 1));
        // Skip debug_output
        // delta_time handled by AnimatedModel
        builder.setQuery("distance_from_camera", () -> (float) Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceTo(entity.position()));
        // Skip effect_emitter_count
        // Skip effect_particle_count
        // Skip equipment_count
        // Skip equipped_item_all_tags
        // Skip equipped_item_any_tag
        // Skip equipped_item_is_attachable
        // Skip eye_target_x_rotation
        // Skip eye_target_y_rotation
        // Skip frame_alpha
        // Skip get_actor_info_id
        // Skip get_animation_frame
        // Skip get_default_bone_pivot
        // Skip get_equipped_item_name
        // Skip get_locator_offset
        // Skip get_name
        // Skip get_root_locator_offset
        if (entity instanceof LivingEntity)
            builder.setQuery("ground_speed", ((LivingEntity) entity)::getSpeed);
        // Skip has_any_family
        if (entity instanceof LivingEntity)
            builder.setQuery("has_armor_slot", 1, context ->
            {
                int index = (int) context.resolve(0);
                if (index < 0 || index >= 4)
                    return 0.0F;

                LivingEntity livingEntity = (LivingEntity) entity;
                ItemStack stack = livingEntity.getItemBySlot(EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, index));
                return stack.isEmpty() ? 0.0F : 1.0F;
            });
        // Skip has_biome_tag
        // Skip has_block_property
        if (entity instanceof AbstractClientPlayerEntity)
            builder.setQuery("has_cape", () -> ((AbstractClientPlayerEntity) entity).getCloakTextureLocation() != null ? 1.0F : 0.0F);
        builder.setQuery("has_collision", () -> entity.noPhysics ? 0.0F : 1.0F);
        builder.setQuery("has_gravity", () -> entity.isNoGravity() ? 0.0F : 1.0F);
        if (entity instanceof TameableEntity)
            builder.setQuery("has_owner", () -> ((TameableEntity) entity).getOwnerUUID() != null ? 1.0F : 0.0F);
        if (entity instanceof ProjectileEntity)
            builder.setQuery("has_owner", () -> ((ProjectileEntity) entity).getOwner() != null ? 1.0F : 0.0F);
        builder.setQuery("has_rider", () -> entity.getPassengers().isEmpty() ? 0.0F : 1.0F);
        // Skip has_target. This is not accessible on the client
        // Skip head_roll_angle
        builder.setQuery("head_x_rotation", () -> pitch);
        builder.setQuery("head_y_rotation", () -> yaw);
        if (entity instanceof LivingEntity)
            builder.setQuery("health", ((LivingEntity) entity)::getHealth);
        // Skip heightmap
        if (entity instanceof LivingEntity)
            builder.setQuery("hurtDir", () -> ((LivingEntity) entity).hurtDir);
        if (entity instanceof LivingEntity)
            builder.setQuery("hurt_time", () -> (float) ((LivingEntity) entity).hurtTime);
        builder.setQuery("invulnerable_ticks", () -> (float) entity.invulnerableTime);
        builder.setQuery("is_alive", () -> entity.isAlive() ? 1.0F : 0.0F);
        if (entity instanceof IAngerable)
            builder.setQuery("is_angry", () -> ((IAngerable) entity).isAngry() ? 1.0F : 0.0F);
        // Skip is_attached_to_entity
        // Skip is_avoiding_block
        // Skip is_avoiding_mobs
        if (entity instanceof LivingEntity)
            builder.setQuery("is_baby", () -> ((LivingEntity) entity).isBaby() ? 1.0F : 0.0F);
        // Skip is_breathing
        // Skip is_bribed
        // Skip is_carrying_block
        // Skip is_casting
        // Skip is_celebrating
        // Skip is_celebrating_special
        // Skip is_charged
        // Skip is_charging
        if (entity instanceof AbstractChestedHorseEntity)
            builder.setQuery("is_chested", () -> ((AbstractChestedHorseEntity) entity).hasChest() ? 1.0F : 0.0F);
        if (entity instanceof ParrotEntity)
            builder.setQuery("is_dancing", () -> ((ParrotEntity) entity).isPartyParrot() ? 1.0F : 0.0F);
        // Skip is_delayed_attacking
        if (entity instanceof LivingEntity)
            builder.setQuery("is_eating", () -> ((LivingEntity) entity).getUseItem().isEdible() ? 1.0F : 0.0F);
        if (entity instanceof ElderGuardianEntity)
            builder.setQuery("is_elder", 1.0F);
        // Skip is_emoting
        // Skip is_enchanted
        builder.setQuery("is_fire_immune", () -> entity.fireImmune() ? 1.0F : 0.0F);
        builder.setQuery("is_first_person", () -> entity == Minecraft.getInstance().getCameraEntity() && Minecraft.getInstance().options.getCameraType() == PointOfView.FIRST_PERSON ? 1.0F : 0.0F);
        builder.setQuery("is_ghost", () -> entity.isSpectator() ? 1.0F : 0.0F);
        if (entity instanceof LivingEntity)
            builder.setQuery("is_gliding", () -> ((LivingEntity) entity).isFallFlying() ? 1.0F : 0.0F);
        // Skip is_grazing
        // Skip is_idling
        if (entity instanceof CreeperEntity)
            builder.setQuery("is_ignited", () -> ((CreeperEntity) entity).isIgnited() ? 1.0F : 0.0F);
        if (entity instanceof PatrollerEntity)
            builder.setQuery("is_illager_captain", () -> ((PatrollerEntity) entity).isPatrolLeader() ? 1.0F : 0.0F);
        builder.setQuery("is_in_contact_with_water", () -> entity.isInWaterOrRain() ? 1.0F : 0.0F);
        if (entity instanceof AnimalEntity)
            builder.setQuery("is_in_love", () -> ((AnimalEntity) entity).isInLove() ? 1.0F : 0.0F);
        // Skip is_in_ui
        builder.setQuery("is_in_water", () -> entity.isInWater() ? 1.0F : 0.0F);
        builder.setQuery("is_in_water_or_rain", () -> entity.isInWaterOrRain() ? 1.0F : 0.0F);
        // Skip is_interested
        builder.setQuery("is_invisible", () -> entity.isInvisible() ? 1.0F : 0.0F);
        // Skip is_item_equipped
        // Skip is_jumping
        builder.setQuery("is_laying_down", () -> entity.getPose() == Pose.SLEEPING ? 1.0F : 0.0F);
        // Skip is_laying_egg
        if (entity instanceof MobEntity)
            builder.setQuery("is_leashed", () -> ((MobEntity) entity).isLeashed() ? 1.0F : 0.0F);
        if (entity instanceof LivingEntity)
            builder.setQuery("is_levitating", () -> ((LivingEntity) entity).getEffect(Effects.LEVITATION) != null ? 1.0F : 0.0F);
        // Skip is_lingering
        builder.setQuery("is_moving", () -> entity.getDeltaMovement().lengthSqr() > 1.0E-7D ? 1.0F : 0.0F);
        builder.setQuery("is_on_fire", () -> entity.isOnFire() ? 1.0F : 0.0F);
        builder.setQuery("is_on_ground", () -> entity.isOnGround() ? 1.0F : 0.0F);
        builder.setQuery("is_on_screen", 1.0F); // If being rendered, it must be on screen
        builder.setQuery("is_onfire", () -> entity.isOnFire() ? 1.0F : 0.0F);
        // Skip is_orphaned
        // Skip is_persona_or_premium_skin
        // Skip is_playing_dead
        if (entity instanceof IChargeableMob)
            builder.setQuery("is_powered", () -> ((IChargeableMob) entity).isPowered() ? 1.0F : 0.0F);
        if (entity instanceof TurtleEntity)
            builder.setQuery("is_pregnant", () -> ((TurtleEntity) entity).isLayingEgg() ? 1.0F : 0.0F);
        // Skip is_ram_attacking
        // Skip is_resting
        builder.setQuery("is_riding", () -> entity.isPassenger() ? 1.0F : 0.0F);
        // Skip is_roaring
        if (entity instanceof PandaEntity)
            builder.setQuery("is_rolling", () -> ((PandaEntity) entity).isRolling() ? 1.0F : 0.0F);
        if (entity instanceof IEquipable)
            builder.setQuery("is_saddled", () -> ((IEquipable) entity).isSaddled() ? 1.0F : 0.0F);
        // Skip is_scared
        // Skip is_selected_item
        if (entity instanceof WolfEntity)
            builder.setQuery("is_shaking_wetness", () -> ((WolfEntity) entity).getWetShade(partialTicks) < 1.0F ? 1.0F : 0.0F);
        if (entity instanceof SheepEntity)
            builder.setQuery("is_sheared", () -> ((SheepEntity) entity).isSheared() ? 1.0F : 0.0F);
        // Skip is_shield_powered
        builder.setQuery("is_silent", () -> entity.isSilent() ? 1.0F : 0.0F);
        if (entity instanceof TameableEntity)
            builder.setQuery("is_sitting", () -> ((TameableEntity) entity).isInSittingPose() ? 1.0F : 0.0F);
        if (entity instanceof LivingEntity)
            builder.setQuery("is_sleeping", () -> ((LivingEntity) entity).isSleeping() ? 1.0F : 0.0F);
        builder.setQuery("is_sneaking", () -> entity.isDiscrete() ? 1.0F : 0.0F);
        // Skip is_sneezing
        builder.setQuery("is_sprinting", () -> entity.isSprinting() ? 1.0F : 0.0F);
        builder.setQuery("is_stackable", 1.0F); // Everything can be stacked
        // Skip is_stalking
        builder.setQuery("is_standing", () -> entity.getPose() == Pose.STANDING ? 1.0F : 0.0F);
        // Skip is_stunned
        builder.setQuery("is_swimming", () -> entity.isSwimming() ? 1.0F : 0.0F);
        if (entity instanceof TameableEntity)
            builder.setQuery("is_tamed", () -> ((TameableEntity) entity).isTame() ? 1.0F : 0.0F);
        // Skip is_transforming
        if (entity instanceof LivingEntity)
        {
            builder.setQuery("is_using_item", () -> ((LivingEntity) entity).isUsingItem() ? 1.0F : 0.0F);
            builder.setQuery("is_wall_climbing", () -> ((LivingEntity) entity).onClimbable() ? 1.0F : 0.0F);
            builder.setQuery("item_in_use_duration", () -> (float) ((LivingEntity) entity).getTicksUsingItem() / 20.0F);
            // Skip item_is_charged
            builder.setQuery("item_max_use_duration", () -> (float) ((LivingEntity) entity).getUseItem().getUseDuration() / 20.0F);
            // Skip item_remaining_use_duration
        }
        // Skip item_slot_to_bone_name
        // Skip key_frame_lerp_time
        builder.setQuery("last_frame_time", () ->
        {
            FrameTimer frameTimer = Minecraft.getInstance().getFrameTimer();
            return (float) frameTimer.getLog()[frameTimer.getLogEnd()] / 1_000_000_000F; // ns to s
        });
        builder.setQuery("last_frame_time", 1, context ->
        {
            int index = (int) Math.min(context.resolve(0), 240); // Extended from 30 to 240 since that's what FrameTimer stores
            FrameTimer frameTimer = Minecraft.getInstance().getFrameTimer();
            if (index == 0)
                return (float) frameTimer.getLog()[frameTimer.getLogEnd()] / 1_000_000_000F; // ns to s
            if (index < 0)
                throw new MolangException("Invalid argument for last_frame_time(): " + index);
            int wrappedIndex = frameTimer.getLogEnd() - index;
            while (wrappedIndex < 0)
                wrappedIndex += 240;
            return (float) frameTimer.getLog()[frameTimer.wrapIndex(wrappedIndex)] / 1_000_000_000F; // ns to s
        });
        if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity))
            builder.setQuery("last_hit_by_player", () -> ((LivingEntity) entity).getLastHurtByMob() instanceof PlayerEntity ? 1.0F : 0.0F);
        // Skip lie_amount
        // Skip life_span
        // life_time handled by AnimatedModel
        builder.setQuery("lod_index", -1, context ->
        {
            if (context.getParameters() <= 0)
                return 0;

            double distance = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceTo(entity.position());
            for (int i = 0; i < context.getParameters(); i++)
                if (distance < context.resolve(0))
                    return i;
            return context.getParameters() - 1;
        });
        builder.setQuery("log", 1, context ->
        {
            float value = context.resolve(0);
            System.out.println(value);
            return value;
        });
        if (entity instanceof LivingEntity)
        {
            LivingEntity livingEntity = (LivingEntity) entity;
            builder.setQuery("main_hand_item_max_duration", () -> !livingEntity.getMainHandItem().isEmpty() ? livingEntity.getMainHandItem().getUseDuration() : 0.0F);
            builder.setQuery("main_hand_item_use_duration", () -> livingEntity.isUsingItem() && livingEntity.getUsedItemHand() == Hand.MAIN_HAND ? livingEntity.getTicksUsingItem() : 0.0F);
            builder.setQuery("off_hand_item_max_duration", () -> !livingEntity.getOffhandItem().isEmpty() ? livingEntity.getOffhandItem().getUseDuration() : 0.0F); // Added since items can be used in offhand
            builder.setQuery("off_hand_item_use_duration", () -> livingEntity.isUsingItem() && livingEntity.getUsedItemHand() == Hand.OFF_HAND ? livingEntity.getTicksUsingItem() : 0.0F); // Added since items can be used in offhand
        }
        // Skip mark_variant
        // Skip max_durability
        if (entity instanceof LivingEntity)
            builder.setQuery("max_health", ((LivingEntity) entity)::getMaxHealth);
        // Skip max_trade_tier
        builder.setQuery("maximum_frame_time", () ->
        {
            FrameTimer frameTimer = Minecraft.getInstance().getFrameTimer();
            return (float) frameTimer.getLog()[frameTimer.getLogEnd()] / 1_000_000_000F; // ns to s
        });
        builder.setQuery("maximum_frame_time", 1, context ->
        {
            FrameTimer frameTimer = Minecraft.getInstance().getFrameTimer();
            int index = (int) Math.min(context.resolve(0), 240); // Extended from 30 to 240 since that's what FrameTimer stores
            if (index == 0)
                return (float) frameTimer.getLog()[frameTimer.getLogEnd()] / 1_000_000_000F; // ns to s
            if (index < 0)
                throw new MolangException("Invalid argument for last_frame_time(): " + index);
            int wrappedIndex = frameTimer.getLogEnd() - index;
            while (wrappedIndex < 0)
                wrappedIndex += 240;

            int finalWrappedIndex = wrappedIndex;
            return (float) LongStream.range(0, index).map(i -> frameTimer.getLog()[frameTimer.wrapIndex((int) (finalWrappedIndex + i))]).max().orElse(0L) / 1_000_000_000F; // ns to s
        });
        builder.setQuery("minimum_frame_time", () ->
        {
            FrameTimer frameTimer = Minecraft.getInstance().getFrameTimer();
            return (float) frameTimer.getLog()[frameTimer.getLogEnd()] / 1_000_000_000F; // ns to s
        });
        builder.setQuery("minimum_frame_time", 1, context ->
        {
            FrameTimer frameTimer = Minecraft.getInstance().getFrameTimer();
            int index = (int) Math.min(context.resolve(0), 240); // Extended from 30 to 240 since that's what FrameTimer stores
            if (index == 0)
                return (float) frameTimer.getLog()[frameTimer.getLogEnd()] / 1_000_000_000F; // ns to s
            if (index < 0)
                throw new MolangException("Invalid argument for last_frame_time(): " + index);
            int wrappedIndex = frameTimer.getLogEnd() - index;
            while (wrappedIndex < 0)
                wrappedIndex += 240;

            int finalWrappedIndex = wrappedIndex;
            return (float) LongStream.range(0, index).map(i -> frameTimer.getLog()[frameTimer.wrapIndex((int) (finalWrappedIndex + i))]).min().orElse(0L) / 1_000_000_000F; // ns to s
        });
        builder.setQuery("model_scale", 1.0F);
        builder.setQuery("modified_distance_moved", () -> entity.moveDist);
        if (entity instanceof LivingEntity)
            builder.setQuery("modified_move_speed", ((LivingEntity) entity)::getSpeed);
        builder.setQuery("moon_brightness", () -> entity.level.getMoonBrightness());
        builder.setQuery("moon_phase", () -> (float) entity.level.getMoonPhase());
        // Skip noise
        builder.setQuery("on_fire_time", () -> (float) entity.getRemainingFireTicks() / 20.0F);
        // Skip out_of_control
        // Skip overlay_alpha
        // Skip owner_identifier
        if (entity instanceof PlayerEntity)
            builder.setQuery("player_level", () -> (float) ((PlayerEntity) entity).experienceLevel);
        builder.setQuery("position", 1, context ->
        {
            int index = (int) context.resolve(0);
            if (index < 0 || index >= 3)
                throw new MolangException("Invalid argument for position(): " + index);
            return (float) (index == 0 ? entity.getX(partialTicks) : index == 1 ? entity.getY(partialTicks) : entity.getZ(partialTicks));
        });
        builder.setQuery("position_delta", 1, context ->
        {
            int index = (int) context.resolve(0);
            if (index < 0 || index >= 3)
                throw new MolangException("Invalid argument for position(): " + index);
            return (float) (index == 0 ? entity.getDeltaMovement().x() : index == 1 ? entity.getDeltaMovement().y() : entity.getDeltaMovement().z());
        });
        if (entity instanceof SlimeEntity)
            builder.setQuery("previous_squish_value", () -> ((SlimeEntity) entity).oSquish);
        // Skip remaining_durability
        // Skip roll_counter
        // Skip rotation_to_camera TODO
        // Skip shake_angle
        // Skip shake_time
        // Skip shield_blocking_bob
        // Skip sit_amount
        // Skip skin_id
        // Skip sleep_rotation
        // Skip sneeze_counter
        // Skip spellcolor
        // Skip standing_scale
        // Skip structural_integrity
        // Skip swell_amount
        // Skip swelling_dir
        // Skip swim_amount
        // Skip tail_angle
        builder.setQuery("target_x_rotation", () -> entity.getViewXRot(partialTicks));
        builder.setQuery("target_y_rotation", () -> entity.getViewYRot(partialTicks));
        // Skip texture_frame_index
        builder.setQuery("time_of_day", () -> entity.level.getTimeOfDay(partialTicks) / 24_000L);
        // Skip time_stamp
        // Skip total_emitter_count
        // Skip total_particle_count
        // Skip trade_tier
        // Skip unhappy_counter
        // Skip variant
        builder.setQuery("vertical_speed", () -> (float) entity.getDeltaMovement().y());
        builder.setQuery("walk_distance", () -> MathHelper.lerp(partialTicks, entity.walkDistO, entity.walkDist));
        // Skip wing_flap_position
        // Skip wing_flap_speed
        // Skip yaw_speed

        // Custom Queries
        builder.setQuery("limb_swing", limbSwing);
        builder.setQuery("limb_swing_amount", limbSwingAmount);

        return builder;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float animationTicks, float netHeadYaw, float headPitch)
    {
        GeometryModel model = this.getModel();
        model.resetTransformation();
        if (model instanceof AnimatedModel && this.animations.length > 0)
        {
            MolangRuntime.Builder builder = this.createRuntime(entity, limbSwing, limbSwingAmount, netHeadYaw, headPitch);
            if (entity instanceof MolangVariableProvider)
                builder.setVariables((MolangVariableProvider) entity);
            if (this.variableProvider != null)
                builder.setVariables(this.variableProvider);
            ((AnimatedModel) model).applyAnimations(animationTicks / 20F, builder, this.getAnimations());
        }
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        matrixStack.translate(0, 1.5F, 0); // what?
        GeometryModelRenderer.render(this.getModel(), this.texture, matrixStack, Minecraft.getInstance().renderBuffers().bufferSource(), packedLight, packedOverlay, red, green, blue, alpha);
    }

    /**
     * @return The model this model is wrapping
     */
    public GeometryModel getModel()
    {
        return GeometryModelManager.getModel(this.model);
    }

    /**
     * @return The name of the texture table to render with
     */
    @Nullable
    public ResourceLocation getTexture()
    {
        return texture;
    }

    /**
     * @return The animations
     */
    public AnimationData[] getAnimations()
    {
        return Arrays.stream(this.animations).map(AnimationManager::getAnimation).filter(animation -> animation != AnimationData.EMPTY).toArray(AnimationData[]::new);
    }

    /**
     * Sets the texture table to render with.
     *
     * @param texture The new texture
     */
    public void setTexture(@Nullable ResourceLocation texture)
    {
        this.texture = texture;
    }

    /**
     * Sets the new animation to use.
     *
     * @param animations The animations to play
     */
    public void setAnimations(ResourceLocation... animations)
    {
        this.animations = animations;
    }

    /**
     * Sets an additional provider for MoLang variables.
     *
     * @param variableProvider The provider for variables in addition to the entity's variable provider
     */
    public void setVariableProvider(@Nullable MolangVariableProvider variableProvider)
    {
        this.variableProvider = variableProvider;
    }
}
