package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class StructureVizSettingsTaskFactory extends AbstractTaskFactory 
                                             implements NetworkTaskFactory {

	private StructureManager structureManager;

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
		List<CyNode> nodeList = new ArrayList<CyNode>();
		// Get all of the selected nodes
		return new TaskIterator(new StructureVizSettingsTask(network, structureManager));
	}

}
