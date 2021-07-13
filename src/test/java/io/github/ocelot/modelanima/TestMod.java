package io.github.ocelot.modelanima;

import io.github.ocelot.modelanima.client.ClientInit;
import io.github.ocelot.sonar.Sonar;
import io.github.ocelot.sonar.common.item.SpawnEggItemBase;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(TestMod.MOD_ID)
public class TestMod
{
    public static final String MOD_ID = "examplemod";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);

    public static final RegistryObject<EntityType<Yeti>> YETI = register("yeti", EntityType.Builder.of(Yeti::new, MobCategory.MONSTER).sized(1.4F, 2.7F).clientTrackingRange(10), 0x577B8A, 0xFAFAFA);

    public TestMod()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Sonar.init(modBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modBus.addListener(ClientInit::initClient));
        BLOCKS.register(modBus);
        TILE_ENTITIES.register(modBus);
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        modBus.addListener(this::init);
        modBus.addListener(this::attributeSetup);
    }

    private void attributeSetup(EntityAttributeCreationEvent event)
    {
        event.put(YETI.get(), Yeti.createAttributes().build());
    }

    private void init(FMLCommonSetupEvent event)
    {
    }

    /**
     * Registers a new entity with an egg under the specified id.
     *
     * @param id             The id of the entity
     * @param builder        The entity builder
     * @param primaryColor   The egg color of the egg item
     * @param secondaryColor The spot color for the egg item
     * @param <T>            The type of entity being created
     */
    private static <T extends Entity> RegistryObject<EntityType<T>> register(String id, EntityType.Builder<T> builder, int primaryColor, int secondaryColor)
    {
        RegistryObject<EntityType<T>> object = register(id, builder);
        ITEMS.register(id + "_spawn_egg", () -> new SpawnEggItemBase<>(object, primaryColor, secondaryColor, true, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
        return object;
    }

    /**
     * Registers a new entity under the specified id.
     *
     * @param id      The id of the entity
     * @param builder The entity builder
     * @param <T>     The type of entity being created
     */
    private static <T extends Entity> RegistryObject<EntityType<T>> register(String id, EntityType.Builder<T> builder)
    {
        return ENTITIES.register(id, () -> builder.build(id));
    }
}
