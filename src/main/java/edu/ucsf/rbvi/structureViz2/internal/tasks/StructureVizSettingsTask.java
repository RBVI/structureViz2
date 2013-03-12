package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class StructureVizSettingsTask extends AbstractTask {
	CyNetwork network;

	@ContainsTunables
	public StructureManager structureManager = null;

	public StructureVizSettingsTask(CyNetwork network, StructureManager structureManager) {
		this.network = network;
		this.structureManager = structureManager;
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
	}

}
