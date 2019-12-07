package com.raptor.sdu.resourcecreator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONValue;

@SuppressWarnings({ "unchecked", "rawtypes" })
public final class JSONPrettyPrinter {
	private final int maxSimpleLineLength;
	private final String indentStr;
	private final int indentAmount;
	private final String newline;
	private final boolean sortKeys;
//	private final boolean spaceBeforeColon, spaceAfterColon, spaceBeforeComma, spaceAfterComma,
//			spaceAfterOpeningBracket, spaceBeforeClosingBracket;

	private int indent = 0;
	
	public JSONPrettyPrinter(String indentStr, int indentAmount, String newline, int maxSimpleLineLength, boolean sortKeys) {
		this.indentStr = indentStr;
		this.indentAmount = indentAmount;
		this.newline = newline;
		this.maxSimpleLineLength = maxSimpleLineLength;
		this.sortKeys = sortKeys;
	}
	
	public String toJSONString(Object obj) {
		if(obj == null)
			return "null";
		if(obj instanceof Map)
			return toJSONString((Map)obj);
		if(obj instanceof CharSequence || obj instanceof Character)
			return '"' + JSONValue.escape(obj.toString()) + '"';
		if(obj instanceof Double) {
			Double d = (Double)obj;
			if(d.isInfinite() || d.isNaN())
				return "null";
			else
				return d.toString();
		}
		if(obj instanceof Float) {
			Float f = (Float)obj;
			if(f.isInfinite() || f.isNaN())
				return "null";
			else
				return f.toString();
		}
		return obj.toString();
	}
	
	public String toJSONString(CharSequence str) {
		if(str == null)
			return "null";
		else
			return '"' + JSONValue.escape(str.toString()) + '"';
	}

	public String toJSONString(List list) {
		if(list == null)
			return "null";
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		if(!list.isEmpty()) {
			List<String> strs = simpleList(list);
			boolean first = true;
			if(strs == null) {
				indent++;
				for(Object obj : list) {
					if(first) first = false;
					else sb.append(',');
					appendNewline(sb);
					sb.append(toJSONString(obj));
				}
				indent--;
				appendNewline(sb);
			} else {
				sb.append(' ');
				for(String str : strs) {
					if(first) first = false;
					else sb.append(", ");
					sb.append(str);
				}
				sb.append(' ');
			}
		}
		sb.append(']');
		return sb.toString();
	}
	
	private List<String> simpleList(List list) {
		if(list.isEmpty())
			return Collections.emptyList();
		for(Object obj : list) {
			if(!(obj instanceof CharSequence || obj instanceof Number || obj instanceof Boolean || obj instanceof Character))
				return null;
		}
		List<String> strs = new ArrayList<>(list.size());
		int length = 4;
		for(Object obj : list) {
			if(length > 2) {
				length += 2;
			}
			String str;
			if(obj instanceof CharSequence || obj instanceof Character) {
				str = '"' + JSONValue.escape(obj.toString()) + '"';
			} else {
				str = obj.toString();
			}
			length += str.length();
			if(length > maxSimpleLineLength) {
				return null;
			}
			strs.add(str);
		}
		return strs;
	}
	
	public String toJSONString(Map map) {
		if(map == null)
			return "null";
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		if(!map.isEmpty()) {
			indent++;
			boolean first = true;
			Iterator<Map.Entry> iterator;
			
			if(sortKeys) {
				iterator = ((Set<Map.Entry>)map.entrySet()).stream()
							.sorted((entry1, entry2) -> ((Comparable)entry1.getKey()).compareTo(entry2.getKey()))
							.iterator();
			} else {
				iterator = ((Set<Map.Entry>)map.entrySet()).iterator();
			}
			
			while(iterator.hasNext()) {
				Map.Entry entry = iterator.next();
				if(first) first = false;
				else sb.append(',');
				appendNewline(sb);
				appendKeyValue(entry.getKey(), entry.getValue(), sb);
			}
			indent--;
			appendNewline(sb);
		}
		sb.append('}');
		return sb.toString();
	}
	
	private void appendNewline(StringBuilder sb) {
		sb.ensureCapacity(sb.length() + newline.length() + indent*indentStr.length());
		sb.append(newline);
		for(int i = 0; i < indent; i++) {
			sb.append(indentStr);
		}
	}
	
	private void appendKeyValue(Object key, Object value, StringBuilder sb) {
		sb.append('"').append(key == null? "null" : JSONValue.escape(key.toString())).append('"');
		sb.append(':').append(' ');
		sb.append(toJSONString(value));
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private boolean useSpacesInsteadOfTabs;
		private int indentAmount;
		private boolean useWindowsNewline;
		private int maxSimpleLineLength = 25;
		private boolean sortKeys;
//		private boolean spaceBeforeColon = false, spaceAfterColon = true, spaceBeforeComma = false,
//				spaceAfterComma = true, spaceAfterOpeningBracket = true, spaceBeforeClosingBracket = true;

		public Builder useSpacesInsteadOfTabs(boolean useSpacesInsteadOfTabs) {
			this.useSpacesInsteadOfTabs = useSpacesInsteadOfTabs;
			return this;
		}

		public Builder indentAmount(int indentAmount) {
			if(indentAmount < 0)
				throw new IllegalArgumentException("indentAmount must be >= 0");
			this.indentAmount = indentAmount;
			return this;
		}
		
		public Builder useWindowsNewline(boolean useWindowsNewline) {
			this.useWindowsNewline = useWindowsNewline;
			return this;
		}
		
		public Builder maxSimpleLineLength(int maxSimpleLineLength) {
			if(maxSimpleLineLength < 0)
				throw new IllegalArgumentException("maxSimpleLineLength must be >= 0");
			this.maxSimpleLineLength = maxSimpleLineLength;
			return this;
		}
		
		public Builder sortKeys(boolean sortKeys) {
			this.sortKeys = sortKeys;
			return this;
		}

//		public Builder spaceBeforeColon(boolean spaceBeforeColon) {
//			this.spaceBeforeColon = spaceBeforeColon;
//			return this;
//		}
//
//		public Builder spaceAfterColon(boolean spaceAfterColon) {
//			this.spaceAfterColon = spaceAfterColon;
//			return this;
//		}
//
//		public Builder spaceBeforeComma(boolean spaceBeforeComma) {
//			this.spaceBeforeComma = spaceBeforeComma;
//			return this;
//		}
//
//		public Builder spaceAfterComma(boolean spaceAfterComma) {
//			this.spaceAfterComma = spaceAfterComma;
//			return this;
//		}
//
//		public Builder spaceAfterOpeningBracket(boolean spaceAfterOpeningBracket) {
//			this.spaceAfterOpeningBracket = spaceAfterOpeningBracket;
//			return this;
//		}
//
//		public Builder spaceBeforeClosingBracket(boolean spaceBeforeClosingBracket) {
//			this.spaceBeforeClosingBracket = spaceBeforeClosingBracket;
//			return this;
//		}

		public JSONPrettyPrinter build() {
			return new JSONPrettyPrinter(useSpacesInsteadOfTabs? " " : "\t", indentAmount, useWindowsNewline? "\r\n" : "\n", maxSimpleLineLength, sortKeys);
		}

	}

}
