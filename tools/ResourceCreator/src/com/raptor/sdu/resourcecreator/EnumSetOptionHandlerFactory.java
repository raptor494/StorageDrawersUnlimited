package com.raptor.sdu.resourcecreator;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.EnumSet;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.OptionHandlerRegistry.OptionHandlerFactory;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Setter;

public class EnumSetOptionHandlerFactory implements OptionHandlerFactory {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public OptionHandler<?> getHandler(CmdLineParser parser, OptionDef o, Setter setter) {
		if(setter.asFieldSetter() == null)
			throw new IllegalArgumentException("EnumSetOptionHandler can only be used with fields");
		Field f = (Field)setter.asAnnotatedElement();
		Type type = unwrapType(f.getGenericType());
		if(type instanceof Class)
			throw new IllegalArgumentException("No type arguments were given to the EnumSet field " + f);
		if(!(type instanceof ParameterizedType))
			throw new IllegalArgumentException("EnumSetOptionHandler cannot be applied to a field of type " + type.getTypeName());
		ParameterizedType ptype = (ParameterizedType)type;
		Type[] typeargs = ptype.getActualTypeArguments();
		if(typeargs.length != 1)
			throw new IllegalArgumentException("Expected exactly 1 type argument to the type of field " + f + " but received " + typeargs.length);
		Type enumType = unwrapType(typeargs[0]);
		if(!(enumType instanceof Class))
			throw new IllegalArgumentException("The type argument to the type of field " + f + " must be an enum");
		Class<?> enumClass = (Class<?>)enumType;
		if(!enumClass.isEnum())
			throw new IllegalArgumentException("The type argument to the type of field " + f + " must be an enum");
		return new EnumSetOptionHandler<>(parser, o, (Setter<? super EnumSet>)setter, (Class<Enum>)enumClass);
	}
	
	static Type unwrapType(Type type) {
		for(;;) {
			if(type instanceof TypeVariable)
				type = ((TypeVariable<?>)type).getBounds()[0];
			else if(type instanceof WildcardType)
				type = ((WildcardType)type).getUpperBounds()[0];
			else return type;
		}
	}

}
