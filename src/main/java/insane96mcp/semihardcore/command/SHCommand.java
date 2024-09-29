package insane96mcp.semihardcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import insane96mcp.semihardcore.SemiHardcore;
import insane96mcp.semihardcore.capability.PlayerLifeImpl;
import insane96mcp.semihardcore.module.MaxHealth;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SHCommand {
	public static final String COMMAND_FAIL = SemiHardcore.RESOURCE_PREFIX + "command_fail";
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("semihardcore").requires(source -> source.hasPermission(2))
				.then(Commands.argument("targetPlayer", EntityArgument.player())
						.then(Commands.literal("lives")
								.then(Commands.literal("get")
										.executes(context -> getLives(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer")))
								)
								.then(Commands.literal("set")
										.then(Commands.argument("amount", IntegerArgumentType.integer())
												.then(Commands.argument("max", IntegerArgumentType.integer()) //TODO Remove, makes no sense
														.executes(context -> setLives(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), IntegerArgumentType.getInteger(context, "amount"), IntegerArgumentType.getInteger(context, "max")))
												)
												.executes(context -> setLives(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), IntegerArgumentType.getInteger(context, "amount")))
										)
								)
								.then(Commands.literal("add")
										.then(Commands.argument("amount", IntegerArgumentType.integer())
												.then(Commands.argument("max", IntegerArgumentType.integer())
														.executes(context -> addLives(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), IntegerArgumentType.getInteger(context, "amount"), IntegerArgumentType.getInteger(context, "max")))
												)
												.executes(context -> addLives(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), IntegerArgumentType.getInteger(context, "amount")))
										)
								)
						)
						.then(Commands.literal("health")
								.then(Commands.literal("get")
										.executes(context -> getHealth(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer")))
								)
								.then(Commands.literal("set")
										.then(Commands.argument("amount", IntegerArgumentType.integer())
												.executes(context -> setHealth(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), IntegerArgumentType.getInteger(context, "amount")))
										)
								)
								.then(Commands.literal("add")
										.then(Commands.argument("amount", IntegerArgumentType.integer())
												.executes(context -> addHealth(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), IntegerArgumentType.getInteger(context, "amount")))
										)
								)
						)
				)
		);
	}

	private static int getLives(CommandSourceStack source, ServerPlayer targetPlayer) {
		AtomicInteger lives = new AtomicInteger(0);
		targetPlayer.getCapability(PlayerLifeImpl.INSTANCE).ifPresent(cap -> lives.set(cap.getLives()));
		source.sendSuccess(() -> Component.translatable(SemiHardcore.RESOURCE_PREFIX + "player_get_lives_left", targetPlayer.getName(), lives), true);
		return lives.get();
	}

	private static int setLives(CommandSourceStack source, ServerPlayer targetPlayer, int amount) {
		return setLives(source, targetPlayer, amount, -1);
	}

	private static int setLives(CommandSourceStack source, ServerPlayer targetPlayer, int amount, int max) {
		AtomicBoolean success = new AtomicBoolean(false);
		targetPlayer.getCapability(PlayerLifeImpl.INSTANCE).ifPresent(cap -> {
			if (max == -1) cap.setLives(amount);
			else cap.setLives(amount, max);
			source.sendSuccess(() -> Component.translatable(SemiHardcore.RESOURCE_PREFIX + "player_set_lives_left", targetPlayer.getName(), amount), true);
			success.set(true);
		});
		if (!success.get()) {
			source.sendFailure(Component.translatable(COMMAND_FAIL));
		}
		return amount;
	}

	private static int addLives(CommandSourceStack source, ServerPlayer targetPlayer, int amount) {
		return addLives(source, targetPlayer, amount, -1);
	}

	private static int addLives(CommandSourceStack source, ServerPlayer targetPlayer, int amount, int max) {
		AtomicBoolean success = new AtomicBoolean(false);
		targetPlayer.getCapability(PlayerLifeImpl.INSTANCE).ifPresent(cap -> {
			if (max == -1) cap.addLives(amount);
			else cap.addLives(amount, max);
			source.sendSuccess(() -> Component.translatable(SemiHardcore.RESOURCE_PREFIX + "player_add_lives_left", amount, targetPlayer.getName()), true);
			success.set(true);
		});
		if (!success.get()) {
			source.sendFailure(Component.translatable(COMMAND_FAIL));
		}
		return amount;
	}

	private static int getHealth(CommandSourceStack source, ServerPlayer targetPlayer) {
		AtomicInteger health = new AtomicInteger(0);
		targetPlayer.getCapability(PlayerLifeImpl.INSTANCE).ifPresent(playerLife -> health.set(20 + playerLife.getHealthModifier()));
		MaxHealth.updateMaxHealth(targetPlayer);
		source.sendSuccess(() -> Component.translatable(SemiHardcore.RESOURCE_PREFIX + "player_get_health", targetPlayer.getName(), health), true);
		return health.get();
	}

	private static int setHealth(CommandSourceStack source, ServerPlayer targetPlayer, int amount) {
		targetPlayer.getCapability(PlayerLifeImpl.INSTANCE).ifPresent(playerLife -> playerLife.setHealthModifier(amount - 20));
		MaxHealth.updateMaxHealth(targetPlayer);
		source.sendSuccess(() -> Component.translatable(SemiHardcore.RESOURCE_PREFIX + "player_set_health", targetPlayer.getName(), amount), true);
		return amount;
	}

	private static int addHealth(CommandSourceStack source, ServerPlayer targetPlayer, int amount) {
		targetPlayer.getCapability(PlayerLifeImpl.INSTANCE).ifPresent(playerLife -> playerLife.addHealthModifier(amount));
		MaxHealth.updateMaxHealth(targetPlayer);
		source.sendSuccess(() -> Component.translatable(SemiHardcore.RESOURCE_PREFIX + "player_add_health", amount, targetPlayer.getName()), true);
		return amount;
	}
}