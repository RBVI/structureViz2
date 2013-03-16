package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class Structure {

	private CyNetwork network;
	private CyIdentifiable cyObject;
	private List<ChimeraStructuralObject> chimeraObjects;
	private Map<String, ChimeraModel> chimeraModels;

	public Structure(CyNetwork network, CyIdentifiable cyObject) {
		this.network = network;
		this.cyObject = cyObject;
		chimeraObjects = new ArrayList<ChimeraStructuralObject>();
		chimeraModels = new HashMap<String, ChimeraModel>();
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

	public void addChimeraModel(ChimeraModel chimeraModel) {
		chimeraModels.put(chimeraModel.getName(), chimeraModel);
		addChimeraObject(chimeraModel);
	}

	public Collection<String> getChimeraModelNames() {
		return chimeraModels.keySet();
	}

	public ChimeraModel getChimeraModel(String modelName) {
		if (chimeraModels.containsKey(modelName)) {
			return chimeraModels.get(modelName);
		}
		return null;
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
		// TODO: remove all depending on it objects
	}

}
