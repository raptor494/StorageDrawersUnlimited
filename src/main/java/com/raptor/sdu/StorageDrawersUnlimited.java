package com.raptor.sdu;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jaquadro.minecraft.storagedrawers.block.BlockDrawers;
import com.jaquadro.minecraft.storagedrawers.block.BlockTrim;
import com.raptor.sdu.type.ModListParser;
import com.raptor.sdu.type.ModListParser.Line;
import com.raptor.sdu.type.ModListSyntaxException;
import com.raptor.sdu.type.StreamableIterable;
import com.raptor.sdu.type.SupportedMod;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(StorageDrawersUnlimited.MODID)
public class StorageDrawersUnlimited {
	public static final String MODID = "storagedrawersunlimited";

	// Commented out until such time that I require a proxy
//	public static final CommonProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);

	public static final Logger logger = LogManager.getLogger(MODID);

	public static final Collection<SupportedMod> SUPPORTED_MODS;
	public static final StreamableIterable<SupportedMod> ENABLED_MODS;
	public static final Map<String, Set<SupportedMod>> MODID_LOOKUP;
	public static final StreamableIterable<Block> BLOCKS;
	public static final StreamableIterable<Item> ITEMS;
	public static final StreamableIterable<BlockDrawers> DRAWERS, DRAWERS_FULL_1, DRAWERS_FULL_2, DRAWERS_FULL_4,
			DRAWERS_HALF_1, DRAWERS_HALF_2, DRAWERS_HALF_4;
	public static final StreamableIterable<BlockTrim> TRIMS;

	private static File configFolder;

	public static File getConfigFolder() {
		if(configFolder == null) {
			configFolder = FMLPaths.CONFIGDIR.get().resolve("storagedrawersunlimited").toFile();
		}
		return configFolder;
	}

	public StorageDrawersUnlimited() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SDUConfig.spec);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@SubscribeEvent
	public void setup(FMLCommonSetupEvent event) {
		if(!getConfigFolder().exists() || !configFolder.isDirectory())
			configFolder.mkdirs();
		loadUserDefinedMods();
//		proxy.setup(event);
	}

	/**
	 * <strong>ONLY</strong> use this if you know that an error should never be
	 * thrown! It's to trick the compiler into throwing an exception which does
	 * not inherit from Error or RuntimeException, without wrapping it in a
	 * RuntimeException first.
	 */
	@SuppressWarnings("unchecked")
	private static <E extends Throwable> E sneakyThrow(Throwable error) throws E {
		throw(E) error;
	}

	private static Field modifiers_field;

	static {
		try {
			modifiers_field = Field.class.getDeclaredField("modifiers");
			modifiers_field.setAccessible(true);
		} catch(NoSuchFieldException | SecurityException e) {
			throw sneakyThrow(e);
		}
	}

	private static Field getMutableField(Class<?> clazz, String name) {
		Field field;
		try {
			field = clazz.getDeclaredField(name);
			field.setAccessible(true);
		} catch(NoSuchFieldException | SecurityException e) {
			throw sneakyThrow(e);
		}
		if(Modifier.isFinal(field.getModifiers())) {
			try {
				modifiers_field.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			} catch(IllegalArgumentException | IllegalAccessException e) {
				throw sneakyThrow(e);
			}
		}
		return field;
	}

	private static void setField(Field field, Object value) {
		try {
			field.set(null, value);
		} catch(IllegalArgumentException | IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}

	private static void setField(String name, Object value) {
		setField(getMutableField(StorageDrawersUnlimited.class, name), value);
	}

	static {
		SUPPORTED_MODS = new ArrayList<>();
		MODID_LOOKUP = new HashMap<>();

		// load builtin.sdmods
		List<Line> lines;
		try(Scanner scan = new Scanner(StorageDrawersUnlimited.class.getResourceAsStream("builtin.sdmods"))) {
			lines = ModListParser.readLines(scan);
			if(scan.ioException() != null) {
				throw new Error("Error reading builtin.sdmods for StorageDrawersUnlimited", scan.ioException());
			}
		} catch(NullPointerException e) {
			throw new Error("Missing com/raptor/sdu/builtin.sdmods in StorageDrawersUnlimited jar file!");
		}

		try {
			int numberOfModsLoaded = ModListParser.parseModList("jar:com/raptor/sdu/builtin.sdmods", lines);
			logger.info("Read %d mods from jar:com/raptor/sdu/builtin.sdmods", numberOfModsLoaded);
		} catch(ModListSyntaxException e) {
			logger.log(Level.ERROR, e);
		}

		ENABLED_MODS = () -> SUPPORTED_MODS.stream().filter(SupportedMod::isEnabled);

		BLOCKS = () -> ENABLED_MODS.stream().flatMap(SupportedMod::blocks);
		ITEMS = () -> ENABLED_MODS.stream().flatMap(SupportedMod::items);

		DRAWERS = () -> ENABLED_MODS.stream().flatMap(SupportedMod::drawers);
		DRAWERS_FULL_1 = () -> ENABLED_MODS.stream().flatMap(SupportedMod::drawers_full_1);
		DRAWERS_FULL_2 = () -> ENABLED_MODS.stream().flatMap(SupportedMod::drawers_full_2);
		DRAWERS_FULL_4 = () -> ENABLED_MODS.stream().flatMap(SupportedMod::drawers_full_4);
		DRAWERS_HALF_1 = () -> ENABLED_MODS.stream().flatMap(SupportedMod::drawers_half_1);
		DRAWERS_HALF_2 = () -> ENABLED_MODS.stream().flatMap(SupportedMod::drawers_half_2);
		DRAWERS_HALF_4 = () -> ENABLED_MODS.stream().flatMap(SupportedMod::drawers_half_4);
		TRIMS = () -> ENABLED_MODS.stream().flatMap(SupportedMod::trims);
	}

	private static boolean isSDMODSFile(File file) {
		return file.isFile() && StringUtils.endsWithIgnoreCase(file.getName(), ".sdmods");
	}

	private static void loadUserDefinedMods() {
		for(File modList : configFolder.listFiles(StorageDrawersUnlimited::isSDMODSFile)) {
			List<Line> lines;
			String fileName = modList.toPath().relativize(configFolder.toPath()).toString();
			try(Scanner scan = new Scanner(modList)) {
				lines = ModListParser.readLines(scan);
			} catch(FileNotFoundException e) {
				logger.log(Level.ERROR, e);
				continue;
			}

			try {
				int numberOfModsLoaded = ModListParser.parseModList(fileName, lines);
				logger.info("Read %d mods from %s", numberOfModsLoaded, fileName);
			} catch(ModListSyntaxException e) {
				logger.log(Level.ERROR, "Error parsing modlist file " + fileName, e);
			}
		}

		setField("SUPPORTED_MODS", Collections.unmodifiableCollection(SUPPORTED_MODS));
		for(Entry<String, Set<SupportedMod>> entry : MODID_LOOKUP.entrySet()) {
			entry.setValue(Collections.unmodifiableSet(entry.getValue()));
		}
		setField("MODID_LOOKUP", Collections.unmodifiableMap(MODID_LOOKUP));
	}
	
	public static Optional<SupportedMod> getModFromModid(String modid) {
		return MODID_LOOKUP.getOrDefault(modid, Collections.emptySet()).stream().findFirst();
	}

}
