package io.github.ocelot.modelanima;

import com.mojang.brigadier.CommandDispatcher;
import io.github.ocelot.modelanima.api.client.animation.AnimationManager;
import io.github.ocelot.modelanima.api.client.animation.LocalAnimationLoader;
import io.github.ocelot.modelanima.api.client.geometry.GeometryModelManager;
import io.github.ocelot.modelanima.api.client.geometry.LocalGeometryModelLoader;
import io.github.ocelot.modelanima.api.client.texture.GeometryTextureManager;
import io.github.ocelot.modelanima.api.client.texture.LocalTextureTableLoader;
import io.github.ocelot.modelanima.client.ClientInit;
import io.github.ocelot.sonar.Sonar;
import io.github.ocelot.sonar.common.item.SpawnEggItemBase;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);

    public static final RegistryObject<EntityType<Yeti>> YETI = register("yeti", EntityType.Builder.of(Yeti::new, EntityClassification.MONSTER).sized(1.4F, 2.7F).clientTrackingRange(10), 0x577B8A, 0xFAFAFA);

    public TestMod()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Sonar.init(modBus);
        ModelAnima.init(modBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
        {
            GeometryModelManager.addLoader(new LocalGeometryModelLoader());
            GeometryTextureManager.addProvider(new LocalTextureTableLoader());
            AnimationManager.addLoader(new LocalAnimationLoader());
            modBus.addListener(ClientInit::initClient);
        });
        BLOCKS.register(modBus);
        TILE_ENTITIES.register(modBus);
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        modBus.addListener(this::init);
        modBus.addListener(this::attributeSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void attributeSetup(EntityAttributeCreationEvent event)
    {
        event.put(YETI.get(), Yeti.createAttributes().build());
    }

    private void init(FMLCommonSetupEvent event)
    {
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event)
    {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
        TestCommand.register(dispatcher);
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
        ITEMS.register(id + "_spawn_egg", () -> new SpawnEggItemBase<>(object, primaryColor, secondaryColor, true, new Item.Properties().tab(ItemGroup.TAB_MISC)));
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
