package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.io.File;
import java.net.URL;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class PaintStructureTask extends AbstractTask {
	private View<CyNode> nodeView;
	private CyNetworkView netView;
	private StructureManager structureManager;
	private CyServiceRegistrar registrar;
	public static final String IMAGE_COLUMN="_chimeraImage";

	public PaintStructureTask(View<CyNode> nodeView, CyNetworkView netView, CyServiceRegistrar registrar,
	                          StructureManager structureManager) {
		this.netView = netView;
		this.nodeView = nodeView;
		this.structureManager = structureManager;
		this.registrar = registrar;
	}

	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle("Painting Image");
		taskMonitor.setStatusMessage("Painting image ...");

		File imageFile = structureManager.saveChimeraImage();
		if (imageFile == null) {
			// Inform the user somehow
			return;
		}
		
		// Convert the file to a URL
		String imageString;
		try {
			imageString = "file://"+imageFile.toURI().getPath();
		} catch (Exception e) {
			// Inform user and bail
			return;
		}

		// Create our image column
		CyTable nodeTable = netView.getModel().getDefaultNodeTable();
		if (nodeTable.getColumn(IMAGE_COLUMN) == null) {
			nodeTable.createColumn(IMAGE_COLUMN, String.class, false);
		}
		nodeTable.getRow(nodeView.getModel().getSUID()).set(IMAGE_COLUMN, imageString);

		// Get the visual property 
		VisualMappingManager vmm = registrar.getService(VisualMappingManager.class);

		// A little ugly, but this is the best way I found to get the CUSTOM_GRAPHICS VisualProperty
		Set<VisualLexicon> lexSet = vmm.getAllVisualLexicon();
		VisualProperty<?> cgProp = null;
		for (VisualLexicon vl: lexSet) {
			cgProp = vl.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
			if (cgProp != null) break;
		}
		if (cgProp == null) {
			System.out.println("Can't find the CUSTOMGRAPHICS visual property!!!!");
			return;
		}

		// Create a passthrough mapping to that column
		VisualMappingFunctionFactory factory = registrar.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
		PassthroughMapping map = (PassthroughMapping) factory.createVisualMappingFunction(IMAGE_COLUMN, String.class, cgProp);

		// Get the current visual style
		VisualStyle style = vmm.getCurrentVisualStyle();

		// Add our map
		style.addVisualMappingFunction(map);
		style.apply(nodeTable.getRow(nodeView.getModel().getSUID()), nodeView);
	}
}
