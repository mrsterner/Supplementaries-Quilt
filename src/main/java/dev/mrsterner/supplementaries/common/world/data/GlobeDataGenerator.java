package net.mehvahdjukaar.supplementaries.common.world.data;

import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.minecraft.util.MathHelper;

import java.util.Random;

public class GlobeDataGenerator {
    //object instance

    private static final int SIDE = 8;
    private static final int WIDTH = SIDE * 4;
    private static final int HEIGHT = SIDE * 2;
    private static final int SCALE = 20;

    public static Random RAND = new Random(1);
    public static Pixel[][] pixels = new Pixel[WIDTH][HEIGHT];

    public enum Biome {
        TEMPERATE, HOT, COLD, MUSHROOM, MOUNTAIN, MESA
    }

    public enum Feature {
        NORMAL, SUNKEN, ICEBERG, MUSHROOM
    }

    public enum TerrainType {
        NULL, LAND, WATER
    }

    public enum Face {
        F1, F2, F3, F4, TOP, BOT, NA
    }

    public static class Col {
        public static final byte BLACK = 0;
        public static final byte WATER = 1;
        public static final byte WATER_S = 2;
        public static final byte WATER_D = 3;
        public static final byte SUNKEN = 4;
        public static final byte GREEN = 5;
        public static final byte GREEN_S = 6;
        public static final byte HOT_S = 7;
        public static final byte HOT = 8;
        public static final byte COLD = 9;
        public static final byte COLD_S = 10;
        public static final byte ICEBERG = 11;
        public static final byte MUSHROOM = 12;
        public static final byte MUSHROOM_S = 13;
        public static final byte TAIGA = 14;


        public static final byte MESA = 15;
        public static final byte MESA_S = 16;
        public static final byte MOUNTAIN = 17;
        public static final byte MOUNTAIN_S = 18;
    }


    public static class Pos {
        public final int x;
        public final int y;

        Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Pos up() {
            int x = this.x;
            int y = this.y;
            Face f = getFace(x, y);
            if (f == Face.NA) return this;
            //border
            if (y == SIDE) {
                switch (f) {
                    case F1:
                        return new Pos(SIDE, x);
                    case F2:
                        return new Pos(x, y - 1);
                    case F3:
                        return new Pos(2 * SIDE - 1, (3 * SIDE) - x - 1);
                    case F4:
                        return new Pos((5 * SIDE) - x - 1, 0);
                }
            }
            if (y == 0) {
                //top face
                switch (f) {
                    case TOP:
                        return new Pos((5 * SIDE) - x - 1, SIDE);
                    case BOT:
                        return new Pos(x - SIDE, 2 * SIDE - 1);
                }
            }
            return new Pos(x, y - 1);
        }

        public Pos down() {
            int x = this.x;
            int y = this.y;
            Face f = getFace(x, y);
            if (f == Face.NA) return this;
            //border
            if (y == (2 * SIDE) - 1) {
                switch (f) {
                    case F1:
                        return new Pos(2 * SIDE, SIDE - x - 1);
                    case F2:
                        return new Pos(SIDE + x, 0);
                    case F3:
                        return new Pos(3 * SIDE - 1, x - (2 * SIDE));
                    case F4:
                        return new Pos((6 * SIDE) - x - 1, SIDE - 1);
                }
            }
            if (y == SIDE - 1) {
                //top face
                switch (f) {
                    case TOP:
                        return new Pos(x, y + 1);
                    case BOT:
                        return new Pos((6 * SIDE) - x - 1, 2 * SIDE - 1);
                }
            }
            return new Pos(x, y + 1);
        }

        public Pos left() {
            int x = this.x;
            int y = this.y;
            Face f = getFace(x, y);
            if (f == Face.NA) return this;

            if (x == SIDE && f == Face.TOP)
                return new Pos(y, SIDE);
            else if (x == 2 * SIDE && f == Face.BOT)
                return new Pos(SIDE - y, 2 * SIDE - 1);


            if (x == 0) x = 4 * SIDE;
            return new Pos(x - 1, y);
        }

