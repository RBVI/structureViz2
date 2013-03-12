package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.List;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class OpenStructuresTask extends AbstractTask {
	private List<CyNode> nodeList;
	private CyNetworkView netView;
	private StructureManager structureManager;

	public OpenStructuresTask(List<CyNode> nodeList, CyNetworkView netView,
			StructureManager structureManager) {
		this.nodeList = nodeList;
		this.netView = netView;
		this.structureManager = structureManager;
	}

	public void run(TaskMonitor taskMonitor) {
		System.out.println("open sturctures task for network view " + netView.getSUID());
		System.out.println("selected nodes: " + nodeList.size());
		if (structureManager.hasNodeStructures(netView.getModel(), nodeList)) {
			// launch a dialog with list of structures to open
			System.out.println("structures found");
			List<String> nodeStructures = structureManager.getNodeStructures(
					netView.getModel(), nodeList);
			for (String structure : nodeStructures) {
				System.out.println(structure);
			}
		}
	}

}
