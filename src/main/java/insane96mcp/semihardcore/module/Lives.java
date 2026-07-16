package insane96mcp.semihardcore.module;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.semihardcore.SemiHardcore;
import insane96mcp.semihardcore.util.PlayerLifeData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@LoadFeature(canBeDisabled = false)
public class Lives extends Feature {
	private static final ResourceLocation LIVES_INITIALIZED = SemiHardcore.location("lives_initialized");

	@Config(min = 1, description = "How many lives players spawn with")
	public static Integer startingLives = 6;
	@Config(min = 0, description = "Max lives players can have. 0 for infinite")
	public static Integer maxLives = 0;
	@Config(description = "Announce players' life lost to chat")
	public static Boolean announceLifeLostToChat = true;

	//TODO doesn't work if two or more players die in the same tick
	private boolean hasChangedGameRule = false;
	private double x;
	private double y;
	private double z;
	private float rotationX = 0;
	private float rotationY = 0;

	@SubscribeEvent
	public void onPlayerJoin(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof ServerPlayer player)
				|| ModNBTData.getPersisted(player, LIVES_INITIALIZED, Boolean.class))
			return;

		PlayerLifeData.setLives(player, startingLives);
		ModNBTData.putPersisted(player, LIVES_INITIALIZED, true);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerDeath(LivingDeathEvent event)
	{
		if (!this.isEnabled()
				|| event.getEntity().level().isClientSide
				|| !(event.getEntity() instanceof ServerPlayer player)
				|| event.getEntity() instanceof FakePlayer
				|| event.getEntity().level().getLevelData().isHardcore()
				|| player.gameMode.getGameModeForPlayer() == GameType.CREATIVE
				|| player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
			return;

		if (PlayerLifeData.isOptOut(player))
			return;
		PlayerLifeData.addLives(player, -1);
		if (PlayerLifeData.getLives(player) <= 0) {
			player.setRespawnPosition(player.level().dimension(), player.blockPosition(), player.getXRot(), true, false);
			GameRules.BooleanValue immediateRespawn = player.level().getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
			if (!immediateRespawn.get()) {
				immediateRespawn.set(true, player.level().getServer());
				hasChangedGameRule = true;
			}
			x = player.getX();
			y = player.getY();
			z = player.getZ();
			rotationX = player.getXRot();
			rotationY = player.getYRot();
			LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, player.level());
			lightningBolt.setVisualOnly(true);
			lightningBolt.setPos(player.position());
			player.level().addFreshEntity(lightningBolt);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
	{
		if (!this.isEnabled()
				|| event.getEntity().level().isClientSide
				|| !(event.getEntity() instanceof ServerPlayer player)
				|| event.isEndConquered() //This event is called even when the player enters the End Portal in the end
				|| player.level().getLevelData().isHardcore()
				|| player.gameMode.getGameModeForPlayer() == GameType.CREATIVE)
			return;

		if (PlayerLifeData.isOptOut(player))
			return;
		int lives = PlayerLifeData.getLives(player);
		if (lives > 0 && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
			player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(SemiHardcore.lang("life_lost"))));
			if (lives > 1)
				player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(SemiHardcore.lang("lives_remaining"), lives)));
			else
				player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(SemiHardcore.lang("life_remaining"), lives)));

			if (announceLifeLostToChat)
				player.server.getPlayerList().broadcastSystemMessage(Component.translatable(SemiHardcore.lang("player_life_lost"), player.getDisplayName().getString(), lives), false);
		}
		else {
			player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(SemiHardcore.lang("gg_wp"))));
			player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(SemiHardcore.lang("no_lives_remaining"), lives)));
			player.setGameMode(GameType.SPECTATOR);
			player.teleportTo((ServerLevel) player.level(), x, y, z, rotationY, rotationX);
			if (hasChangedGameRule)
				player.level().getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(false, player.level().getServer());

			if (announceLifeLostToChat)
				player.server.getPlayerList().broadcastSystemMessage(Component.translatable(SemiHardcore.lang("player_no_lives_remaining"), player.getDisplayName().getString(), lives), false);
		}
	}

}
