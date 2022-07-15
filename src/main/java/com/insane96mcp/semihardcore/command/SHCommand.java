package com.insane96mcp.semihardcore.command;

import com.insane96mcp.semihardcore.capability.PlayerLife;
import com.insane96mcp.semihardcore.setup.Strings;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.atomic.AtomicInteger;

public class SHCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("semihardcore").requires(source -> source.hasPermission(2))
                .then(Commands.argument("targetPlayer", EntityArgument.player())
                        .then(Commands.literal("lives")
                                .then(Commands.literal("get")
                                        .executes(context -> getLives(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer")))
                                )
                                .then(Commands.literal("set")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                                .executes(context -> setLives(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), IntegerArgumentType.getInteger(context, "amount")))
                                        )
                                )
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                                .executes(context -> addLives(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), IntegerArgumentType.getInteger(context, "amount")))
                                        )
                                )
                        )
                        .then(Commands.literal("health")
                                .then(Commands.literal("get")
                                        .executes(context -> getHealth(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer")))
                                )
                                .then(Commands.literal("set")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                                .executes(context -> setHealth(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), IntegerArgumentType.getInteger(context, "amount")))
                                        )
                                )
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                                .executes(context -> addHealth(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), IntegerArgumentType.getInteger(context, "amount")))
                                        )
                                )
                        )
                )
        );
    }

    private static int getLives(CommandSourceStack source, ServerPlayer targetPlayer) {
        AtomicInteger lives = new AtomicInteger(0);
        targetPlayer.getCapability(PlayerLife.INSTANCE).ifPresent(cap -> lives.set(cap.getLives()));
        source.sendSuccess(new TranslatableComponent(Strings.Translatable.PLAYER_GET_LIVES_LEFT, targetPlayer.getName(), lives), true);
        return lives.get();
    }

    private static int setLives(CommandSourceStack source, ServerPlayer targetPlayer, int amount) {
        targetPlayer.getCapability(PlayerLife.INSTANCE).ifPresent(cap -> cap.setLives(amount));
        source.sendSuccess(new TranslatableComponent(Strings.Translatable.PLAYER_SET_LIVES_LEFT, targetPlayer.getName(), amount), true);
        return amount;
    }

    private static int addLives(CommandSourceStack source, ServerPlayer targetPlayer, int amount) {
        targetPlayer.getCapability(PlayerLife.INSTANCE).ifPresent(cap -> cap.addLives(amount));
        source.sendSuccess(new TranslatableComponent(Strings.Translatable.PLAYER_ADD_LIVES_LEFT, amount, targetPlayer.getName()), true);
        return amount;
    }

    private static int getHealth(CommandSourceStack source, ServerPlayer targetPlayer) {
        AtomicInteger health = new AtomicInteger(0);
        targetPlayer.getCapability(PlayerLife.INSTANCE).ifPresent(playerLife -> health.set(20 + playerLife.getHealthModifier()));
        source.sendSuccess(new TranslatableComponent(Strings.Translatable.PLAYER_GET_HEALTH, targetPlayer.getName(), health), true);
        return health.get();
    }

    private static int setHealth(CommandSourceStack source, ServerPlayer targetPlayer, int amount) {
        targetPlayer.getCapability(PlayerLife.INSTANCE).ifPresent(playerLife -> playerLife.setHealthModifier(amount - 20));
        source.sendSuccess(new TranslatableComponent(Strings.Translatable.PLAYER_SET_HEALTH, targetPlayer.getName(), amount), true);
        return amount;
    }

    private static int addHealth(CommandSourceStack source, ServerPlayer targetPlayer, int amount) {
        targetPlayer.getCapability(PlayerLife.INSTANCE).ifPresent(playerLife -> playerLife.addHealthModifier(amount));
        source.sendSuccess(new TranslatableComponent(Strings.Translatable.PLAYER_ADD_HEALTH, amount, targetPlayer.getName()), true);
        return amount;
    }
}
