package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class Structure {

	private CyNetwork network;
	private CyIdentifiable cyObject;
	private List<ChimeraStructuralObject> chimeraObjects;

	public Structure(CyNetwork network, CyIdentifiable cyObject) {
		this.network = network;
		this.cyObject = cyObject;
		chimeraObjects = new ArrayList<ChimeraStructuralObject>();
	}

	public CyIdentifiable getCyObject() {
		return cyObject;
	}

	public CyNetwork getCyNetwork() {
		return network;
	}

	public void addChimeraObject(ChimeraStructuralObject chimeraObj) {
		chimeraObjects.add(chimeraObj);
	}

	public List<ChimeraStructuralObject> getChimeraObjects() {
		return chimeraObjects;
	}

	public boolean hasChimeraObject(ChimeraStructuralObject chimeraObj) {
		return chimeraObjects.contains(chimeraObj);
	}

	public void removeChimeraObject(ChimeraStructuralObject chimeraObj) {
		if (chimeraObjects.contains(chimeraObj)) {
			chimeraObjects.remove(chimeraObj);
		}
		// TODO: Remove all objects depending on this object if any, e.g. residues, chains, etc. 
	}

	public List<ChimeraModel> getChimeraModels(String modelName) {
		List<ChimeraModel> models = new ArrayList<ChimeraModel>();
		for (ChimeraStructuralObject chimObj : chimeraObjects) {
			if (chimObj instanceof ChimeraModel) {
				ChimeraModel model = (ChimeraModel) chimObj;
				if (model.getModelName().equals(modelName)) {
					models.add(model);
				}
			}
		}
		return models;
	}

	public Set<String> getChimeraModelNames() {
		Set<String> names = new HashSet<String>();
		for (ChimeraStructuralObject chimObj : chimeraObjects) {
			if (chimObj instanceof ChimeraModel) {
				names.add(((ChimeraModel)chimObj).getModelName());
			}
		}
		return names;
	}

	public boolean hasChimeraModelName(String name) {
		for (ChimeraStructuralObject chimObj : chimeraObjects) {
			if (chimObj instanceof ChimeraModel && name.equals(((ChimeraModel)chimObj).getModelName())) {
					return true;
				}
			}
		return false;
	}
	
}
