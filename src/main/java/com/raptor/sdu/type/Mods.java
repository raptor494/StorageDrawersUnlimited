package com.raptor.sdu.type;

import static com.raptor.sdu.SDUnlimited.configFolder;
import static com.raptor.sdu.SDUnlimited.logger;
import static com.raptor.sdu.type.Mod.internal_modlist;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

import com.raptor.sdu.SDUnlimited;
import com.raptor.sdu.block.BlockUnlimitedDrawers;
import com.raptor.sdu.block.BlockUnlimitedTrim;
import com.raptor.sdu.item.ItemUnlimitedDrawers;
import com.raptor.sdu.item.ItemUnlimitedTrim;
import com.raptor.sdu.type.ModListParser.ModListSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class Mods {

	/*public static final Mod
	AETHER = new Mod(new String[] { "aether", "aether_legacy" },
			material("skyroot")
				.planks("skyroot_planks", 0)
				.slab("skyroot_slab", 0)
				.planks("aether_legacy:skyroot_plank", 0)
				.slab("aether_legacy:skyroot_slab", 0),
				//.planks("unlimitedchiselworks:chisel_planks_oak_aether_legacy_skyroot_plank_0")
				//.planks("unlimitedchiselworks:chisel_planks_oak_aether_skyroot_planks_0"),
			material("greatroot")
				.planks("dark_skyroot_planks", 0)
				.slab("greatroot_slab", 0),
				//.planks("unlimitedchiselworks:chisel_planks_oak_aether_dark_skyroot_planks_0"),
			material("therawood")
				.planks("therawood_planks", 0)
				.slab("therawood_slab", 0),
				//.planks("unlimitedchiselworks:chisel_planks_oak_aether_therawood_planks_0"),
			material("wisproot")
				.planks("light_skyroot_planks", 0)
				.slab("wisproot_slab", 0)),
				//.planks("unlimitedchiselworks:chisel_planks_oak_aether_light_skyroot_planks_0")),
	
	NATURA = new Mod.ExtrasOverride("natura",
			material("eucalyptus")
				.planks("overworld_planks", 5)
				.slab("overworld_slab2", 0),
			material("sakura")
				.planks("overworld_planks", 7)
				.slab("overworld_slab2", 2),
			material("ghostwood")
				.planks("nether_planks", 0)
				.slab("nether_slab", 0),
			material("redwood")
				.planks("overworld_planks", 8)
				.slab("overworld_slab2", 3),
			material("bloodwood")
				.planks("nether_planks", 1)
				.slab("nether_slab", 1)
				.planks("goodnightsleep:blood_plank")
				.slab("goodnightsleep:blood_slab"),
			material("hopseed")
				.planks("overworld_planks", 6)
				.slab("overworld_slab2", 1),
			material("maple")
				.planks("overworld_planks", 0)
				.slab("overworld_slab", 0),
			material("silverbell")
				.planks("overworld_planks", 1)
				.slab("overworld_slab", 1),
			material("purpleheart")
				.planks("overworld_planks", 2)
				.slab("overworld_slab", 2),
			material("tigerwood")
				.planks("overworld_planks", 3)
				.slab("overworld_slab", 3),
			material("willow")
				.planks("overworld_planks", 4)
				.slab("overworld_slab", 4),
			material("darkwood")
				.planks("nether_planks", 2)
				.slab("nether_slab", 2),
			material("fusewood")
				.planks("nether_planks", 3)
				.slab("nether_slab", 3)),
	
	BIOMES_O_PLENTY = new Mod.ExtrasOverride("biomesoplenty",
			material("sacredoak")
				.planks("planks_0", 0)
				.slab("wood_slab_0", 0),
			material("cherry")
				.planks("planks_0", 1)
				.slab("wood_slab_0", 1),
			material("dark")
				.planks("planks_0", 2)
				.slab("wood_slab_0", 2),
			material("fir")
				.planks("planks_0", 3)
				.slab("wood_slab_0", 3),
			material("ethereal")
				.planks("planks_0", 4)
				.slab("wood_slab_0", 4),
			material("magic")
				.planks("planks_0", 5)
				.slab("wood_slab_0", 5),
			material("mangrove")
				.planks("planks_0", 6)
				.slab("wood_slab_0", 6),
			material("palm")
				.planks("planks_0", 7)
				.slab("wood_slab_0", 7),
			material("redwood")
				.planks("planks_0", 8)
				.slab("wood_slab_1", 0),
			material("willow")
				.planks("planks_0", 9)
				.slab("wood_slab_1", 1),
			material("pine")
				.planks("planks_0", 10)
				.slab("wood_slab_1", 2),
			material("hellbark")
				.planks("planks_0", 11)
				.slab("wood_slab_1", 3),
			material("jacaranda")
				.planks("planks_0", 12)
				.slab("wood_slab_1", 4),
			material("mahogany")
				.planks("planks_0", 13)
				.slab("wood_slab_1", 5),
			material("ebony")
				.planks("planks_0", 14)
				.slab("wood_slab_1", 6)
				.planks("forestry:planks.0", 9)
				.planks("forestry:planks.fireproof.0", 9)
				.slab("forestry:slabs.1", 1),
			material("eucalyptus")
				.planks("planks_0", 15)
				.slab("wood_slab_1", 7)),
	
	FORESTRY = new Mod.ExtrasOverride("forestry",
			material("larch")
				.planks("planks.0", 0)
				.planks("planks.fireproof.0", 0)
				.slab("slabs.0", 0),
			material("teak")
				.planks("planks.0", 1)
				.planks("planks.fireproof.0", 1)
				.slab("slabs.0", 1),
			material("acacia")
				.planks("planks.0", 2)
				.planks("planks.fireproof.0", 2)
				.slab("slabs.0", 2),
			material("lime")
				.planks("planks.0", 3)
				.planks("planks.fireproof.0", 3)
				.slab("slabs.0", 3),
			material("chestnut")
				.planks("planks.0", 4)
				.planks("planks.fireproof.0", 4)
				.slab("slabs.0", 4),
			material("wenge")
				.planks("planks.0", 5)
				.planks("planks.fireproof.0", 5)
				.slab("slabs.0", 5),
			material("baobab")
				.planks("planks.0", 6)
				.planks("planks.fireproof.0", 6)
				.slab("slabs.0", 6),
			material("sequoia")
				.planks("planks.0", 7)
				.planks("planks.fireproof.0", 7)
				.slab("slabs.0", 7),
			material("kapok")
				.planks("planks.0", 8)
				.planks("planks.fireproof.0", 8)
				.slab("slabs.1", 0),
			reference(BIOMES_O_PLENTY, "ebony"),
			material("mahogany")
				.planks("planks.0", 10)
				.planks("planks.fireproof.0", 10)
				.slab("slabs.1", 2),
			material("balsa")
				.planks("planks.0", 11)
				.planks("planks.fireproof.0", 11)
				.slab("slabs.1", 3),
			material("willow")
				.planks("planks.0", 12)
				.planks("planks.fireproof.0", 12)
				.slab("slabs.1", 4),
			material("walnut")
				.planks("planks.0", 13)
				.planks("planks.fireproof.0", 13)
				.slab("slabs.1", 5),
			material("greenheart")
				.planks("planks.0", 14)
				.planks("planks.fireproof.0", 14)
				.slab("slabs.1", 6),
			material("cherry")
				.planks("planks.0", 15)
				.planks("planks.fireproof.0", 15)
				.slab("slabs.1", 7),
			material("mahoe")
				.planks("planks.1", 0)
				.planks("planks.fireproof.1", 0)
				.slab("slabs.2", 0),
			material("poplar")
				.planks("planks.1", 1)
				.planks("planks.fireproof.1", 1)
				.slab("slabs.2", 1),
			material("palm")
				.planks("planks.1", 2)
				.planks("planks.fireproof.1", 2)
				.slab("slabs.2", 2),
			material("papaya")
				.planks("planks.1", 3)
				.planks("planks.fireproof.1", 3)
				.slab("slabs.2", 3),
			material("pine")
				.planks("planks.1", 4)
				.planks("planks.fireproof.1", 4)
				.slab("slabs.2", 4),
			material("plum")
				.planks("planks.1", 5)
				.planks("planks.fireproof.1", 5)
				.slab("slabs.2", 5),
			material("maple")
				.planks("planks.1", 6)
				.planks("planks.fireproof.1", 6)
				.slab("slabs.2", 6),
			material("citrus")
				.planks("planks.1", 7)
				.planks("planks.fireproof.1", 7)
				.slab("slabs.2", 7),
			material("giganteum")
				.planks("planks.1", 8)
				.planks("planks.fireproof.1", 8)
				.slab("slabs.3", 0),
			material("ipe")
				.planks("planks.1", 9)
				.planks("planks.fireproof.1", 9)
				.slab("slabs.3", 1),
			material("padauk")
				.planks("planks.1", 10)
				.planks("planks.fireproof.1", 10)
				.slab("slabs.3", 2),
			material("cocobolo")
				.planks("planks.1", 11)
				.planks("planks.fireproof.1", 11)
				.slab("slabs.3", 3),
			material("zebrawood")
				.planks("planks.1", 12)
				.planks("planks.fireproof.1", 12)
				.slab("slabs.3", 4)),
	
	IMMERSIVE_ENGINEERING = new Mod.ExtrasOverride("immersiveengineering",
			material("treated_wood")
				.planks("treated_wood")
				.slab("treated_wood_slab")),
	
	THAUMCRAFT = new Mod("thaumcraft",
			material("greatwood")
				.planks("plank_greatwood")
				.slab("slab_greatwood"),
			material("silverwood")
				.planks("plank_silverwood")
				.slab("slab_silverwood")),
	
	RUSTIC = new Mod("rustic",
			material("olive")
				.planks("planks", 0)
				.slab("olive_slab_item"),
			material("ironwood")
				.planks("planks", 1)
				.slab("ironwood_slab_item")),
	
	BOTANIA = new Mod("botania",
			material("livingwood")
				.planks("livingwood")
				.slab("livingwood0slab")
				.slab("livingwood1slab"),
			material("dreamwood")
				.planks("dreamwood")
				.slab("dreamwood0slab")
				.slab("dreamwood1slab"),
			material("shimmerwood")
				.planks("shimmerwoodplanks")
				.slab("shimmerwoodplanks0slab")),
	
	BLUE_SKIES = new Mod("blue_skies",
			material("bluebright")
				.planks("bluebright_planks")
				.slab("bluebright_slab"),
			material("starlit")
				.planks("starlit_planks")
				.slab("starlit_slab"),
			material("dusk")
				.planks("dusk_planks")
				.slab("dusk_slab"),
			material("lunar")
				.planks("lunar_planks")
				.slab("lunar_slab"),
			material("cherry")
				.planks("cherry_planks")
				.slab("cherry_slab")),
	
	PROJECT_VIBRANT_JOURNEYS = new Mod("pvj",
			material("aspen")
				.planks("planks_aspen")
				.slab("aspen_slab"),
			material("baobab")
				.planks("planks_baobab")
				.slab("baobab_slab"),
			material("cherry_blossom")
				.planks("planks_cherry_blossom")
				.slab("cherry_blossom_slab"),
			material("cottonwood")
				.planks("planks_cottonwood")
				.slab("cottonwood_slab"),
			material("fir")
				.planks("planks_fir")
				.slab("fir_slab"),
			material("jacaranda")
				.planks("planks_jacaranda")
				.slab("jacaranda_slab"),
			material("juniper")
				.planks("planks_juniper")
				.slab("juniper_slab"),
			material("mangrove")
				.planks("planks_mangrove")
				.slab("mangrove_slab"),
			material("maple")
				.planks("planks_maple")
				.slab("maple_slab"),
			material("palm")
				.planks("planks_palm")
				.slab("palm_slab"),
			material("pine")
				.planks("planks_pine")
				.slab("pine_slab"),
			material("redwood")
				.planks("planks_redwood")
				.slab("redwood_slab"),
			material("willow")
				.planks("planks_willow")
				.slab("willow_slab")),
	
	TRAVERSE = new Mod("traverse",
			material("fir")
				.planks("fir_planks")
				.slab("fir_slab")),
	
	BIOMES_YOULL_GO = new Mod("byg",
			//material("ancient_birch")
			//	.planks("ancientbirchplanks")
			//	.slab("ancientbirchslab"),
			material("cherry")
				.planks("cherryplanks")
				.slab("cherryslab"),
			material("cika")
				.planks("cikaplanks")
				.slab("cikaslab"),
			//material("enchanted_wood")
			//	.planks("enchantedplanks")
			//	.slab("enchantedslab"),
			//material("golden_birch")
			//	.planks("goldenbirchplanks")
			//	.slab("goldenbirchslab"),
			material("great_oak")
				.planks("greatoakplanks")
				.slab("greatoakslab"),
			material("jacaranda")
				.planks("jacarandaplanks")
				.slab("jacarandaslab"),
			material("mangrove")
				.planks("mangroveplanks")
				.slab("mangroveslab"),
			material("maple")
				.planks("mapleplanks")
				.slab("mapleslab"),
			//material("frozen_oak")
			//	.planks("frozenoakplanks")
			//	.slab("frozenoakslab"),
			material("pine")
				.planks("pineplanks")
				.slab("pineslab"),
			material("skyris")
				.planks("skyrisplanks")
				.slab("skyrisslab"),
			material("willow")
				.planks("willowplanks")
				.slab("willowslab"),
			material("zelkova")
				.planks("zelkovaplanks")
				.slab("zelkovaslab"),
			material("redwood")
				.planks("redwoodplanks")
				.slab("redwoodslab"),
			material("baobab")
				.planks("baobabplanks")
				.slab("baobabslab"),
			material("hollow_oak")
				.planks("hollowoakplanks")
				.slab("hollowoakslab"),
			material("spectral_wood")
				.planks("spectralplanks")
				.slab("spectralslab"),
			material("holly")
				.planks("hollyplanks")
				.slab("hollyslab"),
			material("rainbow_eucalyptus")
				.planks("rainboweucalyptusplanks")
				.slab("rainboweucalyptusslab"),
			material("witch_hazel")
				.planks("witchhazelplanks")
				.slab("witchhazelslab")),
	
	QUARK = new Mod("quark",
			material("white_stained_wood")
				.planks("stained_planks", 0)
				.slab("stained_planks_white_slab"),
			material("orange_stained_wood")
				.planks("stained_planks", 1)
				.slab("stained_planks_orange_slab"),
			material("magenta_stained_wood")
				.planks("stained_planks", 2)
				.slab("stained_planks_magenta_slab"),
			material("light_blue_stained_wood")
				.planks("stained_planks", 3)
				.slab("stained_planks_light_blue_slab"),
			material("yellow_stained_wood")
				.planks("stained_planks", 4)
				.slab("stained_planks_yellow_slab"),
			material("lime_stained_wood")
				.planks("stained_planks", 5)
				.slab("stained_planks_lime_slab"),
			material("pink_stained_wood")
				.planks("stained_planks", 6)
				.slab("stained_planks_pink_slab"),
			material("gray_stained_wood")
				.planks("stained_planks", 7)
				.slab("stained_planks_gray_slab"),
			material("light_gray_stained_wood")
				.planks("stained_planks", 8)
				.slab("stained_planks_silver_slab"),
			material("cyan_stained_wood")
				.planks("stained_planks", 9)
				.slab("stained_planks_cyan_slab"),
			material("purple_stained_wood")
				.planks("stained_planks", 10)
				.slab("stained_planks_purple_slab"),
			material("blue_stained_wood")
				.planks("stained_planks", 11)
				.slab("stained_planks_blue_slab"),
			material("brown_stained_wood")
				.planks("stained_planks", 12)
				.slab("stained_planks_brown_slab"),
			material("green_stained_wood")
				.planks("stained_planks", 13)
				.slab("stained_planks_green_slab"),
			material("red_stained_wood")
				.planks("stained_planks", 14)
				.slab("stained_planks_red_slab"),
			material("black_stained_wood")
				.planks("stained_planks", 15)
				.slab("stained_planks_black_slab"),
			material("thatch") // added thatch because I added thatch for betweenlands and quark's thatch is differently colored
				.planks("thatch")
				.slab("thatch_slab")
				.setGrassy()),
	
	INTEGRATED_DYNAMICS = new Mod("integrateddynamics",
			material("menril")
				.planks("menril_planks")
				.slab("menril_planks_stairs")),
	
	THE_BETWEENLANDS = new Mod("thebetweenlands",
			material("weedwood")
				.planks("weedwood_planks")
				.slab("weedwood_plank_slab"),
			material("rubber_tree")
				.planks("rubber_tree_planks")
				.slab("rubber_tree_plank_slab"),
			material("giant_root")
				.planks("giant_root_planks")
				.slab("giant_root_plank_slab"),
			material("hearthgrove")
				.planks("hearthgrove_planks")
				.slab("hearthgrove_plank_slab"),
			material("nibbletwig")
				.planks("nibbletwig_planks")
				.slab("nibbletwig_plank_slab"),
			material("thatch")
				.planks("thatch")
				.slab("thatch_slab")
				.planks("tropicraft:bundle", 0)
				.slab("tropicraft:slab", 1)
				.setGrassy()),
	
	TROPICRAFT = new Mod("tropicraft",
			material("bamboo")
				.planks("tropicraft:bundle", 1)
				.slab("tropicraft:slab", 0)
				.planks("growthcraft_bamboo:bamboo_plank")
				.slab("growthcraft_bamboo:bamboo_slab_half", 0),
			material("palm")
				.planks("tropicraft:plank", 1)
				.slab("tropicraft:slab", 3),
			material("mahogany")
				.planks("tropicraft:plank", 0)
				.slab("tropicraft:slab", 4),
			reference(THE_BETWEENLANDS, "thatch")),
	
	GLACIDUS = new Mod("glacidus",
			material("underground_wood")
				.planks("underground_planks")
				.slab("underground_slab", 0)),
	
	GOOD_NIGHTS_SLEEP = new Mod("goodnightsleep",
			material("dream_wood")
				.planks("dream_plank")
				.slab("dream_slab"),
			material("white_wood")
				.planks("white_plank")
				.slab("white_slab"),
			material("dead_wood")
				.planks("dead_plank")
				.slab("dead_slab"),
			reference(NATURA, "bloodwood")),
	
	GROWTHCRAFT_BAMBOO = new Mod("growthcraft_bamboo",
			reference(TROPICRAFT, "bamboo")),
	
	GROWTHCRAFT_APPLES = new Mod("growthcraft_apples",
			material("apple_wood")
				.planks("apple_planks")
				.slab("apple_slab_half")),
	
	ATUM = new Mod("atum",
			material("palm")
				.planks("palm_planks")
				.slab("palm_slab"),
			material("deadwood")
				.planks("deadwood_planks")
				.slab("deadwood_slab")),
	
	NATURES_AURA = new Mod("naturesaura",
			material("ancient_wood")
				.planks("ancient_planks")
				.slab("ancient_slab")),
	
	PRODIGY_TECH = new Mod("prodigytech",
			material("zorra")
				.planks("zorra_planks"),
			material("particle_board")
				.planks("particle_board")
				.planks("particle_board_planks")),
	
	EREBUS = new Mod("erebus",
			material("asper")
				.planks("erebus:planks", 4)
				.slab("slab_planks_asper"),
			material("balsam")
				.planks("erebus:planks", 6)
				.slab("slab_planks_balsam"),
			material("bamboo")
				.planks("erebus:planks", 8)
				.slab("slab_planks_bamboo"),
			material("baobab")
				.planks("erebus:planks", 0)
				.slab("slab_planks_babab"),
			material("cypress")
				.planks("erebus:planks", 5)
				.slab("slab_planks_cypress"),
			material("eucalyptus")
				.planks("erebus:planks", 1)
				.slab("slab_planks_eucalyptus"),
			material("mahogany")
				.planks("erebus:planks", 2)
				.slab("slab_planks_mahogany"),
			material("marshwood")
				.planks("erebus:planks", 10)
				.slab("slab_planks_marshwood"),
			material("mossbark")
				.planks("erebus:planks", 3)
				.slab("slab_planks_mossbark"),
			material("petrified_wood")
				.planks("planks_petrified_wood")
				.slab("slab_planks_petrified_wood"),
			material("rotten_wood")
				.planks("erebus:planks", 9)
				.slab("slab_planks_rotten"),
			material("scorched_wood")
				.planks("erebus:planks", 11)
				.slab("slab_planks_scorched"),
			material("varnished_wood")
				.planks("erebus:planks", 12)
				.slab("slab_planks_varnished"),
			material("white_wood")
				.planks("erebus:planks", 7)
				.slab("slab_planks_white"))
	;
*/
	public static final StreamableFilterableIterable<Mod> MODS, ENABLED_MODS;
	private static final Map<String, Mod> BY_ID;
	public static final StreamableFilterableIterable<Block> BLOCKS;
	public static final StreamableFilterableIterable<Item> ITEMS;
	public static final StreamableFilterableIterable<BlockUnlimitedDrawers> DRAWERS_BLOCKS;
	public static final StreamableFilterableIterable<ItemUnlimitedDrawers> DRAWERS_ITEMS;
	public static final StreamableFilterableIterable<BlockUnlimitedTrim> TRIM_BLOCKS;
	public static final StreamableFilterableIterable<ItemUnlimitedTrim> TRIM_ITEMS;
	
	static {
		// load builtin.sdmods
		{
			Scanner scan0;
			try(Scanner scan = scan0 = new Scanner(SDUnlimited.class.getResourceAsStream("builtin.sdmods"))) {
				ModListParser.parseModList("builtin.sdmods", scan);
			} catch(NullPointerException e) {
				throw new Error("Missing com/raptor/sdu/builtin.sdmods in StorageDrawersUnlimited jar file!");
			}
			if(scan0.ioException() != null) {
				throw new Error("Error reading builtin.sdmods for StorageDrawersUnlimited", scan0.ioException());
			}
		}
		
		// load user-defined mods
		for(File modList : configFolder.listFiles(file -> file.isFile() && StringUtils.endsWithIgnoreCase(file.getName(), ".sdmods"))) {
			try(Scanner scan = new Scanner(modList)) {
				ModListParser.parseModList(modList.getName(), scan);
			} catch(ModListSyntaxException | FileNotFoundException e) {
				logger.log(Level.ERROR, e);
			}
		}
		
		MODS = StreamableFilterableIterable.wrap(internal_modlist);
		ENABLED_MODS = MODS.filter(Mod::isEnabled);
		BY_ID = MODS.stream()
				.map(mod -> Pair.of(mod.modid, mod))
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		BLOCKS = () -> new NestedIterator<>(ENABLED_MODS.stream()
				.map(mod -> new NestedIterator<>(mod.stream()
						.map(DrawerMaterial::blockIterator)
						.iterator()))
				.iterator());
		ITEMS = () -> new NestedIterator<>(ENABLED_MODS.stream()
				.map(mod -> new NestedIterator<>(mod.stream()
						.map(DrawerMaterial::itemIterator)
						.iterator()))
				.iterator());
		DRAWERS_BLOCKS = BLOCKS.filter(BlockUnlimitedDrawers.class);
		DRAWERS_ITEMS = ITEMS.filter(ItemUnlimitedDrawers.class);
		TRIM_BLOCKS = BLOCKS.filter(BlockUnlimitedTrim.class);
		TRIM_ITEMS = ITEMS.filter(ItemUnlimitedTrim.class);
	}
	
	public static void init() {
		for(Mod mod : ENABLED_MODS) {
			mod.init();
		}
	}
	
	public static Mod fromModId(String modid) {
		return BY_ID.get(modid);
	}
	
	public static DrawerMaterial.BuilderImpl material(String name) {
		return new DrawerMaterial.BuilderImpl(name);
	}
	
	public static DrawerMaterial.Builder reference(Mod mod, String name) {
		return new DrawerMaterial.MaterialReference(mod.getMaterial(name));
	}
	
	private Mods() {
		throw new UnsupportedOperationException(getClass().getName() + " cannot be instantiated");
	}
}
