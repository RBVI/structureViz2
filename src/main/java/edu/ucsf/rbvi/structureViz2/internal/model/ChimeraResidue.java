/* vim: set ts=2: */
/**
 * Copyright (c) 2006 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

/**
 * This class provides the implementation for the ChimeraResidue, object
 * 
 * @author scooter
 * 
 */

public class ChimeraResidue implements ChimeraStructuralObject, Comparable<ChimeraResidue> {

	/* Constants */
	public static final int SINGLE_LETTER = 0; // Display residues as a single
																							// letter
	public static final int THREE_LETTER = 1; // Display residues as three letters
	public static final int FULL_NAME = 2; // Display full residue names

	private String type; // Residue type
	private String index; // Residue index
	private String chainId; // ChainID for this residue
	private int modelNumber; // model number for this residue
	private int subModelNumber; // sub-model number for this residue
	protected int residueNumber;
	protected String insertionCode;
	private ChimeraModel chimeraModel; // ChimeraModel thie residue is part of
	private Object userData; // user data to associate with this residue
	// public static HashMap<String, String> aaNames = null; // a map of amino acid
	// names
	private static int displayType = THREE_LETTER; // the current display type
	private boolean selected = false; // the selection state

	/**
	 * Constructor to create a new ChimeraResidue
	 * 
	 * @param type
	 *          the residue type
	 * @param index
	 *          the index of the residue
	 * @param modelNumber
	 *          the model number this residue is part of
	 */
	public ChimeraResidue(String type, String index, int modelNumber) {
		this(type, index, modelNumber, 0);
	}

	/**
	 * Constructor to create a new ChimeraResidue
	 * 
	 * @param type
	 *          the residue type
	 * @param index
	 *          the index of the residue
	 * @param modelNumber
	 *          the model number this residue is part of
	 * @param subModelNumber
	 *          the sub-model number this residue is part of
	 */
	public ChimeraResidue(String type, String index, int modelNumber, int subModelNumber) {
		this.type = type;
		this.index = index;
		this.modelNumber = modelNumber;
		this.subModelNumber = subModelNumber;
		splitInsertionCode(this.index);
		// if (aaNames == null) {
		// initNames();
		// }
	}

