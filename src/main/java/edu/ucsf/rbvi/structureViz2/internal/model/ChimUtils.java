package edu.ucsf.rbvi.structureViz2.internal.model;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

public abstract class ChimUtils {

	static int MAX_SUB_MODELS = 1000;

	/**
	 * Parse the model number returned by Chimera and return the int value
	 */
	public static int[] parseModelNumber(String inputLine) {
		int hash = inputLine.indexOf('#');
		int space = inputLine.indexOf(' ', hash);
		int decimal = inputLine.substring(hash + 1, space).indexOf('.');
		// model number is between hash+1 and space
		int modelNumber = -1;
		int subModelNumber = 0;
		try {
			if (decimal > 0) {
				subModelNumber = Integer.parseInt(inputLine.substring(decimal + hash + 2, space));
				space = decimal + hash + 1;
			}
			modelNumber = Integer.parseInt(inputLine.substring(hash + 1, space));
		} catch (Exception e) {
			LoggerFactory.getLogger(edu.ucsf.rbvi.structureViz2.internal.model.ChimUtils.class).error(
					"Unexpected return from Chimera: " + inputLine);
		}
		return new int[] { modelNumber, subModelNumber };
	}

	/**
	 * Parse the model number returned by Chimera and return the int value
	 */
	public static int[] parseModelNumber2(String inputLine) {
		int hash = inputLine.indexOf('#');
		int space = inputLine.indexOf(',', hash);
		int decimal = inputLine.substring(hash + 1, space).indexOf('.');
		// model number is between hash+1 and space
		int modelNumber = -1;
		int subModelNumber = 0;
		try {
			if (decimal > 0) {
				subModelNumber = Integer.parseInt(inputLine.substring(decimal + hash + 2, space));
				space = decimal + hash + 1;
			}
			modelNumber = Integer.parseInt(inputLine.substring(hash + 1, space));
		} catch (Exception e) {
			LoggerFactory.getLogger(edu.ucsf.rbvi.structureViz2.internal.model.ChimUtils.class).error(
					"Unexpected return from Chimera: " + inputLine);
		}
		return new int[] { modelNumber, subModelNumber };
	}

