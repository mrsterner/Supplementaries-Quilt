package net.mehvahdjukaar.supplementaries.common.capabilities.mobholder;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Collection;

//this is still a mess
public class BucketHelper {
    //bucket item mob name (not id). Many mods don't extend the base BucketItem class nor the IBucketable interface... whyy
    private static final BiMap<Item, String> BUCKET_TO_MOB_MAP = HashBiMap.create();

    private static Method FISH_TYPE = null;

    //only use this to access the map
    public static @Nullable
    EntityType<?> getEntityType(Item bucket) {
        if (BUCKET_TO_MOB_MAP.containsKey(bucket)) {
            String mobId = BUCKET_TO_MOB_MAP.get(bucket);
            ResourceLocation res = new ResourceLocation(mobId);
            if(ForgeRegistries.ENTITIES.containsKey(res)) {
                EntityType<?> en = ForgeRegistries.ENTITIES.get(res);
                if (en != null) {
                    return ForgeRegistries.ENTITIES.get(new ResourceLocation(mobId));
                }
            }
        } else if (bucket instanceof MobBucketItem bucketItem) {
            try {
                if (FISH_TYPE == null) {
                    FISH_TYPE = ObfuscationReflectionHelper.findMethod(MobBucketItem.class, "getFishType");
                }
                EntityType<?> en = (EntityType<?>) FISH_TYPE.invoke(bucketItem);
                if (en != null) {
                    BUCKET_TO_MOB_MAP.putIfAbsent(bucket, en.getRegistryName().toString());
                    return en;
                }
            } catch (Exception ignored) {
            }
        }
        //try parsing
        else {
            String mobId = null;
            String itemName = bucket.getRegistryName().toString();
            if (itemName.contains("_bucket")) {
                mobId = itemName.replace("_bucket", "");
            } else if (itemName.contains("bucket_of_")) {
                mobId = itemName.replace("_bucket", "");
            } else if (itemName.contains("bucket_")) {
                mobId = itemName.replace("bucket_", "");
            }
            if(mobId != null) {
                ResourceLocation res = new ResourceLocation(mobId);
                if(ForgeRegistries.ENTITIES.containsKey(res)){
                    EntityType<?> en = ForgeRegistries.ENTITIES.get(res);
                    if(en != null) {
                        BUCKET_TO_MOB_MAP.putIfAbsent(bucket, en.getRegistryName().toString());
                        return en;
                    }
                }
            }
        }
        return null;
    }

    //TODO: rething all this and remove this one
    public static void tryAddingFromEntityId(String id) {
        //EntityType<?> en = ForgeRegistries.ENTITIES.get(new ResourceLocation(id));
      //  if (en != null && !BUCKET_TO_MOB_MAP.containsValue(en)) {
       //     BUCKET_TO_MOB_MAP.putIfAbsent()
//
      //  }
    }

    public static Collection<Item> getValidBuckets() {
        return BUCKET_TO_MOB_MAP.keySet();
    }

    //if it needs to have water
    public static boolean isFishBucket(Item item) {
        return getEntityType(item) != null;
    }

    public static boolean isBucketableEntity(String mobId) {
        return BUCKET_TO_MOB_MAP.containsValue(mobId);
    }

    public static void associateMobToBucketIfAbsent(EntityType<?> entity, Item item) {
        if(!BUCKET_TO_MOB_MAP.containsKey(item)){
            String name = entity.getRegistryName().toString();
            if(!BUCKET_TO_MOB_MAP.inverse().containsKey(name)) {
                BUCKET_TO_MOB_MAP.putIfAbsent(item, name);
            }
        }
    }
}
