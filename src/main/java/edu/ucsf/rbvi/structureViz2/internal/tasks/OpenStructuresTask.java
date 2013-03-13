package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraManager;
import edu.ucsf.rbvi.structureViz2.internal.model.Structure;
import edu.ucsf.rbvi.structureViz2.internal.model.Structure.StructureType;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class OpenStructuresTask extends AbstractTask {
	private List<CyNode> nodeList;
	private CyNetworkView netView;
	private StructureManager structureManager;
	private ChimeraManager chimeraManager;

	public OpenStructuresTask(List<CyNode> nodeList, CyNetworkView netView,
			StructureManager structureManager, ChimeraManager chimeraManager) {
		this.nodeList = nodeList;
		this.netView = netView;
		this.structureManager = structureManager;
		this.chimeraManager = chimeraManager;
	}

	public void run(TaskMonitor taskMonitor) {
		System.out.println("open sturctures task for network view " + netView.getSUID());
		System.out.println("selected nodes: " + nodeList.size());
		if (structureManager.hasNodeStructures(netView.getModel(), nodeList)) {
			// launch a dialog with list of structures to open
			Map<CyIdentifiable, List<String>> nodeStructures = structureManager.getNodeStructures(
					netView.getModel(), nodeList);
			if (nodeStructures == null) {
				// nothing found
				return;
			}
			System.out.println("structures found: " + nodeStructures.size());
			for (CyIdentifiable node : nodeStructures.keySet()) {
				for (String structureName : nodeStructures.get(node)) {
					Structure currentNodeStructure = structureManager.getStructure(netView.getModel(), node);
					if (currentNodeStructure == null) {
						// create new structure object for this node and network
						currentNodeStructure = new Structure(netView.getModel(), node, StructureType.PDB_MODEL);
						System.out.println("Node: " + node.getSUID() + "\tstructure: " + structureName);
						structureManager.addStructure(currentNodeStructure);
						chimeraManager.openStructure(structureName, currentNodeStructure, false);
					} else {
						// update current structure with the new information
						System.out.println("Structures already exist");
						//chimeraManager.openStructure(structureName, currentNodeStructure, true);
					}
				}
			}
		}
	}

}
