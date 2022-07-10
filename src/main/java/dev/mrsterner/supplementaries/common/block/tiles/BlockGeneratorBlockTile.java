package net.mehvahdjukaar.supplementaries.common.block.tiles;


import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.common.world.generation.structure.RoadSignFeature;
import net.mehvahdjukaar.supplementaries.common.world.generation.structure.StructureLocator;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.MathHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//turn back now while you can. You have been warned
public class BlockGeneratorBlockTile extends BlockEntity {

    private boolean firstTick = true;

    //TODO: make them not spawn in villages
    public List<Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>>> threadResult = null;

    public BlockGeneratorBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BLOCK_GENERATOR_TILE.get(), pos, state);
    }

    private static final BlockState trapdoor = Blocks.SPRUCE_TRAPDOOR.getDefaultState ();
    private static final BlockState lantern = Blocks.LANTERN.getDefaultState ().with(LanternBlock.HANGING, true);
    private static final BlockState lanternDown = Blocks.LANTERN.getDefaultState ();
    private static final BlockState fence = Blocks.SPRUCE_FENCE.getDefaultState ();
    private static final BlockState jar = lantern;//ModRegistry.JAR.get().getDefaultState (); //TODO: replace with new firefly jar
    private static final BlockState slab = Blocks.SPRUCE_SLAB.getDefaultState ();
    private static final BlockState log = Blocks.STRIPPED_SPRUCE_LOG.getDefaultState ();
    private static final BlockState stoneSlab = Blocks.STONE_SLAB.getDefaultState ();
    private static final BlockState stone = Blocks.STONE.getDefaultState ();
    private static final BlockState stair = Blocks.STONE_STAIRS.getDefaultState ();
    private static final BlockState air = Blocks.AIR.getDefaultState ();
    private static final BlockState path = Blocks.DIRT_PATH.getDefaultState ();
    private static final BlockState path_2 = Blocks.SMOOTH_SANDSTONE.getDefaultState ();


    private double averageAngles(float a, float b) {
        a = (float) (a * Math.PI / 180);
        b = (float) (b * Math.PI / 180);

        return (180 / Math.PI) * MathHelper.atan2(MathHelper.sin(a) + MathHelper.sin(b), MathHelper.cos(a) + MathHelper.cos(b));
    }

    //TODO: cleanup
    //TODO: this has to be the worst code I've written here
    public static void tick(World pLevel, BlockPos pPos, BlockState pState, BlockGeneratorBlockTile tile) {
        //if you are reading this I'm sorry...
        if (pWorld == null || pLevel.isClient()) return;

        if (tile.firstTick) {
            tile.firstTick = false;

            ServerWorld world = (ServerLevel) pLevel;
            BlockPos pos = pPos.below(2);

            /*
            //lets hope world is thread safe
            try {
                Executors.newSingleThreadExecutor()
                        .submit(() -> tile.threadResult = StructureLocator.find(world, posX, posZ, 2));
            } catch (Exception e) {
                tile.failAndRemove(pLevel, pPos, e);
            }
            */


            Thread thread = new Thread(() -> {
                try {
                    tile.threadResult = StructureLocator.findNearestMapFeatures(
                            world, ModTags.WAY_SIGN_DESTINATIONS, pos, 250,
                            false, 2);
                } catch (Exception e) {
                    tile.failAndRemove(pLevel, pPos, e);
                }
            });
            thread.start();


        }

        try {
            if (tile.threadResult != null) {

                ServerWorld world = (ServerLevel) pLevel;
                BlockPos pos = pPos.below(2);

                BlockState topState = trapdoor;

                List<Pair<Integer, BlockPos>> villages = new ArrayList<>();
                for (var r : tile.threadResult) {
                    villages.add(Pair.of((int) MathHelper.sqrt((float) r.getFirst().distToCenterSqr(pos.getX(), pos.getY(), pos.getZ())), r.getFirst()));
                }

                //if I am in a village
                boolean inVillage = false;//locateResult.getRight();

                if (inVillage) {
                    var b = world.getBiome(pos);
                    BlockState replace = b.is(BiomeTags.HAS_VILLAGE_DESERT) ? path_2 : path;
                    replaceCobbleWithPath(world, pos, replace);
                }


                if (villages.size() >= 1) {


                    Random rand = world.random;
                    //if two signs will spawn
                    boolean twoSigns = true;
                    BlockPos village1;
                    BlockPos village2;
                    int dist1;
                    int dist2;


                    //only 1 sing found/ 1 sign post. always to closest village. posts that are relatively close to a village will always have two.
                    //posts in a village will point away
                    if (villages.size() == 1 || (0.3 > rand.nextFloat() && villages.get(0).getFirst() > 192)) {
                        dist1 = villages.get(0).getFirst();
                        village1 = villages.get(0).getSecond();
                        dist2 = dist1;
                        village2 = village1;
                        twoSigns = false;
                    } else {
                        boolean inv = rand.nextBoolean();
                        dist1 = villages.get(inv ? 0 : 1).getFirst();
                        village1 = villages.get(inv ? 0 : 1).getSecond();
                        dist2 = villages.get(inv ? 1 : 0).getFirst();
                        village2 = villages.get(inv ? 1 : 0).getSecond();
                    }


                    world.setBlockState(pos, ModRegistry.SIGN_POST.get().getDefaultState (), 3);
                    if (world.getBlockEntity(pos) instanceof SignPostBlockTile sign) {
                        sign.setHeldBlock(Blocks.SPRUCE_FENCE.getDefaultState ());


                        boolean left = rand.nextBoolean();

                        sign.up = true;
                        sign.leftUp = left;
                        sign.pointToward(village1, true);


                        sign.down = twoSigns;
                        sign.leftDown = left;
                        sign.pointToward(village2, false);
                        if (Math.abs(sign.yawUp - sign.yawDown) > 90) {
                            sign.leftDown = !sign.leftDown;
                            sign.pointToward(village2, false);
                        }


                        if (ServerConfigs.cached.DISTANCE_TEXT) {
                            sign.textHolder.setLine(0, getSignText(dist1));
                            if (twoSigns)
                                sign.textHolder.setLine(1, getSignText(dist2));
                        }
                        //sign.setChanged();


                        float yaw = MathHelper.wrapDegrees(90 + (float) tile.averageAngles(-sign.yawUp + 180, -sign.yawDown + 180));
                        Direction backDir = Direction.fromYRot(yaw);

                        float diff = MathHelper.degreesDifference(yaw, backDir.asRotation());

                        Direction sideDir = (diff < 0 ? backDir.getClockWise() : backDir.getCounterClockWise());

                        ArrayList<Direction> lampDir = new ArrayList<>();
                        //lampDir.remove(sideDir);
                        lampDir.add(backDir.getOpposite());
                        lampDir.add(backDir.getOpposite());
                        lampDir.add(backDir.getOpposite());
                        if (Math.abs(diff) > 30) {
                            lampDir.add(sideDir.getOpposite());
                        }

                        boolean hasGroundLantern = false;

                        var biome = ResourceKey.create(ForgeRegistries.Keys.BIOMES, world.getBiome(pos).value().getRegistryName());

                        boolean hasFirefly = (BiomeDictionary.hasType(biome, BiomeDictionary.Type.MAGICAL) ||
                                BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP) ||
                                BiomeDictionary.hasType(biome, BiomeDictionary.Type.SPOOKY) ? 0.2f : 0.01f) > rand.nextFloat();


                        //stone
                        if (0.3 > rand.nextFloat() && MathHelper.degreesDifferenceAbs(sign.getPointingYaw(true) + 180, yaw) > 70) {
                            BlockPos stonePos = pos.below().offset(backDir.getNormal());
                            if (rand.nextBoolean()) {
                                world.setBlockState(stonePos, stoneSlab, 2);
                            } else {
                                world.setBlockState(stonePos, stair.with(StairBlock.FACING, sideDir), 2);
                            }
                            stonePos = stonePos.offset(sideDir.getNormal());
                            world.setBlockState(stonePos, stone, 2);
                            if (0.35 > rand.nextFloat()) {
                                world.setBlockState(stonePos.above(), hasFirefly ? jar : lanternDown, 3);
                                hasGroundLantern = true;
                            }
                            stonePos = stonePos.offset(sideDir.getNormal());
                            if (!RoadSignFeature.isNotSolid(world, stonePos.below())) {
                                if (rand.nextBoolean()) {
                                    world.setBlockState(stonePos, stoneSlab, 2);
                                } else {
                                    world.setBlockState(stonePos, stair.with(StairBlock.FACING, sideDir.getOpposite()), 2);
                                }
                            }

                        }


                        if (!hasGroundLantern) {

                            //lanterns
                            pos = pos.above(2);

                            BlockState light = hasFirefly ? jar : lantern;

                            Direction dir = lampDir.get(rand.nextInt(lampDir.size()));

                            boolean doubleSided = 0.25 > rand.nextFloat();
                            if (doubleSided) {
                                dir = dir.getClockWise();
                            }

                            //wall lanterns
                            if (0.32 > rand.nextFloat()) {
                                topState = 0.32 > rand.nextFloat() ? trapdoor : air;

                                WallLanternBlock wl = ModRegistry.WALL_LANTERN.get();
                                wl.placeOn(lanternDown, pos.below(), dir, world);

                                //double
                                if (doubleSided) {
                                    wl.placeOn(lanternDown, pos.below(), dir.getOpposite(), world);
                                }

                            } else {
                                boolean isTrapdoor = 0.4 > rand.nextFloat();

                                if (!isTrapdoor) topState = fence;

                                //double
                                if (doubleSided) {
                                    BlockPos backPos = pos.relative(dir.getOpposite());

                                    world.setBlockState(backPos, isTrapdoor ? trapdoor : fence, 2);

                                    if (0.25 > rand.nextFloat()) {
                                        topState = isTrapdoor ? slab : log;
                                    }

                                    world.setBlockState(backPos.below(), light, 3);
                                }

                                pos = pos.relative(dir);
                                BlockState frontState = isTrapdoor ? trapdoor : fence;
                                world.setBlockState(pos, frontState, 2);

                                world.setBlockState(pos.below(), light, 3);
                            }


                        }


                    }
                } else {

                    ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
                    NbtCompound com = new NbtCompound();
                    ListTag listTag = new ListTag();
                    listTag.add(StringTag.valueOf("nothing here but monsters\n\n\n"));
                    com.put("pages", listTag);
                    book.setTag(com);
                    BlockPos belowPos = pPos.below(2);
                    world.setBlockState(belowPos, ModRegistry.NOTICE_BOARD.get().getDefaultState ().with(NoticeBoardBlock.HAS_BOOK, true)
                            .with(NoticeBoardBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(world.random)), 3);
                    if (world.getBlockEntity(belowPos) instanceof NoticeBoardBlockTile board) {
                        board.setDisplayedItem(book);
                        //te.setChanged();
                    }
                }

                world.setBlockState(pPos, topState, 3);
            }

        } catch (Exception exception) {
            tile.failAndRemove(pLevel, pPos, exception);
        }
    }

    private void failAndRemove(World level, BlockPos pos, Exception e) {
        level.removeBlock(pos, false);
        Supplementaries.LOGGER.warn("failed to generate road sign at " + pos + ": " + e);
    }

    private static Component getSignText(int d) {
        int s;
        if (d < 100) s = 10;
        else if (d < 2000) s = 100;
        else s = 1000;
        return new TranslatableComponent("message.supplementaries.road_sign", (((d + (s / 2)) / s) * s));
    }

    private static void replaceCobbleWithPath(World world, BlockPos pos, BlockState path) {
        //generate cobble path

        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (Math.abs(i) == 2 && Math.abs(j) == 2) continue;
                if (i == 0 && j == 0) continue;
                BlockPos pathPos = pos.offset(i, -2, j);
                BlockState state = world.getBlockState(pathPos);
                if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.MOSSY_COBBLESTONE)) {
                    world.setBlockState(pathPos, path, 2);
                }
            }
        }
    }

}
