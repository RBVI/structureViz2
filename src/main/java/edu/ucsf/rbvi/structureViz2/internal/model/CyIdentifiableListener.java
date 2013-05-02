package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.events.AboutToRemoveEdgesListener;
import org.cytoscape.model.events.AboutToRemoveNodesEvent;
import org.cytoscape.model.events.AboutToRemoveNodesListener;

public class CyIdentifiableListener implements AboutToRemoveNodesListener,
		AboutToRemoveEdgesListener {

	private StructureManager structureManager;

	public CyIdentifiableListener(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public void handleEvent(AboutToRemoveEdgesEvent e) {
		if (structureManager.getChimeraManager().isChimeraLaunched()) {
			structureManager.deassociate(new HashSet<CyIdentifiable>(e.getEdges()), true);
		}
	}

	public void handleEvent(AboutToRemoveNodesEvent e) {
		if (structureManager.getChimeraManager().isChimeraLaunched()) {
			Set<CyIdentifiable> objects = new HashSet<CyIdentifiable>();
			objects.addAll(e.getNodes());
			for (CyNode node : e.getNodes()) {
				if (e.getSource() != null) {
					objects.addAll(e.getSource().getAdjacentEdgeList(node, Type.ANY));
				}
			}
			structureManager.deassociate(objects, true);
		}
	}

}
