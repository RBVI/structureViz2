package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class CreateStructureNetworkTaskFactory extends AbstractTaskFactory implements
		NetworkTaskFactory {

	private StructureManager structureManager;
	private CyNetworkFactory cyNetworkFactory;
	private CyNetworkManager cyNetworkManager;

	public CreateStructureNetworkTaskFactory(StructureManager structureManager,
			CyNetworkFactory cyFact, CyNetworkManager cyNetManager) {
		this.structureManager = structureManager;
		this.cyNetworkFactory = cyFact;
		this.cyNetworkManager = cyNetManager;
	}

	public boolean isReady(CyNetwork arg0) {
		if (structureManager.getChimeraManager().isChimeraLaunched()
				&& structureManager.getChimeraManager().getChimeraModelsCount(false) > 0) {
			return true;
		}
		return false;

	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetwork arg0) {
		return new TaskIterator(new CreateStructureNetworkTask(structureManager, cyNetworkFactory,
				cyNetworkManager));
	}

}
