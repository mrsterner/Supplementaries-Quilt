package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ISoapWashable;
import net.mehvahdjukaar.supplementaries.client.particles.ParticleUtil;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSpawnBlockParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Criteria;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.ActionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public class SoapItem extends Item {
    public static final FoodProperties SOAP_FOOD = (new FoodProperties.Builder())
            .nutrition(0).saturationMod(0.1F).alwaysEat().effect(
                    () -> new MobEffectInstance(MobEffects.POISON, 120, 2), 1).build();
    ;

    public SoapItem(Properties pProperties) {
        super(pProperties.food(SOAP_FOOD));
    }

    @Override
    public ActionResultHolder<ItemStack> use(World level, Player player, Hand hand) {
        if (!hasBeenEatenBefore(player, level)) {
            ItemStack itemstack = player.getItemInHand(hand);
            if (player.canEat(true)) {
                player.startUsingItem(hand);
                return ActionResultHolder.consume(itemstack);
            } else {
                return ActionResultHolder.fail(itemstack);
            }
        } else {
            return ActionResultHolder.pass(player.getItemInHand(hand));
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, World pLevel, LivingEntity entity) {
        //stack.hurtAndBreak(1, entity, (e)-> {stack.grow(1); e.stopUsingItem();});
        if (pLevel.isClient()) {
            Vec3 v = entity.getViewVector(0).normalize();
            double x = entity.getX() + v.x;
            double y = entity.getEyeY() + v.y - 0.12;
            double z = entity.getZ() + v.z;
            for(int j = 0; j<4; j++) {
                Random r = entity.getRandom();
                v = v.scale(0.1 + r.nextFloat() * 0.1f);
                double dx = v.x + ((0.5 - r.nextFloat()) * 0.9);
                double dy = v.y + ((0.5 - r.nextFloat()) * 0.06);
                double dz = v.z + ((0.5 - r.nextFloat()) * 0.9);

                pLevel.addParticle(ModRegistry.SUDS_PARTICLE.get(), x, y, z, dx, dy, dz);
            }
        }
        return super.finishUsingItem(pStack, pLevel, entity);
    }

    public static boolean hasBeenEatenBefore(Player player, World level) {
        ResourceLocation res = Supplementaries.res("husbandry/soap");
        if (level instanceof ServerWorld serverWorld && player instanceof ServerPlayerEntity serverPlayer) {
            Advancement a = serverLevel.getServer().getAdvancements().getAdvancement(res);
            if (a != null) {
                return serverPlayer.getAdvancements().getOrStartProgress(a).isDone();
            }
        } else if (player instanceof LocalPlayer localPlayer) {
            var advancements = localPlayer.connection.getAdvancements();
            Advancement a = advancements.getAdvancements().get(res);
            return a != null;
        }
        return false;
    }

    //TODO: replace with event
    @Override
    public ActionResult useOn(UseOnContext context) {
        World level = context.getWorld();

        if (tryCleaning(context.getItemInHand(), level, context.getBlockPos(), context.getPlayer()))
            return ActionResult.success(level.isClient());

        return super.useOn(context);
    }

    //move all of this into the event so it takes priority
    public static boolean tryCleaning(ItemStack stack, World level, BlockPos pos, @Nullable Player player) {
        BlockState newState = null;
        BlockState oldState = level.getBlockState(pos);
        Block b = oldState.getBlock();
        boolean success = false;

        if (b instanceof ISoapWashable soapWashable) {
            success = soapWashable.tryWash(level, pos, oldState);
        } else {

            ItemStack temp = new ItemStack(Items.IRON_AXE);

            Optional<BlockState> optional = Optional.ofNullable(b.getToolModifiedState(oldState, level, pos, null, temp, ToolActions.AXE_WAX_OFF));
            if (optional.isPresent()) {
                newState = optional.get();
            }

            optional = Optional.ofNullable(b.getToolModifiedState(oldState, level, pos, null, temp, ToolActions.AXE_SCRAPE));
            while (optional.isPresent()) {
                newState = optional.get();
                optional = Optional.ofNullable(b.getToolModifiedState(newState, level, pos, null, temp, ToolActions.AXE_SCRAPE));
            }

            //try parsing it if mods aren't using that tool modifier state (cause they arent god darn)
            if (newState == null) {
                ResourceLocation r = oldState.getBlock().getRegistryName();
                //hardcoding goes brr. This is needed and I can't just use forge event since I only want to react to axe scrape, not stripping
                String name = r.getPath();
                String[] keywords = new String[]{"waxed_", "weathered_", "exposed_", "oxidized_",
                        "_waxed", "_weathered", "_exposed", "_oxidized"};
                for (String key : keywords) {
                    if (name.contains(key)) {
                        String newName = name.replace(key, "");
                        Block bb = ForgeRegistries.BLOCKS.get(new ResourceLocation(r.getNamespace(), newName));
                        if(bb == null){
                            //tries minecraft namespace
                            bb = ForgeRegistries.BLOCKS.get(new ResourceLocation(newName));
                        }
                        if (bb != null && bb != Blocks.AIR) {
                            newState = bb.withPropertiesOf(oldState);
                            break;
                        }
                    }
                }
            }

            if (newState != null && newState != oldState) {
                success = true;
                if (!level.isClient()) {
                    level.setBlockState(pos, newState, 11);
                }
            }
        }

        if (level instanceof ServerWorld serverLevel) {
            if (success) {
                level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON,
                        player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, 1.0F, 1.0F);
                NetworkHandler.sendToAllInRangeClients(pos, serverLevel, 64,
                        new ClientBoundSpawnBlockParticlePacket(pos, ParticleUtil.EventType.BUBBLE_CLEAN));
                stack.decrement(1);

                if (player != null) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                } else {
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos);
                }
            }
        }
        return success;
    }

}
