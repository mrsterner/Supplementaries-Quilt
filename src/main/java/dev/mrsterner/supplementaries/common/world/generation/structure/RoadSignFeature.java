package net.mehvahdjukaar.supplementaries.common.world.generation.structure;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.common.block.blocks.StructureTempBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Material;

import java.util.Random;


public class RoadSignFeature extends Feature<NoneFeatureConfiguration> {

    public RoadSignFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }


    private final BlockState cobble = Blocks.COBBLESTONE.getDefaultState ();
    private final BlockState mossyCobble = Blocks.MOSSY_COBBLESTONE.getDefaultState ();
    private final BlockState fence = Blocks.SPRUCE_FENCE.getDefaultState ();

    private final BlockState wall = Blocks.COBBLESTONE_WALL.getDefaultState ();
    private final BlockState mossyWall = Blocks.MOSSY_COBBLESTONE_WALL.getDefaultState ();

    private static boolean canGoThrough(WorldAccess world, BlockPos pos) {
        if (!world.getFluidState(pos).isEmpty()) return false;

        return world.isStateAtPosition(pos, (state) -> {
            Material material = state.getMaterial();
            return material.isReplaceable() || material == Material.LEAVES || material == Material.PLANT;
        });
    }


    public static boolean isReplaceable(WorldAccess world, BlockPos pos) {

        return world.isStateAtPosition(pos, (state) -> {
            if (state.getBlock() instanceof StructureTempBlock) return true;
            Material material = state.getMaterial();
            return material.isReplaceable() && material != Material.LEAVES;
        });
    }

    public static boolean isNotSolid(WorldAccess world, BlockPos pos) {
        return !world.isStateAtPosition(pos, (state) -> state.isRedstoneConductor(world, pos));
    }


    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext) {

        WorldGenWorld reader = pContext.level();
        Random rand = pContext.random();
        BlockPos pos = pContext.origin();

        /*
        if(!reader.getWorld().dimension().equals(World.OVERWORLD))return false;
        if(pos.getY()>90 || pos.getY()<50)return false;
        if(!reader.getWorld().getChunkSource().generator.getBiomeSource().canGenerateStructure(Structure.VILLAGE))return false;


        //find nearest solid block
        for(pos = pos.above(); canGoThrough(reader,pos) && pos.getY() > 2; pos = pos.below()) {}

        if(isNotSolid(reader, pos))return false;


        for(int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {

                //checks for empty blocks around wall
                for(int h = 2; h<=5; h++) {
                    //skip angles
                    //if(Math.abs(i)==2&&Math.abs(j)==2)continue;
                    if (!isReplaceable(reader,pos.offset(i,h,j))) {
                        return false;
                    }
                }
                //allows 1 block of leaves at the base
                if (!canGoThrough(reader,pos.offset(i,1,j)))return false;
                //thick solid base. no floaty sings here
                if(isNotSolid(reader, pos.offset(i, 0, j)))return false;
                if(isNotSolid(reader, pos.offset(i, -1, j)))return false;
                //if(isNotSolid(reader, pos.offset(i, -2, j)))return false;
            }
        }*/

        //for jigsaw
        pos = pos.below();

        //add air blocks around
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {

                if (Math.abs(i) == 2 && Math.abs(j) == 2) continue;
                for (int k = 1; k <= 4; ++k) {
                    if ((Math.abs(i) == 2 || Math.abs(j) == 2) && k == 1) continue;
                    reader.setBlockState(pos.offset(i, k, j), ModRegistry.STRUCTURE_TEMP.get().getDefaultState (), 2);
                }
            }
        }

        float humidity = reader.getBiome(pos).value().getDownfall();


        //generate cobble path
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (Math.abs(i) == 2 && Math.abs(j) == 2) continue;
                reader.setBlockState(pos.offset(i, -1, j), this.cobble, 2);

                BlockPos pathPos = pos.offset(i, 0, j);
                double dist = pos.distToCenterSqr(pathPos.getX(), pathPos.getY(), pathPos.getZ()) / 5.2f;

                if (rand.nextFloat() < dist - 0.15) continue;
                ;
                boolean m = (humidity * 0.75) > rand.nextFloat();
                reader.setBlockState(pathPos, m ? this.mossyCobble : this.cobble, 2);
            }
        }


        //post

        boolean m = (humidity * 0.75) > rand.nextFloat();

        pos = pos.above();
        reader.setBlockState(pos, m ? this.mossyWall : this.wall, 2);
        pos = pos.above();
        reader.setBlockState(pos, this.fence, 2);
        pos = pos.above();
        reader.setBlockState(pos, this.fence, 2);
        reader.setBlockState(pos.above(), ModRegistry.BLOCK_GENERATOR.get().getDefaultState (), 2);


        return true;
    }


}
