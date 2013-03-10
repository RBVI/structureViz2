package edu.ucsf.rbvi.structureViz2.internal;

import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.work.TaskFactory;
import static org.cytoscape.work.ServiceProperties.*;

import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;
import edu.ucsf.rbvi.structureViz2.internal.tasks.OpenStructuresTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.StructureVizSettingsTaskFactory;

public class CyActivator extends AbstractCyActivator {
	private static Logger logger =
	    LoggerFactory.getLogger(edu.ucsf.rbvi.structureViz2.internal.CyActivator.class);

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		// We'll need the CyApplication Manager to get current network, etc.
		CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);

		// We'll need the CyServiceRegistrar to register listeners
		CyServiceRegistrar cyServiceRegistrar = getService(bc,CyServiceRegistrar.class);

		// See if we have a graphics console or not
		boolean haveGUI = true;
		ServiceReference ref =
		    bc.getServiceReference("org.cytoscape.application.swing.CySwingApplication");
		
		if (ref == null) {
			haveGUI = false;
			// Issue error and return
		}

		// We'll need two context objects to manage everything: the Chimera interface itself,
		// and a structure manager that helps map from Chimera objects to Cytoscape objects

		// Create the structure manager.  Note that later on, we'll register it as a TaskFactory
		// since it also provides various settings
		StructureManager structureManager = new StructureManager();

		// Create the Chimera interface
		ChimeraManager chimeraManager = new ChimeraManager(structureManager);

		TaskFactory openStructures = new OpenStructuresTaskFactory(structureManager, chimeraManager);
		Properties openStructuresProps = new Properties();
		openStructuresProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		openStructuresProps.setProperty(TITLE, "Open Structures...");
		openStructuresProps.setProperty(COMMAND, "openStructures");
		openStructuresProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		openStructuresProps.setProperty(ENABLE_FOR, "networkAndView");
		openStructuresProps.setProperty(IN_TOOL_BAR, "true");
		openStructuresProps.setProperty(MENU_GRAVITY, "1.0");
		registerService(bc, openStructures, NetworkViewTaskFactory.class, openStructuresProps);
		registerService(bc, openStructures, NodeViewTaskFactory.class, openStructuresProps);

		StructureVizSettingsTaskFactory settingsTask = 
		    new StructureVizSettingsTaskFactory(structureManager);
		Properties settingsProps = new Properties();
		settingsProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		settingsProps.setProperty(TITLE, "Settings...");
		settingsProps.setProperty(COMMAND, "set");
		settingsProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		settingsProps.setProperty(IN_TOOL_BAR, "true");
		settingsProps.setProperty(ENABLE_FOR, "network");
		settingsProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		settingsProps.setProperty(MENU_GRAVITY, "10.0");
		registerService(bc, structureManager, NetworkTaskFactory.class, settingsProps);
	}
}
