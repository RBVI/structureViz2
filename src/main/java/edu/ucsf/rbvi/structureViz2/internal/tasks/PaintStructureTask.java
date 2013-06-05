package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.io.File;

import java.net.URL;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class PaintStructureTask extends AbstractTask {
	private View<CyNode> nodeView;
	private CyNetworkView netView;
	private StructureManager structureManager;
	public static final String IMAGE_COLUMN="_chimeraImage";

	public PaintStructureTask(View<CyNode> nodeView, CyNetworkView netView, 
	                          StructureManager structureManager) {
		this.netView = netView;
		this.nodeView = nodeView;
		this.structureManager = structureManager;
	}

	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle("Painting Image");
		taskMonitor.setStatusMessage("Painting image ...");

		File imageFile = structureManager.saveChimeraImage();
		System.out.println("imageFile is at "+imageFile.getAbsolutePath());
		if (imageFile == null) {
			// Inform the user somehow
			return;
		}
		
		// Convert the file to a URL
		URL imageURL;
		try {
			imageURL = imageFile.toURI().toURL();
		} catch (Exception e) {
			// Inform user and bail
			return;
		}

		// Create our image column
		CyTable nodeTable = netView.getModel().getDefaultNodeTable();
		if (nodeTable.getColumn(IMAGE_COLUMN) == null) {
			nodeTable.createColumn(IMAGE_COLUMN, String.class, false);
		}
		nodeTable.getRow(nodeView.getModel().getSUID()).set(IMAGE_COLUMN, imageURL.toString());

		// Create a passthrough mapping to that column

	}
}
