package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.CyActivator;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class CreateStructureNetworkTaskFactory extends AbstractTaskFactory implements
		NetworkTaskFactory {

	private StructureManager structureManager;
	private CyActivator structureViz;

	public CreateStructureNetworkTaskFactory(StructureManager structureManager,
			CyActivator structureViz) {
		this.structureManager = structureManager;
		this.structureViz = structureViz;
	}

	public boolean isReady(CyNetwork arg0) {
		if (structureManager.getChimeraManager().getChimeraModelsCount(false) > 0) {
			return true;
		}
		return false;

	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetwork arg0) {
		return new TaskIterator(new CreateStructureNetworkTask(structureManager, structureViz));
	}

}
