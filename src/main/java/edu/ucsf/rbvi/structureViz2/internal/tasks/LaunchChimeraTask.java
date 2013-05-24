package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class LaunchChimeraTask extends AbstractTask {

	private StructureManager structureManager;

	@Tunable(description = "Path to Chimera executable")
	public String chimeraPath;

	public LaunchChimeraTask(StructureManager structureManager) {
		this.structureManager = structureManager;
		chimeraPath = structureManager.getDefaultChimeraPath();
	}

	@ProvidesTitle
	public String getTitle() {
		return "Launch Chimera Options";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Launch Chimera");
		taskMonitor.setStatusMessage("Launching Chimera ...");
		List<String> pathList = structureManager.getChimeraPaths(null);
		chimeraPath = chimeraPath.trim();
		if (chimeraPath.length() > 0) {
			pathList.add(0, chimeraPath);
			structureManager.setDefaultChimeraPath(chimeraPath);
		}
		structureManager.getChimeraManager().launchChimera(pathList);
		if (structureManager.getChimeraManager().isChimeraLaunched()) {
			structureManager.launchModelNavigatorDialog();
		}
	}

}
