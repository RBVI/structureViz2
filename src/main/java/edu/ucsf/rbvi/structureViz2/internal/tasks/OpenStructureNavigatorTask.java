package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class OpenStructureNavigatorTask extends AbstractTask {

	private StructureManager structureManager;
	
	public OpenStructureNavigatorTask(StructureManager structureManager) {
		this.structureManager = structureManager; 
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// open dialog
		taskMonitor.setTitle("Opening Cytoscape Molecular Structure Navigator");
		structureManager.launchModelNavigatorDialog();
	}

}
