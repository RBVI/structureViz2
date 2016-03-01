package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager.ModelType;

public class AssociateStructuresTask extends AbstractTask {

	private StructureManager structureManager;

	private CyNetwork net;

	public AssociateStructuresTask(StructureManager structureManager, CyNetwork net) {
		this.structureManager = structureManager;
		this.net = net;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Associating structures with Cytoscape");
		structureManager.associate(net);
	}

	@ProvidesTitle
	public String getTitle() {
		return "Associating structures with Cytoscape";
	}
}
