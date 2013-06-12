package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureSettings;

public class StructureVizSettingsTask extends AbstractTask {
	// private CyNetwork network;
	// private StructureManager structureManager;

	@ContainsTunables
	public StructureSettings structureSettings = null;

	public StructureVizSettingsTask(CyNetwork network, StructureManager structureManager) {
		// this.network = network;
		// this.structureManager = structureManager;
		structureSettings = new StructureSettings(network, structureManager);
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
	}

	@ProvidesTitle
	public String getTitle() {
		return "StructureViz Settings";
	}

}
