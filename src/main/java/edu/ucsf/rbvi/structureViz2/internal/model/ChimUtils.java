package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.List;

public abstract class ChimUtils {

	public static int parseModelNumber(List<String> chimeraOutput) {
		for (String info : chimeraOutput) {
			if (info.startsWith("#")) {
				try {
					return Integer.parseInt(info.substring(1, info.indexOf(",")));
				} catch (NumberFormatException e) {
					// search in some other way?
					return 1000;
				}
			}
		}
		return 1000;
	}
}
