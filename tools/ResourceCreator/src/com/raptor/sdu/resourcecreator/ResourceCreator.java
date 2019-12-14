package com.raptor.sdu.resourcecreator;

import static com.raptor.sdu.resourcecreator.EnumSetOptionHandlerFactory.unwrapType;
import static com.raptor.sdu.resourcecreator.ResourceCreator.ImageType.*;
import static com.raptor.sdu.resourcecreator.ResourceCreator.ModelType.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerRegistry;

public class ResourceCreator {
	
	static {
		OptionHandlerRegistry registry = OptionHandlerRegistry.getRegistry();
		registry.registerHandler(EnumSet.class, new EnumSetOptionHandlerFactory());
		registry.registerHandler(String.class, new StringOptionHandlerFactory());
	}
	
	@Option(name = "-help", aliases = {"--help", "-h", "/?", "-?"}, help = true)
	private boolean help = false;
	
	@Option(name = "-preview", forbids = "-showimages",
			usage = "Show the values of various variables created from the other options without modifying any files")
	private boolean preview = false;
	
	@Option(name = "-previewimages", forbids = "-showimages",
			usage = "Display images in addition to the variables when doing -preview")
	private EnumSet<PreviewImageType> previewImages = EnumSet.noneOf(PreviewImageType.class);
	
	@Option(name = "-showimages",
			usage = "Show the specified image types when they are created")
	private EnumSet<ImageType> showImageTypes = EnumSet.noneOf(ImageType.class);
	
	@Option(name = "-base", required = true)
	private File baseImgFile;
	
	@Option(name = "-templates", metaVar = "DIR", 
			usage = "The directory of the templates")
	private File templates_dir = new File("templates");
	
	@Option(name = "-trim")
	private File trimImgFile;
	
	@Option(name = "-face")
	private File faceImgFile;
	
	@Option(name = "-materials", 
			usage = "JSON file containing \"planks\" and \"slabs\" lists with a bunch of item ids in them for generating the crafting recipes")
	private File materialsFile;
	
	@Option(name = "-automaterials", depends = "-materials", 
			usage = "If the materials file is specified but absent, generate planks and slabs from the material name")
	private boolean autoMaterials = false;
	
	@Option(name = "-materialid", metaVar = "[<MODID>:]<MATERIAL>",
			usage = "Set the id of this material")
	@RegexValidated("([-.a-z_0-9]+:)?[a-z_0-9]+")
	@ExcludeDebug
	private String material = null;
	
	@Option(name = "-modid")
	@RegexValidated("[-.a-z_0-9]+")
	@ExcludeDebug
	private String modid = "";
	
	@Option(name = "-out", metaVar = "DIR", 
			usage = "The path to the 'assets/<namespace>' directory")
	private File out_dir = new File("output");
	
	@Option(name = "-lang", metaVar = "NAME",
			usage = "The material's localized name")
	@RegexValidated("([^\\s]| (?!\\s))+")
	@ExcludeDebug
	private String materialName;
	
	@Option(name = "-q", aliases = "-quiet", 
			usage = "Suppress informative output")
	private boolean quiet = false;
	
	@Option(name = "-i", aliases = "-ignoremissing",
			usage = "If nonessential files don't exist, act like they weren't even specified.")
	private boolean ignoreNonexistentFiles = false;
	
	@Option(name = "-iq", aliases = "-iquiet", 
			usage = "Don't print warning messages when nonessential files don't exist")
	private boolean hideUsingDefaultFileMessage = false;
	
	@Option(name = "-noimages", 
			usage = "Don't create the texture files (note that you still have to specify -base)")
	private boolean noImages = false;
	
	@Option(name = "-onlyimages", forbids = {"-noimages", "-nomodels", "-onlymodels", "-noblockstates", "-onlyblockstates", "-nolang", "-onlylang", "-norecipes", "-onlyrecipes", "-notags", "-onlytags"})
	private boolean onlyImages = false;
	
	@Option(name = "-nomodels", 
			usage = "Don't create the model files")
	private boolean noModels = false;
	
	@Option(name = "-onlymodels", forbids = {"-nomodels", "-noimages", "-onlyimages", "-noblockstates", "-onlyblockstates", "-nolang", "-onlylang", "-norecipes", "-onlyrecipes", "-notags", "-onlytags"})
	private boolean onlyModels = false;
	
	@Option(name = "-nolang", 
			usage = "Don't create the lang entry")
	private boolean noLang = false;
	
	@Option(name = "-onlylang", forbids = {"-nolang", "-nomodels", "-onlymodels", "-noblockstates", "-onlyblockstates", "-noimages", "-onlyimages", "-norecipes", "-onlyrecipes", "-notags", "-onlytags"})
	private boolean onlyLang = false;
	
	@Option(name = "-norecipes", 
			usage = "Don't create recipes")
	private boolean noRecipes = false;
	
	@Option(name = "-onlyrecipes", forbids = {"-norecipes", "-nomodels", "-onlymodels", "-noblockstates", "-onlyblockstates", "-nolang", "-onlylang", "-noimages", "-onlyimages", "-notags", "-onlytags"})
	private boolean onlyRecipes = false;
	
	@Option(name = "-noblockstates", 
			usage = "Don't create blockstates")
	private boolean noBlockstates = false;
	
	@Option(name = "-onlyblockstates", forbids = {"-noblockstates", "-nomodels", "-onlymodels", "-noimages", "-onlyimages", "-nolang", "-onlylang", "-norecipes", "-onlyrecipes", "-notags", "-onlytags"})
	private boolean onlyBlockstates = false;
	
	@Option(name = "-notags",
			usage = "Don't add tags to storagedrawers/tags/blocks/drawers.json and storagedrawers/tags/items/drawers.json")
	private boolean noTags = false;
	
	@Option(name = "-onlytags", forbids = {"-notags", "-nomodels", "-onlymodels", "-noblockstates", "-onlyblockstates", "-nolang", "-onlylang", "-norecipes", "-onlyrecipes", "-noimages", "-onlyimages"})
	private boolean onlyTags = false;
	
	@Option(name = "-noreplaceimages", forbids = "-noreplace", 
			usage = "Don't replace texture files if they exist")
	private boolean noReplaceImages = false;
	
	@Option(name = "-noreplacerecipes",
			usage = "Don't replace recipe files if they exist")
	private boolean noReplaceRecipes = false;
	
	@Option(name = "-noreplace", forbids = "-noreplaceimages",
			usage = "Don't replace specific texture files if they exist. This can be specified more than once to exclude more than one texture.")
	private EnumSet<ImageType> noReplaceImageTypes = EnumSet.noneOf(ImageType.class);
	
	@Option(name = "-excludeimages", 
			usage = "Don't create the given textures. This can be specified more than once to exclude more than one texture.")
	private EnumSet<ImageType> excludeImageTypes = EnumSet.noneOf(ImageType.class);
	
	@Option(name = "-noreplacemodels", 
			usage = "Don't replace model files if they exist")
	private boolean noReplaceModels = false;
	
	@Option(name = "-excludemodels",
			usage = "Don't create the given models. This can be specified more than once to exclude more than one model.")
	private EnumSet<ModelType> excludeModelTypes = EnumSet.noneOf(ModelType.class);
	
	@Option(name = "-noreplacelang", 
			usage = "Don't replace language entries if they exist")
	private boolean noReplaceLang = false;
	
	@Option(name ="-noreplaceblockstates", 
			usage = "Don't replace blockstate files if they exist")
	private boolean noReplaceBlockstates = false;
	
	@Option(name = "-backup",
			usage = "Backup these types of files.")
	private EnumSet<BackupType> backupTypes = EnumSet.noneOf(BackupType.class);
	
	@Option(name = "-noshade",
			usage = "Don't automatically add the specified type(s) of shading for the textures")
	private EnumSet<ShadingType> excludedShadingTypes = EnumSet.noneOf(ShadingType.class);
	
	@Option(name = "-delete", forbids = {"-noreplaceimages", "-noreplacemodels", "-noreplacelang", "-noreplacerecipes", "-noreplaceblockstates", "-showimages"}, 
			usage = "Delete files rather than creating them")
	private boolean delete = false;
	
	private final File overlays_dir, template_models_dir, template_blockstates_dir, template_recipes_dir;
	private final File textures_dir, block_models_dir, item_models_dir, blockstates_dir, item_tags_dir, recipes_dir, storagedrawers_tags_dir;
	
	private final BufferedImage base, trim, face;
	private final BufferedImage handle_1, handle_2, handle_4;
	private final BufferedImage cutout_1, cutout_h, cutout_v, cutout_4;
	private final BufferedImage /*shading_all,*/ cutout_sort;
	private final BufferedImage shading_face_1, shading_face_2, shading_face_4;
	private final BufferedImage shading_trim_1, shading_trim_h, shading_trim_v, shading_trim_4;
	
