package com.raptor.sdu.type;

import static com.raptor.sdu.SDUnlimited.*;
import static com.raptor.sdu.type.SupportedMod.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import com.jaquadro.minecraft.storagedrawers.block.BlockDrawers;
import com.jaquadro.minecraft.storagedrawers.block.BlockTrim;
import com.raptor.sdu.SDUnlimited;
import com.raptor.sdu.type.ModListParser.ModListSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class Mods {
	public static final Collection<SupportedMod> MODS;
	public static final StreamableIterable<SupportedMod> ENABLED_MODS;
	static final Map<String, SupportedMod> MODID_LOOKUP;
	public static final StreamableIterable<Block> BLOCKS;
	public static final StreamableIterable<Item> ITEMS;
	public static final StreamableIterable<BlockDrawers> DRAWERS, DRAWERS_FULL_1, DRAWERS_FULL_2, DRAWERS_FULL_4, DRAWERS_HALF_1, DRAWERS_HALF_2, DRAWERS_HALF_4;
	public static final StreamableIterable<BlockTrim> TRIMS;
	
	static {
		// load builtin.sdmods
		{
			Scanner scan0;
			try(Scanner scan = scan0 = new Scanner(SDUnlimited.class.getResourceAsStream("builtin.sdmods"))) {
				ModListParser.parseModList("jar:com/raptor/sdu/builtin.sdmods", scan);
			} catch(NullPointerException e) {
				throw new Error("Missing com/raptor/sdu/builtin.sdmods in StorageDrawersUnlimited jar file!");
			}
			if(scan0.ioException() != null) {
				throw new Error("Error reading builtin.sdmods for StorageDrawersUnlimited", scan0.ioException());
			}
		}
		
		MODS = Collections.unmodifiableCollection(internal_modlist);
		ENABLED_MODS = () -> MODS.stream().filter(SupportedMod::isEnabled);
		MODID_LOOKUP = Collections.unmodifiableMap(internal_modid_lookup);
		
		BLOCKS = () -> ENABLED_MODS.stream().flatMap(SupportedMod::blocks);
		ITEMS  = () -> ENABLED_MODS.stream().flatMap(SupportedMod::items);
		
		DRAWERS = () -> ENABLED_MODS.stream().flatMap(SupportedMod::drawers);
		DRAWERS_FULL_1 = () -> ENABLED_MODS.stream().flatMap(SupportedMod::drawers_full_1);
		DRAWERS_FULL_2 = () -> ENABLED_MODS.stream().flatMap(SupportedMod::drawers_full_2);
		DRAWERS_FULL_4 = () -> ENABLED_MODS.stream().flatMap(SupportedMod::drawers_full_4);
		DRAWERS_HALF_1 = () -> ENABLED_MODS.stream().flatMap(SupportedMod::drawers_half_1);
		DRAWERS_HALF_2 = () -> ENABLED_MODS.stream().flatMap(SupportedMod::drawers_half_2);
		DRAWERS_HALF_4 = () -> ENABLED_MODS.stream().flatMap(SupportedMod::drawers_half_4);
		TRIMS = () -> ENABLED_MODS.stream().flatMap(SupportedMod::trims);
	}
	
	public static void loadUserDefinedMods() {
		for(File modList : getConfigFolder().listFiles(file -> file.isFile() && StringUtils.endsWithIgnoreCase(file.getName(), ".sdmods"))) {
			try(Scanner scan = new Scanner(modList)) {
				ModListParser.parseModList(modList.getName(), scan);
			} catch(ModListSyntaxException | FileNotFoundException e) {
				logger.log(Level.ERROR, e);
			}
		}
		for(SupportedMod mod : MODS) {
			MODID_LOOKUP.put(mod.getModID(), mod);
			for(String alias : mod.getAliases()) {
				MODID_LOOKUP.putIfAbsent(alias, mod);
			}
		}
	}
	
	public static SupportedMod fromModId(String modid) {
		return MODID_LOOKUP.get(modid);
	}
	
	private Mods() {
		throw new UnsupportedOperationException(getClass().getName() + " cannot be instantiated");
	}
}
