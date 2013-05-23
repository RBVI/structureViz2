package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class AnnotateStructureNetworkTask extends AbstractTask {

	private StructureManager structureManager;
	private CyNetwork network;

	@Tunable(description = "Available residue attributes")
	public ListMultipleSelection<String> residueAttributes;

	public AnnotateStructureNetworkTask(StructureManager structureManager, CyNetwork aNetwork) {
		this.structureManager = structureManager;
		network = aNetwork;
		residueAttributes = new ListMultipleSelection<String>(
				structureManager.getAllResidueAttributes());
	}

	@ProvidesTitle
	public String getTitle() {
		return "Annotate Structure Network Options";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO: add data type for each attribute
		taskMonitor.setTitle("Annotate structure network");
		if (residueAttributes.getSelectedValues().size() > 0) {
			for (String resAttr : residueAttributes.getSelectedValues()) {
				taskMonitor.setStatusMessage("Getting data for attribute " + resAttr + " ...");
				if (resAttr.equals("SecondaryStructure")) {
					structureManager.annotateSS(network);
				} else {
					structureManager.annotate(network, resAttr, String.class);
				}
			}
		}
	}
}
