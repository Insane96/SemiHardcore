package insane96mcp.semihardcore.util;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.semihardcore.SemiHardcore;
import insane96mcp.semihardcore.module.Lives;
import insane96mcp.semihardcore.module.MaxHealth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Replaces the Forge capability (IPlayerLife) that used to back this data. Lives/healthModifier/optOut are
 * stored in the player's persisted NBT, which vanilla itself carries over death/respawn/dimension change.
 * This will change in 26.1 since NeoForge introduced
 */
public class PlayerLifeData {
    private static final ResourceLocation LIVES = SemiHardcore.location("lives");
    private static final ResourceLocation HEALTH_MODIFIER = SemiHardcore.location("health_modifier");
    private static final ResourceLocation OPT_OUT = SemiHardcore.location("opt_out");

    public static boolean isOptOut(Player player) {
        return ModNBTData.getPersisted(player, OPT_OUT, Boolean.class);
    }

    public static void setOptOut(Player player, boolean optOut) {
        ModNBTData.putPersisted(player, OPT_OUT, optOut);
    }

    public static int getLives(Player player) {
        return ModNBTData.getPersisted(player, LIVES, Integer.class);
    }

    public static void setLives(Player player, int lives) {
        setLives(player, lives, Lives.maxLives == 0 ? Integer.MAX_VALUE : Lives.maxLives);
    }

    public static void setLives(Player player, int lives, int upperCap) {
        ModNBTData.putPersisted(player, LIVES, Mth.clamp(lives, 0, upperCap));
    }

    public static void addLives(Player player, int lives) {
        addLives(player, lives, Lives.maxLives == 0 ? Integer.MAX_VALUE : Lives.maxLives);
    }

    public static void addLives(Player player, int lives, int upperCap) {
        setLives(player, getLives(player) + lives, upperCap);
    }

    public static float getHealthModifier(Player player) {
        return ModNBTData.getPersisted(player, HEALTH_MODIFIER, Float.class);
    }

    public static void setHealthModifier(Player player, float healthModifier) {
        ModNBTData.putPersisted(player, HEALTH_MODIFIER, healthModifier);
    }

    public static void addHealthModifier(Player player, float healthModifier) {
        float newValue = getHealthModifier(player) + healthModifier;
        if (newValue < MaxHealth.cap.min)
            newValue = (float) MaxHealth.cap.min;
        else if (newValue > MaxHealth.cap.max)
            newValue = (float) MaxHealth.cap.max;
        setHealthModifier(player, newValue);
    }
}
