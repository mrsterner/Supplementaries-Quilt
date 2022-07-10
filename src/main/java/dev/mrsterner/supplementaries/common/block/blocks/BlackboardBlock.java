package dev.mrsterner.supplementaries.common.block.blocks;

import com.mojang.datafixers.util.Pair;
import dev.mrsterner.supplementaries.api.moonlightlib.WaterloggableBlock;
import net.mehvahdjukaar.supplementaries.api.ISoapWashable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.items.SoapItem;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static net.minecraft.state.property.Properties.WATERLOGGED;

public class BlackboardBlock extends WaterloggableBlock implements BlockEntityProvider, ISoapWashable {

    protected static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(0.0D, 0.0D, 11.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.SOUTH);
    protected static final VoxelShape SHAPE_EAST = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.EAST);
    protected static final VoxelShape SHAPE_WEST = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.WEST);

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public BlackboardBlock(Settings properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH)
                .with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        if (world.getBlockEntity(pos) instanceof BlackboardBlockTile tile) {
            BlockUtils.addOptionalOwnership(placer, tile);
        }
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.get(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext context) {
        return switch (state.get(FACING)) {
            default -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
        };
    }

    //I started using this convention, so I have to keep it for backwards compat
    private static byte colorToByte(DyeColor color) {
        return switch (color) {
            case BLACK -> (byte) 0;
            case WHITE -> (byte) 1;
            case ORANGE -> (byte) 15;
            default -> (byte) color.getId();
        };
    }

    public static int colorFromByte(byte b) {
        return switch (b) {
            case 0, 1 -> 0xffffff;
            case 15 -> DyeColor.ORANGE.getMapColor().color;
            default -> DyeColor.byId(b).getMapColor().color;
        };
    }

    public static Pair<Integer, Integer> getHitSubPixel(BlockHitResult hit) {
        Vec3d v2 = hit.getPos();
        Vec3d v = v2.rotateY((float) ((hit.getSide().asRotation()) * Math.PI / 180f));
        double fx = ((v.x % 1) * 16);
        if (fx < 0) fx += 16;
        int x = MathHelper.clamp((int) fx, -15, 15);

        int y = 15 - (int) MathHelper.clamp(Math.abs((v.y % 1) * 16), 0, 15);
        return new Pair<>(x, y);
    }

    @Nullable
    public static DyeColor getStackChalkColor(ItemStack stack) {
        Item item = stack.getItem();
        DyeColor color = null;
        if (ServerConfigs.cached.BLACKBOARD_COLOR) {
            if (item.getRegistryName().getNamespace().equals("chalk")) {
                color = DyeColor.byName(item.getRegistryName().getPath().replace("_chalk", ""), DyeColor.WHITE);
            } else color = DyeColor.getColor(stack);
        }
        if (color == null) {

            if (stack.isIn(ModTags.CHALK) || stack.isIn(Tags.Items.DYES_WHITE)) {
                color = DyeColor.WHITE;
            } else if (item == Items.COAL || item == Items.CHARCOAL || stack.is(Tags.Items.DYES_BLACK)) {
                color = DyeColor.BLACK;
            }
        }
        return color;
    }

    @Override
    public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
							  BlockHitResult hit) {
        if (worldIn.getBlockEntity(pos) instanceof BlackboardBlockTile te && te.isAccessibleBy(player) && !te.isWaxed()) {
            ItemStack stack = player.getStackInHand(handIn);

            if (stack.getItem() instanceof HoneycombItem) {
                if (player instanceof ServerPlayerEntity) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
                }
                stack.decrement(1);
                worldIn.syncWorldEvent(player, 3003, pos, 0);
                te.setWaxed(true);
                return ActionResult.success(worldIn.isClient());
            }

            if (hit.getSide() == state.get(FACING)) {

                if (stack.getItem() instanceof SoapItem) return ActionResult.PASS;
                Pair<Integer, Integer> pair = getHitSubPixel(hit);
                int x = pair.getFirst();
                int y = pair.getSecond();

                DyeColor color = getStackChalkColor(stack);
                if (color != null) {
                    byte newColor = colorToByte(color);
                    if (te.getPixel(x, y) != newColor) {
                        te.setPixel(x, y, newColor);
                        te.setChanged();
                    }
                    return ActionResult.success(worldIn.isClient());
                }
            }
            if (!worldIn.isClient()) {
                te.sendOpenGuiPacket(worldIn, pos, player);
            }
            return ActionResult.success(worldIn.isClient());
        }
        return ActionResult.PASS;
    }


    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        boolean flag = context.getWorld().getFluidState(context.getBlockPos()).getType() == Fluids.WATER;
        return this.getDefaultState ().with(FACING, context.getPlayerFacing().getOpposite()).with(WATERLOGGED, flag);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlackboardBlockTile(pPos, pState);
    }

    public ItemStack getBlackboardItem(BlackboardBlockTile te) {
        ItemStack itemstack = new ItemStack(this);
        if (!te.isEmpty()) {
            NbtCompound tag = te.savePixels(new NbtCompound());
            if (!tag.isEmpty()) {
                itemstack.setSubNbt("BlockEntityTag", tag);
            }
        }
        return itemstack;
    }

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		if (world.getBlockEntity(pos) instanceof BlackboardBlockTile te) {
			return this.getBlackboardItem(te);
		}
		return super.getPickStack(world, pos, state);
	}



    @Override
    public boolean tryWash(World level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof BlackboardBlockTile te) {
            if (te.isWaxed()) {
                te.setWaxed(false);
                te.setChanged();
                return true;
            } else if (!te.isEmpty()) {
                te.clear();
                te.setChanged();
                return true;
            }
        }
        return false;
    }
}
