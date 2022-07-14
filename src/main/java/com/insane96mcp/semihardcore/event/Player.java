package com.insane96mcp.semihardcore.event;

import com.insane96mcp.semihardcore.SemiHardcore;
import com.insane96mcp.semihardcore.capability.Lives;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SemiHardcore.MOD_ID)
public class Player {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDeath(LivingDeathEvent event)
    {
        if (event.getEntityLiving().getLevel().isClientSide
                || !(event.getEntityLiving() instanceof ServerPlayer player)
                || event.getEntityLiving() instanceof FakePlayer
                || event.getEntityLiving().getLevel().getLevelData().isHardcore()
                || player.gameMode.getGameModeForPlayer() == GameType.CREATIVE
                || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
            return;

        player.getCapability(Lives.INSTANCE).ifPresent(livesCap -> livesCap.addLives(-1));
        /*CompoundTag tag = EntityHelper.getPlayerPersistentData(player, "LimitedLivesSave");
        int prevDeaths = tag.getInt("deathCount");
        int liveCount = tag.getInt("maxLives");
        if(liveCount == 0)
        {
            liveCount = LimitedLives.config.maxLives.get();
        }
        tag.putDouble("healthOffset", event.getEntityLiving().getAttribute(Attributes.MAX_HEALTH).getBaseValue() - (20D - (20D * prevDeaths / (double)liveCount)));
        tag.putInt("deathCount", prevDeaths + 1);
        tag.putInt("maxLives", LimitedLives.config.maxLives.get());*/
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.getPlayer().level.isClientSide)
            return;

        ServerPlayer player = (ServerPlayer) event.getPlayer();
        event.getPlayer().getCapability(Lives.INSTANCE).ifPresent(livesCap -> {
            if (livesCap.getLives() <= 0)
                player.setGameMode(GameType.SPECTATOR);
        });
        /*CompoundTag tag = EntityHelper.getPlayerPersistentData(event.getPlayer(), "LimitedLivesSave");
        int deaths = tag.getInt("deathCount");
        if(deaths >= LimitedLives.config.maxLives.get())
        {
            //do ban
            ServerPlayer player = (ServerPlayer)event.getPlayer();
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if(LimitedLives.config.banType.get() == LimitedLives.BanType.SPECTATOR || server.isSingleplayer() && server.getSingleplayerName().equals(player.getName().getContents()))
            {
                tag.putInt("gameMode", player.gameMode.getGameModeForPlayer().getId());
                tag.putLong("banTime", System.currentTimeMillis());
                player.setGameMode(GameType.SPECTATOR);
                player.fallDistance = 0.0F;
                player.displayClientMessage(LimitedLives.config.banTime.get() == 0 ? TextComponentHelper.createComponentTranslation(player, "limitedlives.spectateForcePerma") : TextComponentHelper.createComponentTranslation(player, "limitedlives.spectateForce", LimitedLives.config.banTime.get()), false);
            }
            else
            {
                UserBanListEntry userlistbansentry = new UserBanListEntry(player.getGameProfile(), null, LimitedLives.MOD_NAME, LimitedLives.config.banTime.get() == 0 ? null : new Date(System.currentTimeMillis() + (LimitedLives.config.banTime.get() * 1000L)), TextComponentHelper.createComponentTranslation(player, "limitedlives.banReason").toString());
                server.getPlayerList().getBans().add(userlistbansentry);
                player.connection.disconnect(TextComponentHelper.createComponentTranslation(player, "limitedlives.banKickReason"));
            }
        }
        else if(LimitedLives.config.healthAdjust.get())
        {
            double nextHealth = Math.max(20 - (deaths / (double)LimitedLives.config.maxLives.get() * 20D) + tag.getDouble("healthOffset"), 1D);
            event.getPlayer().getAttribute(Attributes.MAX_HEALTH).setBaseValue(nextHealth);
        }*/
    }
}
