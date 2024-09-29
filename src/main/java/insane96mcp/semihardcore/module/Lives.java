package insane96mcp.semihardcore.module;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.semihardcore.SemiHardcore;
import insane96mcp.semihardcore.capability.PlayerLifeImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Lives")
@LoadFeature(module = SemiHardcore.RESOURCE_PREFIX + "base")
public class Lives extends Feature {
	@Config(min = 1)
	@Label(name = "Starting Lives", description = "How many lives players spawn with")
	public static Integer startingLives = 5;
	@Config(min = 0)
	@Label(name = "Max Lives", description = "Max lives players can gain. 0 for infinite")
	public static Integer maxLives = 0;
	@Config
	@Label(name = "Announce Life Lost to Chat", description = "Announce players' life lost to chat")
	public static Boolean announceLifeLostToChat = true;

	public Lives(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	//TODO doesn't work if two or more players die in the same tick
	private boolean hasChangedGameRule = false;
	private double x;
	private double y;
	private double z;
	private float rotationX = 0;
	private float rotationY = 0;
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

		player.getCapability(PlayerLifeImpl.INSTANCE).ifPresent(playerLife -> {
			if (playerLife.isOptOut())
				return;
			playerLife.addLives(-1);
			if (playerLife.getLives() <= 0) {
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
		});
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

		player.getCapability(PlayerLifeImpl.INSTANCE).ifPresent(playerLife -> {
			if (playerLife.isOptOut())
				return;
			if (playerLife.getLives() > 0 && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
				player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(SemiHardcore.RESOURCE_PREFIX + "life_lost")));
				if (playerLife.getLives() > 1)
					player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(SemiHardcore.RESOURCE_PREFIX + "lives_remaining", playerLife.getLives())));
				else
					player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(SemiHardcore.RESOURCE_PREFIX + "life_remaining", playerLife.getLives())));

				if (announceLifeLostToChat) {
					player.server.getPlayerList().broadcastSystemMessage(Component.translatable(SemiHardcore.RESOURCE_PREFIX + "player_life_lost", player.getDisplayName().getString(), playerLife.getLives()), false);
				}
			}
			else {
				player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(SemiHardcore.RESOURCE_PREFIX + "gg_wp")));
				player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(SemiHardcore.RESOURCE_PREFIX + "no_lives_remaining", playerLife.getLives())));
				player.setGameMode(GameType.SPECTATOR);
				player.teleportTo((ServerLevel) player.level(), x, y, z, rotationY, rotationX);
				if (hasChangedGameRule)
					player.level().getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(false, player.level().getServer());

				if (announceLifeLostToChat) {
					player.server.getPlayerList().broadcastSystemMessage(Component.translatable(SemiHardcore.RESOURCE_PREFIX + "player_no_lives_remaining", player.getDisplayName().getString(), playerLife.getLives()), false);
				}
			}
		});
	}

}
