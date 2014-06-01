package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.RINManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class AnnotateStructureNetworkTask extends AbstractTask implements ObservableTask {

	private StructureManager structureManager;
	private RINManager rinManager;
	private CyNetwork network;

	@Tunable(description = "Available residue attributes")
	public ListMultipleSelection<String> residueAttributes = new ListMultipleSelection<String>("");

	public AnnotateStructureNetworkTask(StructureManager structureManager, CyNetwork aNetwork) {
		this.structureManager = structureManager;
		this.rinManager = structureManager.getRINManager();
		network = aNetwork;
		List<String> attrs = structureManager.getAllChimeraResidueAttributes();
		residueAttributes = new ListMultipleSelection<String>(attrs);
		residueAttributes.setSelectedValues(attrs);
	}

	@ProvidesTitle
	public String getTitle() {
		return "Annotations Options";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Annotating Residue Interaction Network");
		if (residueAttributes != null && residueAttributes.getSelectedValues().size() > 0) {
			taskMonitor.setStatusMessage("Getting attribute data from Chimera ...");
			for (String resAttr : residueAttributes.getSelectedValues()) {
				// taskMonitor.setStatusMessage("Getting data for attribute " + resAttr + " ...");
				if (resAttr.equals("SecondaryStructure")) {
					rinManager.annotateSS(network);
				} else if (resAttr.equals("Coordinates")) {
					rinManager.annotateCoord(network, "resCoord");
				} else {
					rinManager.annotate(network, resAttr, resAttr);
				}
			}
		}
	}

	// TODO: [Optional] How could we return annotations?
	public Object getResults(Class expectedClass) {
		if (expectedClass.equals(String.class)) {
			return "Finished";
		}
		return true;
	}
}
