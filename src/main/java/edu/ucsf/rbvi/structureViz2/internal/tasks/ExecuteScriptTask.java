package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.CytoUtils;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager.ModelType;

public class ExecuteScriptTask extends AbstractTask {

	private CyNode node;
	private CyNetwork network;
	private StructureManager structureManager;
	private String column = null;

	@Tunable (description="Column containing the script")
	public ListSingleSelection<String> stringColumns = null;

	public ExecuteScriptTask(CyNode node, CyNetworkView netView,
			StructureManager structureManager) {
		this.node = node;
		this.network = netView.getModel();
		this.structureManager = structureManager;

		// Get the default or user-specified command columns
		List<String> commandColumns = structureManager.getCurrentCommandKeys(network);
		if (commandColumns != null && commandColumns.size() > 0) {
			commandColumns = CytoUtils.getMatchingAttributes(network.getDefaultNodeTable(), commandColumns);
		}

		if (commandColumns == null || commandColumns.size() == 0) {
			List<String> stringAttributes = CytoUtils.getStringAttributes(network.getDefaultNodeTable());
			stringColumns = new ListSingleSelection<>(stringAttributes);
		} else if (commandColumns.size() == 1) {
			column = commandColumns.get(0);
		} else {
			stringColumns = new ListSingleSelection<>(commandColumns);
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Executing Chimera Script");
		// open dialog
		if (!structureManager.getChimeraManager().isChimeraLaunched()) {
			CyApplicationManager cyAppManager = 
							(CyApplicationManager) structureManager.getService(CyApplicationManager.class);
			List<String> pathList = structureManager.getChimeraPaths(cyAppManager.getCurrentNetwork());
			structureManager.getChimeraManager().launchChimera(pathList);
			taskMonitor.showMessage(TaskMonitor.Level.INFO,"Launching UCSF Chimera");
		}

		String commandColumn;
		if (column != null && stringColumns == null) {
			commandColumn = column;
		}  else {
			commandColumn = stringColumns.getSelectedValue();
		}
		String script = network.getRow(node).get(commandColumn, String.class);
		List<String> subScript = CytoUtils.columnSubstitution(structureManager, network, network.getRow(node), script);
		for (String scriptString: subScript) {
			taskMonitor.showMessage(TaskMonitor.Level.INFO,"Sending '"+scriptString+"' to UCSF Chimera");
			List<String> reply = structureManager.getChimeraManager().sendChimeraCommand(scriptString, true);
			for (String msg: reply) {
				taskMonitor.showMessage(TaskMonitor.Level.INFO,"  reply: '"+msg+"'");
			}
		}

	}
}
