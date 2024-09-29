package insane96mcp.semihardcore.setup;

import insane96mcp.insanelib.base.Module;
import insane96mcp.semihardcore.SemiHardcore;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class SHCommonConfig {
	public static final ForgeConfigSpec CONFIG_SPEC;
	public static final CommonConfig COMMON;

	public static final ForgeConfigSpec.Builder builder;

	private static Module BASE;

	static {
		builder = new ForgeConfigSpec.Builder();
		final Pair<CommonConfig, ForgeConfigSpec> specPair = builder.configure(CommonConfig::new);
		COMMON = specPair.getLeft();
		CONFIG_SPEC = specPair.getRight();
	}

	public static class CommonConfig {
		public CommonConfig(final ForgeConfigSpec.Builder builder) {
			BASE = Module.Builder.create(SemiHardcore.RESOURCE_PREFIX + "base", "Base", ModConfig.Type.COMMON, SHCommonConfig.builder).canBeDisabled(false).build();
			Module.loadFeatures(ModConfig.Type.COMMON, SemiHardcore.MOD_ID, this.getClass().getClassLoader());
		}
	}
}
