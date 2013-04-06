package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class CreateStructureNetworkTaskFactory extends AbstractTaskFactory implements TaskFactory {

	private StructureManager structureManager;

	public CreateStructureNetworkTaskFactory(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public boolean isReady() {
		if (structureManager.getChimeraManager().getChimeraModelsCount(false) > 0) {
			return true;
		}
		return false;

	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CreateStructureNetworkTask(structureManager));
	}

}
