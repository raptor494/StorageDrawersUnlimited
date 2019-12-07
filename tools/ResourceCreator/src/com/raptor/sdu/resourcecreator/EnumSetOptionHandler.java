package com.raptor.sdu.resourcecreator;

import java.util.EnumSet;
import java.util.regex.Pattern;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Messages;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

public class EnumSetOptionHandler<E extends Enum<E>> extends OptionHandler<EnumSet<E>> {
	private final Class<E> enumType;
	
	protected EnumSetOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super EnumSet<E>> setter, Class<E> enumType) {
		super(parser, option, setter);
		this.enumType = enumType;
	}
	
	private static final char[] SEPARATORS = {'+', '|', '&', ','};

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		EnumSet<E> set;
		String s = params.getParameter(0).trim().replace('-', '_');
		
		boolean invert = s.charAt(0) == '!';
		if(invert) {
			s = s.substring(1).trim();
		}
		
		if(s.equals("*")) {

			set = EnumSet.allOf(enumType);
			
		} else if(s.isEmpty()) {
			
			set = EnumSet.noneOf(enumType);
			
		} else {
			
			String[] strs = null;
			for(char c : SEPARATORS) {
				if(s.indexOf(c) >= 0) {
					strs = s.split("\\s*" + Pattern.quote(Character.toString(c)) + "\\s*");
					break;
				}
			}
			
			if(strs == null) {
				strs = s.split("\\s+");
			}
			
			set = EnumSet.noneOf(enumType);
			
			for(String name : strs) {
				E value = null;		
				for(E e : enumType.getEnumConstants()) {
					if(name.equalsIgnoreCase(e.name())) {
						value = e;
						break;
					}
				}
				
				if(value == null) {
					if(option.isArgument()) 
						throw new CmdLineException(owner, Messages.ILLEGAL_OPERAND, option.toString(), s);
					else
						throw new CmdLineException(owner, Messages.ILLEGAL_OPERAND, params.getParameter(-1), s);
				}
				
				set.add(value);
			}
			
		}
		
		if(invert) {
			set = EnumSet.complementOf(set);
		}

		setter.addValue(set);
		
		return 1;
	}
	
	@Override
	public String getDefaultMetaVariable() {
		StringBuilder sb = new StringBuilder();
		sb.append("[!]{");
		boolean first = true;
		for(E t : enumType.getEnumConstants()) {
			if(first) first = false;
			else sb.append(" | ");
			sb.append(t);
		}
		sb.append(" | * }");
		return sb.toString();
	}
	
}
