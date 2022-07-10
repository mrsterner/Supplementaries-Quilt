package dev.mrsterner.supplementaries.common.block.blocks;


import net.mehvahdjukaar.selene.map.ExpandedMapData;
import net.mehvahdjukaar.supplementaries.api.IRotatable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.framedblocks.FramedSignPost;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SignPostBlock extends FenceMimicBlock implements EntityBlock, IRotatable {

    public SignPostBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public ActionResult use(BlockState state, World level, BlockPos pos, Player player, Hand handIn,
                                 BlockHitResult hit) {

        if (!level.isClient()) {
            ItemStack itemstack = player.getItemInHand(handIn);
            Item item = itemstack.getItem();

            //put post on map
            if (item instanceof MapItem) {
                if (MapItem.getSavedData(itemstack, level) instanceof ExpandedMapData data) {
                    data.toggleCustomDecoration(level, pos);
                }
                return ActionResult.CONSUME;
            }

            if (level.getBlockEntity(pos) instanceof SignPostBlockTile tile && tile.isAccessibleBy(player)) {

                boolean emptyHand = itemstack.isEmpty();
                boolean isSneaking = player.isShiftKeyDown() && emptyHand;

                if (hit.getDirection().getAxis() != Direction.Axis.Y) {

                    ActionResult result = tile.textHolder.playerInteract(level, pos, player, handIn, tile);
                    if (result != ActionResult.PASS) return result;

                    //sneak right click rotates the sign on z axis
                    if (isSneaking) {
                        double y = hit.getLocation().y;
                        //negative y yay!
                        if (y < 0) y = y + (1 - (int) y);
                        else y = y - (int) y;
                        boolean up = y > 0.5d;
                        if (up) {
                            tile.leftUp = !tile.leftUp;
                        } else {
                            tile.leftDown = !tile.leftDown;
                        }
                        tile.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);
                        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0F, 0.6F);
                        return ActionResult.CONSUME;
                    }
                    //change direction with compass
                    else if (item instanceof CompassItem) {
                        //itemModelProperties code
                        BlockPos pointingPos = CompassItem.isLodestoneCompass(itemstack) ?
                                this.getLodestonePos(level, itemstack) : this.getWorldSpawnPos(level);

                        if (pointingPos != null) {
                            double y = hit.getLocation().y;
                            boolean up = y % ((int) y) > 0.5d;
                            if (up && tile.up) {
                                tile.pointToward(pointingPos, true);
                            } else if (!up && tile.down) {
                                tile.pointToward(pointingPos, false);
                            }
                            tile.setChanged();
                            level.sendBlockUpdated(pos, state, state, 3);
                            return ActionResult.CONSUME;
                        }
                        return ActionResult.FAIL;
                    } else if (CompatHandler.framedblocks && tile.framed) {
                        boolean success = FramedSignPost.handleInteraction(tile, player, handIn, itemstack, level, pos);
                        if (success) return ActionResult.CONSUME;
                    } else if (item instanceof SignPostItem) {
                        //let sign item handle this one
                        return ActionResult.PASS;
                    }
                }
                // open gui (edit sign with empty hand)
                tile.sendOpenGuiPacket(level, pos, player);

                return ActionResult.CONSUME;
            }
            return ActionResult.PASS;
        } else {
            return ActionResult.SUCCESS;
        }
    }

    @Nullable
    private BlockPos getLodestonePos(World world, ItemStack stack) {
        NbtCompound tag = stack.getTag();
        if (tag != null) {
            boolean flag = tag.contains("LodestonePos");
            boolean flag1 = tag.contains("LodestoneDimension");
            if (flag && flag1) {
                Optional<ResourceKey<Level>> optional = CompassItem.getLodestoneDimension(tag);
                if (optional.isPresent() && world.dimension() == optional.get()) {
                    return NbtUtils.readBlockPos(tag.getCompound("LodestonePos"));
                }
            }
        }
        return null;
    }

    @Nullable
    private BlockPos getWorldSpawnPos(World world) {
        return world.dimensionType().natural() ? new BlockPos(world.getLevelData().getXSpawn(),
                world.getLevelData().getYSpawn(), world.getLevelData().getZSpawn()) : null;
    }

    @Override
    public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof SignPostBlockTile tile) {
            double y = target.getLocation().y;
            boolean up = y % ((int) y) > 0.5d;
            if (up && tile.up) {
                return new ItemStack(ModRegistry.SIGN_POST_ITEMS.get(tile.woodTypeUp));
            } else if (!up && tile.down) {
                return new ItemStack(ModRegistry.SIGN_POST_ITEMS.get(tile.woodTypeDown));
            } else return new ItemStack(tile.mimic.getBlock());
        }
        return super.getPickStack(state, target, world, pos, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof SignPostBlockTile tile) {
            List<ItemStack> list = new ArrayList<>();
            list.add(new ItemStack(tile.mimic.getBlock()));

            if (tile.up) {
                ItemStack s = new ItemStack(ModRegistry.SIGN_POST_ITEMS.get(tile.woodTypeUp));
                list.add(s);
            }
            if (tile.down) {
                ItemStack s = new ItemStack(ModRegistry.SIGN_POST_ITEMS.get(tile.woodTypeDown));
                list.add(s);
            }
            return list;
        }
        return super.getDrops(state, builder);
    }


    @Override
    public BlockState rotate(BlockState state, WorldAccess world, BlockPos pos, BlockRotation rot) {
        return state;
    }

    @Override
    public Optional<BlockState> getRotatedState(BlockState state, WorldAccess world, BlockPos pos, BlockRotation rotation, Direction axis, @Nullable Vec3 hit) {
        return Optional.of(state);
    }

    @Override
    public Optional<Direction> rotateOverAxis(BlockState state, WorldAccess world, BlockPos pos, BlockRotation rot, Direction axis, @Nullable Vec3 hit) {

        boolean success = false;
        if (world.getBlockEntity(pos) instanceof SignPostBlockTile tile) {

            boolean simple = hit == null;
            boolean ccw = rot.equals(Rotation.COUNTERCLOCKWISE_90);

            float angle = simple ? (ccw ? 90 : -90) : (22.5f * (ccw ? 1 : -1));

            if (simple) {
                if (tile.rotateSign(true, angle, false)) success = true;
                if (tile.rotateSign(false, angle, false)) success = true;
            } else {
                boolean up = hit.y % ((int) hit.y) > 0.5d;
                if (tile.rotateSign(up, angle, true)) success = true;
                else if (tile.rotateSign(!up, angle, true)) success = true;
            }

            if (success) {
                //world.notifyBlockUpdate(pos, tile.getBlockState(), tile.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
                tile.setChanged();
                if (world instanceof World level) {
                    level.sendBlockUpdated(pos, state, state, 3);
                }
                return Optional.of(Direction.UP);
            }
        }
        return Optional.empty();
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new SignPostBlockTile(pPos, pState);
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, worldIn, pos);
    }
}
