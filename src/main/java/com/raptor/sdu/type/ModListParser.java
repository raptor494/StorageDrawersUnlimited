package com.raptor.sdu.type;

import static com.raptor.sdu.StorageDrawersUnlimited.MODID_LOOKUP;
import static java.lang.Character.isWhitespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public final class ModListParser {
	
	private static final Logger logger = LogManager.getLogger(ModListParser.class);

	private static final Matcher IGNORE_LINE = regex("^\\s*(#.*)?$"),
								 MOD_HEADER = regex("^(?<modid>[-.a-z_0-9]+)(\\s*,\\s*(?<modids>[-.a-z_0-9]+(\\s*,\\s*[-.a-z_0-9]+)*))?\\s*:\\s*(!\\s*(?<disabled>[-.a-z_0-9]+(\\s*,\\s*[-.a-z_0-9]+)*)\\s*)?$"),
								 MATERIAL_HEADER = regex("(?<name>[a-z_0-9]+)\\s*(:\\s*(?=\\w)(?<type>normal|stone|metal|grassy|bamboo)?\\s*(\\blight\\s*=\\s*(?<light>[-+]?\\d+))?\\s*)?$"),
								 MATERIAL_REF = regex("@(?<modid>[-.a-z_0-9]+):(?<id>[a-z_0-9]+)\\s*$");
	
	private static Matcher regex(String regex) {
		return Pattern.compile(regex).matcher("");
	}
	
	public static List<Line> readLines(Scanner scan) {
		List<Line> lines = new ArrayList<>(30);
		for(int lineNumber = 1; scan.hasNextLine(); lineNumber++) {
			String line = scan.nextLine();
			if(!IGNORE_LINE.reset(line).matches())
				lines.add(new Line(lineNumber, line));
		}
		return lines;
	}
	
	/**
	 * @param fileName The name of the file the lines were read from, for error messages.
	 * @param lines The list of lines read from the file.
	 * @return The number of mods read from the file
	 * @throws ModListSyntaxException
	 */
	public static int parseModList(String fileName, List<Line> lines) throws ModListSyntaxException {
//		Map<String, Set<String>> modMaterials = new HashMap<>();
		// check basic syntax
		for(ListIterator<Line> iter = lines.listIterator(); iter.hasNext(); ) {
			Line line = iter.next();
			if(!MOD_HEADER.reset(line).matches())
				throw new ModListSyntaxException(fileName, line, "invalid mod header syntax");
			if(!line.indent.isEmpty())
				throw new ModListSyntaxException(fileName, line, "unexpected indent");
			if(!iter.hasNext())
				throw new ModListSyntaxException(fileName, line, "missing material list for mod " + MOD_HEADER.group("modid"));
			line = iter.next();
			if(line.indent.isEmpty())
				throw new ModListSyntaxException(fileName, line, "no indented material list found after header for mod " + MOD_HEADER.group("modid"));
				
//			Set<String> materials = new HashSet<>();
				
			String indent = line.indent;
			for(;;) {
				if(MATERIAL_REF.reset(line).matches()) {
//					Set<String> otherMaterials = modMaterials.get(MATERIAL_REF.group("modid"));
//					if(otherMaterials == null)
//						throw new ModListSyntaxException(fileName, line, "no such mod with id " + MATERIAL_REF.group("modid"));
//					if(!otherMaterials.contains(MATERIAL_REF.group("id")))
//						throw new ModListSyntaxException(fileName, line, "mod " + MATERIAL_REF.group("modid") + " does not define a material named " + MATERIAL_REF.group("id"));
				} else {
					if(!MATERIAL_HEADER.reset(line).matches())
						throw new ModListSyntaxException(fileName, line, "invalid material header syntax");
//					materials.add(MATERIAL_HEADER.group("name"));
				}
				if(iter.hasNext()) {
					line = iter.next();
					if(!line.indent.equals(indent)) {
						line = iter.previous();
						break;
					}
				} else break;
			}
//			modMaterials.put(MOD_HEADER.group("modid"), materials);
		}
		
		Map<String, SupportedMod.Builder> mods = new HashMap<>(/*modMaterials.size()*/);
//		modMaterials.clear();
//		modMaterials = null;
		
		for(ListIterator<Line> iter = lines.listIterator(); iter.hasNext(); ) {
			SupportedMod.Builder modBuilder = SupportedMod.builder();
			Line line = iter.next();
			MOD_HEADER.reset(line).find();
			String modid = MOD_HEADER.group("modid");
			if(mods.containsKey(modid) || MODID_LOOKUP.containsKey(modid) && MODID_LOOKUP.get(modid).stream().anyMatch(mod -> mod.getModID().equals(modid))) {
				throw new ModListSyntaxException(fileName, line, "Duplicate mod list entry '" + modid + "'");
			}
			modBuilder.modid(modid);
			
			String temp = MOD_HEADER.group("modids");
			if(temp != null) {
				for(String alias : temp.split("\\s*,\\s*")) {
					if(mods.containsKey(alias)) {
						throw new ModListSyntaxException(fileName, line, "Duplicate mod list entry '" + alias + "'");
					}
					modBuilder.modid(alias);
				}
			}
			
			temp = MOD_HEADER.group("disabled");
			if(temp != null) {
				for(String alias : temp.split("\\s*,\\s*")) {
					modBuilder.incompatibleMod(alias);
				}
			}
			
			line = iter.next();
			String indent = line.indent;
			for(;;) {
				if(MATERIAL_REF.reset(line).matches()) {
					final String modid2  = MATERIAL_REF.group("modid");
					final String id      = MATERIAL_REF.group("id");
					final Line   theLine = line;
					modBuilder.drawerMaterial(() -> {
						SupportedMod.Builder modBuilder2 = mods.get(modid2);
						if(modBuilder2 == null) {
							Set<SupportedMod> predefinedMods = MODID_LOOKUP.get(modid2);
							if(predefinedMods != null) {
								for(SupportedMod predefinedMod : predefinedMods) {
									DrawerMaterial mat = predefinedMod.getDrawerMaterial(id);
									if(mat != null) {
										return mat;
									}
								}
							}
							throw new ModListSyntaxException(fileName, theLine, "Could not resolve material reference @" + modid2 + ':' + id);
						}
						SupportedMod mod2 = modBuilder2.build();
						DrawerMaterial mat = mod2.getDrawerMaterial(id);
						if(mat == null) {
							throw new ModListSyntaxException(fileName, theLine, "Could not resolve material reference @" + modid2 + ':' + id);
						}
						return mat;
					});
				} else {
					MATERIAL_HEADER.reset(line).find(); // don't need to check since we already did in the previous loop
					DrawerMaterial.Builder material = DrawerMaterial.builder().name(MATERIAL_HEADER.group("name"));
					
					String type = MATERIAL_HEADER.group("type");
					if(type != null) {
						switch(type) {
						case "normal":
							break;
						case "grassy":
							material.soundType(SoundType.PLANT);
							material.material(Material.PLANTS);
							break;
						case "bamboo":
							material.soundType(SoundType.BAMBOO);
							material.material(Material.BAMBOO);
							break;
						case "stone":
							material.soundType(SoundType.STONE);
							material.material(Material.ROCK);
							break;
						case "metal":
							material.soundType(SoundType.METAL);
							material.material(Material.IRON);
							break;
						default:
							throw new AssertionError();
						}
					}
					
					String light = MATERIAL_HEADER.group("light");
					if(light != null) {
						try {
							material.lightLevel(Integer.parseInt(light));
						} catch(NumberFormatException e) {
							throw new ModListSyntaxException(fileName, line, "Invalid light level " + light);
						}
					}
					
					modBuilder.drawerMaterial(material);
				}
				
				if(iter.hasNext()) {
					line = iter.next();
					if(!line.indent.equals(indent)) {
						line = iter.previous();
						break;
					}
				} else break;
			}
			mods.put(modid, modBuilder);
			
			logger.info("Recognized mod " + modid);
		}
		
		mods.values().forEach(SupportedMod.Builder::build);
		return mods.size();
	}
	
	public static class Line implements CharSequence {
		String indent;
		String content;
		int number;
		
		Line(int lineNumber, String line) {
			int i = line.indexOf('#');
			if(i != -1)
				line = line.substring(0, i);
			if(isWhitespace(line.charAt(line.length()-1)))
				line = StringUtils.stripEnd(line, null);
			
			indent = getLeadingWhitespace(line);
			content = line.substring(indent.length());
			number = lineNumber;
		}
		
		@Override
		public int length() {
			return content.length();
		}
		
		@Override
		public char charAt(int index) {
			return content.charAt(index);
		}
		
		@Override
		public CharSequence subSequence(int start, int end) {
			return content.substring(start, end);
		}
		
		@Override
		public String toString() {
			return content;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof Line)) return false;
			Line l = (Line)obj;
			return this.number == l.number && this.indent.equals(l.indent) && this.content.equals(l.content);
		}
		
		private int hash = 0;
		
		@Override
		public int hashCode() {
			return hash == 0? hash = Objects.hash(content, indent, number) : hash;
		}
		
	}
	
	private static String getLeadingWhitespace(String str) {
		StringBuilder b = new StringBuilder(4);
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(isWhitespace(c))
				b.append(c);
			else break;
		}
		return b.toString();
	}	
	
	private ModListParser() {
		throw new UnsupportedOperationException("ModListParser cannot be instantiated!");
	}

}