	/**
	 * Constructor to create a new ChimeraResidue from an input line
	 * 
	 * @param chimeraInputLine
	 *          a Chimera residue description
	 */
	// invoked when listing (selected) residues: listr spec #0; lists level residue
	// Line: residue id #0:37.A type MET
	public ChimeraResidue(String chimeraInputLine) {
		// initNames();
		String[] split1 = chimeraInputLine.split(":");

		// First half has model number -- get the number
		int numberOffset = split1[0].indexOf('#');
		String model = split1[0].substring(numberOffset + 1);
		int decimalOffset = model.indexOf('.'); // Do we have a sub-model?
		try {
			this.subModelNumber = 0;
			if (decimalOffset > 0) {
				this.subModelNumber = Integer.parseInt(model.substring(decimalOffset + 1));
				this.modelNumber = Integer.parseInt(model.substring(0, decimalOffset));
			} else {
				this.modelNumber = Integer.parseInt(model);
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(edu.ucsf.rbvi.structureViz2.internal.model.ChimeraResidue.class)
					.error("Unexpected return from Chimera: " + model);
			this.modelNumber = -1;
		}

		// Second half has residue info: index & type
		String[] rTokens = split1[1].split(" ");
		this.type = rTokens[2];

		String[] iTokens = rTokens[0].split("\\.");
		if (iTokens.length > 0) {
			this.index = iTokens[0];

			// Careful, might or might not have a chainID
			if (iTokens.length > 1)
				this.chainId = iTokens[1];
			else
				this.chainId = "_";
		} else
			this.index = rTokens[0];

		splitInsertionCode(this.index);
	}

	/**
	 * Set the selected state for this residue
	 * 
	 * @param selected
	 *          the selection state to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Return the selected state of this residue
	 * 
	 * @return the selected state
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Return an array made up of this residue (required for ChimeraStructuralObject interface
	 * 
	 * @return a List with this residue as its sole member
	 */
	public List<ChimeraStructuralObject> getChildren() {
		List<ChimeraStructuralObject> v = new ArrayList<ChimeraStructuralObject>();
		v.add(this);
		return v;
	}

	/**
	 * Return the string representation of this residue as follows: "<i>residue_name</i> <i>index</i>"
	 * where <i>residue_name</i> could be either the single letter, three letter, or full name
	 * representation of the amino acid.
	 * 
	 * @return the string representation
	 */
	public String displayName() {
		return toString();
	}

	/**
	 * Return the string representation of this residue as follows: "<i>residue_name</i> <i>index</i>"
	 * where <i>residue_name</i> could be either the single letter, three letter, or full name
	 * representation of the amino acid.
	 * 
	 * @return the string representation
	 */
	public String toString() {
		if (displayType == FULL_NAME) {
			return (ChimUtils.toFullName(type) + " " + index);
		} else if (displayType == SINGLE_LETTER) {
			return (ChimUtils.toSingleLetter(type) + " " + index);
		} else if (displayType == THREE_LETTER) {
			return (ChimUtils.toThreeLetter(type) + " " + index);
		} else {
			return (type + " " + index);
		}
	}

	/**
	 * Return the Chimera specification for this Residue
	 * 
	 * @return Chimera specification
	 */
	public String toSpec() {
		if (!chainId.equals("_"))
			return ("#" + modelNumber + ":" + index + "." + chainId);
		else
			return ("#" + modelNumber + ":" + index + ".");
	}

	/**
	 * Get the index of this residue
	 * 
	 * @return residue index
	 */
	public String getIndex() {
		return this.index;
	}

	/**
	 * Get the chainID for this residue
	 * 
	 * @return String value of the chainId
	 */
	public String getChainId() {
		return this.chainId;
	}

	/**
	 * Get the type for this residue
	 * 
	 * @return residue type
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Get the model number for this residue
	 * 
	 * @return the model number
	 */
	public int getModelNumber() {
		return this.modelNumber;
	}

	/**
	 * Get the sub-model number for this residue
	 * 
	 * @return the sub-model number
	 */
	public int getSubModelNumber() {
		return this.subModelNumber;
	}

	/**
	 * Get the model this residue is part of
	 * 
	 * @return the ChimeraModel
	 */
	public ChimeraModel getChimeraModel() {
		return this.chimeraModel;
	}

	/**
	 * Set the model this residue is part of
	 * 
	 * @param chimeraModel
	 *          the ChimeraModel this model is part of
	 */
	public void setChimeraModel(ChimeraModel chimeraModel) {
		this.chimeraModel = chimeraModel;
	}

	/**
	 * Get the user data for this residue
	 * 
	 * @return user data
	 */
	public Object getUserData() {
		return userData;
	}

	/**
	 * Set the user data for this Residue
	 * 
	 * @param data
	 *          the user data to associate with this residue
	 */
	public void setUserData(Object data) {
		this.userData = data;
	}

	public int compareTo(ChimeraResidue c2) {
		if (residueNumber < c2.residueNumber)
			return -1;
		else if (residueNumber == c2.residueNumber) {
			if (insertionCode == null && c2.insertionCode == null)
				return 0;
			else if (insertionCode == null)
				return -1;
			else if (c2.insertionCode == null)
				return 1;
			return (insertionCode.compareTo(c2.insertionCode));
		}
		return 1;
	}

	public void splitInsertionCode(String residue) {
		// OK, split the index into number and insertion code
		Pattern p = Pattern.compile("(\\d*)([A-Z]?)");
		Matcher m = p.matcher(residue);
		if (m.matches()) {
			this.residueNumber = Integer.parseInt(m.group(1));
			if (m.groupCount() > 1)
				this.insertionCode = m.group(2);
			else
				this.insertionCode = null;
		}
	}

	/**********************************************
	 * Static routines
	 *********************************************/

	/**
	 * Initialize the residue names
	 */
	// private static void initNames() {
	// // Create our residue name table
	// aaNames = new HashMap<String, String>();
	// aaNames.put("ALA", "A Ala Alanine N[C@@H](C)C(O)=O");
	// aaNames.put("ARG", "R Arg Arginine N[C@@H](CCCNC(N)=N)C(O)=O");
	// aaNames.put("ASN", "N Asn Asparagine N[C@@H](CC(N)=O)C(O)=O");
	// aaNames.put("ASP", "D Asp Aspartic_acid N[C@@H](CC(O)=O)C(O)=O");
	// aaNames.put("CYS", "C Cys Cysteine N[C@@H](CS)C(O)=O");
	// aaNames.put("GLN", "Q Gln Glutamine N[C@H](C(O)=O)CCC(N)=O");
	// aaNames.put("GLU", "E Glu Glumatic_acid N[C@H](C(O)=O)CCC(O)=O");
	// aaNames.put("GLY", "G Gly Glycine NCC(O)=O");
	// aaNames.put("HIS", "H His Histidine N[C@@H](CC1=CN=CN1)C(O)=O");
	// aaNames.put("ILE", "I Ile Isoleucine N[C@]([C@H](C)CC)([H])C(O)=O");
	// aaNames.put("LEU", "L Leu Leucine N[C@](CC(C)C)([H])C(O)=O");
	// aaNames.put("LYS", "K Lys Lysine N[C@](CCCCN)([H])C(O)=O");
	// aaNames.put("DLY", "K Dly D-Lysine NCCCC[C@@H](N)C(O)=O");
	// aaNames.put("MET", "M Met Methionine N[C@](CCSC)([H])C(O)=O");
	// aaNames.put("PHE", "F Phe Phenylalanine N[C@](CC1=CC=CC=C1)([H])C(O)=O");
	// aaNames.put("PRO", "P Pro Proline OC([C@@]1([H])NCCC1)=O");
	// aaNames.put("SER", "S Ser Serine OC[C@](C(O)=O)([H])N");
	// aaNames.put("THR", "T Thr Threonine O[C@H](C)[C@](C(O)=O)([H])N");
	// aaNames.put("TRP", "W Trp Tryptophan N[C@@]([H])(CC1=CN([H])C2=C1C=CC=C2)C(O)=O");
	// aaNames.put("TYR", "Y Tyr Tyrosine N[C@@](C(O)=O)([H])CC1=CC=C(O)C=C1");
	// aaNames.put("VAL", "V Val Valine N[C@@](C(O)=O)([H])C(C)C");
	// aaNames.put("ASX", "B Asx Aspartic_acid_or_Asparagine");
	// aaNames.put("GLX", "Z Glx Glutamine_or_Glutamic_acid");
	// aaNames.put("XAA", "X Xaa Any_or_unknown_amino_acid");
	// aaNames.put("HOH", "HOH HOH Water [H]O[H]");
	// }

	/**
	 * Set the display type.
	 * 
	 * @param type
	 *          the display type
	 */
	public static void setDisplayType(int type) {
		displayType = type;
	}

	public static int getDisplayType() {
		return displayType;
	}

	public boolean hasSelectedChildren() {
		return false;
	}
}
