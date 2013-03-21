package edu.ucsf.rbvi.structureViz2.internal.model;

import java.awt.Color;

import org.slf4j.LoggerFactory;

public abstract class ChimUtils {

	static int MAX_SUB_MODELS = 1000;
	
	/**
	 * Parse the model number returned by Chimera and return
	 * the int value
	 */
	public static int[] parseModelNumber(String inputLine) {
		int hash = inputLine.indexOf('#');
		int space = inputLine.indexOf(' ',hash);
		int decimal = inputLine.substring(hash+1,space).indexOf('.');
		// model number is between hash+1 and space
		int modelNumber = -1;
		int subModelNumber = 0;
		try {
			if (decimal > 0) {
				subModelNumber = Integer.parseInt(inputLine.substring(decimal+hash+2, space));
				space = decimal+hash+1;
			}
			modelNumber = Integer.parseInt(inputLine.substring(hash+1, space));
		} catch (Exception e) {
			LoggerFactory.getLogger(edu.ucsf.rbvi.structureViz2.internal.model.ChimUtils.class).error("Unexpected return from Chimera: "+inputLine);
		}
		return new int[]{modelNumber, subModelNumber};
	}

	/**
	 * Parse the model number returned by Chimera and return
	 * the int value
	 */
	public static int[] parseModelNumber2(String inputLine) {
		int hash = inputLine.indexOf('#');
		int space = inputLine.indexOf(',',hash);
		int decimal = inputLine.substring(hash+1,space).indexOf('.');
		// model number is between hash+1 and space
		int modelNumber = -1;
		int subModelNumber = 0;
		try {
			if (decimal > 0) {
				subModelNumber = Integer.parseInt(inputLine.substring(decimal+hash+2, space));
				space = decimal+hash+1;
			}
			modelNumber = Integer.parseInt(inputLine.substring(hash+1, space));
		} catch (Exception e) {
			LoggerFactory.getLogger(edu.ucsf.rbvi.structureViz2.internal.model.ChimUtils.class).error("Unexpected return from Chimera: "+inputLine);
		}
		return new int[]{modelNumber, subModelNumber};
	}

	/**
	 * Parse the model identifier returned by Chimera and return
	 * the String value
	 */
	public static String parseModelName(String inputLine) {
		int start = inputLine.indexOf("name ");
		if (start < 0) return null;
		// Might get a quoted string (don't understand why, but there you have it)
		if (inputLine.startsWith("\"", start+5)) {
			start += 6; // Skip over the first quote
			int end = inputLine.lastIndexOf('"');
			if (end >= 1) {
							return inputLine.substring(start,end);
			} else
							return inputLine.substring(start);
		} else {
			return inputLine.substring(start+5);
		}
	}

	public static Color parseModelColor(String inputLine) {
		int colorStart = inputLine.indexOf("color ");
		String colorString = inputLine.substring(colorStart + 6);
		String[] rgbStrings = colorString.split(",");
		float[] rgbValues = new float[4];
		for (int i = 0; i < rgbStrings.length; i++) {
			Float f = new Float(rgbStrings[i]);
			rgbValues[i] = f.floatValue();
		}
		if (rgbStrings.length == 4) {
			return new Color(rgbValues[0], rgbValues[1], rgbValues[2], rgbValues[3]);
		} else {
			return new Color(rgbValues[0], rgbValues[1], rgbValues[2]);
		}
	}
	
	
	/**
 	 * Create the key to use for forming the model/submodel key into the modelHash
 	 *
 	 * @param model the model number
 	 * @param subModel the submodel number
 	 * @return the model key as an Integer
 	 */
	public static Integer makeModelKey(int model, int subModel) {
		return new Integer(model*MAX_SUB_MODELS+subModel);
	}

}
