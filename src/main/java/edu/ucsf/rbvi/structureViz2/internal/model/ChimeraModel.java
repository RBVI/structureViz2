package edu.ucsf.rbvi.structureViz2.internal.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager.ModelType;

/**
 * This class provides the implementation for the ChimeraModel, ChimeraChain,
 * and ChimeraResidue objects
 * 
 * @author scooter
 * 
 */
public class ChimeraModel implements ChimeraStructuralObject {

	private String name; // The name of this model
	private ModelType type; // The type of the model
	private int modelNumber; // The model number
	private int subModelNumber; // The sub-model number

	private Color modelColor = null; // The color of this model (from Chimera)
	private Object userData = null; // User data associated with this model
	private boolean selected = false; // The selected state of this model

	private TreeMap<String, ChimeraChain> chainMap; // The list of chains
	private TreeMap<String, ChimeraResidue> residueMap; // The list of residues

	/**
	 * Constructor to create a model
	 * 
	 * @param name
	 *          the name of this model
	 * @param color
	 *          the model Color
	 * @param modelNumber
	 *          the model number
	 * @param subModelNumber
	 *          the sub-model number
	 */
	public ChimeraModel(String name, ModelType type, int modelNumber, int subModelNumber) {
		this.name = name;
		this.type = type;
		this.modelNumber = modelNumber;
		this.subModelNumber = subModelNumber;

		this.chainMap = new TreeMap<String, ChimeraChain>();
		this.residueMap = new TreeMap<String, ChimeraResidue>();
	}

	/**
	 * Constructor to create a model from the Chimera input line
	 * 
	 * @param inputLine
	 *          Chimera input line from which to construct this model
	 */
	public ChimeraModel(String inputLine) {
		this.name = ChimUtils.parseModelName(inputLine);
		this.modelNumber = ChimUtils.parseModelNumber(inputLine)[0];
		this.subModelNumber = ChimUtils.parseModelNumber(inputLine)[1];
		this.chainMap = new TreeMap<String, ChimeraChain>();
		this.residueMap = new TreeMap<String, ChimeraResidue>();
	}

	/**
	 * Add a residue to this model
	 * 
	 * @param residue
	 *          to add to the model
	 */
	public void addResidue(ChimeraResidue residue) {
		residue.setChimeraModel(this);
		residueMap.put(residue.getIndex(), residue);
		String chainId = residue.getChainId();
		if (chainId != null) {
			addResidue(chainId, residue);
		} else {
			addResidue("_", residue);
		}
		// Put it in our map so that we can return it in order
		residueMap.put(residue.getIndex(), residue);
	}

	/**
	 * Add a residue to a chain in this model. If the chain associated with
	 * chainId doesn't exist, it will be created.
	 * 
	 * @param chainId
	 *          to add the residue to
	 * @param residue
	 *          to add to the chain
	 */
	public void addResidue(String chainId, ChimeraResidue residue) {
		ChimeraChain chain = null;
		if (!chainMap.containsKey(chainId)) {
			chain = new ChimeraChain(this.modelNumber, this.subModelNumber, chainId);
			chain.setChimeraModel(this);
			chainMap.put(chainId, chain);
		} else {
			chain = chainMap.get(chainId);
		}
		chain.addResidue(residue);
	}

	public String displayName() {
		return toString();
	}

	/**
	 * Get the ChimeraModel (required for ChimeraStructuralObject interface)
	 * 
	 * @return ChimeraModel
	 */
	public ChimeraModel getChimeraModel() {
		return this;
	}

	/**
	 * Get the model color of this model
	 * 
	 * @return model color of this model
	 */
	public Color getModelColor() {
		return this.modelColor;
	}

	/**
	 * Set the color of this model
	 * 
	 * @param color
	 *          Color of this model
	 */
	public void setModelColor(Color color) {
		this.modelColor = color;
	}

	/**
	 * Return the name of this model
	 * 
	 * @return model name
	 */
	public String getModelName() {
		return name;
	}

	/**
	 * Set the name of this model
	 * 
	 * @param name
	 *          model name
	 */
	public void setModelName(String name) {
		this.name = name;
	}

	/**
	 * Get the model number of this model
	 * 
	 * @return integer model number
	 */
	public int getModelNumber() {
		return modelNumber;
	}

	/**
	 * Set the model number of this model
	 * 
	 * @param modelNumber
	 *          integer model number
	 */
	public void setModelNumber(int modelNumber) {
		this.modelNumber = modelNumber;
	}

	/**
	 * Get the sub-model number of this model
	 * 
	 * @return integer sub-model number
	 */
	public int getSubModelNumber() {
		return subModelNumber;
	}

	/**
	 * Set the sub-model number of this model
	 * 
	 * @param subModelNumber
	 *          integer model number
	 */
	public void setSubModelNumber(int subModelNumber) {
		this.subModelNumber = subModelNumber;
	}

