package net.mehvahdjukaar.supplementaries.common.items;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.selene.api.IFirstPersonAnimationProvider;
import net.mehvahdjukaar.selene.api.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.selene.math.MathHelperUtils;
import net.mehvahdjukaar.selene.util.TwoHandedAnimation;
import net.mehvahdjukaar.supplementaries.client.renderers.RotHlpr;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSounds;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.ActionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BubbleBlower extends Item implements IThirdPersonAnimationProvider, IFirstPersonAnimationProvider {

    public BubbleBlower(Properties properties) {
        super(properties);
    }

    //bubble block

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    public ActionResultHolder<ItemStack> deployBubbleBlock(ItemStack stack, World level, Player player, Hand hand) {
        HitResult result = player.getAbilities().instabuild ? CommonUtil.rayTrace(player, level, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY) :
                CommonUtil.rayTrace(player, level, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, 2.6);

        if (result instanceof BlockHitResult hitResult) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState first = level.getBlockState(pos);
            if (!first.getMaterial().isReplaceable()) {
                pos = pos.relative(hitResult.getDirection());
            }
            first = level.getBlockState(pos);
            if (first.getMaterial().isReplaceable()) {
                BlockState bubble = ModRegistry.BUBBLE_BLOCK.get().getDefaultState ();

                SoundType soundtype = bubble.getSoundType(level, pos, player);
                level.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

                if (!level.isClient()) {
                    level.setBlockStateAndUpdate(pos, bubble);
                }
                if (!(player.getAbilities().instabuild)) {
                    int max = this.getMaxDamage(stack);
                    this.setDamage(stack, Math.min(max, this.getDamage(stack) + ServerConfigs.cached.BUBBLE_BLOWER_COST));
                }

                //player.getCooldowns().addCooldown(this, 10);
                return new ActionResultHolder<>(ActionResult.SUCCESS, stack);
            }
        }
        return new ActionResultHolder<>(ActionResult.PASS, stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xe8a4e4;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 250;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        ListTag enchantments = EnchantedBookItem.getEnchantments(book);
        return enchantments.size() == 1 &&
                EnchantmentHelper.getEnchantmentId(enchantments.getCompound(0)).equals(
                        EnchantmentHelper.getEnchantmentId(ModRegistry.STASIS_ENCHANTMENT.get()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (this.getCharges(stack) != 0) {
            tooltip.add(new TranslatableComponent("message.supplementaries.bubble_blower_tooltip", stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage()));
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return this.getCharges(stack) > 0;
    }

    private int getCharges(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    @Override
    public ActionResultHolder<ItemStack> use(World level, Player player, Hand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        int charges = this.getCharges(itemstack);

        if (charges > 0) {

            int ench = EnchantmentHelper.getItemEnchantmentLevel(ModRegistry.STASIS_ENCHANTMENT.get(), itemstack);
            if (ench > 0) return this.deployBubbleBlock(itemstack, level, player, hand);

            player.startUsingItem(hand);

            return ActionResultHolder.consume(itemstack);
        }
        return ActionResultHolder.fail(itemstack);
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity entity, int count) {
        World level = entity.level;
        int damage = this.getDamage(stack) + 1;
        if (damage > this.getMaxDamage(stack)) {
            entity.stopUsingItem();
            return;
        }
        if (!(entity instanceof Player player) || !(player.isCreative())) {
            this.setDamage(stack, damage);
        }

        int soundLength = 4;
        if (count % soundLength == 0) {
            Player p = entity instanceof Player pl ? pl : null;
            entity.level.playSound(p, entity, ModSounds.BUBBLE_BLOW.get(), entity.getSoundSource(),
                    1.0F, MathHelperUtils.nextWeighted(level.random, 0.20f) + 0.95f);
        }

        //stack.hurtAndBreak(1, entity, (e)-> {stack.grow(1); e.stopUsingItem();});
        if (level.isClient()) {
            Vec3 v = entity.getViewVector(0).normalize();
            double x = entity.getX() + v.x;
            double y = entity.getEyeY() + v.y - 0.12;
            double z = entity.getZ() + v.z;
            Random r = entity.getRandom();
            v = v.scale(0.1 + r.nextFloat() * 0.1f);
            double dx = v.x + ((0.5 - r.nextFloat()) * 0.08);
            double dy = v.y + ((0.5 - r.nextFloat()) * 0.04);
            double dz = v.z + ((0.5 - r.nextFloat()) * 0.08);

            level.addParticle(ModRegistry.SUDS_PARTICLE.get(), x, y, z, dx, dy, dz);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand, TwoHandedAnimation twoHanded) {
        if (entity.getUseItemRemainingTicks() > 0 && entity.getUseItem().getItem() == this) {
            this.animateHands(model, entity, true);
            return true;
        }
        return false;
    }

    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand, TwoHandedAnimation twoHanded) {
        if (entity.getUseItemRemainingTicks() > 0 && entity.getUseItem().getItem() == this) {
            this.animateHands(model, entity, false);
            return true;
        }
        return false;
    }

    public <T extends LivingEntity> void animateHands(HumanoidModel<T> model, T entity, boolean leftHand) {

        ModelPart mainHand = leftHand ? model.leftArm : model.rightArm;
        int dir = (leftHand ? -1 : 1);

        float cr = (entity.isCrouching() ? 0.3F : 0.0F);

        float headXRot = RotHlpr.wrapRad(model.head.xRot);
        float headYRot = RotHlpr.wrapRad(model.head.yRot);

        float pitch = MathHelper.clamp(headXRot, -1.6f, 0.8f) + 0.55f - cr;
        mainHand.xRot = (float) (pitch - Math.PI / 2f) - dir * 0.3f * headYRot;

        float yaw = 0.7f * dir;
        mainHand.yRot = MathHelper.clamp(-yaw * MathHelper.cos(pitch + cr) + headYRot, -1.1f, 1.1f);//yaw;
        mainHand.zRot = -yaw * MathHelper.sin(pitch + cr);

        AnimationUtils.bobModelPart(mainHand, entity.tickCount, -dir);
    }


    @Override
    public void animateItemFirstPerson(LivingEntity entity, ItemStack stack, Hand hand, PoseStack matrixStack, float partialTicks, float pitch, float attackAnim, float handHeight) {
        //is using item
        if (entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0 && entity.getUsedItemHand() == hand) {
            //bow anim

            float timeLeft = (float) stack.getUseDuration() - ((float) entity.getUseItemRemainingTicks() - partialTicks + 1.0F);
            float f12 = 1;//getPowerForTime(stack, timeLeft);

            if (f12 > 0.1F) {
                float f15 = MathHelper.sin((timeLeft - 0.1F) * 1.3F);
                float f18 = f12 - 0.1F;
                float f20 = f15 * f18;
                matrixStack.translate(0, f20 * 0.004F, 0);
            }

            matrixStack.translate(0, 0, f12 * 0.04F);
            matrixStack.scale(1.0F, 1.0F, 1.0F + f12 * 0.2F);
            //matrixStack.mulPose(Vector3f.YN.rotationDegrees((float)k * 45.0F));
        }
    }


}
