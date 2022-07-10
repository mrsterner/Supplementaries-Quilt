package net.mehvahdjukaar.supplementaries.common.block.tiles;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Hand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.UUID;

public class DoubleSkullBlockTile extends EnhancedSkullBlockTile {

    @Nullable
    protected SkullBlockEntity innerTileUp = null;

    private int waxColorInd = -1;
    private ResourceLocation waxTexture = null;

    public DoubleSkullBlockTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModRegistry.SKULL_PILE_TILE.get(), pWorldPosition, pBlockState);
    }

    @Override
    public void saveAdditional(NbtCompound tag) {
        super.saveAdditional(tag);
        this.saveInnerTile("SkullUp", this.innerTileUp, tag);

        if (waxColorInd != -1) {
            tag.putInt("WaxColor", waxColorInd);
        }
    }

    @Override
    public void load(NbtCompound tag) {
        super.load(tag);
        this.innerTileUp = this.loadInnerTile("SkullUp", this.innerTileUp, tag);

        if (tag.contains("WaxColor")) {
            this.waxColorInd = tag.getInt("WaxColor");
            DyeColor d = waxColorInd == 17 ? null : DyeColor.byId(waxColorInd);
            this.waxTexture = Textures.SKULL_CANDLES_TEXTURES.get(d);
        } else {
            waxTexture = null;
        }
    }

    public ItemStack getSkullItemUp() {
        if (this.innerTileUp != null) {
            return new ItemStack(innerTileUp.getBlockState().getBlock());
        }
        return ItemStack.EMPTY;
    }

    public void rotateUp(BlockRotation rotation) {
        if (this.innerTileUp != null) {
            BlockState state = this.innerTileUp.getBlockState();
            int r = this.innerTileUp.getBlockState().get(SkullBlock.ROTATION);
            this.innerTileUp.setBlockStateState(state.with(SkullBlock.ROTATION,
                    rotation.rotate(r, 16)));
        }
    }

    public void rotateUpStep(int step) {
        if (this.innerTileUp != null) {
            BlockState state = this.innerTileUp.getBlockState();
            int r = this.innerTileUp.getBlockState().get(SkullBlock.ROTATION);
            this.innerTileUp.setBlockStateState(state.with(SkullBlock.ROTATION,
                    ((r - step) + 16) % 16));
        }
    }

    @Override
    public void initialize(SkullBlockEntity oldTile, SkullBlock skullBlock, ItemStack skullStack, Player player, Hand hand) {
        super.initialize(oldTile, skullBlock, skullStack, player, hand);
        if (skullStack.getItem() instanceof BlockItem bi) {
            if (bi.getBlock() instanceof SkullBlock upSkull) {
                var context = new ItemPlacementContext(player, hand, skullStack,
                        new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.UP, this.getBlockPos(), false));
                BlockState state = upSkull.getPlacementState(context);
                if (state == null) {
                    state = upSkull.getDefaultState ();
                }
                BlockEntity entity = upSkull.createBlockEntity(this.getBlockPos(), state);
                if (entity instanceof SkullBlockEntity blockEntity) {
                    this.innerTileUp = blockEntity;

                    //sets owner of upper tile
                    GameProfile gameprofile = null;
                    if (skullStack.hasTag()) {
                        NbtCompound compoundtag = skullStack.getTag();
                        if (compoundtag.contains("SkullOwner", 10)) {
                            gameprofile = NbtUtils.readGameProfile(compoundtag.getCompound("SkullOwner"));
                        } else if (compoundtag.contains("SkullOwner", 8) && !StringUtils.isBlank(compoundtag.getString("SkullOwner"))) {
                            gameprofile = new GameProfile(null, compoundtag.getString("SkullOwner"));
                        }
                    }
                    this.innerTileUp.setOwner(gameprofile);
                }
            }
        }
    }

    public void updateWax(BlockState above) {
        int index = -1;
        DyeColor c = null;
        if (above.getBlock() instanceof CandleBlock block) {
            c = CandleSkullBlockTile.colorFromCandle(block);
            if (c == null) index = 17;
            else index = c.getId();
        }
        if (this.waxColorInd != index) {
            this.waxColorInd = index;
            if (this.level instanceof ServerLevel) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
            } else {
                this.waxTexture = waxColorInd == -1 ? null : Textures.SKULL_CANDLES_TEXTURES.get(c);
            }
        }
    }

    public ResourceLocation getWaxTexture() {
        return waxTexture;
    }

    @Nullable
    public BlockState getSkullUp() {
        if (this.innerTileUp != null) {
            return this.innerTileUp.getBlockState();
        }
        return null;
    }

    @Nullable
    public BlockEntity getSkullTileUp() {
        return this.innerTileUp;
    }
}