	/**
	 * Parse the model identifier returned by Chimera and return the String value
	 */
	public static String parseModelName(String inputLine) {
		int start = inputLine.indexOf("name ");
		if (start < 0)
			return null;
		// Might get a quoted string (don't understand why, but there you have it)
		if (inputLine.startsWith("\"", start + 5)) {
			start += 6; // Skip over the first quote
			int end = inputLine.lastIndexOf('"');
			if (end >= 1) {
				return inputLine.substring(start, end);
			} else
				return inputLine.substring(start);
		} else {
			return inputLine.substring(start + 5);
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
	 * @param model
	 *          the model number
	 * @param subModel
	 *          the submodel number
	 * @return the model key as an Integer
	 */
	public static Integer makeModelKey(int model, int subModel) {
		return new Integer(model * MAX_SUB_MODELS + subModel);
	}

	public static ChimeraModel getModel(String atomSpec, ChimeraManager chimeraManager) {
		// System.out.println("getting model for "+atomSpec);
		String[] split = atomSpec.split(":");
		// No model specified....
		if (split[0].length() == 0)
			return null;

		// System.out.println("model = "+split[0].substring(1));
		int model = 0;
		int submodel = 0;
		// TODO: check
		try {
			String[] subSplit = split[0].substring(1).split("\\.");
			if (subSplit.length > 0)
				model = Integer.parseInt(subSplit[0]);
			else
				model = Integer.parseInt(split[0].substring(1));

			if (subSplit.length > 1)
				submodel = Integer.parseInt(subSplit[1]);
		} catch (Exception e) {

		}
		return chimeraManager.getChimeraModel(model, submodel);
	}

	public static ChimeraResidue getResidue(String atomSpec, ChimeraManager chimeraManager) {
		// System.out.println("Getting residue from: "+atomSpec);
		ChimeraModel model = getModel(atomSpec, chimeraManager); // Get the model
		if (model == null) {
			// TODO: change this
			model = chimeraManager.getChimeraModel(0, 0);
		}
		return getResidue(atomSpec, model);
	}

	public static ChimeraResidue getResidue(String atomSpec, ChimeraModel model) {
		// System.out.println("Getting residue from: "+atomSpec);
		String[] split = atomSpec.split(":|@");

		// Split into residue and chain
		String[] residueChain = split[1].split("\\.");

		if (residueChain[0].length() == 0)
			return null;

		if (residueChain.length == 2 && residueChain[1].length() > 0) {
			ChimeraChain chain = model.getChain(residueChain[1]);
			return chain.getResidue(residueChain[0]);
		}
		return model.getResidue(residueChain[0]);
	}

	public static ChimeraChain getChain(String atomSpec, ChimeraModel model) {
		String[] split = atomSpec.split(":|@");

		// Split into residue and chain
		String[] residueChain = split[1].split("\\.");
		if (residueChain.length == 1)
			return null;

		return model.getChain(residueChain[1]);
	}

	public static boolean isBackbone(String atomSpec) {
		String[] split = atomSpec.split("@");
		String atom = split[1];
		if (atom.equals("C") || atom.equals("CA") || atom.equals("N") || atom.equals("H")
				|| atom.equals("O"))
			return true;
		return false;
	}

	/**
	 * This method takes a Chimera atomSpec ([#model]:[residue][.chainID]) and returns the
	 * lowest-level object referenced by the spec. For example, if the spec is "#0", this method will
	 * return a ChimeraModel. If the spec is ":.A", it will return a ChimeraChain, etc.
	 * 
	 * @param atomSpec
	 *          the specification string
	 * @param chimeraManager
	 *          the Chimera object we're currently using
	 * @return a ChimeraStructuralObject of the lowest type
	 */
	public static ChimeraStructuralObject fromSpec(String atomSpec, ChimeraManager chimeraManager) {
		ChimeraModel model = null;
		ChimeraChain chain = null;
		ChimeraResidue residue = null;

		String[] split = atomSpec.split(":|@");
		// 0 = model; 1 = Residue and Chain; 2 = Atom
		if (split[0].length() == 0) {
			// No model specified...
			model = chimeraManager.getChimeraModel(0, 0);
		} else {
			int modelNumber = 0;
			int submodelNumber = 0;
			String[] subSplit = split[0].substring(1).split(".");
			modelNumber = Integer.parseInt(subSplit[0]);
			if (subSplit.length > 1)
				submodelNumber = Integer.parseInt(subSplit[1]);

			model = chimeraManager.getChimeraModel(modelNumber, submodelNumber);
		}

		// Split into residue and chain
		String[] residueChain = split[1].split("\\.");
		// 0 = Residue; 1 = Chain
		if (residueChain.length == 2 && residueChain[1].length() > 0)
			chain = model.getChain(residueChain[1]);

		if (residueChain[0].length() > 0) {
			if (chain == null)
				residue = model.getResidue(residueChain[0]);
			else
				residue = chain.getResidue(residueChain[0]);
		}

		if (residue != null)
			return residue;
		else if (chain != null)
			return chain;

		return model;
	}

	/**
	 * This method takes a Cytoscape attribute specification ([structure#][residue][.chainID]) and
	 * returns the lowest-level object referenced by the spec. For example, if the spec is "1tkk",
	 * this method will return a ChimeraModel. If the spec is ".A", it will return a ChimeraChain,
	 * etc.
	 * 
	 * @param attrSpec
	 *          the specification string
	 * @param chimeraManager
	 *          the Chimera object we're currently using
	 * @return a ChimeraStructuralObject of the lowest type
	 */
	public static ChimeraStructuralObject fromAttribute(String attrSpec, ChimeraManager chimeraManager) {
		if (attrSpec.indexOf(',') > 0 || attrSpec.indexOf('-') > 0) {
			// No support for either lists or ranges
			return null;
		}

		String residue = null;
		String model = null;
		String chain = null;

		ChimeraModel chimeraModel = null;
		ChimeraChain chimeraChain = null;
		ChimeraResidue chimeraResidue = null;

		// System.out.println("Getting object from attribute: "+attrSpec);

		String[] split = attrSpec.split("#|\\.");
		if (split.length == 1) {
			// Residue only
			residue = split[0];
		} else if (split.length == 3) {
			// We have all three
			model = split[0];
			residue = split[1];
			chain = split[2];
		} else if (split.length == 2 && attrSpec.indexOf('#') > 0) {
			// Model and Residue
			model = split[0];
			residue = split[1];
		} else {
			// Residue and Chain
			residue = split[0];
			chain = split[1];
		}

		// System.out.println("model = "+model+" chain = "+chain+" residue = "+residue);

		if (model != null) {
			chimeraModel = chimeraManager.getChimeraModel(0, 0);
		} else {
			chimeraModel = chimeraManager.getChimeraModel(0, 0);
		}
		// System.out.println("ChimeraModel = "+chimeraModel);

		if (chain != null) {
			chimeraChain = chimeraModel.getChain(chain);
			// System.out.println("ChimeraChain = "+chimeraChain);
		}

		if (residue != null) {
			chimeraResidue = null;
			if (chimeraChain != null)
				chimeraResidue = chimeraChain.getResidue(residue);
			chimeraResidue = chimeraModel.getResidue(residue);
			// System.out.println("ChimeraResidue = "+chimeraResidue);
			return chimeraResidue;
		}

		if (chimeraChain != null)
			return chimeraChain;

		if (chimeraModel != null)
			return chimeraModel;

		return null;
	}

	/**
	 * Search for structure references in the residue list
	 * 
	 * @param residueList
	 *          the list of residues
	 * @return a concatenated list of structures encoded in the list
	 */
	public static String findStructures(String residueList) {
		if (residueList == null)
			return null;
		String[] residues = residueList.split(",");
		String structures = new String();
		Map<String, String> structureNameMap = new HashMap<String, String>();
		for (int i = 0; i < residues.length; i++) {
			String[] components = residues[i].split("#");
			if (components.length > 1) {
				structureNameMap.put(components[0], components[1]);
			}
		}
		if (structureNameMap.isEmpty())
			return null;

		String structure = null;
		for (String struct : structureNameMap.keySet()) {
			if (structure == null)
				structure = new String();
			else
				structure = structure.concat(",");
			structure = structure.concat(struct);
		}
		return structure;
	}

}
