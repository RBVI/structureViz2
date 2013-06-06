package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager.ModelType;

public class PaintStructureTaskFactory extends AbstractTaskFactory implements NodeViewTaskFactory {

	private StructureManager structureManager;
	private CyServiceRegistrar registrar;

	public PaintStructureTaskFactory(CyServiceRegistrar registrar, StructureManager structureManager) {
		this.structureManager = structureManager;
		this.registrar = registrar;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public boolean isReady(View<CyNode> nodeView, CyNetworkView netView) {
		// This only applies to the target node, so we just use the context
		List<CyIdentifiable> selectedList = new ArrayList<CyIdentifiable>();
		selectedList.add(nodeView.getModel());

		// Get the list of open structures for this node
		Map<CyIdentifiable, List<String>> stMap = structureManager.getOpenChimObjNames(selectedList);
		if (stMap != null && stMap.containsKey(nodeView.getModel()))
			return true;
		return false;
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		return new TaskIterator(new PaintStructureTask(nodeView, netView, registrar, structureManager));
	}
}
