package edu.ucsf.rbvi.structureViz2.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;
import edu.ucsf.rbvi.structureViz2.internal.tasks.CloseStructuresTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.OpenStructuresTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.StructureVizSettingsTaskFactory;

public class CyActivator extends AbstractCyActivator {
	private static Logger logger = LoggerFactory
			.getLogger(edu.ucsf.rbvi.structureViz2.internal.CyActivator.class);

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		// We'll need the CyApplication Manager to get current network,
		// etc.
		CyApplicationManager cyApplicationManager = getService(bc,
				CyApplicationManager.class);

		// We'll need the CyServiceRegistrar to register listeners
		CyServiceRegistrar cyServiceRegistrar = getService(bc, CyServiceRegistrar.class);

		// See if we have a graphics console or not
		boolean haveGUI = true;
		ServiceReference ref = bc
				.getServiceReference("org.cytoscape.application.swing.CySwingApplication");

		if (ref == null) {
			haveGUI = false;
			// Issue error and return
		}

		// We'll need two context objects to manage everything: the
		// Chimera interface itself, and a structure manager that helps
		// map from Chimera objects to Cytoscape objects

		// Create the structure manager. Note that later on, we'll
		// register it as a TaskFactory since it also provides various
		// settings
		StructureManager structureManager = new StructureManager();

		// Create the Chimera interface
		ChimeraManager chimeraManager = new ChimeraManager(structureManager);

		TaskFactory openStructures = new OpenStructuresTaskFactory(structureManager,
				chimeraManager);
		Properties openStructuresProps = new Properties();
		openStructuresProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		openStructuresProps.setProperty(TITLE, "Open Structures...");
		openStructuresProps.setProperty(COMMAND, "openStructures");
		openStructuresProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		openStructuresProps.setProperty(ENABLE_FOR, "networkAndView");
		openStructuresProps.setProperty(IN_TOOL_BAR, "true");
		// TODO: [bug] the order in the toolbar and context menu is
		// different and the numbers do not seem to have an effect on
		// the order.
		openStructuresProps.setProperty(MENU_GRAVITY, "1.0");
		registerService(bc, openStructures, NodeViewTaskFactory.class, openStructuresProps);
		registerService(bc, openStructures, NetworkViewTaskFactory.class,
				openStructuresProps);

		TaskFactory closeStructures = new CloseStructuresTaskFactory(structureManager,
				chimeraManager);
		Properties closeStructuresProps = new Properties();
		closeStructuresProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		closeStructuresProps.setProperty(TITLE, "Close Structures");
		openStructuresProps.setProperty(COMMAND, "closeStructures");
		openStructuresProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		openStructuresProps.setProperty(ENABLE_FOR, "networkAndView");
		openStructuresProps.setProperty(IN_TOOL_BAR, "true");
		openStructuresProps.setProperty(MENU_GRAVITY, "5.0");
		registerService(bc, closeStructures, NodeViewTaskFactory.class,
				closeStructuresProps);
		registerService(bc, closeStructures, NetworkViewTaskFactory.class,
				closeStructuresProps);

		StructureVizSettingsTaskFactory settingsTask = new StructureVizSettingsTaskFactory(
				structureManager);
		Properties settingsProps = new Properties();
		settingsProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		settingsProps.setProperty(TITLE, "Settings...");
		settingsProps.setProperty(COMMAND, "set");
		settingsProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		settingsProps.setProperty(IN_TOOL_BAR, "true");
		settingsProps.setProperty(ENABLE_FOR, "network");
		settingsProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		settingsProps.setProperty(MENU_GRAVITY, "9.9");
		registerService(bc, settingsTask, NetworkTaskFactory.class, settingsProps);

	}
}
