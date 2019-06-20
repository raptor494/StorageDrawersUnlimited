package com.raptor.storagedrawersplus.resourcecreator;

import static com.raptor.storagedrawersplus.resourcecreator.ResourceCreator.ImageType.front_1;
import static com.raptor.storagedrawersplus.resourcecreator.ResourceCreator.ImageType.front_2;
import static com.raptor.storagedrawersplus.resourcecreator.ResourceCreator.ImageType.front_4;
import static com.raptor.storagedrawersplus.resourcecreator.ResourceCreator.ImageType.side;
import static com.raptor.storagedrawersplus.resourcecreator.ResourceCreator.ImageType.side_h;
import static com.raptor.storagedrawersplus.resourcecreator.ResourceCreator.ImageType.side_v;
import static com.raptor.storagedrawersplus.resourcecreator.ResourceCreator.ImageType.sort;
import static com.raptor.storagedrawersplus.resourcecreator.ResourceCreator.ModelType.full1;
import static com.raptor.storagedrawersplus.resourcecreator.ResourceCreator.ModelType.full2;
import static com.raptor.storagedrawersplus.resourcecreator.ResourceCreator.ModelType.full4;
import static com.raptor.storagedrawersplus.resourcecreator.ResourceCreator.ModelType.half2;
import static com.raptor.storagedrawersplus.resourcecreator.ResourceCreator.ModelType.half4;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class ResourceCreator {
	@Option(name = "-templates", usage = "The directory of the templates", metaVar = "DIR")
	private File templates_dir = new File("templates");
	
	@Option(name = "-help", help = true)
	private boolean help = false;
	
	@Option(name = "-base", required = true)
	private File baseImgFile;
	
	@Option(name = "-trim")
	private File trimImgFile;
	
	@Option(name = "-face")
	private File faceImgFile;
	
	@Option(name = "-name", usage = "Set the name of this material", metaVar = "[<MODID>:]<MATERIAL>")
	private String material = "";
	
	@Option(name = "-modid")
	private String modid = "";
	
	@Option(name = "-out", metaVar = "DIR")
	private File out_dir = new File("output");
	
	@Option(name = "-lang", usage = "The material's localized name", metaVar = "NAME")
	private String materialName = "";
	
	@Option(name = "-q", aliases = "-quiet", usage = "Suppress informative output")
	private boolean quiet = false;
	
	@Option(name = "-i", usage = "If trim image or face image files don't exist, act like they weren't even specified.")
	private boolean ignoreNonexistentImageFiles = false;
	
	@Option(name = "-iq", aliases = "-iquiet", usage = "Don't print warning messages when the trim or face image files don't exist")
	private boolean hideUsingDefaultFileMessage = false;
	
	@Option(name = "-ni", aliases = "-noimages", usage = "Don't create the texture files (note that you still have to specify -base)")
	private boolean noImages = false;
	
	@Option(name = "-nm", aliases = "-nomodels", usage = "Don't create the model files")
	private boolean noModels = false;
	
	@Option(name = "-nl", aliases = "-nolang", usage = "Don't create the lang entry")
	private boolean noLang = false;
	
	@Option(name = "-nri", aliases = "-noreplaceimages", forbids = "-nr", usage = "Don't replace texture files if they exist")
	private boolean noReplaceImages = false;
	
	@Option(name = "-nr", aliases = "-noreplace", metaVar = "TYPE", forbids = "-nri", usage = "Don't replace specific texture files if they exist. This can be specified more than once to exclude more than one texture. TYPE is any of the following: front_1 front_2 front_4 side side_h side_v sort trim")
	private ImageType[] noReplaceImageTypesArray = {};
	
	@Option(name = "-xi", aliases = "-excludeimages", metaVar = "TYPE", usage = "Don't create the given textures. This can be specified more than once to exclude more than one texture. TYPE is any of the following: front_1 front_2 front_4 side side_h side_v sort trim")
	private ImageType[] excludeImageTypesArray = {};
	
	@Option(name = "-nrm", aliases = "-noreplacemodels", usage = "Don't replace model files if they exist")
	private boolean noReplaceModels = false;
	
	@Option(name = "-xm", aliases = "-excludemodels", metaVar = "TYPE", usage = "Don't create the given models. This can be specified more than once to exclude more than one model. TYPE is any of the following: full1 full2 full4 half2 half4 trim")
	private ModelType[] excludeModelTypesArray = {};
	
	@Option(name = "-nrl", aliases = "-noreplacelang", usage = "Don't replace language entries if they exist")
	private boolean noReplaceLang = false;
	
	@Option(name = "-nrb", aliases = "-noreplaceblockstates", usage = "Don't replace blockstate files if they exist")
	private boolean noReplaceBlockstates = false;
	
	private final EnumSet<ImageType> noReplaceImageTypes, excludeImageTypes;
	private final EnumSet<ModelType> excludeModelTypes;
	
	private final File overlays_dir, template_models_dir, template_blockstates_dir, template_lang_file;
	private final File textures_dir, block_models_dir, item_models_dir, blockstates_dir;
	
	private final BufferedImage base, trim, face;
	private final BufferedImage handle_1, handle_2, handle_4;
	private final BufferedImage cutout_1, cutout_h, cutout_v, cutout_4;
	private final BufferedImage /*shading_all,*/ cutout_sort;
	private final BufferedImage shading_face_1, shading_face_2, shading_face_4;
	private final BufferedImage shading_trim_1, shading_trim_h, shading_trim_v, shading_trim_4;
	
	// the blockstate templates
	private final String drawer_blockstate_template, trim_blockstate_template;
	
	// the model templates
	private final String block_basicdrawers_full1, block_basicdrawers_full2, block_basicdrawers_full4, block_basicdrawers_half2, block_basicdrawers_half4;
	private final String block_trim;
	private final String item_basicdrawers_full1, item_basicdrawers_full2, item_basicdrawers_full4, item_basicdrawers_half2, item_basicdrawers_half4;
	private final String item_trim;
	
	// base animation file contents
	private final String animation;
	
	// the lang file stuff
	private final File lang_file;
	private final Map<String, String> lang_format;
	private final Map<String, String> lang;
	
	enum ImageType {
		front_1, front_2, front_4, side, side_h, side_v, sort, trim
	}
	
	enum ModelType {
		full1, full2, full4, half2, half4, trim(false);
		
		public final String fileName;
		
		ModelType() {
			this(true);
		}
		
		ModelType(boolean prefix) {
			fileName = prefix? "basicdrawers_" + name() : name();
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
		template_lang_file = new File(templates_dir, "lang/en_us.lang");
		
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
		
		noReplaceImageTypes = noReplaceImages
				? EnumSet.allOf(ImageType.class)
				: makeEnumSetFrom(noReplaceImageTypesArray);
		
		excludeImageTypes = makeEnumSetFrom(excludeImageTypesArray);		
		
		excludeModelTypes = makeEnumSetFrom(excludeModelTypesArray);
		
		if(out_dir.exists()) {
			if(!out_dir.isDirectory()) {
				error("Error: '-out' parameter must be a file");
				throw fatalError();
			}
		} else {
			out_dir.mkdirs();
		}
		
		Scanner keys = null;
		
		if(material.isEmpty()) {
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
			if(!material.matches("\\w+:\\w+")) {
				error("Invalid material name: " + material);
				error("Material names can only contain letters, numbers, and underscores. They must have a mod id's namepsace prefixed to them if -modid option isn't specified.");
				throw fatalError();
			}
			
			int i = material.indexOf(':');
			modid = material.substring(0, i);
			material = material.substring(i+1);
			
		} else {
			if(!modid.matches("\\w+")) {
				error("Invalid Mod ID: " + modid);
				error("Mod IDs can only contain letters, numbers, and underscores.");
				throw fatalError();
			}
			if(!material.matches("\\w+")) {
				error("Invalid material: " + modid);
				error("Material names can only contain letters, numbers, and underscores.");
				throw fatalError();
			}
		}
		
		textures_dir = new File(out_dir, "textures/blocks/" + modid);
		block_models_dir = new File(out_dir, "models/block/" + modid);
		item_models_dir = new File(out_dir, "models/item/");
		blockstates_dir = new File(out_dir, "blockstates/");
		lang_file = new File(out_dir, "lang/en_us.lang");
		if(!lang_file.getParentFile().exists()) {
			lang_file.getParentFile().mkdirs();
		}
		
		if(!blockstates_dir.exists())
			blockstates_dir.mkdirs();
		if(!textures_dir.exists())
			textures_dir.mkdirs();
		if(!block_models_dir.exists())
			block_models_dir.mkdirs();
		if(!item_models_dir.exists())
			item_models_dir.mkdirs();
		
		if(noModels) {
			drawer_blockstate_template = null;
			trim_blockstate_template = null;
			
			block_basicdrawers_full1 = null;
			block_basicdrawers_full2 = null;
			block_basicdrawers_full4 = null;
			block_basicdrawers_half2 = null;
			block_basicdrawers_half4 = null;
			block_trim = null;
			
			item_basicdrawers_full1 = null;
			item_basicdrawers_full2 = null;
			item_basicdrawers_full4 = null;
			item_basicdrawers_half2 = null;
			item_basicdrawers_half4 = null;
			item_trim = null;			
		} else {
			drawer_blockstate_template = loadText(new File(template_blockstates_dir, "basicdrawers.json"));
			trim_blockstate_template = loadText(new File(template_blockstates_dir, "trim.json"));
			
			block_basicdrawers_full1 = loadText(new File(template_models_dir, "block/basicdrawers_full1.json"));
			block_basicdrawers_full2 = loadText(new File(template_models_dir, "block/basicdrawers_full2.json"));
			block_basicdrawers_full4 = loadText(new File(template_models_dir, "block/basicdrawers_full4.json"));
			block_basicdrawers_half2 = loadText(new File(template_models_dir, "block/basicdrawers_half2.json"));
			block_basicdrawers_half4 = loadText(new File(template_models_dir, "block/basicdrawers_half4.json"));
			block_trim = loadText(new File(template_models_dir, "block/trim.json"));
			
			item_basicdrawers_full1 = loadText(new File(template_models_dir, "item/basicdrawers_full1.json"));
			item_basicdrawers_full2 = loadText(new File(template_models_dir, "item/basicdrawers_full2.json"));
			item_basicdrawers_full4 = loadText(new File(template_models_dir, "item/basicdrawers_full4.json"));
			item_basicdrawers_half2 = loadText(new File(template_models_dir, "item/basicdrawers_half2.json"));
			item_basicdrawers_half4 = loadText(new File(template_models_dir, "item/basicdrawers_half4.json"));
			item_trim = loadText(new File(template_models_dir, "item/trim.json"));
		}
		
		if(noLang) {
			lang_format = null;
		} else {
			lang_format = new HashMap<>();
			loadLang(lang_format, loadText(template_lang_file));
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
			BufferedImage handle_1 = loadImage(new File(overlays_dir, "handle_1.png"));
			BufferedImage handle_2 = loadImage(new File(overlays_dir, "handle_2.png"));
			BufferedImage handle_4 = loadImage(new File(overlays_dir, "handle_4.png"));
			BufferedImage cutout_1 = loadImage(new File(overlays_dir, "cutout_1.png"));
			BufferedImage cutout_h = loadImage(new File(overlays_dir, "cutout_h.png"));
			BufferedImage cutout_v = loadImage(new File(overlays_dir, "cutout_v.png"));
			BufferedImage cutout_4 = loadImage(new File(overlays_dir, "cutout_4.png"));
			BufferedImage shading_all = loadImage(new File(overlays_dir, "shading_all.png"));
			BufferedImage cutout_sort = loadImage(new File(overlays_dir, "cutout_sort.png"));
			BufferedImage shading_face_1 = loadImage(new File(overlays_dir, "shading_face_1.png"));
			BufferedImage shading_face_2 = loadImage(new File(overlays_dir, "shading_face_2.png"));
			BufferedImage shading_face_4 = loadImage(new File(overlays_dir, "shading_face_4.png"));
			BufferedImage shading_trim_1 = loadImage(new File(overlays_dir, "shading_trim_1.png"));
			BufferedImage shading_trim_h = loadImage(new File(overlays_dir, "shading_trim_h.png"));
			BufferedImage shading_trim_v = loadImage(new File(overlays_dir, "shading_trim_v.png"));
			BufferedImage shading_trim_4 = loadImage(new File(overlays_dir, "shading_trim_4.png"));
			
			BufferedImage base = loadImage(baseImgFile);
			BufferedImage trim, face;
			
			Set<File> possibleAnimFiles = new HashSet<>();
			File animFile = new File(baseImgFile + ".mcmeta");
			if(animFile.isFile())
				possibleAnimFiles.add(animFile);
			
			if(trimImgFile == null || ignoreNonexistentImageFiles && !trimImgFile.isFile()) {
				if(!hideUsingDefaultFileMessage)
					println("Trim image file not specified, creating one from the base image file.");
				int width = getMaxWidth(base, shading_all);
				trim = layer(ensureWidth(base, width), ensureWidth(shading_all, width));
			} else {
				trim = loadImage(trimImgFile);
				animFile = new File(trimImgFile + ".mcmeta");
				if(animFile.isFile())
					possibleAnimFiles.add(animFile);
			}
			if(faceImgFile == null || ignoreNonexistentImageFiles && !faceImgFile.isFile()) {
				if(!hideUsingDefaultFileMessage)
					println("Face image file not specified, using base image as face image");
				face = base;
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
			this.shading_face_1 = ensureWidth(shading_face_1, width);
			this.shading_face_2 = ensureWidth(shading_face_2, width);
			this.shading_face_4 = ensureWidth(shading_face_4, width);
			this.shading_trim_1 = ensureWidth(shading_trim_1, width);
			this.shading_trim_h = ensureWidth(shading_trim_h, width);
			this.shading_trim_v = ensureWidth(shading_trim_v, width);
			this.shading_trim_4 = ensureWidth(shading_trim_4, width);
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
				
		if(noLang) {
			lang = null;
		} else {
			lang = new HashMap<>();
			if(lang_file.exists()) {
				loadLang(lang, loadText(lang_file));
			}
			
			materialName = materialName.trim();
			if(materialName.isEmpty()) {
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
	
	@SuppressWarnings("unchecked")
	private static <E extends Enum<E>> EnumSet<E> makeEnumSetFrom(E[] arrayIn) {
		switch(arrayIn.length) {
			case 0:
				return EnumSet.noneOf((Class<E>)arrayIn.getClass().getComponentType());
			case 1:
				return EnumSet.of(arrayIn[0]);
			case 2:
				return EnumSet.of(arrayIn[0], arrayIn[1]);
			case 3:
				return EnumSet.of(arrayIn[0], arrayIn[1], arrayIn[2]);
			case 4:
				return EnumSet.of(arrayIn[0], arrayIn[1], arrayIn[2], arrayIn[3]);
			case 5:
				return EnumSet.of(arrayIn[0], arrayIn[1], arrayIn[2], arrayIn[3], arrayIn[4]);
			default:
				E[] array = (E[])Array.newInstance(arrayIn.getClass().getComponentType(), arrayIn.length-1);
				System.arraycopy(arrayIn, 1, array, 0, array.length);
				return EnumSet.of(arrayIn[0], array);
		}
	}
	
	private void loadLang(Map<String, String> lang, String loadText) {
		try(Scanner scan = new Scanner(loadText)) {
			scan.useDelimiter("\\r?\\n");
			int lineNumber = 1;
			while(scan.hasNext()) {
				String line = scan.next().trim();
				if(!line.isEmpty() && line.charAt(0) != '#') {
					int i = line.indexOf('=');
					if(i < 0){
						error("Invalid lang file: line number " + lineNumber + " has no '=': " + line);
						throw fatalError();
					}
					String key = line.substring(0, i);
					String value = line.substring(i+1);
					lang.put(key, value);
				}
				lineNumber++;
			}
		}
	}
	
	private void saveLang(Map<String, String> lang, File file) throws IOException {
		Files.write(file.toPath(), lang.entrySet().stream()
				.sorted((entry1, entry2) -> {
					String key1 = entry1.getKey();
					String key2 = entry2.getKey();
					if(key1.startsWith("storagedrawers.material.")) {
						if(key2.startsWith("storagedrawers.material."))
							return key1.compareTo(key2);
						else
							return 1;
					} else if(key2.startsWith("storagedrawers.material.")) {
						return -1;
					} else {
						return key1.compareTo(key2);
					}
				})
				.map(Object::toString)
				.collect(Collectors.toList()));
	}
	
	private boolean madeNoImages = true, madeNoModels = true, madeNoBlockstates = true, uneditedLang = true;

	void process() {
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
			mdl_half2();
			mdl_half4();
			mdl_trim();
			if(madeNoModels) println("(none)");
			indent--; println();
			
			println("blockstate:"); indent++;
			blockstates();
			if(madeNoBlockstates) println("(none)");
			indent--; println();
		}
		
		if(!noLang) {
			println("lang:"); indent++;
			lang();
			if(uneditedLang) println("(unchanged)");
			indent--; println();
		}
		
		println("Done.");
	}
	
	void img_front_1() {
		if(excludeImageTypes.contains(front_1)) return;
		
		BufferedImage img = copy(face);
		
		img = layer(img, 
				cutout(trim, cutout_1),
				shading_trim_1,
				shading_face_1,
				handle_1);
		
		saveImage(img, front_1);
	}
	
	
	void img_front_2() {
		if(excludeImageTypes.contains(front_2)) return;
		
		BufferedImage img = copy(face);
		
		img = layer(img,
				cutout(trim, cutout_h),
				shading_trim_h,
				shading_face_2,
				handle_2);
		
		saveImage(img, front_2);
	}
	
	void img_front_4() {
		if(excludeImageTypes.contains(front_4)) return;
		
		BufferedImage img = copy(face);
		
		img = layer(img,
				cutout(trim, cutout_4),
				shading_trim_4,
				shading_face_4,
				handle_4);
		
		saveImage(img, front_4);
	}
	
	void img_side() {
		if(excludeImageTypes.contains(side)) return;
		
		BufferedImage img = copy(base);
		
		img = layer(img,
				cutout(trim, cutout_1),
				shading_trim_1);
		
		saveImage(img, side);
	}
	
	void img_side_h() {
		if(excludeImageTypes.contains(side_h)) return;
		
		BufferedImage img = copy(base);
		
		img = layer(img,
				cutout(trim, cutout_h),
				shading_trim_h);
				
		saveImage(img, side_h);
	}
	
	void img_side_v() {
		if(excludeImageTypes.contains(side_v)) return;
		
		BufferedImage img = copy(base);
		
		img = layer(img,
				cutout(trim, cutout_v),
				shading_trim_v);
		
		saveImage(img, side_v);
	}
	
	void img_sort() {
		if(excludeImageTypes.contains(sort)) return;
		
		BufferedImage img = copy(base);
		
		img = layer(img,
				cutout(trim, cutout_sort),
				shading_trim_1);
		
		saveImage(img, sort);
	}
	
	void img_trim() {
		if(!excludeImageTypes.contains(ImageType.trim))
			saveImage(trim, ImageType.trim);
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
		madeNoBlockstates &= saveFormattedText(drawer_blockstate_template, blockstates_dir, "basicdrawers_" + modid, noReplaceBlockstates);
		indent--;
		
		println("trim:"); indent++;
		madeNoBlockstates &= saveFormattedText(trim_blockstate_template, blockstates_dir, "trim_" + modid, noReplaceBlockstates);
		indent--;
	}
	
	void lang() {
		for(Entry<String, String> entry : lang_format.entrySet()) {
			String key = entry.getKey().replace("${modid}", modid)
					.replace("${material}", material);
			String value = entry.getValue().replace("${MaterialName}", materialName);
			if(noReplaceLang) {
				if(lang.putIfAbsent(key, value) == null)
					println(key,"=",lang.get(key));
			} else if(!value.equals(lang.put(key, value))) {
				uneditedLang = false;
				println(key,"=",value);
			}	
		}
		if(uneditedLang) return;
		try {
			if(!lang_file.exists())
				lang_file.createNewFile();
			saveLang(lang, lang_file);
		} catch(IOException e) {
			e.printStackTrace();
			error("Error while writing to lang file " + lang_file);
			throw fatalError();
		}
	}
	
	void mdl(ModelType type, String blockModel, String itemModel) {
		println("block:"); indent++;
		madeNoModels &= saveFormattedText(blockModel, block_models_dir, type.fileName, noReplaceModels);
		indent--;
		
		println("item:"); indent++;
		madeNoModels &= saveFormattedText(itemModel, item_models_dir, type.fileName + "_" + modid, noReplaceModels);
		indent--;
	}
	
	String loadText(File file) {
		if(!file.exists()) {
			error("Error: file not found");
			error(" >> File " + file);
			throw fatalError();
		}
		if(!file.isFile()) {
			error("Error: not a file");
			error(" >> File " + file);
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
			error(" >> File " + file);
			throw fatalError();
		}
	}
	
	// returns false if file was edited
	boolean saveFormattedText(String text, File output_dir, String type, boolean noReplace) {
		return saveText(formatText(text), output_dir, type, noReplace);
	}
	
	// returns false if file was edited
	boolean saveText(String text, File output_dir, String type, boolean noReplace) {
		File file = new File(output_dir, type + "_" + material + ".json");
		if(noReplace && file.exists()) {
			println("(exists, skipped)");
			return true;
		}
		println(file);
		return saveText(text, file);
	}
	
	boolean saveText(String text, File file) {
		try {
			Files.write(file.toPath(), text.getBytes());
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			error("Error writing to file");
			error(" >> File " + file);
			throw fatalError();
		}
	}
	
	BufferedImage loadImage(File file) {
		if(!file.exists()) {
			error("Error: file not found");
			error(" >> File " + file);
			throw fatalError();
		}
		if(!file.isFile()) {
			error("Error: not a file");
			error(" >> File " + file);
			throw fatalError();
		}
		if(!file.getName().toLowerCase().endsWith(".png")) {
			error("Error: not a PNG file");
			error(" >> File " + file);
			throw fatalError();
		}
		
		BufferedImage result;
		try {
			result = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			error("Error: could not read image");
			error(" >> File " + file);
			throw fatalError();
		}
		
		if(result.getWidth() % 16 != 0) {
			error("Error: image width must be a multiple of 16");
			error(" >> File " + file);
			throw fatalError();
		}
		if(result.getHeight() % result.getWidth() != 0) {
			error("Error: image height must be a multiple of its width");
			error(" >> File " + file);
			throw fatalError();
		}
		
		return result;
	}
	
	void saveImage(BufferedImage img, ImageType type) {
		File file = new File(textures_dir, "drawers_" + material + "_" + type + ".png");
		println(type + ":"); indent++;
		if(noReplaceImageTypes.contains(type) && file.exists()) {
			println("(exists, skipped)"); indent--;
			return;
		}
		println(file); indent--;
		try {
			ImageIO.write(img, "png", file);
			madeNoImages = false;
			if(animation != null) {
				file = new File(file + ".mcmeta");
				indent++; println(file); indent--;
				saveText(animation, file);
			}
		} catch (IOException e) {
			e.printStackTrace();
			error("Error: couldn't write to " + file);
			throw fatalError();
		}
	}
	
	void showImage(Image img) {
		JFrame frame = new JFrame();
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
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	String formatText(String input) {
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
	
	BufferedImage layer(BufferedImage btm, BufferedImage layer0, BufferedImage... layers) {
		BufferedImage result = layer(btm, layer0);
		for(BufferedImage layer : layers) {
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
	
	private int indent = 0;
	private boolean printedLine = false;
	
	void doIndent() {
		if(printedLine) {
			printedLine = false;
			for(int i = 0; i < indent; i++)
				System.out.print("  ");
		}
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