	private final boolean backupTextures, backupItemModels, backupBlockModels, backupLang, backupBlockstates, backupRecipes;
	
	// the blockstate templates
	private final String drawer_blockstate_template, trim_blockstate_template;
	
	// the model templates
	private final String block_basicdrawers_full1, block_basicdrawers_full2, block_basicdrawers_full4, block_basicdrawers_half1, block_basicdrawers_half2, block_basicdrawers_half4;
	private final String block_trim;
	private final String item_basicdrawers_full1, item_basicdrawers_full2, item_basicdrawers_full4, item_basicdrawers_half1, item_basicdrawers_half2, item_basicdrawers_half4;
	private final String item_trim;
	
	// the recipe templates
	private final String recipe_full_drawers_1, recipe_full_drawers_2, recipe_full_drawers_4, recipe_half_drawers_1, recipe_half_drawers_2, recipe_half_drawers_4, recipe_trim;
	
	// base animation file contents
	private final String animation;
	
	// the lang file stuff
	private final File lang_file;
	@ExcludeDebug
	private final JSONObject lang;
	
	// recipe materials stuff
	private final Set<ResourceLocation> planks, slabs;
	
	// the filename templates
	private final String block_model_filename_template, item_model_filename_template, blockstate_filename_template, recipe_filename_template, tag_filename_template, tag_entries_template;
	
	@ExcludeDebug
	private final JSONPrettyPrinter jsonPrettyPrinter;
	
	class ResourceLocation {		
		public final String namespace, key;
		public final boolean isTag;
		private final int hash;
		
		public ResourceLocation(String str) {
			if(!str.matches("#?[-.a-z_0-9]+(:[-.a-z_0-9]+)?")) {
				error("Invalid namespaced key: " + str);
				throw fatalError();
			}
			if(this.isTag = str.charAt(0) == '#') {
				int i = str.indexOf(':');
				if(i == -1) {
					namespace = "minecraft";
					key = str.substring(1);
				} else {
					namespace = str.substring(1, i);
					key = str.substring(i+1);
				}
			} else {
				int i = str.indexOf(':');
				if(i == -1) {
					namespace = modid;
					key = str;
				} else {
					namespace = str.substring(0, i);
					key = str.substring(i+1);
				}
			}
			this.hash = Objects.hash(namespace, key, isTag);
		}
		
		@Override
		public int hashCode() {
			return hash;
		}
		
