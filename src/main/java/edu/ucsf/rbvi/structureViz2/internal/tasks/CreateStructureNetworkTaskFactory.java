package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class CreateStructureNetworkTaskFactory extends AbstractTaskFactory implements
		NetworkTaskFactory {

	private StructureManager structureManager;
	private CyNetworkFactory cyNetworkFactory;
	private CyNetworkManager cyNetworkManager;
	private CyNetworkViewFactory cyNetworkViewFactory;
	private CyNetworkViewManager cyNetworkViewManager;

	public CreateStructureNetworkTaskFactory(StructureManager structureManager,
			CyNetworkFactory cyFact, CyNetworkManager cyNetManager,
			CyNetworkViewFactory cyNetworkViewFactory, CyNetworkViewManager cyNetworkViewManager) {
		this.structureManager = structureManager;
		this.cyNetworkFactory = cyFact;
		this.cyNetworkManager = cyNetManager;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkViewManager = cyNetworkViewManager;
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
		return new TaskIterator(new CreateStructureNetworkTask(structureManager, cyNetworkFactory,
				cyNetworkManager, cyNetworkViewFactory, cyNetworkViewManager));
	}

}
