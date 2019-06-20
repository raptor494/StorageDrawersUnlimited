package com.raptor.sdu.type;

import static java.lang.Character.isWhitespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.raptor.sdu.SDUnlimited;

import net.minecraftforge.oredict.OreDictionary;

public final class ModListParser {

	private static final Matcher IGNORE_LINE = regex("^\\s*(#.*)?$"),
								 MOD_HEADER = regex("^(?<modid>\\w+)(\\s*,\\s*(?<modids>\\w+(\\s*,\\s*\\w+)*))?\\s*:\\s*(!\\s*(?<disabled>\\w+(\\s*,\\s*\\w+)*)\\s*)?$"),
								 MATERIAL_HEADER = regex("(?<name>\\w+)\\s*:\\s*((?<type>normal|grassy)\\s*)?$"),
								 MATERIAL_REF = regex("@(?<modid>\\w+):(?<id>\\w+)\\s*$"),
								 MATERIAL_DEF = regex("(?<type>planks|slab)\\s+(?<id>[.\\w]+(:[.\\w]+)?)(\\s+(?<meta>\\d+|\\*)?)?$");
	
	private static Matcher regex(String regex) {
		return Pattern.compile(regex).matcher("");
	}
	
	public static void parseModList(String fileName, Scanner scan) {
		ArrayList<Line> lines = new ArrayList<>(30);
		// load lines
		for(int lineNumber = 1; scan.hasNextLine(); lineNumber++) {
			String line = scan.nextLine();
			if(!IGNORE_LINE.reset(line).matches())
				lines.add(new Line(lineNumber, line));
		}
		
		Map<String, Set<String>> modMaterials = new HashMap<>();
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
				
			Set<String> materials = new HashSet<>();
				
			String indent = line.indent;
			for(;;) {
				if(MATERIAL_REF.reset(line).matches()) {
					Set<String> otherMaterials = modMaterials.get(MATERIAL_REF.group("modid"));
					if(otherMaterials == null)
						throw new ModListSyntaxException(fileName, line, "no such mod with id " + MATERIAL_REF.group("modid"));
					if(!otherMaterials.contains(MATERIAL_REF.group("id")))
						throw new ModListSyntaxException(fileName, line, "mod " + MATERIAL_REF.group("modid") + " does not define a material named " + MATERIAL_REF.group("id"));
				} else {
					if(!MATERIAL_HEADER.reset(line).matches())
						throw new ModListSyntaxException(fileName, line, "invalid material header syntax");
					if(!iter.hasNext())
						throw new ModListSyntaxException(fileName, line, "missing plank/slab list after material " + MATERIAL_HEADER.group("name") + " in mod " + MOD_HEADER.group("modid"));
					line = iter.next();
					String indent2 = line.indent;
					if(!indent2.startsWith(indent) || indent2.length() == indent.length())
						throw new ModListSyntaxException(fileName, line, "no indented plank/slab list found after header for material " + MATERIAL_HEADER.group("name") + " in mod " + MOD_HEADER.group("modid"));
					for(;;) {
						if(!MATERIAL_DEF.reset(line).matches())
							throw new ModListSyntaxException(fileName, line, "invalid plank/slab entry for material " + MATERIAL_HEADER.group("name") + " in mod " + MOD_HEADER.group("modid"));
						if(iter.hasNext()) {
							line = iter.next();
							if(!line.indent.equals(indent2)) {
								line = iter.previous();
								break;
							}
						} else break;
					}
					materials.add(MATERIAL_HEADER.group("name"));
				}
				if(iter.hasNext()) {
					line = iter.next();
					if(!line.indent.equals(indent)) {
						line = iter.previous();
						break;
					}
				} else break;
			}
			modMaterials.put(MOD_HEADER.group("modid"), materials);
		}
		modMaterials.clear();
		
		Map<String, Mod> mods = new HashMap<>();
		for(ListIterator<Line> iter = lines.listIterator(); iter.hasNext(); ) {
			Line line = iter.next();
			MOD_HEADER.reset(line).find();
			String modid = MOD_HEADER.group("modid");
			
			String temp = MOD_HEADER.group("modids");
			String[] other_modids = temp == null? new String[0] : temp.split("\\s*,\\s*");
			
			temp = MOD_HEADER.group("disabled");
			String[] disabled_modids = temp == null? new String[0] : temp.split("\\s*,\\s*");
			
			List<DrawerMaterial.Builder> materials = new ArrayList<>();
			
			line = iter.next();
			String indent = line.indent;
			for(;;) {
				if(MATERIAL_REF.reset(line).matches()) {
					Mod mod = mods.get(MATERIAL_REF.group("modid"));
					materials.add(reference(mod, MATERIAL_REF.group("id")));
				} else {
					MATERIAL_HEADER.reset(line).find();
					DrawerMaterial.BuilderImpl material = material(MATERIAL_HEADER.group("name"));
					line = iter.next();
					String indent2 = line.indent;
					for(;;) {
						MATERIAL_DEF.reset(line).find();
						String id = MATERIAL_DEF.group("id");
						temp = MATERIAL_DEF.group("meta");
						int meta = temp == null || temp.equals("*")? OreDictionary.WILDCARD_VALUE : Integer.parseUnsignedInt(temp);
						switch(MATERIAL_DEF.group("type")) {
							case "planks":
								material.planks(id, meta);
								break;
							case "slab":
								material.slab(id, meta);
								break;
							default:
								throw new AssertionError();
						}
						if(iter.hasNext()) {
							line = iter.next();
							if(!line.indent.equals(indent2)) {
								line = iter.previous();
								break;
							}
						} else break;
					}
					String type = MATERIAL_HEADER.group("type");
					if(type != null) {
						switch(type) {
						case "normal":
							break;
						case "grassy":
							material.setGrassy();
							break;
						default:
							throw new AssertionError();
						}
					}
					materials.add(material);
				}
				if(iter.hasNext()) {
					line = iter.next();
					if(!line.indent.equals(indent)) {
						line = iter.previous();
						break;
					}
				} else break;
			}
			mods.put(modid, new Mod(modid, other_modids, disabled_modids, materials.toArray(new DrawerMaterial.Builder[materials.size()])));
			SDUnlimited.logger.info("Recognized mod " + modid);
		}
	}
	
	private static class Line implements CharSequence {
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
	
	public static DrawerMaterial.BuilderImpl material(String name) {
		return new DrawerMaterial.BuilderImpl(name);
	}
	
	public static DrawerMaterial.Builder reference(Mod mod, String name) {
		return new DrawerMaterial.MaterialReference(mod.getMaterial(name));
	}
	
	
	private ModListParser() {
		throw new UnsupportedOperationException("ModListParser cannot be instantiated!");
	}
	
	public static class ModListSyntaxException extends RuntimeException {
		private static final long serialVersionUID = -274200232220169644L;

		public ModListSyntaxException(String fileName, Line line, String message) {
			super("at " + fileName + ":" + line.number + ": " + message + "\n\t" + line);
		}
		
	}

}
