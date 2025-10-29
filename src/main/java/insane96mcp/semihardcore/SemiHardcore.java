package insane96mcp.semihardcore;

import com.mojang.logging.LogUtils;
import insane96mcp.semihardcore.capability.PlayerLifeImpl;
import insane96mcp.semihardcore.capability.PlayerLifeProvider;
import insane96mcp.semihardcore.command.SHCommand;
import insane96mcp.semihardcore.setup.SHCommonConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SemiHardcore.MOD_ID)
public class SemiHardcore
{
    public static final String MOD_ID = "semihardcore";
    public static final String RESOURCE_PREFIX = MOD_ID + ":";
    public static final Logger LOGGER = LogUtils.getLogger();

	public SemiHardcore(FMLJavaModLoadingContext context)
    {
        context.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, SHCommonConfig.CONFIG_SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
            event.addCapability(PlayerLifeProvider.IDENTIFIER, new PlayerLifeProvider());
    }

    @SubscribeEvent
    public void eventPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        oldPlayer.reviveCaps();
        oldPlayer.getCapability(PlayerLifeImpl.INSTANCE).ifPresent(newLives -> newPlayer.getCapability(PlayerLifeImpl.INSTANCE).ifPresent(oldLives -> {
            oldLives.setLives(newLives.getLives());
            oldLives.setHealthModifier(newLives.getHealthModifier());
            oldLives.setOptOut(newLives.isOptOut());
        }));
        oldPlayer.invalidateCaps();
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        SHCommand.register(event.getDispatcher());
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String lang(String path) {
        return MOD_ID + "." + path;
    }
}