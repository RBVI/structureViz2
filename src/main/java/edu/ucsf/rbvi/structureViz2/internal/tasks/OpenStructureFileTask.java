package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager.ModelType;

public class OpenStructureFileTask extends AbstractTask {

	private StructureManager structureManager;

	private CyNetwork net;

	@Tunable(description = "Structure file", params = "fileCategory=unspecified;input=true")
	public File structureFile = null;

	public OpenStructureFileTask(StructureManager structureManager, CyNetwork net) {
		this.structureManager = structureManager;
		this.net = net;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Opening Structure from File");
		if (structureFile == null || !structureFile.isFile()) {
			taskMonitor.setStatusMessage("Structure file could not be read.");
			return;
		}
		taskMonitor.setStatusMessage("Opening structure ...");
		Map<CyIdentifiable, List<String>> structuresToOpen = new HashMap<CyIdentifiable, List<String>>();
		List<String> structures = new ArrayList<String>();
		// structureFile.getAbsolutePath()
		structures.add(structureFile.getAbsolutePath());
		structuresToOpen.put(net, structures);
		if (!structureManager.openStructures(net, structuresToOpen, ModelType.PDB_MODEL)) {
			taskMonitor.setStatusMessage("Structure could not be opened.");
		}

		// open dialog
		if (structureManager.getChimeraManager().isChimeraLaunched()) {
			structureManager.launchModelNavigatorDialog();
		} else {
			taskMonitor.setStatusMessage("Chimera could not be launched.");
		}

	}

	@ProvidesTitle
	public String getTitle() {
		return "Open structure from file";
	}
}