		@Override
		public String toString() {
			return namespace + ':' + key;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj instanceof ResourceLocation) {
				ResourceLocation loc = (ResourceLocation)obj;
				return this.namespace.equals(loc.namespace) && this.key.equals(loc.key);
			}
			return false;
		}
		
	}
	
	enum PreviewImageType {
		base		(x->x.base), 
		trim		(x->x.trim), 
		face		(x->x.face), 
//		handle_1	(x->x.handle_1), 
//		handle_2	(x->x.handle_2),
//		handle_4	(x->x.handle_4),
//		cutout_1	(x->x.cutout_1),
//		cutout_h	(x->x.cutout_h),
//		cutout_v	(x->x.cutout_v),
//		cutout_4	(x->x.cutout_4),
//		cutout_sort	(x->x.cutout_sort),
		front_1		(ResourceCreator::make_img_front_1),
		front_2		(ResourceCreator::make_img_front_2),
		front_4		(ResourceCreator::make_img_front_4),
		side		(ResourceCreator::make_img_side),
		side_h		(ResourceCreator::make_img_side_h),
		side_v		(ResourceCreator::make_img_side_v),
		sort		(ResourceCreator::make_img_sort);
		
		private Function<ResourceCreator, BufferedImage> getter;
		
		PreviewImageType(Function<ResourceCreator, BufferedImage> getter) {
			this.getter = getter;
		}
		
		public BufferedImage get(ResourceCreator creatorIn) {
			return getter.apply(creatorIn);
		}
		
	}
	
	enum ImageType {
		front_1, front_2, front_4, side, side_h, side_v, sort, trim
	}
	
	enum ShadingType {
		face_1, face_2, face_4, trim_1, trim_h, trim_v, trim_4
	}
	
	enum ModelType {
		full1("full_drawers_1", "${Material} Drawers 1x1"), 
		full2("full_drawers_2", "${Material} Drawers 1x2"),
		full4("full_drawers_4", "${Material} Drawers 2x2"),
		half1("half_drawers_1", "${Material} Half Drawers 1x1"),
		half2("half_drawers_2", "${Material} Half Drawers 1x2"),
		half4("half_drawers_4", "${Material} Half Drawers 2x2"),
		trim("trim", "${Material} Trim");
		
		static final EnumSet<ModelType> VALUES = EnumSet.allOf(ModelType.class);
		static final EnumSet<ModelType> DRAWER_TYPES = EnumSet.complementOf(EnumSet.of(trim));
		
		private final String translationKey, translationValue;
		
		ModelType(String translationKey, String translationValue) {
			this.translationKey = translationKey;
			this.translationValue = translationValue;
		}
		
		public String translateKey(String modid, String material) {
			return "block.storagedrawersunlimited." + modid + '_' + material + '_' + translationKey;
		}
		
		public String translateValue(String materialName) {
			return translationValue.replace("${Material}", materialName);
		}
	}
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface ExcludeDebug {}
	
	private static final int TEXTURE_FLAG = 1;
	private static final int ITEM_MODEL_FLAG = 1<<1;
	private static final int BLOCK_MODEL_FLAG = 1<<2;
	private static final int BLOCKSTATE_FLAG = 1<<3;
	private static final int LANG_FLAG = 1<<4;
	private static final int RECIPES_FLAG = 1<<5;
	
	enum BackupType { 
		models(ITEM_MODEL_FLAG|BLOCK_MODEL_FLAG), 
		item_models(ITEM_MODEL_FLAG), 
		block_models(BLOCK_MODEL_FLAG), 
		textures(TEXTURE_FLAG), 
		blockstates(BLOCKSTATE_FLAG),
		lang(LANG_FLAG),
		recipes(RECIPES_FLAG);
		
		public final int flags;
		
		BackupType(int flags) {
			this.flags = flags;
		}
		
	}
	
	public static void main(String[] args) {
		new ResourceCreator(args).process();
	}
	
	ResourceCreator(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		
		try {
			parser.parseArgument(args);
		} catch(CmdLineException e) {
			error(e.getMessage());
			error("java -jar ResourceCreator.jar -base FILE -trim FILE [-name NAME] [-out DIR] [OPTION...]");
			parser.printUsage(System.err);
			//error();
			//error("  Example: java -jar ResourceCreator.jar" + parser.printExample(OptionHandlerFilter.ALL));
			throw fatalError();
		}
		
		if(help) {
			System.out.println("java -jar ResourceCreator.jar -base FILE -trim FILE [-name NAME] [-out DIR] [OPTION...]");
			parser.printUsage(System.out);
			//System.out.println();
			//System.out.println("  Example: java -jar ResourceCreator.jar" + parser.printExample(OptionHandlerFilter.ALL));
			throw cleanExit();
		}
		
		if(!templates_dir.exists() || !templates_dir.isDirectory()) {
			error("Error: missing templates directory");
			throw fatalError();
		}

		overlays_dir = new File(templates_dir, "overlays");
		template_models_dir = new File(templates_dir, "models");
		template_blockstates_dir = new File(templates_dir, "blockstates");
		template_recipes_dir = new File(templates_dir, "recipes");
		
		if(onlyImages) {
			noImages = false;
			noBlockstates = true;
			noRecipes = true;
			noTags = true;
			noLang = true;
			noModels = true;
		} else if(onlyBlockstates) {
			noImages = true;
			noBlockstates = false;
			noRecipes = true;
			noTags = true;
			noLang = true;
			noModels = true;
		} else if(onlyRecipes) {
			noImages = true;
			noBlockstates = true;
			noRecipes = false;
			noTags = true;
			noLang = true;
			noModels = true;
		} else if(onlyTags) {
			noImages = true;
			noBlockstates = true;
			noRecipes = true;
			noTags = false;
			noLang = true;
			noModels = true;
		} else if(onlyLang) {
			noImages = true;
			noBlockstates = true;
			noRecipes = true;
			noTags = true;
			noLang = false;
			noModels = true;
		} else if(onlyModels) {
			noImages = true;
			noBlockstates = true;
			noRecipes = true;
			noTags = true;
			noLang = true;
			noModels = false;
		}
		
		if(!noImages && (!overlays_dir.exists() || !overlays_dir.isDirectory())) {
			error("Error: missing 'templates/overlay' directory");
			error(overlays_dir.getAbsolutePath());
			throw fatalError();
		}
		if(!noModels && (!template_models_dir.exists() || !template_models_dir.isDirectory())) {
			error("Error: missing 'templates/models' directory");
			throw fatalError();
		}
		if(!noModels && (!template_blockstates_dir.exists() || !template_blockstates_dir.isDirectory())) {
			error("Error: missing 'templates/blockstates' directory");
			throw fatalError();
		}
		if(!noRecipes && (!template_recipes_dir.exists() || !template_recipes_dir.isDirectory())) {
			error("Error: missing 'templates/recipes' directory");
			throw fatalError();
		}
		
		if(noReplaceImages) {
			noReplaceImageTypes = EnumSet.allOf(ImageType.class);
		}
		
		{
			Properties filenames = new Properties();
			File filenames_properties_file = new File(templates_dir, "filenames.properties");
			try(Reader reader = new FileReader(filenames_properties_file)) {
				filenames.load(reader);
				requireKey(filenames, "models.blocks", "filenames.properties");
				requireKey(filenames, "models.items", "filenames.properties");
				requireKey(filenames, "blockstates", "filenames.properties");
				requireKey(filenames, "recipes", "filenames.properties");
				requireKey(filenames, "tags", "filenames.properties");
			}
			catch(IOException | IllegalArgumentException e) {
				error("Error: could not read filenames.properties template");
				error(" >> File " + getCanonicalPath(filenames_properties_file));
				throw fatalError();
			}
			
			block_model_filename_template = filenames.getProperty("models.blocks");
			item_model_filename_template  = filenames.getProperty("models.items");
			blockstate_filename_template  = filenames.getProperty("blockstates");
			recipe_filename_template      = filenames.getProperty("recipes");
			tag_filename_template  = filenames.getProperty("tags");
			tag_entries_template = filenames.getProperty("tag_entries");
		}
		
		if(out_dir.exists()) {
			if(!out_dir.isDirectory()) {
				error("Error: '-out' parameter must be a file");
				throw fatalError();
			}
		} else {
			out_dir.mkdirs();
		}
		
		Scanner keys = null;
		
		if(material == null || material.isEmpty()) {
			Matcher m = Pattern.compile("base_(?<name>\\w+)\\.(?i)png").matcher(baseImgFile.getName());
			if(m.matches() && (trimImgFile == null || trimImgFile.getName().matches("trim_" + m.group("name") + "\\.(?i)png"))
							&& (faceImgFile == null || faceImgFile.getName().matches("face_" + m.group("name") + "\\.(?i)png"))) {
				material = m.group("name");
			} else {
				keys = new Scanner(System.in);
				System.out.print("Enter the material name: ");
				try {
					material = keys.nextLine();
				} catch(NoSuchElementException e) {
					// this means the user did the ctrl-c quit command
					throw cleanExit();
				}
			}
		}
		
		if(modid.isEmpty()) {
			if(!material.matches("[-.a-z_0-9]+:[a-z_0-9]+")) {
				error("Invalid material name: " + material);
				error("Material names can only contain letters, numbers, and underscores. They must have a mod id's namepsace prefixed to them if -modid option isn't specified.");
				throw fatalError();
			}
			
			int i = material.indexOf(':');
			modid = material.substring(0, i);
			material = material.substring(i+1);
			
		} else {
			if(!modid.matches("[-.a-z_0-9]+")) {
				error("Invalid Mod ID: " + modid);
				error("Mod IDs can only contain letters, numbers, and underscores.");
				throw fatalError();
			}
			if(!material.matches("[a-z_0-9]+")) {
				error("Invalid material: " + modid);
				error("Material names can only contain letters, numbers, and underscores.");
				throw fatalError();
			}
		}
			
		if(backupTypes.contains(BackupType.models)) {
			backupItemModels = backupBlockModels = true;
		} else {
			backupItemModels = backupTypes.contains(BackupType.item_models);
			backupBlockModels = backupTypes.contains(BackupType.block_models);
		}
		backupTextures = backupTypes.contains(BackupType.textures);
		backupBlockstates = backupTypes.contains(BackupType.blockstates);
		backupLang = backupTypes.contains(BackupType.lang);
		backupRecipes = backupTypes.contains(BackupType.recipes);
		
		textures_dir = new File(out_dir, "textures/block/" + modid);
		block_models_dir = new File(out_dir, "models/block/" + modid);
		item_models_dir = new File(out_dir, "models/item/");
		blockstates_dir = new File(out_dir, "blockstates/");
		recipes_dir = new File(out_dir.getParentFile().getParent(), "data/" + out_dir.getName() + "/recipes");
		item_tags_dir = new File(recipes_dir.getParent(), "tags/items");
		lang_file = new File(out_dir, "lang/en_us.json");
		storagedrawers_tags_dir = new File(out_dir.getParentFile().getParent(), "data/storagedrawers/tags");
		if(!lang_file.getParentFile().exists())
			lang_file.getParentFile().mkdirs();		
		if(!blockstates_dir.exists() && !noBlockstates)
			blockstates_dir.mkdirs();
		if(!textures_dir.exists() && !noImages)
			textures_dir.mkdirs();
		if(!block_models_dir.exists() && !noModels)
			block_models_dir.mkdirs();
		if(!item_models_dir.exists() && !noModels)
			item_models_dir.mkdirs();
		if(!recipes_dir.exists() && !noRecipes)
			recipes_dir.mkdirs();
		if(!item_tags_dir.exists() && !noRecipes)
			item_tags_dir.mkdirs();
		if(!storagedrawers_tags_dir.exists() && !noTags)
			storagedrawers_tags_dir.mkdirs();
		
		if(noModels) {			
			block_basicdrawers_full1 = null;
			block_basicdrawers_full2 = null;
			block_basicdrawers_full4 = null;
			block_basicdrawers_half1 = null;
			block_basicdrawers_half2 = null;
			block_basicdrawers_half4 = null;
			block_trim = null;
			
			item_basicdrawers_full1 = null;
			item_basicdrawers_full2 = null;
			item_basicdrawers_full4 = null;
			item_basicdrawers_half1 = null;
			item_basicdrawers_half2 = null;
			item_basicdrawers_half4 = null;
			item_trim = null;			
		} else {			
			block_basicdrawers_full1 = loadText(new File(template_models_dir, "block/basicdrawers_full1.json"));
			block_basicdrawers_full2 = loadText(new File(template_models_dir, "block/basicdrawers_full2.json"));
			block_basicdrawers_full4 = loadText(new File(template_models_dir, "block/basicdrawers_full4.json"));
			block_basicdrawers_half1 = loadText(new File(template_models_dir, "block/basicdrawers_half1.json"));
			block_basicdrawers_half2 = loadText(new File(template_models_dir, "block/basicdrawers_half2.json"));
			block_basicdrawers_half4 = loadText(new File(template_models_dir, "block/basicdrawers_half4.json"));
			block_trim = loadText(new File(template_models_dir, "block/trim.json"));
			
			item_basicdrawers_full1 = loadText(new File(template_models_dir, "item/basicdrawers_full1.json"));
			item_basicdrawers_full2 = loadText(new File(template_models_dir, "item/basicdrawers_full2.json"));
			item_basicdrawers_full4 = loadText(new File(template_models_dir, "item/basicdrawers_full4.json"));
			item_basicdrawers_half1 = loadText(new File(template_models_dir, "item/basicdrawers_half1.json"));
			item_basicdrawers_half2 = loadText(new File(template_models_dir, "item/basicdrawers_half2.json"));
			item_basicdrawers_half4 = loadText(new File(template_models_dir, "item/basicdrawers_half4.json"));
			item_trim = loadText(new File(template_models_dir, "item/trim.json"));
		}
		
		if(noBlockstates) {
			drawer_blockstate_template = null;
			trim_blockstate_template = null;
		} else {
			drawer_blockstate_template = loadText(new File(template_blockstates_dir, "basicdrawers.json"));
			trim_blockstate_template = loadText(new File(template_blockstates_dir, "trim.json"));
		}
		
		recipes:
		if(!noRecipes) {
			if(materialsFile == null) {
				if(ignoreNonexistentFiles) {
					noRecipes = true;
					break recipes;
				}
				error("Missing materials file argument (-materials)");
				throw fatalError();
			}
			if(!materialsFile.exists() || !materialsFile.isFile()) {
				if(autoMaterials) {
					break recipes;
				}
				if(ignoreNonexistentFiles) {
					noRecipes = true;
					break recipes;
				}
				error("Materials file " + materialsFile + " does not exist");
				throw fatalError();
			}
			
		}
		
		if(noImages) {
			handle_1 = null;
			handle_2 = null;
			handle_4 = null;
			cutout_1 = null;
			cutout_h = null;
			cutout_v = null;
			cutout_4 = null;
			//shading_all = null;
			cutout_sort = null;
			shading_face_1 = null;
			shading_face_2 = null;
			shading_face_4 = null;
			shading_trim_1 = null;
			shading_trim_h = null;
			shading_trim_v = null;
			shading_trim_4 = null;
			
			base = null;
			trim = null;
			face = null;
			
			animation = null;
		} else {
			final BufferedImage handle_1 = loadImage(new File(overlays_dir, "handle_1.png"));
			final BufferedImage handle_2 = loadImage(new File(overlays_dir, "handle_2.png"));
			final BufferedImage handle_4 = loadImage(new File(overlays_dir, "handle_4.png"));
			final BufferedImage cutout_1 = loadImage(new File(overlays_dir, "cutout_1.png"));
			final BufferedImage cutout_h = loadImage(new File(overlays_dir, "cutout_h.png"));
			final BufferedImage cutout_v = loadImage(new File(overlays_dir, "cutout_v.png"));
			final BufferedImage cutout_4 = loadImage(new File(overlays_dir, "cutout_4.png"));
			final BufferedImage shading_all = loadImage(new File(overlays_dir, "shading_all.png"));
			final BufferedImage cutout_sort = loadImage(new File(overlays_dir, "cutout_sort.png"));
			final BufferedImage shading_face_1 = loadImage(new File(overlays_dir, "shading_face_1.png"));
			final BufferedImage shading_face_2 = loadImage(new File(overlays_dir, "shading_face_2.png"));
			final BufferedImage shading_face_4 = loadImage(new File(overlays_dir, "shading_face_4.png"));
			final BufferedImage shading_trim_1 = loadImage(new File(overlays_dir, "shading_trim_1.png"));
			final BufferedImage shading_trim_h = loadImage(new File(overlays_dir, "shading_trim_h.png"));
			final BufferedImage shading_trim_v = loadImage(new File(overlays_dir, "shading_trim_v.png"));
			final BufferedImage shading_trim_4 = loadImage(new File(overlays_dir, "shading_trim_4.png"));
			
			final BufferedImage base = loadImage(baseImgFile);			
			final BufferedImage trim, face;
			
			Set<File> possibleAnimFiles = new HashSet<>();
			File animFile = new File(baseImgFile + ".mcmeta");
			if(animFile.isFile())
				possibleAnimFiles.add(animFile);
			
			if(trimImgFile == null || ignoreNonexistentFiles && !trimImgFile.isFile()) {
				if(!hideUsingDefaultFileMessage)
					println("Trim image file not specified, creating one from the base image file.");
				int width = getMaxWidth(base, shading_all);
				trim = layer(ensureWidth(copy(base), width), ensureWidth(shading_all, width));
			} else {
				trim = loadImage(trimImgFile);
				animFile = new File(trimImgFile + ".mcmeta");
				if(animFile.isFile())
					possibleAnimFiles.add(animFile);
			}
			if(faceImgFile == null || ignoreNonexistentFiles && !faceImgFile.isFile()) {
				if(!hideUsingDefaultFileMessage)
					println("Face image file not specified, using base image as face image");
				face = copy(base);
			} else {
				face = loadImage(faceImgFile);
				animFile = new File(faceImgFile + ".mcmeta");
				if(animFile.isFile())
					possibleAnimFiles.add(animFile);
			}
			
			int width = getMaxWidth(handle_1,
									handle_2,
									handle_4,
									cutout_1,
									cutout_h,
									cutout_v,
									cutout_4,
									//shading_all,
									cutout_sort,
									shading_face_1,
									shading_face_2,
									shading_face_4,
									shading_trim_1,
									shading_trim_h,
									shading_trim_v,
									shading_trim_4,
									base,
									trim,
									face);
			
			this.handle_1 = ensureWidth(handle_1, width);
			this.handle_2 = ensureWidth(handle_2, width);
			this.handle_4 = ensureWidth(handle_4, width);
			this.cutout_1 = ensureWidth(cutout_1, width);
			this.cutout_h = ensureWidth(cutout_h, width);
			this.cutout_v = ensureWidth(cutout_v, width);
			this.cutout_4 = ensureWidth(cutout_4, width);
			//this.shading_all = ensureWidth(shading_all, width);
			this.cutout_sort = ensureWidth(cutout_sort, width);
			this.shading_face_1 = excludedShadingTypes.contains(ShadingType.face_1)? null : ensureWidth(shading_face_1, width);
			this.shading_face_2 = excludedShadingTypes.contains(ShadingType.face_2)? null : ensureWidth(shading_face_2, width);
			this.shading_face_4 = excludedShadingTypes.contains(ShadingType.face_4)? null : ensureWidth(shading_face_4, width);
			this.shading_trim_1 = excludedShadingTypes.contains(ShadingType.trim_1)? null : ensureWidth(shading_trim_1, width);
			this.shading_trim_h = excludedShadingTypes.contains(ShadingType.trim_h)? null : ensureWidth(shading_trim_h, width);
			this.shading_trim_v = excludedShadingTypes.contains(ShadingType.trim_v)? null : ensureWidth(shading_trim_v, width);
			this.shading_trim_4 = excludedShadingTypes.contains(ShadingType.trim_4)? null : ensureWidth(shading_trim_4, width);
			this.base = ensureWidth(base, width);
			this.trim = ensureWidth(trim, width);
			this.face = (face == base)? this.base : ensureWidth(face, width);
			
			if(possibleAnimFiles.isEmpty()) {
				this.animation = null;
			} else if(possibleAnimFiles.size() == 1) {
				this.animation = loadText(possibleAnimFiles.iterator().next());
			} else {
				System.out.println("Multiple possible animation .mcmeta files were found. Which one do you want to use?");
				File[] files = possibleAnimFiles.toArray(new File[possibleAnimFiles.size()]);
				for(int i = 0; i < files.length; i++) {
					System.out.println("  " + i + ": " + files[i].getName());
				}
				if(keys == null)
					keys = new Scanner(System.in);
				int choice = 0;
				try {
					boolean loop = true;
					do {
						System.out.print("Enter the number corresponding to your desired file: ");
						try {
							choice = Integer.parseUnsignedInt(keys.nextLine());
							if(choice >= files.length)
								error("That is not a valid option. Try again.");
							else loop = false;
						} catch(NumberFormatException e) {
							error("That is not a valid option. Try again.");
						}
					} while(loop);
				} catch(NoSuchElementException e) {
					// this means the user did the ctrl-c quit command
					throw cleanExit();
				}
				this.animation = loadText(files[choice]);
			}
		}
		
		JSONObject materialsJSON;
		
		if(materialsFile == null) {
			
			planks = slabs = Collections.emptySet();
			materialsJSON = new JSONObject();
			
		} else if(autoMaterials && !(materialsFile.exists() && materialsFile.isFile())) {
			
			planks = Collections.singleton(new ResourceLocation(material + "_planks"));
			slabs  = Collections.singleton(new ResourceLocation(material + "_slab"));
			
			materialsJSON = new JSONObject();
			
		} else {
			
			JSONParser jsonParser = new JSONParser();
			try(Reader reader = new FileReader(materialsFile)) {
				
				materialsJSON = (JSONObject)jsonParser.parse(reader);
				
				if(!materialsJSON.containsKey("planks")) {
					error("The materials JSON file is missing the 'planks' key");
					error(" >> File " + getCanonicalPath(materialsFile));
					throw fatalError();
				}
				
				if(!materialsJSON.containsKey("slabs")) {
					error("The materials JSON file is missing the 'slabs' key");
					error(" >> File " + getCanonicalPath(materialsFile));
					throw fatalError();
				}
				
				@SuppressWarnings("unchecked")
				List<String> planksArray = (JSONArray)materialsJSON.get("planks"),
							 slabsArray  = (JSONArray)materialsJSON.get("slabs");
				
				planks = new HashSet<>(planksArray.size());
				for(String str : planksArray) {
					planks.add(new ResourceLocation(str));
				}
				
				slabs = new HashSet<>(slabsArray.size());
				for(String str : slabsArray) {
					slabs.add(new ResourceLocation(str));
				}
				
			} catch(IOException | ClassCastException | ParseException e) {
				e.printStackTrace();
				error("Error reading the materials file");
				error(" >> File " + getCanonicalPath(materialsFile));
				throw fatalError();
			}
			
		}
				
		if(noLang) {
			lang = null;
		} else {
			if(lang_file.exists()) {
				JSONParser jsonParser = new JSONParser();
				
				try(Reader reader = new FileReader(lang_file)) {
					
					lang = (JSONObject)jsonParser.parse(reader);
					
				} catch(IOException | ClassCastException | ParseException e) {
					e.printStackTrace();
					error("error reading the lang file");
					throw fatalError();
				}
				
			} else {
				lang = new JSONObject();
			}
			
			materialName = materialName == null? "" : materialName.trim();
			if(materialName.isEmpty()) {
				if(materialsJSON.containsKey("name")) {
					materialName = (String)materialsJSON.get("name");
					if(!hideUsingDefaultFileMessage)
						println("Material name not specified, using name specified in materials file \"" + materialName + '"');
				} else {
					StringBuilder b = new StringBuilder(material.length());
					char last = '_';
					for(int i = 0; i < material.length(); i++) {
						char c = material.charAt(i);
						if(c == '_') {
							b.append(' ');
						} else if(last == '_') {
							b.append(Character.toUpperCase(c));
						} else {
							b.append(c);
						}
						last = c;
					}
					materialName = b.toString();
					if(!hideUsingDefaultFileMessage)
						println("Material name not specified, using generated name \"" + materialName + '"');
				}
			}
		}
		
		if(noRecipes) {			
			recipe_full_drawers_1 = null;
			recipe_full_drawers_2 = null;
			recipe_full_drawers_4 = null;
			recipe_half_drawers_1 = null;
			recipe_half_drawers_2 = null;
			recipe_half_drawers_4 = null;
			recipe_trim           = null;
		} else {
			recipe_full_drawers_1 = loadText(new File(template_recipes_dir, "full_drawers_1.json"));
			recipe_full_drawers_2 = loadText(new File(template_recipes_dir, "full_drawers_2.json"));
			recipe_full_drawers_4 = loadText(new File(template_recipes_dir, "full_drawers_4.json"));
			recipe_half_drawers_1 = loadText(new File(template_recipes_dir, "half_drawers_1.json"));
			recipe_half_drawers_2 = loadText(new File(template_recipes_dir, "half_drawers_2.json"));
			recipe_half_drawers_4 = loadText(new File(template_recipes_dir, "half_drawers_4.json"));
			recipe_trim = loadText(new File(template_recipes_dir, "trim.json"));
		}
		
		jsonPrettyPrinter = JSONPrettyPrinter.builder().sortKeys(true).build();
		
		println("modid: " + modid);
		println("material id: " + material);
		if(materialName != null) {
			println("material name: " + materialName);
		}
	}
	
	@SuppressWarnings("rawtypes")
	void requireKey(Map map, String key, String filename) {
		if(!map.containsKey(key))
			throw new IllegalArgumentException("missing '" + key + "' key in " + filename);
	}
	
	@ExcludeDebug
	private boolean madeNoImages = true, madeNoModels = true, madeNoBlockstates = true, uneditedLang = true, madeNoRecipes = true, madeNoTags = true;
	
	static final Collection<Field> debugFields;

	void process() {
		if(preview) {
			boolean oldQuiet = quiet;
			quiet = false;
			for(Field field : debugFields) {
				if(field.getName().equals("quiet")) {
					println("quiet = " + oldQuiet);
				} else {
					debugField(field);
				}
			}
			if(!noLang) {
				println("lang:"); indent++;
				for(ModelType shape : ModelType.VALUES) {
					String key = shape.translateKey(modid, material);
					print(key + " = ");
					addTranslation(key, shape.translateValue(materialName));
				}
				if(uneditedLang)
					println("(lang unchanged)");
				indent--; println();
			}
			for(PreviewImageType previewImageType : previewImages) {
				showImage(previewImageType.get(this), previewImageType.name());
			}
		} else if(!previewImages.isEmpty()) {
			for(PreviewImageType previewImageType : previewImages) {
				showImage(previewImageType.get(this), previewImageType.name());
			}
		} else {
			if(!noImages) {
				println("textures:"); indent++;
				img_front_1();
				img_front_2();
				img_front_4();
				img_side();
				img_side_h();
				img_side_v();
				img_sort();
				img_trim();
				if(madeNoImages) println("(none)");
				indent--; println();
			}
			
			if(!noModels) {
				println("models:"); indent++;		
				mdl_full1();
				mdl_full2();
				mdl_full4();
				mdl_half1();
				mdl_half2();
				mdl_half4();
				mdl_trim();
				if(madeNoModels) println("(none)");
				indent--; println();
			}
			
			if(!noBlockstates) {
				println("blockstate:"); indent++;
				blockstates();
				if(madeNoBlockstates) println("(none)");
				indent--; println();
			}
			
			if(!noLang) {
				println("lang:"); indent++;
				lang();
				if(uneditedLang) println("(lang unchanged)");
				indent--; println();
			}
			
			if(!noRecipes) {
				println("recipes:"); indent++;
				recipes();
				if(madeNoRecipes) println("(none)");
				indent--; println();
			}
			
			if(!noTags) {
				println("tags:"); indent++;
				tags();
				if(madeNoTags) println("(none)");
				indent--; println();
			}
		}
		println("Done.");
	}
	
	void img_front_1() {
		if(excludeImageTypes.contains(front_1)) return;
		
		saveImage(make_img_front_1(), front_1);
	}
	
	BufferedImage make_img_front_1() {
		return delete? null : layer(copy(face), 
				cutout(trim, cutout_1),
				shading_trim_1,
				shading_face_1,
				handle_1);
	}	
	
	void img_front_2() {
		if(excludeImageTypes.contains(front_2)) return;
		
		saveImage(make_img_front_2(), front_2);
	}
	
	BufferedImage make_img_front_2() {
		return delete? null : layer(copy(face),
				cutout(trim, cutout_h),
				shading_trim_h,
				shading_face_2,
				handle_2);
	}
	
	void img_front_4() {
		if(excludeImageTypes.contains(front_4)) return;
		
		saveImage(make_img_front_4(), front_4);
	}
	
	BufferedImage make_img_front_4() {
		return delete? null : layer(copy(face),
				cutout(trim, cutout_4),
				shading_trim_4,
				shading_face_4,
				handle_4);
	}
	
	void img_side() {
		if(excludeImageTypes.contains(side)) return;
		
		saveImage(make_img_side(), side);
	}
	
	BufferedImage make_img_side() {
		return delete? null : layer(copy(base),
				cutout(trim, cutout_1),
				shading_trim_1);
	}
	
	void img_side_h() {
		if(excludeImageTypes.contains(side_h)) return;
		
		saveImage(make_img_side_h(), side_h);
	}
	
	BufferedImage make_img_side_h() {
		return delete? null : layer(copy(base),
				cutout(trim, cutout_h),
				shading_trim_h);
	}
	
	void img_side_v() {
		if(excludeImageTypes.contains(side_v)) return;
		
		saveImage(make_img_side_v(), side_v);
	}
	
	BufferedImage make_img_side_v() {
		return delete? null : layer(copy(base),
				cutout(trim, cutout_v),
				shading_trim_v);
	}
	
	void img_sort() {
		if(excludeImageTypes.contains(sort)) return;
		
		saveImage(make_img_sort(), sort);
	}
	
	BufferedImage make_img_sort() {
		return delete? null : layer(copy(base),
				cutout(trim, cutout_sort),
				shading_trim_1);
	}
	
	void img_trim() {
		if(!excludeImageTypes.contains(ImageType.trim))
			saveImage(delete? null : trim, ImageType.trim);
	}
	
	void mdl_full1() {
		if(excludeModelTypes.contains(full1)) return;
		
		println("full1:"); indent++;
		mdl(full1, block_basicdrawers_full1, item_basicdrawers_full1);
		indent--;
	}
	
	void mdl_full2() {
		if(excludeModelTypes.contains(full2)) return;
		
		println("full2:"); indent++;
		mdl(full2, block_basicdrawers_full2, item_basicdrawers_full2);
		indent--;
	}
	
	void mdl_full4() {
		if(excludeModelTypes.contains(full4)) return;
		
		println("full4:"); indent++;
		mdl(full4, block_basicdrawers_full4, item_basicdrawers_full4);
		indent--;
	}
	
	void mdl_half1() {
		if(excludeModelTypes.contains(half1)) return;
		
		println("half1:"); indent++;
		mdl(half1, block_basicdrawers_half1, item_basicdrawers_half1);
		indent--;
	}
	
	void mdl_half2() {
		if(excludeModelTypes.contains(half2)) return;
		
		println("half2:"); indent++;
		mdl(half2, block_basicdrawers_half2, item_basicdrawers_half2);
		indent--;
	}
	
	void mdl_half4() {
		if(excludeModelTypes.contains(half4)) return;
		
		println("half4:"); indent++;
		mdl(half4, block_basicdrawers_half4, item_basicdrawers_half4);
		indent--;
	}
	
	void mdl_trim() {
		if(excludeModelTypes.contains(ModelType.trim)) return;
		
		println("trim: "); indent++;
		mdl(ModelType.trim, block_trim, item_trim);
		indent--;
	}
	
	void blockstates() {
		println("drawers:"); indent++;
		for(ModelType shape : ModelType.DRAWER_TYPES) {
			madeNoBlockstates &= saveFormattedJSON(delete? null : drawer_blockstate_template.replace("${shape}", shape.name()).replace("${model}", formatModidAndMaterial(block_model_filename_template.replace("${shape}", shape.translationKey))), blockstates_dir, blockstate_filename_template.replace("${shape}", shape.translationKey), noReplaceBlockstates, backupBlockstates);
		}
		indent--;
		
		println("trim:"); indent++;
		madeNoBlockstates &= saveFormattedJSON(delete? null : trim_blockstate_template.replace("${model}", formatModidAndMaterial(block_model_filename_template.replace("${shape}", ModelType.trim.translationKey))), blockstates_dir, blockstate_filename_template.replace("${shape}", ModelType.trim.translationKey), noReplaceBlockstates, backupBlockstates);
		indent--;
	}
	
	@SuppressWarnings("unchecked")
	String addTranslation(String key, String value) {
		return (String)lang.compute(key, (k, v) -> {
			if(delete)
				return null;
			if(value.equals(v)) {
				println(v + " (unchanged)");
				return v;
			} else {
				println(value);
				uneditedLang = false;
				return value;
			}
		});
	}
	
	void lang() {
		uneditedLang = true;
		for(ModelType shape : ModelType.VALUES) {
			addTranslation(shape.translateKey(modid, material), shape.translateValue(materialName));
		}
		if(uneditedLang) return;
		
		if(backupLang && lang_file.exists()) {
			backup(lang_file);
		}
		
		try(Writer writer = new FileWriter(lang_file)) {
			
			writer.write(jsonPrettyPrinter.toJSONString(lang));
			
		} catch(IOException e) {
			e.printStackTrace();
			error("Error while writing to lang file");
			error(" >> File " + getCanonicalPath(lang_file));
			throw fatalError();
		}
	}
	
	void recipes() {
		String planksStr = getMaterialString(planks, "planks");
		String slabsStr  = getMaterialString(slabs,  "slabs");
		
		recipe(ModelType.full1, recipe_full_drawers_1, planksStr, slabsStr);
		recipe(ModelType.full2, recipe_full_drawers_2, planksStr, slabsStr);
		recipe(ModelType.full4, recipe_full_drawers_4, planksStr, slabsStr);
		recipe(ModelType.half1, recipe_half_drawers_1, planksStr, slabsStr);
		recipe(ModelType.half2, recipe_half_drawers_2, planksStr, slabsStr);
		recipe(ModelType.half4, recipe_half_drawers_4, planksStr, slabsStr);
		recipe(ModelType.trim, recipe_trim, planksStr, slabsStr);
	}
	
	String getMaterialString(Set<ResourceLocation> materials, String type) {
		println(type + ":"); indent++;
		File tagFile = new File(item_tags_dir, modid + '/' + formatModidAndMaterial(tag_filename_template).replace("${type}", type) + ".json");
		if(delete) {
			if(!tagFile.exists() || !tagFile.isFile()) {
				println("(doesn't exist, skipping)"); indent--;
				return null;
			} else {
				println(tagFile); indent--;
				tagFile.delete();
				return null;
			}
		}
		switch(materials.size()) {
			case 0:
			{				
				if(tagFile.exists() && tagFile.isFile() && !noReplaceRecipes) {
					if(backupRecipes)
						backup(tagFile);
					tagFile.delete();
					madeNoRecipes = false;
				}
				println("(none)"); indent--;
				return null;
			}
//			case 1:
//			{
//				ResourceLocation loc = materials.iterator().next();
//				if(tagFile.exists() && tagFile.isFile() && !noReplaceRecipes) {
//					if(backupRecipes)
//						backup(tagFile);
//					tagFile.delete();
//					madeNoRecipes = false;
//				}
//				if(loc.isTag)
//					println('#' + loc.toString());
//				else
//					println(loc);
//				indent--;
//				return (loc.isTag? "\"tag\": \"" : "\"item\": \"") + loc + '"';
//			}
			default:
			{
				File tagsDir = new File(item_tags_dir, modid);
				if(!tagsDir.exists() || !tagsDir.isDirectory())
					tagsDir.mkdirs();
				StringBuilder textBuilder = new StringBuilder();
				textBuilder.append("{\n\t\"values\": [],\n\t\"optional\": [");
				boolean first = true;
				for(ResourceLocation loc : materials) {
					if(first) first = false;
					else textBuilder.append(',');
					textBuilder.append("\n\t\t\"");
					if(loc.isTag) {
						textBuilder.append('#');
						print('#');
					}
					textBuilder.append(loc).append('"');
					println(loc);
				}
				textBuilder.append("\n\t]\n}");
				saveText(textBuilder.toString(), tagFile, backupRecipes);
				madeNoRecipes = false;
				indent--;
				return "\"tag\": \"" + recipes_dir.getParentFile().getName() + ':' + modid + '/' + material + '_' + type + '"'; 
			}	
		}
	}
	
	void recipe(ModelType type, String template, String planksStr, String slabsStr) {
		if(!delete) {
			if(template.contains("${planks}")) {
				if(planksStr == null) {
					if(!ignoreNonexistentFiles)
						println("Warning: could not create recipe for type " + type + ", no planks were specified in the materials file");
					return;
				} else {
					template = template.replace("${planks}", planksStr);
				}
			}
			if(template.contains("${slabs}")) {
				if(slabsStr == null) {
					if(!ignoreNonexistentFiles)
						println("Warning: could not create recipe for type " + type + ", no slabs were specified in the materials file");
					return;
				} else {
					template = template.replace("${slabs}", slabsStr);
				}
			}
		}
		
		File dir = new File(recipes_dir, modid);
		if(!delete && (!dir.exists() || !dir.isDirectory()))
			dir.mkdirs();
		
		madeNoRecipes &= saveFormattedJSON(delete? null : template, dir, recipe_filename_template.replace("${shape}", type.translationKey), noReplaceRecipes, backupRecipes);
	}
	
	void tags() {
		tag(new File(storagedrawers_tags_dir, "items/drawers.json"));
		tag(new File(storagedrawers_tags_dir, "blocks/drawers.json"));
	}
	
	@SuppressWarnings("unchecked")
	void tag(File file) {
		JSONParser jsonParser = new JSONParser();
		JSONObject tags_obj;
		if(file.exists() && file.isFile()) {
			if(delete) {
				println(file);
				file.delete();
				return;
			}
			try(Reader reader = new FileReader(file)) {
				
				tags_obj = (JSONObject)jsonParser.parse(reader);
				
			} catch(IOException | ClassCastException | ParseException e) {
				e.printStackTrace();
				error("Error reading the tags file");
				error(" >> File " + getCanonicalPath(file));
				throw fatalError();
			}
		} else {
			if(delete) {
				println("(doesn't exist, skipping)");
				return;
			}
			tags_obj = new JSONObject();
			File parent_dir = file.getParentFile();
			if(!parent_dir.exists() || !parent_dir.isDirectory())
				parent_dir.mkdirs();
		}
		
		println(file + ":"); indent++;
		
		tags_obj.put("replace", false);
		
		JSONArray tags_optional = (JSONArray)tags_obj.computeIfAbsent("optional", (Object key) -> new JSONArray());
		boolean added = false;
		
		if(!tags_obj.containsKey("values")) {
			added = true;
			tags_obj.put("values", new JSONArray());
		}
		
		for(ModelType type : ModelType.VALUES) {
			String entry = formatModidAndMaterial(tag_entries_template.replace("${type}", type.translationKey));
			if(!tags_optional.contains(entry)) {
				tags_optional.add(entry);
				madeNoTags = false;
				added = true;
				println(entry);
			}
		}
		
		if(!added) {
			println("(unchanged)");
		}
		
		indent--; println();
		
		if(added) {
			try(Writer writer = new FileWriter(file)) {
				
				writer.write(jsonPrettyPrinter.toJSONString(tags_obj));
				
			} catch(IOException e) {
				e.printStackTrace();
				error("Error while writing to tag file");
				error(" >> File " + getCanonicalPath(file));
				throw fatalError();
			}
		}
	}
	
	void mdl(ModelType type, String blockModel, String itemModel) {
		println("block:"); indent++;
		madeNoModels &= saveFormattedJSON(delete? null : blockModel, block_models_dir, block_model_filename_template.replace("${shape}", type.translationKey).replace("${modid}", modid), noReplaceModels, backupBlockModels);
		indent--;
		
		println("item:"); indent++;
		madeNoModels &= saveFormattedJSON(delete? null : itemModel, item_models_dir, item_model_filename_template.replace("${shape}", type.translationKey).replace("${modid}", modid), noReplaceModels, backupItemModels);
		indent--;
	}
	
	String loadText(File file) {
		if(!file.exists()) {
			error("Error: file not found");
			error(" >> File " + getCanonicalPath(file));
			throw fatalError();
		}
		if(!file.isFile()) {
			error("Error: not a file");
			error(" >> File " + getCanonicalPath(file));
			throw fatalError();
		}
		/*if(!file.getName().toLowerCase().endsWith(".json")) {
			error("Error: not a JSON file");
			error(" >> File " + file);
			System.fatalError();
			return null;
		}*/
		
		try(Scanner scan = new Scanner(file)) {
			scan.useDelimiter("\\A");
			return scan.hasNext()? scan.next() : "";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			error("Error: could not read file");
			error(" >> File " + getCanonicalPath(file));
			throw fatalError();
		}
	}
	
	// returns false if file was edited
	boolean saveFormattedJSON(String text, File output_dir, String type, boolean noReplace, boolean backup) {
		File file = new File(output_dir, type.replace("${modid}", modid).replace("${material}", material) + ".json");
		if(delete) {
			if(!file.exists() || !file.isFile()) {
				println("(doesn't exist, skipped)");
				return true;
			} else {
				println(file);
				if(backup)
					backup(file);
				return file.delete();
			}
		} else if(noReplace && file.exists()) {
			println("(exists, skipped)");
			return true;
		}
		println(file);
		return saveText(formatModidAndMaterial(text), file, backup);
	}
	
	boolean saveText(String text, File file, boolean noReplace, boolean backup) {
		if(delete) {
			if(!file.exists() || !file.isFile()) {
				println("(doesn't exist, skipped)");
				return true;
			} else {
				println(file);
				if(backup)
					backup(file);
				return file.delete();
			}
		} else if(noReplace && file.exists()) {
			println("(exists, skipped)");
			return true;
		}
		println(file);
		return saveText(text, file, backup);
	}
	
	// returns false if file was edited
	boolean saveText(String text, File file, boolean backup) {
		if(delete) {
			if(!file.exists() || !file.isFile()) {
				println("(doesn't exist, skipped)");
				return true;
			} else {
				println(file);
				if(backup)
					backup(file);
				return file.delete();
			}
		}
		try {
			Files.write(file.toPath(), text.getBytes());
			if(backup && file.exists())
				backup(file);
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			error("Error writing to file");
			error(" >> File " + getCanonicalPath(file));
			throw fatalError();
		}
	}
	
	BufferedImage loadImage(File file) {
		if(!file.exists()) {
			error("Error: file not found");
			error(" >> File " + getCanonicalPath(file));
			throw fatalError();
		}
		if(!file.isFile()) {
			error("Error: not a file");
			error(" >> File " + getCanonicalPath(file));
			throw fatalError();
		}
		if(!file.getName().toLowerCase().endsWith(".png")) {
			error("Error: not a PNG file");
			error(" >> File " + getCanonicalPath(file));
			throw fatalError();
		}
		
		BufferedImage result;
		try {
			result = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			error("Error: could not read image");
			error(" >> File " + getCanonicalPath(file));
			throw fatalError();
		}
		
		if(result.getWidth() % 16 != 0) {
			error("Error: image width must be a multiple of 16");
			error(" >> File " + getCanonicalPath(file));
			throw fatalError();
		}
		if(result.getHeight() % result.getWidth() != 0) {
			error("Error: image height must be a multiple of its width");
			error(" >> File " + getCanonicalPath(file));
			throw fatalError();
		}
		
		return result;
	}
	
	void saveImage(BufferedImage img, ImageType type) {
		File file = new File(textures_dir, "drawers_" + material + "_" + type + ".png");
		println(type + ":"); indent++;
		if(!delete) {
			if(noReplaceImageTypes.contains(type) && file.exists()) {
				println("(exists, skipped)"); indent--;
				return;
			}
			if(showImageTypes.contains(type)) {
				showImage(img, type.name());
			}
		}
		try {
			if(backupTextures) {
				backup(file);
			}
			if(delete) {
				if(!file.exists() || !file.isFile()) {
					if(animation != null) {
						file = new File(file + ".mcmeta");
						if(file.exists() && file.isFile()) {
							println(file); indent--;
							file.delete();
							return;
						}
					}
					println("(doesn't exist, skipped)"); indent--;
				} else {
					println(file); indent--;
					file.delete();
				}
			} else {
				println(file); indent--;
				ImageIO.write(img, "png", file);
				madeNoImages = false;
				if(animation != null) {
					file = new File(file + ".mcmeta");
					indent++; println(file); indent--;
					saveText(animation, file, backupTextures);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			error("Error: couldn't write to " + file);
			throw fatalError();
		}
	}
	
	void showImage(Image img, String title) {
		JDialog frame = new JDialog();
		frame.setTitle(title);
		frame.setModalityType(ModalityType.APPLICATION_MODAL);
		if(Math.max(img.getWidth(null), img.getHeight(null)) < 256) {
			int ratio = img.getHeight(null) / img.getWidth(null);
			int height = 256 * ratio;
			img = img.getScaledInstance(256, height, Image.SCALE_REPLICATE);
		}
		frame.getContentPane().setLayout(new BorderLayout());
		JLabel label = new JLabel(new ImageIcon(img));
		frame.getContentPane().add(label, BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}
	
	String formatModidAndMaterial(String input) {
		return input.replace("${material}", material)
				    .replace("${modid}", modid);
	}
	
	BufferedImage ensureWidth(BufferedImage img, int width) {
		if(img.getWidth() != width) {
			int ratio = img.getHeight() / img.getWidth();
			int height = width * ratio;
			Image scaled = img.getScaledInstance(width, height, Image.SCALE_REPLICATE);
			if(scaled instanceof BufferedImage) {
				img = (BufferedImage) scaled;
			} else {
				img = copy(scaled);
			}
		}
		return img;
	}
	
	int getMaxWidth(BufferedImage... images) {
		int max = 0;
		for(BufferedImage img : images) {
			if(img.getWidth() > max) {
				max = img.getWidth();
			}
		}
		return max;
	}
	
	BufferedImage copy(Image input) {
		int width = input.getWidth(null);
		int height = input.getHeight(null);
		BufferedImage img = newBufferedImageOfSize(width, height);
		Graphics2D g = img.createGraphics();
		g.drawImage(input, 0, 0, width, height, null);
		g.dispose();
		return img;
	}
	
	BufferedImage cutout(BufferedImage img, BufferedImage mask) {
		if(img.getWidth() != mask.getWidth())
			throw new AssertionError("image and mask widths do not match!");
		if(mask.getHeight() != mask.getWidth())
			throw new AssertionError("mask is not square!");
		
		final int size = img.getWidth();
		BufferedImage result = copy(img);
		for(int i = 0, numtimes = img.getHeight() / size; i < numtimes; i++) {
			int yOffset = size * i;
			for(int y = yOffset, yFinal = size + yOffset; y < yFinal; y++)
			for(int x = 0; x < size; x++) {
				Color c = new Color(mask.getRGB(x, y % mask.getHeight()), true);
				if(c.equals(Color.WHITE)) {
					result.setRGB(x, y, 0);
				} else if(!c.equals(Color.BLACK)) {
					error("Error: invalid cutout image: cutout images can only contain black and/or white pixels.");
					throw fatalError();
				}
			}
		}
		
		return result;
	}
	
	BufferedImage layer(BufferedImage btm, BufferedImage top) {
		if(btm.getWidth() != top.getWidth())
			throw new AssertionError("image and mask widths do not match!");
		
		if(btm.getHeight() == top.getHeight()) {
			int width = btm.getWidth();
			int height = btm.getHeight();
			for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++) {
				Color c0 = new Color(btm.getRGB(x, y), true);
				Color c1 = new Color(top.getRGB(x, y), true);
				Color c01 = merge(c0, c1);
				btm.setRGB(x, y, c01.getRGB());
			}
		} else if(top.getWidth() == top.getHeight()) {
			assert top.getHeight() == btm.getWidth();
			assert top.getHeight() % btm.getHeight() == 0;
			
			final int size = btm.getWidth();
			final int numtimes = btm.getHeight() / size;
			for(int i = 0; i < numtimes; i++) {
				int yOffset = size * i;
				for(int by = yOffset, ty = 0; ty < size; by++, ty++)
				for(int x = 0; x < size; x++) {
					Color c0 = new Color(btm.getRGB(x, by), true);
					Color c1 = new Color(top.getRGB(x, ty), true);
					Color c01 = merge(c0, c1);
					btm.setRGB(x, by, c01.getRGB());
				}
			}
		} else if(btm.getWidth() == btm.getHeight()) {
			assert btm.getHeight() == top.getWidth();
			assert btm.getHeight() % top.getHeight() == 0;
			
			final int size = btm.getWidth();
			final int numtimes = top.getHeight() / size;
			BufferedImage result = newBufferedImageOfSize(top);
			for(int i = 0; i < numtimes; i++) {
				int yOffset = size * i;
				for(int by = 0, ty = yOffset; by < size; by++, ty++)
				for(int x = 0; x < size; x++) {
					Color c0 = new Color(btm.getRGB(x, by), true);
					Color c1 = new Color(top.getRGB(x, ty), true);
					Color c01 = merge(c0, c1);
					result.setRGB(x, ty, c01.getRGB());
				}
			}
			btm = result;
		}
		
		return btm;
	}
	
	BufferedImage layer(BufferedImage btm, /*@Nullable*/ BufferedImage layer0, /*(@Nullable*/ BufferedImage/*)*/... layers) {
		BufferedImage result = layer(btm, layer0);
		for(BufferedImage layer : layers) {
			if(layer != null)
				result = layer(result, layer);
		}
		return result;
	}
	
	Color merge(Color c1, Color c2) {
		assert c1.getAlpha() == 255;
		switch(c2.getAlpha()) {
			case 0: return c1;
			case 255: return c2;
		}
		
		int tAlpha = c2.getAlpha();
		int bRed = c1.getRed(), tRed = c2.getRed();
		int bGreen = c1.getGreen(), tGreen = c2.getGreen();
		int bBlue = c1.getBlue(), tBlue = c2.getBlue();
		
		int red = (tRed * tAlpha + bRed * (255 - tAlpha)) / 255;
		int green = (tGreen * tAlpha + bGreen * (255 - tAlpha)) / 255;
		int blue = (tBlue * tAlpha + bBlue * (255 - tAlpha)) / 255;
		
		return new Color(red, green, blue);
	}
	
	BufferedImage newBufferedImageOfSize(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}
	
	BufferedImage newBufferedImageOfSize(Image in) {
		return newBufferedImageOfSize(in.getWidth(null), in.getHeight(null));
	}
	
	void backup(File file) {
		backup(file.toPath());
	}
	
	void backup(Path path) {
		try {
			Files.copy(path, path.getParent().resolve(path.getFileName() + ".backup"), StandardCopyOption.REPLACE_EXISTING);
		}
		catch(IOException e) {
			e.printStackTrace();
			error("Error saving backup for file, backup will not be created");
			error(" >> File " + getCanonicalPath(path.toFile()));
		}
	}
	
	String getCanonicalPath(File file) {
		try {
			return file.getCanonicalPath();
		}
		catch(IOException e) {
			return file.getAbsolutePath();
		}
	}
	
	@ExcludeDebug
	private int indent = 0;
	@ExcludeDebug
	private boolean printedLine = false;
	
	void doIndent() {
		if(printedLine) {
			printedLine = false;
			for(int i = 0; i < indent; i++)
				System.out.print("  ");
		}
	}
	
	static {
		class FieldPair {
			int index;
			Field field;
			
			FieldPair(int index, Field field) {
				super();
				this.index = index;
				this.field = field;
			}
		}
		
		Field[] fields = ResourceCreator.class.getDeclaredFields();
		FieldPair[] fieldPairs = new FieldPair[fields.length];
		for(int i = 0; i < fields.length; i++) {
			fieldPairs[i] = new FieldPair(i, fields[i]);
		}
		
		debugFields = Arrays.stream(fieldPairs)
				.filter(fieldPair -> !Modifier.isStatic(fieldPair.field.getModifiers()) && fieldPair.field.getType() != BufferedImage.class
					&& !fieldPair.field.isAnnotationPresent(ExcludeDebug.class))
				.sorted((fieldPair1, fieldPair2) -> {
					int x = sortTypes(fieldPair1.field.getGenericType(), fieldPair2.field.getGenericType());
					if(x == 0)
						return Integer.compare(fieldPair1.index, fieldPair2.index);
					else return x;
				})
				.map(fieldPair -> fieldPair.field)
				.collect(Collectors.toList());
		for(Field field : debugFields) {
			field.setAccessible(true);
		}
	}
	
	static final List<Class<?>> primitive_ordering = Arrays.asList(boolean.class, byte.class, short.class, char.class, int.class, long.class, float.class, double.class); 
	
	static int sortTypes(Type type1, Type type2) {
		if(type1.equals(type2))
			return 0;
		
		type1 = unwrapType(type1);
		type2 = unwrapType(type2);
		
		if(type1 instanceof Class && type2 instanceof Class) {
			Class<?> class1 = (Class<?>)type1,
					 class2 = (Class<?>)type2;
			
			if(class1.isPrimitive() && class2.isPrimitive()) {
				return Integer.compare(primitive_ordering.indexOf(class1), primitive_ordering.indexOf(class2));
			}
			
			if(class1.isArray() == class2.isArray()) {
				if(class1 == String.class)
					return 1;
				else if(class2 == String.class)
					return -1;
				else 
					return class1.getCanonicalName().compareTo(class2.getCanonicalName());
			} else {
				return Boolean.compare(class1.isArray(), class2.isArray());
			}
		}
		
		if(type1 == String.class)
			return 1;
		if(type2 == String.class)
			return -1;
		
		return type1.getTypeName().compareTo(type2.getTypeName());
	}
	
	void debugField(Field field) {
		try {
			debugField(field.getName(), field.get(this));
		}
		catch(IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	void debugField(String name, Object value) {
		String str = name + " = " + debugString(value).trim().replaceAll("\\s+", " ");
		if(!(value instanceof File || value instanceof Path) && str.length() > 80)
			str = str.substring(0, 81) + " ...";
		println(str);
	}
	
	String debugString(Object value) {
		if(value == null)
			return "null";
		if(value.getClass().isArray())
			return debugArrayString(value);
		if(value instanceof Path)
			value = ((Path)value).toFile();
		if(value instanceof File)
			value = getCanonicalPath((File)value);
		return value.toString();
	}
	
	String debugArrayString(Object array) {
		int length = Array.getLength(array);
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for(int i = 0; i < length; i++) {
			if(i > 0) sb.append(", ");
			sb.append(debugString(Array.get(array, i)));
		}
		sb.append(']');
		return sb.toString();
	}
	
	void println(String msg) {
		if(!quiet) {
			doIndent();
			System.out.println(msg);
			printedLine = true;
		}
	}
	
	void println(Object msg) {
		if(!quiet) {
			doIndent();
			if(msg instanceof File)
				System.out.println(out_dir.toPath().relativize(((File)msg).toPath()));
			else
				System.out.println(msg);
			printedLine = true;
		}
	}
	
	void println(File file) {
		if(!quiet) {
			doIndent();
			System.out.println(out_dir.toPath().relativize(file.toPath()));
			printedLine = true;
		}
	}
	
	void println(String message1, String... messages) {
		if(quiet) return;
		doIndent();
		System.out.print(message1);
		for(String message : messages) {
			if(message != null) {
				System.out.print(' ');
				System.out.print(message);
			}
		}
		System.out.println();
		printedLine = true;
	}
	
	void println(Object message1, Object... messages) {
		if(quiet) return;
		
		if(message1 instanceof File)
			System.out.print(out_dir.toPath().relativize(((File)message1).toPath()));
		else
			System.out.print(message1);
		for(Object message : messages) {
			System.out.print(' ');
			if(message instanceof File)
				System.out.print(out_dir.toPath().relativize(((File)message).toPath()));
			else
				System.out.print(message);
		}
		System.out.println();
		printedLine = true;
	}
	
	void print(String msg) {
		if(!quiet) {
			doIndent();
			System.out.print(msg);
		}
	}
	
	void print(Object msg) {
		if(!quiet) {
			doIndent();
			if(msg instanceof File)
				System.out.print(out_dir.toPath().relativize(((File)msg).toPath()));
			else
				System.out.print(msg);
		}
	}
	
	void print(char c) {
		if(!quiet) {
			doIndent();
			System.out.print(c);
		}
	}
	
	void print(File file) {
		if(!quiet) {
			doIndent();
			System.out.print(out_dir.toPath().relativize(file.toPath()));
		}
	}
	
	void print(String message1, String... messages) {
		if(quiet) return;
		
		doIndent();
		System.out.print(message1);
		for(String message : messages) {
			if(message != null) {
				System.out.print(' ');
				System.out.print(message);
			}
		}
	}
	
	void print(Object message1, Object... messages) {
		if(quiet) return;
		
		doIndent();
		if(message1 instanceof File)
			System.out.print(out_dir.toPath().relativize(((File)message1).toPath()));
		else
			System.out.print(message1);
		for(Object message : messages) {
			System.out.print(' ');
			if(message instanceof File)
				System.out.print(out_dir.toPath().relativize(((File)message).toPath()));
			else
				System.out.print(message);
		}
	}
	
	void println() {
		if(!quiet) {
			System.out.println();
			printedLine = true;
		}
	}
	
	void error(String message) {
		System.err.println(message);
	}
	
	AssertionError fatalError() {
		System.exit(1);
		return new AssertionError();
	}
	
	AssertionError cleanExit() {
		System.exit(0);
		return new AssertionError();
	}
}
