package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class ShowDialogTaskFactory extends AbstractTaskFactory implements TaskFactory {

	private StructureManager structureManager;

	public ShowDialogTaskFactory(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public boolean isReady() {
		if (structureManager.getChimeraManager().isChimeraLaunched()
				&& !structureManager.isMNDialogOpen()) {
			return true;
		}
		return false;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ShowDialogTask(structureManager));
	}

}
