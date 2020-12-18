package io.github.ocelot.modelanima;

import com.mojang.brigadier.CommandDispatcher;
import io.github.ocelot.modelanima.api.client.geometry.LocalGeometryModelLoader;
import io.github.ocelot.modelanima.api.client.texture.GeometryTextureManager;
import io.github.ocelot.modelanima.api.client.texture.LocalTextureTableProvider;
import io.github.ocelot.modelanima.api.common.geometry.texture.GeometryModelTexture;
import io.github.ocelot.modelanima.client.TestLayer;
import io.github.ocelot.modelanima.client.TestTextureTableProvider;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings("deprecation")
@Mod(TestMod.MOD_ID)
public class TestMod
{
    public static final String MOD_ID = "examplemod";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);

    public TestMod()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
        {
            LocalGeometryModelLoader.init(modBus);
            GeometryTextureManager.init(modBus);
            GeometryTextureManager.addProvider(new LocalTextureTableProvider());
            GeometryTextureManager.addProvider(new TestTextureTableProvider());
        });
        BLOCKS.register(modBus);
        TILE_ENTITIES.register(modBus);
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        modBus.addListener(this::init);
        modBus.addListener(this::initClient);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void init(FMLCommonSetupEvent event)
    {
    }

    private void initClient(FMLClientSetupEvent event)
    {
        DeferredWorkQueue.runLater(() ->
        {
            for (PlayerRenderer renderer : Minecraft.getInstance().getRenderManager().getSkinMap().values())
            {
                renderer.addLayer(new TestLayer(renderer));
            }
        });
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event)
    {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
        TestCommand.register(dispatcher);
    }
}
