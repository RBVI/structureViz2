package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.structureViz2.internal.model.RINManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class SyncColorsTask extends AbstractTask {

	private StructureManager structureManager;
	private RINManager rinManager;
	private CyNetworkView networkView;

	@Tunable(description = "Apply colors from current network view to associated Chimera models", gravity = 1.0, dependsOn = "chimeraToCytoscape=false")
	public boolean cytoscapeToChimera;

	@Tunable(description = "Apply colors from associated Chimera models to current network view", gravity = 2.0, dependsOn = "cytoscapeToChimera=false")
	public boolean chimeraToCytoscape;

	public SyncColorsTask(StructureManager structureManager, CyNetworkView networkView) {
		this.structureManager = structureManager;
		this.rinManager = structureManager.getRINManager();
		this.networkView = networkView;
		chimeraToCytoscape = false;
		cytoscapeToChimera = false;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Synchronizing Colors with Chimera");
		if (chimeraToCytoscape) {
			taskMonitor
					.setStatusMessage("Applying colors from current network view to associated Chimera models ...");
			rinManager.syncChimToCyColors(networkView);
		} else {
			taskMonitor
					.setStatusMessage("Applying colors from associated Chimera models to current network view ...");
			rinManager.syncCyToChimColors(networkView);
		}
	}

}
