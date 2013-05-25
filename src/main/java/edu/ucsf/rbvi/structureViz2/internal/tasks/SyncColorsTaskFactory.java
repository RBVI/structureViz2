package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class SyncColorsTaskFactory extends AbstractTaskFactory implements NetworkViewTaskFactory {

	private StructureManager structureManager;

	public SyncColorsTaskFactory(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public boolean isReady(CyNetworkView networkView) {
		if (structureManager.getChimeraManager().isChimeraLaunched()
				&& structureManager.getChimeraManager().getChimeraModelsCount(false) > 0) {
			return true;
		}
		return false;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(new SyncColorsTask(structureManager, networkView));
	}

}
