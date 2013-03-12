package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class StructureVizSettingsTaskFactory extends AbstractTaskFactory 
                                             implements NetworkTaskFactory {

	StructureManager structureManager;

	public StructureVizSettingsTaskFactory(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public boolean isReady(CyNetwork network) {
		return true;
	}

	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new StructureVizSettingsTask(network, structureManager));
	}

}
