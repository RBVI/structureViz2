package edu.ucsf.rbvi.structureViz2.internal.model;

import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;

public class CyNetworkListener implements NetworkAboutToBeDestroyedListener {

	private StructureManager structureManager;

	public CyNetworkListener(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		structureManager.deassociate(e.getNetwork());
	}

}
