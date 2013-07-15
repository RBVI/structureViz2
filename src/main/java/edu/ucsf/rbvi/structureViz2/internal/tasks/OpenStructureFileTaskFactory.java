package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class OpenStructureFileTaskFactory extends AbstractTaskFactory implements
		NetworkViewTaskFactory {

	private StructureManager structureManager;

	public OpenStructureFileTaskFactory(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public boolean isReady(CyNetworkView networkView) {
		return true;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(new OpenStructureFileTask(structureManager, networkView));
	}

}
