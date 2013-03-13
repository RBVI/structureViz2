package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class Structure {
	
	private CyNetwork network;
	private CyIdentifiable cyObj;
	private StructureType type;
	private List<ChimeraModel> models;
	
	public enum StructureType {PDB_MODEL, MODBASE_MODEL, SMILES};
	
	public Structure(CyNetwork network, CyIdentifiable cyObj, StructureType type) {
		this.network = network;
		this.cyObj = cyObj;
		this.type = type;
		models = new ArrayList<ChimeraModel>();
		
	}
	
	public CyIdentifiable getCyIdentifiable() {
		return this.cyObj;
	}
	
	public CyNetwork getNetwork() {
		return this.network;
	}

	public StructureType getType() {
		return this.type;
	}


}
