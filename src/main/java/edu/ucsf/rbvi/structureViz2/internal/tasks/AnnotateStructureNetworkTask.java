package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class AnnotateStructureNetworkTask extends AbstractTask {

	private StructureManager structureManager;
	private CyNetwork network;

	@Tunable(description = "Get secondary structure information")
	public boolean ssAnnot;

	public AnnotateStructureNetworkTask(StructureManager structureManager, CyNetwork aNetwork) {
		this.structureManager = structureManager;
		network = aNetwork;
		ssAnnot = true;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		structureManager.annotateSS(network);
	}

}