	public ModelType getModelType() {
		return type;
	}

	public void setModelType(ModelType type) {
		this.type = type;
	}

	/**
	 * Get the user data for this model
	 * 
	 * @return user data
	 */
	public Object getUserData() {
		return userData;
	}

	/**
	 * Set the user data for this model
	 * 
	 * @param data
	 *          user data to associate with this model
	 */
	public void setUserData(Object data) {
		this.userData = data;
	}

	/**
	 * Return the selected state of this model
	 * 
	 * @return the selected state
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Set the selected state of this model
	 * 
	 * @param selected
	 *          a boolean to set the selected state to
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Return the chains in this model as a List
	 * 
	 * @return the chains in this model as a list
	 */
	public List<ChimeraStructuralObject> getChildren() {
		return new ArrayList<ChimeraStructuralObject>(chainMap.values());
	}

	/**
	 * Return the chains in this model as a colleciton
	 * 
	 * @return the chains in this model
	 */
	public Collection<ChimeraChain> getChains() {
		return chainMap.values();
	}

	/**
	 * Get the number of chains in this model
	 * 
	 * @return integer chain count
	 */
	public int getChainCount() {
		return chainMap.size();
	}

	/**
	 * Get the list of chain names associated with this model
	 * 
	 * @return return the list of chain names for this model
	 */
	public Collection<String> getChainNames() {
		return chainMap.keySet();
	}

	/**
	 * Get the residues associated with this model
	 * 
	 * @return the list of residues in this model
	 */
	public Collection<ChimeraResidue> getResidues() {
		return residueMap.values();
	}

	/**
	 * Get the number of residues in this model
	 * 
	 * @return integer residues count
	 */
	public int getResidueCount() {
		return residueMap.size();
	}

	/**
	 * Get a specific chain from the model
	 * 
	 * @param chain
	 *          the ID of the chain to return
	 * @return ChimeraChain associated with the chain
	 */
	public ChimeraChain getChain(String chain) {
		if (chainMap.containsKey(chain)) {
			return chainMap.get(chain);
		}
		return null;
	}

	/**
	 * Return a specific residue based on its index
	 * 
	 * @param index
	 *          of the residue to return
	 * @return the residue associated with that index
	 */
	public ChimeraResidue getResidue(String index) {
		if (residueMap.containsKey(index)) {
			return residueMap.get(index);
		}
		return null;
	}

	public boolean hasSelectedChildren() {
		if (selected) {
			return true;
		} else {
			for (ChimeraChain chain : getChains()) {
				if (chain.hasSelectedChildren()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Return the list of selected residues
	 * 
	 * @return all selected residues
	 */
	public List<ChimeraResidue> getSelectedResidues() {
		List<ChimeraResidue> residueList = new ArrayList<ChimeraResidue>();
		if (selected) {
			residueList.addAll(getResidues());
		} else {
			for (ChimeraChain chain : getChains()) {
				residueList.addAll(chain.getSelectedResidues());
			}
		}
		return residueList;

	}

	/**
	 * Return the Chimera specification for this model
	 */
	public String toSpec() {
		if (subModelNumber == 0)
			return ("#" + modelNumber);
		return ("#" + modelNumber + "." + subModelNumber);
	}

	/**
	 * Return a string representation for the model.
	 */
	public String toString() {
		// TODO: String representation of the model? -> Show associated nodes in Cytoscape as a tooltip?
		// String nodeName = " {none}";
		// if (structure != null && structure.getIdentifier() != null) {
		// nodeName = structure.getIdentifier();
		// if (structure.getGraphObjectList().size() > 1)
		// nodeName = "s {"+nodeName+"}";
		// else
		// nodeName = " "+nodeName;
		// }
		// String displayName = name;
		// if (name.length() > 14)
		// displayName = name.substring(0, 13) + "...";
		// if (getChainCount() > 0) {
		// return ("Node" + nodeName + " [Model " + toSpec() + " " + displayName +
		// " ("
		// + getChainCount() + " chains, " + getResidueCount() + " residues)]");
		// } else if (getResidueCount() > 0) {
		// return ("Node" + nodeName + " [Model " + toSpec() + " " + displayName +
		// " ("
		// + getResidueCount() + " residues)]");
		// } else {
		// return ("Node" + nodeName + " [Model " + toSpec() + " " + displayName +
		// "]");
		// }
		String displayName = name;
		if (name.length() > 14)
			displayName = name.substring(0, 13) + "...";
		if (getChainCount() > 0) {
			return ("[Model " + toSpec() + " " + displayName + " (" + getChainCount() + " chains, "
					+ getResidueCount() + " residues)]");
		} else if (getResidueCount() > 0) {
			return ("[Model " + toSpec() + " " + displayName + " (" + getResidueCount() + " residues)]");
		} else {
			return ("[Model " + toSpec() + " " + displayName + "]");
		}

	}

}