        public Pos right() {
            int x = this.x;
            int y = this.y;
            Face f = getFace(x, y);
            if (f == Face.NA) return this;

            if (x == 2 * SIDE - 1 && f == Face.TOP)
                return new Pos(3 * SIDE - y, SIDE);
            else if (x == 3 * SIDE - 1 && f == Face.BOT)
                return new Pos(2 * SIDE + y, 2 * SIDE - 1);


            if (x == 4 * SIDE - 1) x = -1;
            return new Pos(x + 1, y);
        }
    }

    public static Pixel pfp(Pos p) {
        return pixels[p.x][p.y];
    }

    public static double dist(double x, double y, double x1, double y1) {
        return MathHelper.sqrt((float) (Math.pow((x - x1), 2) + Math.pow((y - y1), 2)));
    }

    public static byte[][] generate(long seed) {
        RAND = new Random(seed);
        pixels = new Pixel[WIDTH][HEIGHT];
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                pixels[x][y] = new Pixel(getFace(x, y) == Face.NA);
            }
        }
        generateLand();
        applyEffects();

        fixBottomFace();
        return getByteMatrix();
    }

    //I messed up I have to rotate bottom face by 180
    public static void fixBottomFace() {
        int N = 8;
        Pixel[][] mat = new Pixel[N][N];
        for (int x = 16; x < 24; x++) {
            System.arraycopy(pixels[x], 0, mat[x - 16], 0, 8);
        }
        // rotate the matrix by 180 degrees
        for (int i = 0; i < N / 2; i++) {
            for (int j = 0; j < N; j++) {
                Pixel temp = mat[i][j];
                mat[i][j] = mat[N - i - 1][N - j - 1];
                mat[N - i - 1][N - j - 1] = temp;
            }
        }
        for (int k = 0; k < 8; k++) {
            System.arraycopy(mat[k], 0, pixels[k + 16], 0, 8);
        }
    }

    public static byte[][] getByteMatrix() {
        byte[][] matrix = {{0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 3, 2, 2, 2, 3, 3},
                {0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 2, 1, 1, 1, 2, 3},
                {0, 0, 0, 0, 0, 0, 0, 0, 3, 1, 1, 1, 1, 1, 1, 2},
                {0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 1, 1, 1, 1, 1, 2},
                {0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 1, 1, 1, 1, 1, 2},
                {0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 1, 1, 1, 1, 1, 3},
                {0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 2, 1, 1, 2, 2, 3},
                {0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 2, 2, 3, 3, 2},
                {3, 3, 3, 2, 2, 2, 3, 3, 3, 3, 2, 2, 2, 3, 3, 3},
                {3, 2, 1, 1, 1, 2, 2, 3, 3, 2, 1, 1, 1, 1, 2, 3},
                {3, 2, 1, 1, 1, 1, 2, 2, 3, 1, 1, 1, 1, 1, 2, 3},
                {2, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 2},
                {2, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 2},
                {3, 1, 1, 1, 1, 1, 1, 2, 3, 2, 1, 1, 1, 1, 1, 3},
                {3, 2, 2, 1, 1, 1, 2, 3, 3, 2, 2, 1, 1, 2, 2, 3},
                {3, 3, 3, 2, 2, 3, 3, 3, 3, 3, 2, 2, 2, 3, 3, 3},
                {3, 3, 2, 2, 2, 3, 3, 2, 3, 3, 3, 2, 2, 2, 3, 3},
                {3, 2, 1, 1, 1, 1, 2, 3, 3, 2, 1, 1, 1, 2, 2, 3},
                {3, 1, 1, 1, 1, 1, 1, 3, 3, 2, 1, 1, 1, 1, 1, 3},
                {2, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 2},
                {2, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 2},
                {2, 2, 1, 1, 1, 1, 2, 3, 2, 2, 1, 1, 1, 1, 1, 2},
                {3, 2, 2, 1, 1, 1, 2, 3, 3, 2, 2, 1, 1, 1, 2, 3},
                {3, 3, 3, 2, 2, 3, 3, 3, 3, 3, 3, 2, 2, 3, 3, 3},
                {0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 2, 2, 2, 3, 2},
                {0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 2, 1, 1, 1, 2, 3},
                {0, 0, 0, 0, 0, 0, 0, 0, 3, 1, 1, 1, 1, 1, 1, 2},
                {0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 1, 1, 1, 1, 1, 2},
                {0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 1, 1, 1, 1, 1, 2},
                {0, 0, 0, 0, 0, 0, 0, 0, 3, 1, 1, 1, 1, 1, 2, 3},
                {0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 1, 1, 1, 2, 2, 3},
                {0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 2, 2, 2, 3, 3, 2}};


        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                byte color = pixels[x][y].getColor();
                if (color != Col.BLACK)
                    matrix[x][y] = (byte) Math.max(color, matrix[x][y]);
            }
        }


        return matrix;
    }


    public static class Pixel {
        public TerrainType terrain = TerrainType.WATER;
        public Biome biome = Biome.TEMPERATE;
        public boolean shaded = false;
        public Feature specialFeature = Feature.NORMAL;

        public Pixel() {
        }

        public Pixel(boolean isnull) {
            if (isnull)
                this.terrain = TerrainType.NULL;
        }

        public int getTemp() {
            if (this.biome == Biome.TEMPERATE) {
                return this.isShaded() ? 1 : 0;
            }
            return this.isShaded() ? 3 : 2;

        }

        public void setLand() {
            this.terrain = TerrainType.LAND;

        }

        public void setWater() {
            this.terrain = TerrainType.WATER;
        }

        public boolean isIceberg() {
            return this.specialFeature == Feature.ICEBERG;
        }

        public boolean isMushroom() {
            return this.specialFeature == Feature.MUSHROOM;
        }

        public boolean isSunken() {
            return this.specialFeature == Feature.SUNKEN;
        }

        public boolean isShaded() {
            return shaded;
        }

        public boolean isLand() {
            return this.terrain == TerrainType.LAND;
        }

        public boolean isHot() {
            return this.biome == Biome.HOT;
        }

        public boolean isTemperate() {
            return this.biome == Biome.TEMPERATE;
        }

        public boolean isCold() {
            return this.biome == Biome.COLD;
        }

        public boolean isWater() {
            return this.terrain == TerrainType.WATER;
        }

        public boolean isNull() {
            return this.terrain == TerrainType.NULL;
        }

        public byte getColor() {
            boolean s = this.isShaded();
            if (this.isSunken()) return Col.SUNKEN;
            else if (this.isIceberg()) return Col.ICEBERG;
            else if (this.isMushroom()) return Col.MUSHROOM;
            switch (this.terrain) {
                case LAND:
                    if (this.isLand()) {
                        switch (this.biome) {
                            case HOT:
                                return s ? Col.HOT_S : Col.HOT;
                            case COLD:
                                return s ? Col.COLD_S : Col.COLD;
                            case MUSHROOM:
                                return s ? Col.MUSHROOM_S : Col.MUSHROOM;
                            default:
                            case TEMPERATE:
                                return s ? Col.GREEN_S : Col.GREEN;
                        }
                    }
                case WATER:
                    return s ? Col.WATER_S : Col.WATER;
                default:
                case NULL:
                    return Col.BLACK;
            }
        }
    }


    public static Face getFace(int x, int y) {
        if (y < SIDE) {
            if (x < SIDE) return Face.NA;
            else if (x < 2 * SIDE) return Face.TOP;
            else if (x < 3 * SIDE) return Face.BOT;
            else return Face.NA;
        } else {
            if (x < SIDE) return Face.F1;
            else if (x < 2 * SIDE) return Face.F2;
            else if (x < 3 * SIDE) return Face.F3;
            else return Face.F4;
        }
    }


    public static void applyEffects() {
        shadeWater();
        generateIce();
        //if(genHot)generateHot();
        generateHotBiomes();
        shadeTemperateHot();
        shadeHot();
        shadeTemperateCold();
        shadeCold();
        coastEffects();
        //averageOut();
        generateMushrooms();
        generateIcebergs2();

        if (CommonUtil.FESTIVITY.isChristmas()) {
            christmas();
        } else if (CommonUtil.FESTIVITY.isEarthDay()) {
            meltice();
        }
    }

    public static void meltice() {
        for (Pixel[] pixel : pixels) {
            for (Pixel value : pixel) {
                if (value.biome == Biome.COLD) {
                    value.biome = Biome.TEMPERATE;
                }
                if (value.specialFeature == Feature.ICEBERG) {
                    value.specialFeature = Feature.NORMAL;
                }
            }
        }
    }

    public void desertify() {
        for (Pixel[] pixel : pixels) {
            for (Pixel value : pixel) {
                if (value.biome == Biome.TEMPERATE)
                    value.biome = Biome.HOT;
            }
        }
    }

    public static void christmas() {
        for (Pixel[] pixel : pixels) {
            for (Pixel value : pixel) {
                value.biome = Biome.COLD;
                if (value.specialFeature != Feature.NORMAL) {
                    value.specialFeature = Feature.ICEBERG;
                }
            }
        }
    }

    public static void generateMushrooms() {
        //sides
        int min = 0;
        int additional = 3;

        int count = min + RAND.nextInt(additional);
        int c = 0;
        while (c < count) {
            int x = RAND.nextInt(WIDTH);
            int y = SIDE + RAND.nextInt(SIDE);


            float p = pixels[x][y].isWater() ? 0.9f : 0.1f;
            if (RAND.nextFloat() < p) {
                c++;
                pixels[x][y].specialFeature = Feature.MUSHROOM;
            }

        }
    }

    public void generateIcebergs() {
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                Pos pos = new Pos(x, y);
                Pixel pixel = pfp(pos);

                if (pixel.isWater() && RAND.nextFloat() < 0.005) {
                    pixels[x][y].specialFeature = Feature.ICEBERG;
                }
            }
        }

    }

    public static void generateIcebergs2() {
        //sides
        int min = 1;
        int additional = 3;

        int count = min + RAND.nextInt(additional);
        int c = 0;
        int tries = 0;
        while (c < count && tries < 1000) {
            int x = RAND.nextInt(WIDTH);
            int y = SIDE + RAND.nextInt(SIDE);
            Pos p = new Pos(x, y);
            if (pixels[x][y].isWater() && pfp(p.up()).isWater() && pfp(p.down()).isWater()
                    && pfp(p.left()).isWater() && pfp(p.right()).isWater()) {
                c++;
                pixels[x][y].specialFeature = Feature.ICEBERG;
            }
            tries++;

        }
    }


    public static void generateHotBiomes() {
        //sides
        int min = 6;
        int additional = 4;

        int count = min + RAND.nextInt(additional);
        int c = 0;
        try {
            while (c < count) {
                int x = RAND.nextInt(WIDTH);
                int y = SIDE + RAND.nextInt(SIDE);
                double k = RAND.nextFloat();
                double p = 0.5 * Math.sin((y - k) * 2 * Math.PI / 4d) + 0.3;
                if (RAND.nextFloat() < p && pixels[x][y].isLand()) {
                    c++;
                    setHotBiome(new Pos(x, y), 8);
                }

            }
        } catch (Exception e) {
            int a = 1;
        }
    }

    public static void setHotBiome(Pos p, int dist) {
        int x = p.x;
        int y = p.y;
        if (dist < 0 || pixels[x][y].isHot()) return;
        int d = dist - RAND.nextInt(10);
        pixels[x][y].biome = Biome.HOT;
        setHotBiome(p.up(), d - 2);
        setHotBiome(p.down(), d - 2);
        setHotBiome(p.left(), d);
        setHotBiome(p.right(), d);
    }

    public void genBiomes() {
        int min = 6;
        int additional = 4;
        int c = 0;
        int count = min + RAND.nextInt(additional);
        while (c < 0) {
            float chance = RAND.nextFloat();
            if (chance < 0.5) {
                if (this.doGenHot()) c++;
            } else if (chance < 0.6) {
                //hot
            } else {
                //mountains
            }

        }


    }

    public boolean doGenHot() {
        int x = RAND.nextInt(WIDTH);
        int y = SIDE + RAND.nextInt(SIDE);
        double k = RAND.nextFloat();
        double p = 0.5 * Math.sin((y - k) * 2 * Math.PI / 4d) + 0.3;
        if (RAND.nextFloat() < p && pixels[x][y].isLand()) {
            setHotBiome(new Pos(x, y), 8);
            return true;
        }
        return false;
    }


    public void generateHot() {
        int j = 4 + RAND.nextInt(2);
        Pos[] list = new Pos[j];

        for (int i = 0; i < j; i++) {
            list[i] = new Pos(RAND.nextInt(WIDTH), SIDE + RAND.nextInt(SIDE));
        }

        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                Pos pos = new Pos(x, y);
                Pixel pixel = pfp(pos);
                if (y >= SIDE) {
                    boolean flag = false;
                    for (int m = 0; m < j; m++) {
                        Pos k = list[m];
                        if (dist(k.x, k.y, x, y) < (2 + RAND.nextInt(2))) flag = true;
                    }

                    if (flag) continue;
                    double k = RAND.nextFloat();
                    double p = 0.5 * Math.sin((y - k) * 2 * Math.PI / 4d) + 0.3;
                    if (RAND.nextFloat() < p) {
                        pixels[x][y].biome = Biome.HOT;
                    }
                }
            }
        }
    }

    public static void shadeCold() {
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                Pos pos = new Pos(x, y);
                Pixel pixel = pfp(pos);
                if (pixel.isCold() && pixel.isLand() && y < SIDE) {
                    double p = 0.2;
                    if (pfp(pos.up()).isTemperate()) {
                        p += 0.15;
                    }
                    if (pfp(pos.down()).isTemperate()) {
                        p += 0.15;
                    }
                    if (pfp(pos.left()).isTemperate()) {
                        p += 0.15;
                    }
                    if (pfp(pos.right()).isTemperate()) {
                        p += 0.15;
                    }
                    if (RAND.nextFloat() < p) pixels[x][y].shaded = true;
                }
            }
        }
    }

    public static void shadeHot() {
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                Pos pos = new Pos(x, y);
                Pixel pixel = pfp(pos);
                if (pixel.isHot() && pixel.isLand()) {
                    double p = 0.1;
                    if (pfp(pos.up()).isTemperate()) {

                        p += pfp(pos.up()).isShaded() ? 0.19 : 0.35;
                    }
                    if (pfp(pos.down()).isTemperate()) {
                        p += pfp(pos.down()).isShaded() ? 0.19 : 0.35;
                    }
                    if (pfp(pos.right()).isTemperate()) {
                        p += pfp(pos.right()).isShaded() ? 0.19 : 0.35;
                    }
                    if (pfp(pos.left()).isTemperate()) {
                        p += pfp(pos.left()).isShaded() ? 0.19 : 0.35;
                    }
                    if (RAND.nextFloat() < p) pixels[x][y].shaded = true;
                }
            }
        }
    }

    public static void shadeTemperateHot() {
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                Pos pos = new Pos(x, y);
                Pixel pixel = pfp(pos);
                if (pixel.isTemperate() && pixel.isLand() && y >= SIDE) {
                    double p = 0.1;
                    if (pfp(pos.up()).isHot()) {
                        p += 0.25;
                    }
                    if (pfp(pos.down()).isHot()) {
                        p += 0.25;
                    }
                    if (pfp(pos.left()).isHot()) {
                        p += 0.25;
                    }
                    if (pfp(pos.right()).isHot()) {
                        p += 0.25;
                    }
                    if (RAND.nextFloat() < p) pixels[x][y].shaded = true;
                }
            }
        }
    }

    public static void shadeTemperateCold() {
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                Pos pos = new Pos(x, y);
                Pixel pixel = pfp(pos);
                if (pixel.isTemperate() && pixel.isLand() && y < SIDE) {
                    double p = 0.1;
                    if (pfp(pos.up()).isCold()) {
                        p += 0.25;
                    }
                    if (pfp(pos.down()).isCold()) {
                        p += 0.25;
                    }
                    if (pfp(pos.left()).isCold()) {
                        p += 0.25;
                    }
                    if (pfp(pos.right()).isCold()) {
                        p += 0.25;
                    }
                    if (RAND.nextFloat() < p) pixels[x][y].specialFeature = Feature.SUNKEN;
                }
            }
        }
    }


    public static void generateIce() {
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                Pos pos = new Pos(x, y);
                Pixel pixel = pfp(pos);

                boolean flag = false;
                double d = dist(x + 0.5, y + 0.5, 12, 4);
                if (RAND.nextFloat() > ((d - 0.8) / 2)) flag = true;

                double d2 = dist(x + 0.5, y + 0.5, 20, 4);
                if (RAND.nextFloat() > ((d2 - 0.8) / 2)) flag = true;

                if (flag) {
                    pixels[x][y].biome = Biome.COLD;
                    pixels[x][y].setLand();
                }


            }
        }
    }

    public static void averageOut() {
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                Pos pos = new Pos(x, y);
                Pixel pixel = pfp(pos);
                if (y >= SIDE && pixel.isLand() && RAND.nextFloat() > 0.8) {
                    int t = 0;
                    t += pfp(pos.up()).getTemp();
                    t += pfp(pos.down()).getTemp();
                    t += pfp(pos.left()).getTemp();
                    t += pfp(pos.right()).getTemp();
                    t += pfp(pos.up().left()).getTemp();
                    t += pfp(pos.up().right()).getTemp();
                    t += pfp(pos.down().left()).getTemp();
                    t += pfp(pos.down().right()).getTemp();
                    t += pixel.getTemp();
                    double av = t / 9f;
                    setTemperature(x, y, (int) (av + 0.5));
                }
            }
        }
    }

    public static void setTemperature(int x, int y, int t) {
        if (t < 2) {
            pixels[x][y].biome = Biome.TEMPERATE;
            pixels[x][y].shaded = (t % 2) != 0;
        } else {
            pixels[x][y].biome = Biome.HOT;
            pixels[x][y].shaded = (t % 2) == 0;
        }

    }


    public static void shadeWater() {
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                Pos pos = new Pos(x, y);
                Pixel pixel = pfp(pos);
                Pixel p2 = pfp(pos.up());
                if (pixel.isWater() && p2.isLand()) {
                    pixels[x][y].shaded = true;
                }
            }
        }
    }

    public static void coastEffects() {
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                Pos pos = new Pos(x, y);
                Pixel pixel = pfp(pos);
                if (pixel.isLand() && (pfp(pos.right()).isWater() ||
                        pfp(pos.up()).isWater() ||
                        pfp(pos.down()).isWater() ||
                        pfp(pos.left()).isWater()) && RAND.nextFloat() > 0.7) {
                    pixels[x][y].specialFeature = pixel.biome != Biome.COLD ? Feature.SUNKEN : Feature.ICEBERG;
                }
            }
        }
    }

    public static void generateLand() {
        //sides
        int min = 10;
        int additional = 18;

        int count = min + RAND.nextInt(additional);
        for (int i = 0; i < count; i++) {
            int x = RAND.nextInt(WIDTH);
            int y = RAND.nextInt(HEIGHT);
            setLand(new Pos(x, y), 10);
        }
    }

    public static void setLand(Pos p, int dist) {
        int x = p.x;
        int y = p.y;
        if (dist < 0 || pixels[x][y].isLand()) return;
        //int d = dist - this.rand.nextInt(10);
        pixels[x][y].setLand();
        setLand(p.up(), dist - RAND.nextInt(10));
        setLand(p.down(), dist - RAND.nextInt(10));
        setLand(p.left(), dist - RAND.nextInt(10));
        setLand(p.right(), dist - RAND.nextInt(10));
        //TODO: apply this fix to other gens
    }

}
