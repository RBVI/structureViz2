package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.RINManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class AnnotateStructureNetworkTask extends AbstractTask {

	private RINManager rinManager;
	private CyNetwork network;

	@Tunable(description = "Available residue attributes")
	public ListMultipleSelection<String> residueAttributes;

	// TODO: Pass rinManager and not the structureManager?
	public AnnotateStructureNetworkTask(StructureManager structureManager, CyNetwork aNetwork) {
		this.rinManager = structureManager.getRINManager();
		network = aNetwork;
		residueAttributes = new ListMultipleSelection<String>(
				structureManager.getAllResidueAttributes());
	}

	@ProvidesTitle
	public String getTitle() {
		return "Annotations Options";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Annotate residue interaction network");
		if (residueAttributes.getSelectedValues().size() > 0) {
			for (String resAttr : residueAttributes.getSelectedValues()) {
				taskMonitor.setStatusMessage("Getting data for attribute " + resAttr + " ...");
				if (resAttr.equals("SecondaryStructure")) {
					rinManager.annotateSS(network);
				} else if (resAttr.equals("Coordinates")) {
					rinManager.annotateCoord(network, "resCoord");
				} else if (resAttr.equals("averageBFactor") || resAttr.equals("averageOccupancy")) {
					rinManager.annotate(network, resAttr, rinManager.getAttrCommand(resAttr));
				} else {
					rinManager.annotate(network, resAttr, resAttr);
				}
			}
		}
	}
}
