package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.util.EdgeList;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.CytoUtils;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraModel;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraStructuralObject;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager.ModelType;

public class OpenUnassociatedStructuresTask extends AbstractTask implements ObservableTask {
	private StructureManager structureManager;
	private Set<ChimeraStructuralObject> newModels;

	@Tunable(description = "Structure file", params = "fileCategory=unspecified;input=true", gravity = 1.0)
	public File structureFile = null;

	@Tunable(description = "PDB ID to fetch", gravity = 2.0)
	public String pdbID = "";

	@Tunable(description = "Modbase models to fetch", gravity = 3.0)
	public String modbaseID = "";

	public OpenUnassociatedStructuresTask(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	@ProvidesTitle
	public String getTitle() {
		return "Open Unassociated Structures Options";
	}

	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle("Opening Structures");
		Set<ChimeraStructuralObject> allModels = new HashSet<ChimeraStructuralObject>();
		newModels = new HashSet<ChimeraStructuralObject>();

		for (ChimeraStructuralObject chimObj: structureManager.getAllChimObjs()) {
			if (chimObj instanceof ChimeraModel)
				allModels.add(chimObj);
		}

		// reinitialize tunables for the nogui arguments if any of them is set
		// otherwise, all selected nodes in structurePairs will be considered
		int openNodes = 0;
		// open PDB models
		Map<CyIdentifiable, List<String>> structuresToOpen = new HashMap<CyIdentifiable, List<String>>();
		// add structure file
		List<String> structures = new ArrayList<String>();
		if (structureFile != null && structureFile.isFile()) {
			// taskMonitor.setStatusMessage("Opening structure from file ...");
			// System.out.println("Opening structure from file: "+structureFile.getAbsolutePath());
			structures.add(structureFile.getAbsolutePath());
		}
		// add pdbIDs from nongui tunable
		if (pdbID != null && pdbID.length() > 0) {
			structures.add(pdbID);
		}
		// create artificial mapping
		if (structures.size() > 0) {
			structuresToOpen.put(null, structures);
		}
		// open all PDB models
		if (structuresToOpen.size() > 0) {
			taskMonitor.setStatusMessage("Opening structures ...");
			// System.out.println("Opening "+structuresToOpen.size()+" structures");
			if (!structureManager.openStructures(null, structuresToOpen, ModelType.PDB_MODEL)) {
				taskMonitor.setStatusMessage("Structures could not be opened.");
			}
			openNodes += structuresToOpen.size();
		}
		structures.clear();
		structuresToOpen.clear();

		// add modbase IDs from nongui tunable
		if (modbaseID != null && modbaseID.length() > 0) {
			structures.add(modbaseID);
		}
		if (structures.size() > 0) {
			structuresToOpen.put(null, structures);
		}

		// open modbase models
		if (structuresToOpen.size() > 0) {
			taskMonitor.setStatusMessage("Opening modbase models ...");
			if (!structureManager.openStructures(null, structuresToOpen, ModelType.MODBASE_MODEL)) {
				taskMonitor.setStatusMessage("ModBase models could not be opened.");
			}
			openNodes += structuresToOpen.size();
		}

		if (openNodes > 0) {
			// open dialog
			if (structureManager.getChimeraManager().isChimeraLaunched()) {
				structureManager.launchModelNavigatorDialog();
			} else {
				taskMonitor.setStatusMessage("Chimera has not been launched.");
			}
		} else {
			taskMonitor.setStatusMessage("No structures could be matched from input.");
		}

		// Get the new models
		for (ChimeraStructuralObject obj: structureManager.getAllChimObjs()) {
			if (obj instanceof ChimeraModel && !allModels.contains(obj))
				newModels.add(obj);
		}
	}

	public <R> R getResults(Class<? extends R> type) {
		if (type.equals(String.class)) {
			String result = "";
			for (ChimeraStructuralObject obj: newModels) {
				ChimeraModel model = (ChimeraModel)obj;
				result += "#"+model.getModelNumber()+" "+model.getModelName()+"\n";
			}
			return (R)result;
		}
		return (R)newModels;
	}

}
