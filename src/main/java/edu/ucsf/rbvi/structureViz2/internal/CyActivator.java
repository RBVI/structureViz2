package edu.ucsf.rbvi.structureViz2.internal;

import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.work.TaskFactory;
import static org.cytoscape.work.ServiceProperties.*;

import edu.ucsf.rbvi.structureViz2.internal.tasks.OpenStructuresTaskFactory;

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

		TaskFactory openStructures = new OpenStructuresTaskFactory();
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

	}
}
