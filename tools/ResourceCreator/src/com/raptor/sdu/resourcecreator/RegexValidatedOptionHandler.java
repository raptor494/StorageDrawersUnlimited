package com.raptor.sdu.resourcecreator;

import java.lang.reflect.AnnotatedElement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Messages;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

public class RegexValidatedOptionHandler extends OptionHandler<String> {
	private final Matcher regex;
	private final boolean global;
	
	public RegexValidatedOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super String> setter) {
		super(parser, option, setter);
		AnnotatedElement element = setter.asAnnotatedElement();
		if(element.isAnnotationPresent(RegexValidated.class)) {
			RegexValidated regexValidated = element.getAnnotation(RegexValidated.class);
			this.regex = Pattern.compile(regexValidated.value()).matcher("");
			this.global = regexValidated.global();
		} else {
			throw new IllegalArgumentException("RegexValidatedOptionHandler's element must have the @RegexValidated annotation");
		}
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		String argument = params.getParameter(0);
		if(global? regex.reset(argument).matches() : regex.reset(argument).find()) {
			setter.addValue(argument);
			return 1;
		} else {
			throw new CmdLineException(owner, Messages.ILLEGAL_OPERAND, params.getParameter(-1), argument);
		}
	}

	@Override
	public String getDefaultMetaVariable() {
		return Messages.DEFAULT_META_STRING_OPTION_HANDLER.format();
	}

}
