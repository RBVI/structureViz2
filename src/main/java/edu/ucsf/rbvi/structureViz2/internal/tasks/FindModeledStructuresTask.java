package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.structureViz2.internal.model.CytoUtils;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager.ModelType;

public class FindModeledStructuresTask extends AbstractTask {

	private List<CyNode> nodeList;
	private CyNetworkView netView;
	private StructureManager structureManager;

	public FindModeledStructuresTask(List<CyNode> nodeList, CyNetworkView netView,
			StructureManager structureManager) {
		this.nodeList = nodeList;
		this.netView = netView;
		this.structureManager = structureManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Opening Modeled Structures");
		String message = "<html><b>Warning</b>: Modeled structures are predictions, not experimental data.<br>";
		message += "These structures are from the ModBase web service at <a href=\"http://modbase.salilab.org\">http://modbase.salilab.org/</a><br>";
		message += "Measures of model reliability, or likelihood of correctness, are provided in the<br>";
		message += "Chimera ModBase Model List.</html>";
		taskMonitor.setStatusMessage(message);
		Map<CyIdentifiable, List<String>> selectedStructureNames = new HashMap<CyIdentifiable, List<String>>();
		for (CyNode node : nodeList) {
			List<String> structures = new ArrayList<String>();
			String nodeName = CytoUtils.getNodeName(netView.getModel(), node);
			if (nodeName.startsWith("gi")) {
				nodeName = nodeName.substring(2);
			}
			structures.add(nodeName);
			selectedStructureNames.put(node, structures);
		}
		if (selectedStructureNames.size() > 0) {
			// open structures
			if (!structureManager.openStructures(netView.getModel(), selectedStructureNames,
					ModelType.MODBASE_MODEL)) {
				taskMonitor.setStatusMessage("Structures could not be opened.");
			}
		}
		// open dialog
		if (structureManager.getChimeraManager().isChimeraLaunched()) {
			structureManager.launchModelNavigatorDialog();
		} else {
			taskMonitor.setStatusMessage("Chimera could not be launched.");
		}
	}
}
