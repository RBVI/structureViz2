package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class ExitChimeraTask extends AbstractTask {

	private StructureManager structureManager;

	public ExitChimeraTask(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Exiting Chimera");
		structureManager.exitChimera();
	}

}
