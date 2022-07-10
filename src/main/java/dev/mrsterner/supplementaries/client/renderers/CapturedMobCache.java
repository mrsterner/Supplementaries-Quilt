package net.mehvahdjukaar.supplementaries.client.renderers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.mehvahdjukaar.supplementaries.common.capabilities.mobholder.MobContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CapturedMobCache {

    public static LoadingCache<UUID, Entity> MOB_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public Entity load(UUID key) {
                    return null;
                }
            });

    public static void addMob(Entity e) {
        if (e == null) e = DEFAULT_PIG.get();
        MOB_CACHE.put(e.getUUID(), e);
    }

    public static final Lazy<EndCrystal> PEDESTAL_CRYSTAL = Lazy.of(() -> {
        EndCrystal entity = new EndCrystal(EntityType.END_CRYSTAL, Minecraft.getInstance().level);
        entity.setShowBottom(false);
        return entity;
    });

    private static final Lazy<Entity> DEFAULT_PIG = Lazy.of(() -> new Pig(EntityType.PIG, Minecraft.getInstance().level));

    @Nullable
    public static Entity getOrCreateCachedMob(UUID id, NbtCompound tag) {
        Entity e = MOB_CACHE.getIfPresent(id);
        if (e == null) {
            World world = Minecraft.getInstance().level;
            if (world != null) {
                NbtCompound mobData = tag.getCompound("EntityData");

                e = MobContainer.createEntityFromNBT(mobData, id, world);
                addMob(e);
            }
        }
        return e;
    }

    public static void tickCrystal() {
        PEDESTAL_CRYSTAL.get().time++;
    }
}
