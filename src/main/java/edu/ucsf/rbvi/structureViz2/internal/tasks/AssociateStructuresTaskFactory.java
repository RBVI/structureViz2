package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class AssociateStructuresTaskFactory extends AbstractTaskFactory implements NetworkTaskFactory {

	private StructureManager structureManager;

	public AssociateStructuresTaskFactory(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public boolean isReady(CyNetwork network) {
		return true;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AssociateStructuresTask(structureManager, null));
	}

	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new AssociateStructuresTask(structureManager, network));
	}

}
