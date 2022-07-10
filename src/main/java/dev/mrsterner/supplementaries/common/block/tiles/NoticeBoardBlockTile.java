package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.common.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.common.block.util.IMapDisplay;
import net.mehvahdjukaar.supplementaries.common.block.util.TextHolder;
import net.mehvahdjukaar.supplementaries.common.inventories.NoticeBoardContainerMenu;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.cctweaked.CCPlugin;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class NoticeBoardBlockTile extends ItemDisplayTile implements Nameable, IMapDisplay {
    //client stuff
    private String text = null;
    private int fontScale = 1;
    private List<FormattedCharSequence> cachedPageLines = Collections.emptyList();
    //used to tell renderer when it has to slit new line(have to do it there cause i need fontrenderer function)
    private boolean needsVisualRefresh = true;
    private Material cachedPattern = null;

    private boolean powered = false;
    private int pageNumber = 0;

    private DyeColor textColor = DyeColor.BLACK;
    // private int packedFrontLight =0;
    private boolean textVisible = true; //for culling

    //TODO: FINISH THIS!
    private final TextHolder textHolder;

    public NoticeBoardBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.NOTICE_BOARD_TILE.get(), pos, state);
        this.textHolder = new TextHolder(1);
    }

    @Override
    public Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.notice_board");
    }

    //refreshTextures blockState and plays sound. server side
    @Override
    public void updateTileOnInventoryChanged() {

        boolean shouldHaveBook = !this.getDisplayedItem().isEmpty();

        BlockState state = this.getBlockState();
        if (state.get(BlockStateProperties.HAS_BOOK) != shouldHaveBook) {
            this.level.setBlockState(this.worldPosition, state.with(BlockStateProperties.HAS_BOOK, shouldHaveBook), 2);
            if (shouldHaveBook) {
                this.level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1F,
                        this.level.random.nextFloat() * 0.10F + 0.85F);
            } else {
                this.pageNumber = 0;
                this.level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1F,
                        this.level.random.nextFloat() * 0.10F + 0.50F);
            }
        }
    }

    @Override
    public ItemStack getMapStack() {
        return this.getDisplayedItem();
    }

    @Override
    public void updateClientVisualsOnLoad() {

        ItemStack itemstack = getDisplayedItem();
        Item item = itemstack.getItem();
        this.cachedPattern = null;
        if (item instanceof BannerPatternItem bannerPatternItem) {
            this.cachedPattern = ClientRegistry.FLAG_MATERIALS.get(bannerPatternItem.getBannerPattern());
        }

        this.needsVisualRefresh = true;
        this.cachedPageLines = Collections.emptyList();
        this.text = null;
        NbtCompound com = itemstack.getTag();
        if ((item instanceof WrittenBookItem && WrittenBookItem.makeSureTagIsValid(com)) ||
                (item instanceof WritableBookItem && WritableBookItem.makeSureTagIsValid(com))) {

            ListTag pages = com.getList("pages", 8).copy();
            if (pages.size() > 0) {
                if (this.pageNumber >= pages.size()) {
                    this.pageNumber = this.pageNumber % pages.size();
                }
                this.text = pages.getString(this.pageNumber);
            }

        } else if (CompatHandler.computercraft) {
            if (CCPlugin.checkForPrintedBook(item)) {

                if (com != null) {
                    int pages = CCPlugin.getPages(itemstack);

                    if (this.pageNumber >= pages) {
                        this.pageNumber = this.pageNumber % pages;
                    }
                    String[] text = CCPlugin.getText(itemstack);
                    StringBuilder combined = new StringBuilder();
                    for (int i = 0; i < 21; i++) {
                        int ind = this.pageNumber * 21 + i;
                        if (ind < text.length) {
                            combined.append(text[ind]);
                            combined.append(" ");
                        }
                    }
                    this.text = combined.toString();
                }
            }
        }
    }

    @Override
    public void load(NbtCompound compound) {
        this.textColor = DyeColor.byName(compound.getString("Color"), DyeColor.BLACK);
        this.textVisible = compound.getBoolean("TextVisible");
        this.pageNumber = compound.getInt("PageNumber");
        super.load(compound);
    }

    @Override
    public void saveAdditional(NbtCompound tag) {
        super.saveAdditional(tag);
        tag.putString("Color", this.textColor.getName());
        tag.putBoolean("TextVisible", this.textVisible);
        tag.putInt("PageNumber", this.pageNumber);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new NoticeBoardContainerMenu(id, player, this);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return this.isEmpty() && (ServerConfigs.cached.NOTICE_BOARDS_UNRESTRICTED || isPageItem(stack.getItem()));
    }

    public static boolean isPageItem(Item item) {
        return item.builtInRegistryHolder().is(ItemTags.LECTERN_BOOKS) || item instanceof MapItem || item instanceof BannerPatternItem || (CompatHandler.computercraft && CCPlugin.checkForPrintedBook(item));
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    //TODO: remove some of these
    public DyeColor getTextColor() {
        return this.textColor;
    }

    public boolean setTextColor(DyeColor newColor) {
        if (newColor != this.getTextColor()) {
            this.textColor = newColor;
            return true;
        } else {
            return false;
        }
    }

    public boolean shouldSkipTileRenderer() {
        return !textVisible || !getBlockState().get(NoticeBoardBlock.HAS_BOOK);
    }

    public void setTextVisible(boolean textVisible) {
        this.textVisible = textVisible;
    }

    public Material getCachedPattern() {
        return cachedPattern;
    }

    public String getText() {
        return text;
    }

    public int getFontScale() {
        return this.fontScale;
    }

    public void setFontScale(int s) {
        this.fontScale = s;
    }

    public void setCachedPageLines(List<FormattedCharSequence> l) {
        this.cachedPageLines = l;
    }

    public List<FormattedCharSequence> getCachedPageLines() {
        return this.cachedPageLines;
    }

    public boolean getFlag() {
        if (this.needsVisualRefresh) {
            this.needsVisualRefresh = false;
            return true;
        }
        return false;
    }

    public Direction getDirection() {
        return this.getBlockState().get(NoticeBoardBlock.FACING);
    }

    //server side only
    public void updatePower(boolean powered) {
        if (powered != this.powered && powered) {
            this.pageNumber++;
            this.level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1F,
                    this.level.random.nextFloat() * 0.10F + 1.45F);
            this.setChanged();
        }
        this.powered = powered;
    }


    public ActionResult interact(Player player, Hand handIn, BlockPos pos, BlockState state) {
        ItemStack itemStack = player.getItemInHand(handIn);

        boolean server = !player.level.isClient();

        //TODO: add text holder
        //ActionResult result = textHolder.interactWithPlayer(level, player, handIn, tile::setChanged);
        //if(result != ActionResult.PASS) return result;


        if (player.isShiftKeyDown() && !this.isEmpty()) {
            if (server) {
                ItemStack it = this.removeItemNoUpdate(0);
                BlockPos newPos = pos.offset(state.get(NoticeBoardBlock.FACING).getNormal());
                ItemEntity drop = new ItemEntity(player.level, newPos.getX() + 0.5, newPos.getY() + 0.5, newPos.getZ() + 0.5, it);
                drop.setDefaultPickUpDelay();
                player.level.addFreshEntity(drop);
                this.setChanged();
            }
        }
        //try place
        else if (!super.interact(player, handIn).consumesAction()) {
            if (server) {
                player.openMenu(this);
            }
        }
        return ActionResult.success(!server);
    }
}
