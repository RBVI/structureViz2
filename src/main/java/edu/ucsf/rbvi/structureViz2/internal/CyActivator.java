package edu.ucsf.rbvi.structureViz2.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.events.AboutToRemoveEdgesListener;
import org.cytoscape.model.events.AboutToRemoveNodesListener;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ucsf.rbvi.structureViz2.internal.model.CyIdentifiableListener;
import edu.ucsf.rbvi.structureViz2.internal.model.CyNetworkListener;
import edu.ucsf.rbvi.structureViz2.internal.model.CySelectionListener;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;
import edu.ucsf.rbvi.structureViz2.internal.tasks.AlignStructuresTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.AnnotateStructureNetworkTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.CloseStructuresTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.CreateStructureNetworkTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.ExitChimeraTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.LaunchChimeraTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.OpenStructuresTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.SendCommandTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.StructureVizSettingsTaskFactory;

public class CyActivator extends AbstractCyActivator {
	private static Logger logger = LoggerFactory
			.getLogger(edu.ucsf.rbvi.structureViz2.internal.CyActivator.class);

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		// See if we have a graphics console or not
		boolean haveGUI = true;
		ServiceReference ref = bc.getServiceReference(CySwingApplication.class.getName());

		if (ref == null) {
			haveGUI = false;
			// Issue error and return
		}

		// Create the context object
		StructureManager structureManager = new StructureManager(bc, haveGUI);

		// Create and register our listeners
		CySelectionListener selectionListener = new CySelectionListener(structureManager);
		registerService(bc, selectionListener, RowsSetListener.class, new Properties());
		CyNetworkListener networkListener = new CyNetworkListener(structureManager);
		registerService(bc, networkListener, NetworkAddedListener.class, new Properties());
		registerService(bc, networkListener, NetworkAboutToBeDestroyedListener.class,
				new Properties());
		// TODO: Listen for new attribute values and not nodes/edges added
		CyIdentifiableListener cyIdentifiableListener = new CyIdentifiableListener(structureManager);
		registerService(bc, cyIdentifiableListener, AboutToRemoveNodesListener.class,
				new Properties());
		registerService(bc, cyIdentifiableListener, AboutToRemoveEdgesListener.class,
				new Properties());
		// TODO: Do we need to register with CyServiceRegistrar?

		// TODO: Add a task for opening the molecular navigator dialog

		// Menu task factories
		TaskFactory openStructures = new OpenStructuresTaskFactory(structureManager);
		Properties openStructuresProps = new Properties();
		openStructuresProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		openStructuresProps.setProperty(TITLE, "Open Structures...");
		openStructuresProps.setProperty(COMMAND, "openStructures");
		openStructuresProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		openStructuresProps.setProperty(ENABLE_FOR, "networkAndView");
		openStructuresProps.setProperty(IN_MENU_BAR, "true");
		openStructuresProps.setProperty(MENU_GRAVITY, "1.0");
		registerService(bc, openStructures, NodeViewTaskFactory.class, openStructuresProps);
		registerService(bc, openStructures, NetworkViewTaskFactory.class, openStructuresProps);

		TaskFactory alignStructures = new AlignStructuresTaskFactory(structureManager);
		Properties alignStructuresProps = new Properties();
		alignStructuresProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		alignStructuresProps.setProperty(TITLE, "Align Structures");
		alignStructuresProps.setProperty(COMMAND, "alignStructures");
		alignStructuresProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		alignStructuresProps.setProperty(ENABLE_FOR, "networkAndView");
		alignStructuresProps.setProperty(IN_MENU_BAR, "true");
		alignStructuresProps.setProperty(MENU_GRAVITY, "3.0");
		registerService(bc, alignStructures, NodeViewTaskFactory.class, alignStructuresProps);
		registerService(bc, alignStructures, NetworkViewTaskFactory.class, alignStructuresProps);

		TaskFactory createStructureNet = new CreateStructureNetworkTaskFactory(structureManager);
		Properties createStructureNetProps = new Properties();
		createStructureNetProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		createStructureNetProps.setProperty(TITLE, "Create Networks");
		createStructureNetProps.setProperty(COMMAND, "createStructureNetworks");
		createStructureNetProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		createStructureNetProps.setProperty(ENABLE_FOR, "network");
		createStructureNetProps.setProperty(IN_MENU_BAR, "true");
		createStructureNetProps.setProperty(MENU_GRAVITY, "5.0");
		registerService(bc, createStructureNet, TaskFactory.class, createStructureNetProps);
		structureManager.setCreateStructureNetFactory(createStructureNet);

