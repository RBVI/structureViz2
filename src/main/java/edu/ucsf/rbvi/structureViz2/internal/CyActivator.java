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
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.EdgeViewTaskFactory;
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
import edu.ucsf.rbvi.structureViz2.internal.tasks.CloseStructuresEdgeTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.CloseStructuresTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.CreateStructureNetworkTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.ExitChimeraTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.LaunchChimeraTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.OpenStructureNavigatorTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.OpenStructuresEdgeTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.OpenStructuresTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.PaintStructureTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.SendCommandTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.StructureVizSettingsTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.tasks.SyncColorsTaskFactory;

// TODO: [Optional] Add task for opening and closing the molecular navigator dialog
// TODO: [3.1] Improve non-gui mode
// TODO: [!] Use logger to log messages

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

		// Get a handle on the CyServiceRegistrar
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);

		// Create and register our listeners
		// Listens for changes in selection and attributes we are interested in
		CySelectionListener selectionListener = new CySelectionListener(structureManager);
		registerService(bc, selectionListener, RowsSetListener.class, new Properties());
		// Listens for new networks added and network destroyed
		CyNetworkListener networkListener = new CyNetworkListener(structureManager);
		registerService(bc, networkListener, NetworkAddedListener.class, new Properties());
		registerService(bc, networkListener, NetworkAboutToBeDestroyedListener.class,
				new Properties());
		// Listens for nodes/edges to be removed
		CyIdentifiableListener cyIdentifiableListener = new CyIdentifiableListener(structureManager);
		registerService(bc, cyIdentifiableListener, AboutToRemoveNodesListener.class,
				new Properties());
		registerService(bc, cyIdentifiableListener, AboutToRemoveEdgesListener.class,
				new Properties());

		// Menu task factories
		TaskFactory openStructures = new OpenStructuresTaskFactory(structureManager);
		Properties openStructuresProps = new Properties();
		openStructuresProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		openStructuresProps.setProperty(TITLE, "Open Structures For Node(s)");
		openStructuresProps.setProperty(COMMAND, "openStructuresNodes");
		openStructuresProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		openStructuresProps.setProperty(ENABLE_FOR, "networkAndView");
		openStructuresProps.setProperty(IN_MENU_BAR, "true");
		openStructuresProps.setProperty(MENU_GRAVITY, "1.0");
		registerService(bc, openStructures, NodeViewTaskFactory.class, openStructuresProps);
		registerService(bc, openStructures, NetworkViewTaskFactory.class, openStructuresProps);

		TaskFactory openStructuresEdge = new OpenStructuresEdgeTaskFactory(structureManager);
		Properties openStructuresEdgeProps = new Properties();
		openStructuresEdgeProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		openStructuresEdgeProps.setProperty(TITLE, "Open Structures For Edge(s)");
		openStructuresEdgeProps.setProperty(COMMAND, "openStructuresEdges");
		openStructuresEdgeProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		openStructuresEdgeProps.setProperty(ENABLE_FOR, "networkAndView");
		openStructuresEdgeProps.setProperty(IN_MENU_BAR, "true");
		openStructuresEdgeProps.setProperty(MENU_GRAVITY, "1.0");
		registerService(bc, openStructuresEdge, EdgeViewTaskFactory.class, openStructuresEdgeProps);
		registerService(bc, openStructuresEdge, NetworkViewTaskFactory.class,
				openStructuresEdgeProps);

		TaskFactory alignStructures = new AlignStructuresTaskFactory(structureManager);
		Properties alignStructuresProps = new Properties();
		alignStructuresProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		alignStructuresProps.setProperty(TITLE, "Align Structures");
		alignStructuresProps.setProperty(COMMAND, "alignStructures");
		alignStructuresProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		alignStructuresProps.setProperty(ENABLE_FOR, "networkAndView");
		alignStructuresProps.setProperty(IN_MENU_BAR, "true");
		alignStructuresProps.setProperty(MENU_GRAVITY, "2.0");
		registerService(bc, alignStructures, NodeViewTaskFactory.class, alignStructuresProps);
		registerService(bc, alignStructures, NetworkViewTaskFactory.class, alignStructuresProps);

		// Note that this isn't in the main menu since it only applies to a particular
		// node.
		TaskFactory paintStructure = new PaintStructureTaskFactory(registrar, structureManager);
		Properties paintStructureProps = new Properties();
		paintStructureProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		paintStructureProps.setProperty(TITLE, "Paint Structure onto Node");
		paintStructureProps.setProperty(COMMAND, "paintStructure");
		paintStructureProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		paintStructureProps.setProperty(ENABLE_FOR, "networkAndView");
		paintStructureProps.setProperty(IN_MENU_BAR, "false");
		paintStructureProps.setProperty(MENU_GRAVITY, "3.0");
		registerService(bc, paintStructure, NodeViewTaskFactory.class, paintStructureProps);

		TaskFactory closeStructures = new CloseStructuresTaskFactory(structureManager);
		Properties closeStructuresProps = new Properties();
		closeStructuresProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		closeStructuresProps.setProperty(TITLE, "Close Structures For Node(s)");
		closeStructuresProps.setProperty(COMMAND, "closeStructuresNodes");
		closeStructuresProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		closeStructuresProps.setProperty(ENABLE_FOR, "networkAndView");
		closeStructuresProps.setProperty(IN_MENU_BAR, "true");
		closeStructuresProps.setProperty(MENU_GRAVITY, "4.0");
		registerService(bc, closeStructures, NodeViewTaskFactory.class, closeStructuresProps);
		registerService(bc, closeStructures, NetworkViewTaskFactory.class, closeStructuresProps);

		TaskFactory closeStructuresEdge = new CloseStructuresEdgeTaskFactory(structureManager);
		Properties closeStructuresEdgeProps = new Properties();
		closeStructuresEdgeProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		closeStructuresEdgeProps.setProperty(TITLE, "Close Structures For Edge(s)");
		closeStructuresEdgeProps.setProperty(COMMAND, "closeStructuresEdges");
		closeStructuresEdgeProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		closeStructuresEdgeProps.setProperty(ENABLE_FOR, "networkAndView");
		closeStructuresEdgeProps.setProperty(IN_MENU_BAR, "true");
		closeStructuresEdgeProps.setProperty(MENU_GRAVITY, "4.0");
		registerService(bc, closeStructuresEdge, EdgeViewTaskFactory.class,
				closeStructuresEdgeProps);
		registerService(bc, closeStructuresEdge, NetworkViewTaskFactory.class,
				closeStructuresEdgeProps);

		TaskFactory createStructureNet = new CreateStructureNetworkTaskFactory(structureManager);
		Properties createStructureNetProps = new Properties();
		createStructureNetProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		createStructureNetProps.setProperty(TITLE, "Create Residue Network");
		createStructureNetProps.setProperty(COMMAND, "createRIN");
		createStructureNetProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		createStructureNetProps.setProperty(ENABLE_FOR, "network");
		createStructureNetProps.setProperty(IN_MENU_BAR, "true");
		createStructureNetProps.setProperty(MENU_GRAVITY, "5.0");
		createStructureNetProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		registerService(bc, createStructureNet, TaskFactory.class, createStructureNetProps);
		structureManager.setCreateStructureNetFactory(createStructureNet);

		NetworkTaskFactory annotateFactory = new AnnotateStructureNetworkTaskFactory(
				structureManager);
		Properties annotateProps = new Properties();
		annotateProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		annotateProps.setProperty(TITLE, "Annotate Residue Network");
		annotateProps.setProperty(COMMAND, "annotateRIN");
		annotateProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		annotateProps.setProperty(IN_MENU_BAR, "true");
		annotateProps.setProperty(ENABLE_FOR, "network");
		annotateProps.setProperty(MENU_GRAVITY, "6.0");
		registerService(bc, annotateFactory, NetworkTaskFactory.class, annotateProps);

		NetworkViewTaskFactory syncColorsFactory = new SyncColorsTaskFactory(structureManager);
		Properties syncColorsProps = new Properties();
		syncColorsProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		syncColorsProps.setProperty(TITLE, "Syncronize Colors");
		syncColorsProps.setProperty(IN_MENU_BAR, "true");
		syncColorsProps.setProperty(COMMAND, "syncColors");
		syncColorsProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		syncColorsProps.setProperty(ENABLE_FOR, "networkAndView");
		syncColorsProps.setProperty(MENU_GRAVITY, "7.0");
		registerService(bc, syncColorsFactory, NetworkViewTaskFactory.class, syncColorsProps);

		TaskFactory launchChimera = new LaunchChimeraTaskFactory(structureManager);
		Properties launchChimeraProps = new Properties();
		launchChimeraProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		launchChimeraProps.setProperty(TITLE, "Launch Chimera");
		launchChimeraProps.setProperty(COMMAND, "launchChimera");
		launchChimeraProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		launchChimeraProps.setProperty(IN_MENU_BAR, "true");
		launchChimeraProps.setProperty(MENU_GRAVITY, "8.0");
		launchChimeraProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		registerService(bc, launchChimera, TaskFactory.class, launchChimeraProps);

		TaskFactory openDialog = new OpenStructureNavigatorTaskFactory(structureManager);
		Properties openDialogProps = new Properties();
		openDialogProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		openDialogProps.setProperty(TITLE, "Open Structure Navigator");
		openDialogProps.setProperty(COMMAND, "openStructureNavigator");
		openDialogProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		openDialogProps.setProperty(IN_MENU_BAR, "true");
		openDialogProps.setProperty(MENU_GRAVITY, "9.0");
		registerService(bc, openDialog, TaskFactory.class, openDialogProps);		
		
		TaskFactory exitChimera = new ExitChimeraTaskFactory(structureManager);
		Properties exitChimeraProps = new Properties();
		exitChimeraProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		exitChimeraProps.setProperty(TITLE, "Exit Chimera");
		exitChimeraProps.setProperty(COMMAND, "exitChimera");
		exitChimeraProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		exitChimeraProps.setProperty(IN_MENU_BAR, "true");
		exitChimeraProps.setProperty(MENU_GRAVITY, "10.0");
		registerService(bc, exitChimera, TaskFactory.class, exitChimeraProps);

		StructureVizSettingsTaskFactory settingsTask = new StructureVizSettingsTaskFactory(
				structureManager);
		Properties settingsProps = new Properties();
		settingsProps.setProperty(PREFERRED_MENU, "Apps.StructureViz");
		settingsProps.setProperty(TITLE, "Settings...");
		settingsProps.setProperty(COMMAND, "set");
		settingsProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		settingsProps.setProperty(IN_MENU_BAR, "true");
		// settingsProps.setProperty(ENABLE_FOR, "network");
		settingsProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		settingsProps.setProperty(MENU_GRAVITY, "11.0");
		registerService(bc, settingsTask, NetworkTaskFactory.class, settingsProps);

		// Command task factories
		TaskFactory sendCommandTaskFactory = new SendCommandTaskFactory(structureManager);
		Properties commandProps = new Properties();
		commandProps.setProperty(COMMAND, "sendCommand");
		commandProps.setProperty(COMMAND_NAMESPACE, "structureViz");
		registerService(bc, sendCommandTaskFactory, TaskFactory.class, commandProps);

	}

}
