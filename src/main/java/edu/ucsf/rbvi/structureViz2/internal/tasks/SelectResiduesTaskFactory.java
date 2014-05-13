package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class SelectResiduesTaskFactory extends AbstractTaskFactory implements NodeViewTaskFactory {

	private StructureManager structureManager;

	public SelectResiduesTaskFactory(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		// Check for functional residues
		List<String> residueList = structureManager.getResidueList(networkView.getModel(),
				nodeView.getModel());
		if (residueList.size() == 0) {
			return false;
		}
		// Get the list of open structures for this node
		List<CyIdentifiable> selectedList = new ArrayList<CyIdentifiable>();
		selectedList.add(nodeView.getModel());
		Map<CyIdentifiable, List<String>> stMap = structureManager
				.getOpenChimObjNames(selectedList);
		if (stMap != null && stMap.containsKey(nodeView.getModel())) {
			return true;
		}
		return false;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(new SelectResiduesTask(nodeView, networkView, structureManager));
	}

}