		TaskFactory closeStructures = new CloseStructuresTaskFactory(structureManager);
		Properties closeStructuresProps = new Properties();
		closeStructuresProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		closeStructuresProps.setProperty(TITLE, "Close Structures");
		closeStructuresProps.setProperty(COMMAND, "closeStructures");
		closeStructuresProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		closeStructuresProps.setProperty(ENABLE_FOR, "networkAndView");
		closeStructuresProps.setProperty(IN_MENU_BAR, "true");
		closeStructuresProps.setProperty(MENU_GRAVITY, "7.0");
		registerService(bc, closeStructures, NodeViewTaskFactory.class, closeStructuresProps);
		registerService(bc, closeStructures, NetworkViewTaskFactory.class, closeStructuresProps);

		TaskFactory launchChimera = new LaunchChimeraTaskFactory(structureManager);
		Properties launchChimeraProps = new Properties();
		launchChimeraProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		launchChimeraProps.setProperty(TITLE, "Launch Chimera");
		launchChimeraProps.setProperty(COMMAND, "launchChimera");
		launchChimeraProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		launchChimeraProps.setProperty(IN_MENU_BAR, "true");
		launchChimeraProps.setProperty(MENU_GRAVITY, "8.0");
		registerService(bc, launchChimera, TaskFactory.class, launchChimeraProps);

		TaskFactory exitChimera = new ExitChimeraTaskFactory(structureManager);
		Properties exitChimeraProps = new Properties();
		exitChimeraProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		exitChimeraProps.setProperty(TITLE, "Exit Chimera");
		exitChimeraProps.setProperty(COMMAND, "exitChimera");
		exitChimeraProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		exitChimeraProps.setProperty(IN_MENU_BAR, "true");
		exitChimeraProps.setProperty(MENU_GRAVITY, "9.0");
		registerService(bc, exitChimera, TaskFactory.class, exitChimeraProps);

		// TaskFactory importNet = new
		// ImportTrajectoryRINTaskFactory(structureManager, null);
		// Properties importNetProps = new Properties();
		// importNetProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		// importNetProps.setProperty(TITLE, "Import Network from Chimera");
		// importNetProps.setProperty(IN_MENU_BAR, "true");
		// importNetProps.setProperty(MENU_GRAVITY, "11.0");
		// registerService(bc, importNet, TaskFactory.class, importNetProps);

		StructureVizSettingsTaskFactory settingsTask = new StructureVizSettingsTaskFactory(
				structureManager);
		Properties settingsProps = new Properties();
		settingsProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		settingsProps.setProperty(TITLE, "Settings...");
		settingsProps.setProperty(COMMAND, "set");
		settingsProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		settingsProps.setProperty(IN_MENU_BAR, "true");
		settingsProps.setProperty(ENABLE_FOR, "network");
		settingsProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		settingsProps.setProperty(MENU_GRAVITY, "10.0");
		registerService(bc, settingsTask, NetworkTaskFactory.class, settingsProps);

		// Command task factories
		TaskFactory sendCommandTaskFactory = new SendCommandTaskFactory(structureManager);
		Properties commandProps = new Properties();
		commandProps.setProperty(COMMAND, "sendCommand");
		commandProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		registerService(bc, sendCommandTaskFactory, TaskFactory.class, commandProps);

		TaskFactory annotateFactory = new AnnotateStructureNetworkTaskFactory(structureManager);
		Properties annotateProps = new Properties();
		annotateProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		annotateProps.setProperty(TITLE, "Annotate Structure Network");
		annotateProps.setProperty(COMMAND, "annotateRIN");
		annotateProps.setProperty(COMMAND_NAMESPACE, "rinalyzer");
		annotateProps.setProperty(IN_MENU_BAR, "true");
		annotateProps.setProperty(ENABLE_FOR, "network");
		registerService(bc, annotateFactory, NetworkTaskFactory.class, annotateProps);

	}

}
