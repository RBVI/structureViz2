package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class LaunchChimeraTask extends AbstractTask {

	private StructureManager structureManager;

	public LaunchChimeraTask(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		structureManager.getChimeraManager().launchChimera(structureManager.getChimeraPaths(null));
		if (structureManager.getModelNavigatorDialog() == null) {
			structureManager.launchModelNavigatorDialog();
		}
	}

}
