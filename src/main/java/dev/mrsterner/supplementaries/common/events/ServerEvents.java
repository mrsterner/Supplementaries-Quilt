package net.mehvahdjukaar.supplementaries.common.events;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RakedGravelBlock;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.entities.PearlMarker;
import net.mehvahdjukaar.supplementaries.common.entities.goals.EatFodderGoal;
import net.mehvahdjukaar.supplementaries.common.items.AbstractMobContainerItem;
import net.mehvahdjukaar.supplementaries.common.items.CandyItem;
import net.mehvahdjukaar.supplementaries.common.items.FluteItem;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSendLoginPacket;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncAntiqueInk;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.utils.MovableFakePlayer;
import net.mehvahdjukaar.supplementaries.common.world.data.GlobeData;
import net.mehvahdjukaar.supplementaries.common.world.songs.FluteSongsReloadListener;
import net.mehvahdjukaar.supplementaries.common.world.songs.SongsManager;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.*;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Set;


@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {

    //high priority event to override other wall lanterns
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlockHigh(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();
        if (!player.isSpectator() && !event.isCanceled()) {
            ItemsOverrideHandler.tryHighPriorityClickedBlockOverride(event, event.getItemStack());
        }
    }

    //block placement should stay low in priority to allow other more important mod interaction that use the event
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();
        if (!player.isSpectator() && !event.isCanceled()) {
            ItemsOverrideHandler.tryPerformClickedBlockOverride(event, event.getItemStack(), false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player playerIn = event.getPlayer();

        ItemsOverrideHandler.tryPerformClickedItemOverride(event, playerIn.getItemInHand(event.getHand()));
    }

    //raked gravel
    @SubscribeEvent
    public static void onHoeUsed(UseHoeEvent event) {
        UseOnContext context = event.getContext();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        if (ServerConfigs.cached.RAKED_GRAVEL) {
            if (world.getBlockState(pos).is(Blocks.GRAVEL)) {
                BlockState raked = ModRegistry.RAKED_GRAVEL.get().getDefaultState ();
                if (raked.canSurvive(world, pos)) {
                    world.setBlockState(pos, RakedGravelBlock.getConnectedState(raked, world, pos, context.getPlayerFacing()), 11);
                    world.playSound(context.getPlayer(), pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    event.setResult(Event.Result.ALLOW);
                }
            }
        }
    }


    @SubscribeEvent
    public static void onAttachTileCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        CapabilityHandler.attachCapabilities(event);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER) {
            CandyItem.checkSweetTooth(event.player);
        }
    }

    private static final boolean FODDER_ENABLED = RegistryConfigs.Reg.FODDER_ENABLED.get();

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (FODDER_ENABLED) {
            Entity entity = event.getEntity();
            if (entity instanceof Animal animal) {
                EntityType<?> type = event.getEntity().getType();
                if (type.is(ModTags.EATS_FODDER)) {
                    animal.goalSelector.addGoal(3,
                            new EatFodderGoal(animal, 1, 8, 2, 30));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        try {
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                    new ClientBoundSendLoginPacket(UsernameCache.getMap()));
        } catch (Exception exception) {
            Supplementaries.LOGGER.warn("failed to end login message: " + exception);
        }
        GlobeData.sendGlobeData(event);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(final AddReloadListenerEvent event) {
        event.addListener(new FluteSongsReloadListener());
    }

    @SubscribeEvent
    public static void onPistonMoved(final PistonEvent.Post event) {

        if (event.getPistonMoveType() == PistonEvent.PistonMoveType.RETRACT) {
            WorldAccess level = event.getWorld();
            var pos = event.getPos();
        }
    }

    @SubscribeEvent
    public static void noteBlockEvent(final NoteBlockEvent.Play event) {
        SongsManager.recordNote(event.getWorld(), event.getPos());
    }

    //TODO: remove when forge PR gets approved. using mixin right now
    //antique ink sync
    //@SubscribeEvent
    public static void onPlayerStartTracking(final ChunkWatchEvent.Watch event) {
        ServerWorld serverWorld = event.getWorld();

        //TODO: this is slow af. mixin in ChunkHolder
        ChunkAccess chunk = serverLevel.getChunkSource().getChunk(event.getPos().x, event.getPos().z, ChunkStatus.FULL, false);
        if (chunk != null) {
            Set<BlockPos> positions = chunk.getBlockEntitiesPos();
            WorldAccess level = event.getWorld();
            for (BlockPos pos : positions) {
                BlockEntity te = level.getBlockEntity(pos);
                if (te != null) {
                    var cap = te.getCapability(CapabilityHandler.ANTIQUE_TEXT_CAP);
                    if (cap.isPresent()) {
                        MinecraftServer server = serverLevel.getServer();
                        var c = cap.orElse(null);
                        boolean a = c.hasAntiqueInk();
                        server.tell(new TickTask(server.getTickCount(), () ->
                                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(event::getPlayer),
                                        new ClientBoundSyncAntiqueInk(pos, a))));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onSaplingGrow(SaplingGrowTreeEvent event) {
        WorldAccess level = event.getWorld();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos.below());
        if (state.getBlock() instanceof PlanterBlock) {
            level.syncWorldEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos.below(), Block.getId(state));
            level.setBlockState(pos.below(), Blocks.ROOTED_DIRT.getDefaultState (), 2);
            level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1, 0.71f);
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onProjectileImpact(final ProjectileImpactEvent event) {
        PearlMarker.onProjectileImpact(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDimensionUnload(WorldEvent.Unload event) {
        if (event.getWorld() instanceof ServerWorld serverLevel)
            MovableFakePlayer.unloadLevel(serverLevel);
    }

    //TODO: Use for cages
    //for flute and cage
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof FluteItem) {
            if (FluteItem.interactWithPet(stack, event.getPlayer(), event.getTarget(), event.getHand())) {
                event.setCancellationResult(ActionResult.SUCCESS); // we need this for event to be actually cancelled
                event.setCanceled(true);
            }
        } else if (stack.getItem() instanceof AbstractMobContainerItem containerItem) {
            if (!containerItem.isFull(stack)) {
                var res = containerItem.doInteract(stack, event.getPlayer(), event.getTarget(), event.getHand());
                if (res.consumesAction()){
                    event.setCancellationResult(ActionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }

}
