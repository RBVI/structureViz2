package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.List;

import org.cytoscape.work.AbstractTask;
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

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		List<String> pathList = structureManager.getChimeraPaths(null);
		chimeraPath = chimeraPath.trim();
		if (chimeraPath.length() > 0) {
			pathList.add(chimeraPath);
			structureManager.setDefaultChimeraPath(chimeraPath);
		}
		if (structureManager.getChimeraManager().launchChimera(pathList)
				&& structureManager.getModelNavigatorDialog() == null) {
			structureManager.launchModelNavigatorDialog();
		}
	}

}
