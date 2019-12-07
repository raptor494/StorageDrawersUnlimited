package com.raptor.sdu;

import java.util.Collections;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public final class SDUConfig {
	static final ForgeConfigSpec spec;
	public static final SDUConfig INSTANCE;
	
	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		INSTANCE = new SDUConfig(builder);
		spec = builder.build();
	}
	
	public final ConfigValue<List<? extends String>> forcedMods;
	public final ConfigValue<Boolean> forceAll;
	public final ConfigValue<List<? extends String>> disabledMods;
	
	SDUConfig(ForgeConfigSpec.Builder builder) {
		forcedMods = builder
				.comment("A list of mod ids whose drawers should",
						 "always be created, even if the mod is not present.")
				.defineList("force load", Collections.emptyList(), obj -> isValidModID((String)obj));
		forceAll = builder
				.comment("Set this to true to force all available mods to load.")
				.define("force all", false);
		disabledMods = builder
				.comment("A list of mod ids whose drawers should",
						 "never be created, even if the mod is loaded.")
				.defineList("disable", Collections.emptyList(), obj -> isValidModID((String)obj));
	}
	
	public static boolean isValidModID(String str) {
		if(str.isEmpty())
			return false;
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(c == '/' || !ResourceLocation.isValidPathCharacter(c))
				return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> getForcedMods() {
		return (List<String>)INSTANCE.forcedMods.get();
	}
	
	public static boolean shouldForceAll() {
		return INSTANCE.forceAll.get();
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> getDisabledMods() {
		return (List<String>)INSTANCE.disabledMods.get();
	}
	
}
