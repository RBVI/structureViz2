package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class SyncColorsTask extends AbstractTask {

	private StructureManager structureManager;
	private CyNetworkView networkView;

	@Tunable(description = "Sync colors from Chimera to Cytoscape", dependsOn = "cytoscapeToChimera=false")
	public boolean chimeraToCytoscape;

	@Tunable(description = "Sync colors from Cytoscape to Chiemra", dependsOn = "chimeraToCytoscape=false")
	public boolean cytoscapeToChimera;

	public SyncColorsTask(StructureManager structureManager, CyNetworkView networkView) {
		this.structureManager = structureManager;
		this.networkView = networkView;
		chimeraToCytoscape = false;
		cytoscapeToChimera = false;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (chimeraToCytoscape) {
			structureManager.mapChimeraColorToCytoscape(networkView);
		} else {

		}
	}

}
