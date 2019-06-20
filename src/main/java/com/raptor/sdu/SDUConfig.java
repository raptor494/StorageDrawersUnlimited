package com.raptor.sdu;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RequiresMcRestart;

@Config(modid = SDUnlimited.MODID)
public class SDUConfig {
	
	@Name("force load")
	@Comment({"A list of mod ids whose drawers should",
			"always be created, even if the mod is not present."})
	@RequiresMcRestart
	public static String[] forcedMods = new String[0];
	
	@Name("force all")
	@Comment("Set this to true to force all available mods to load.")
	public static boolean force_all = false;
	
	@Name("disable")
	@Comment({"A list of mod ids whose drawers should",
			"never be created, even if the mod is loaded."})
	@RequiresMcRestart
	public static String[] disabledMods = new String[0];
	
}
