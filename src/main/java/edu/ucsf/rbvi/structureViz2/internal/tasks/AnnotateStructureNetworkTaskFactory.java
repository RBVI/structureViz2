package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class AnnotateStructureNetworkTaskFactory extends AbstractTaskFactory implements
		NetworkTaskFactory {

	private StructureManager structureManager;

	public AnnotateStructureNetworkTaskFactory(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public boolean isReady(CyNetwork network) {
		if (structureManager.getChimeraManager().isChimeraLaunched()
				&& structureManager.getChimeraManager().getChimeraModelsCount(false) > 0) {
			return true;
		}
		return false;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new AnnotateStructureNetworkTask(structureManager, network));
	}

}
