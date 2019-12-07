package com.raptor.sdu.resourcecreator;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.OptionHandlerRegistry.OptionHandlerFactory;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Setter;
import org.kohsuke.args4j.spi.StringOptionHandler;

public class StringOptionHandlerFactory implements OptionHandlerFactory {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public OptionHandler<?> getHandler(CmdLineParser parser, OptionDef o, Setter setter) {
		if(setter.asAnnotatedElement().isAnnotationPresent(RegexValidated.class)) {
			return new RegexValidatedOptionHandler(parser, o, setter);
		} else {
			return new StringOptionHandler(parser, o, setter);
		}
	}

}
