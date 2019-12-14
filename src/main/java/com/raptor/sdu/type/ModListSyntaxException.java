package com.raptor.sdu.type;

import com.raptor.sdu.type.ModListParser.Line;

public class ModListSyntaxException extends RuntimeException {
	private static final long serialVersionUID = -274200232220169644L;

	public ModListSyntaxException(String fileName, Line line, String message) {
		super("at " + fileName + ":" + line.number + ": " + message + "\n\t" + line);
	}
	
}