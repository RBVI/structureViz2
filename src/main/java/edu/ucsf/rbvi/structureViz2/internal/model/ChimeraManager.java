package edu.ucsf.rbvi.structureViz2.internal.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ucsf.rbvi.structureViz2.internal.port.ListenerThreads;

/**
 * This object maintains the Chimera communication information.
 */
public class ChimeraManager {

	static private Process chimera;
	static private ListenerThreads chimeraListenerThreads;
	static private List<ChimeraModel> currentModels;
	static private Map<Integer, ChimeraModel> currentModelsMap;

	// TODO: can we just get the logger from Cytoscape or do we have to pass it as
	// a context object?
	static private Logger logger;
	private StructureManager structureManager;

	public ChimeraManager(StructureManager structureManager) {
		this.structureManager = structureManager;
		logger = LoggerFactory.getLogger(edu.ucsf.rbvi.structureViz2.internal.CyActivator.class);
		chimera = null;
		chimeraListenerThreads = null;
		currentModels = new ArrayList<ChimeraModel>();
		currentModelsMap = new HashMap<Integer, ChimeraModel>();
	}

	public void openStructure(String modelName, Structure structure, boolean update) {
		if (!isChimeraLaunched()) {
			launchChimera();
		}
		// if (structure.getType() == Structure.StructureType.MODBASE_MODEL)
		// chimeraSend("listen stop models; listen stop selection; open "+structure.modelNumber()+" modbase:"+structure.name());
		// else if (structure.getType() == Structure.StructureType.SMILES)
		// chimeraSend("listen stop models; listen stop selection; open "+structure.modelNumber()+" smiles:"+structure.name());
		// else
		sendChimeraCommand("listen stop models; listen stop selection", false);
		List<String> response = sendChimeraCommand("open " + modelName, true);
		if (response != null) {
			// get model number from chimera?
			for (String info : response) {
				System.out.println(info);
			}
		}
	}

	private List<String> sendChimeraCommand(String command, boolean reply) {
		if (!isChimeraLaunched()) {
			return null;
		}

		chimeraListenerThreads.clearResponse(command);
		String text = command.concat("\n");
		System.out.println("send command to chimera: " + text);
		try {
			// send the command
			chimera.getOutputStream().write(text.getBytes());
			chimera.getOutputStream().flush();
		} catch (IOException e) {
			logger.info("Unable to execute command: "+text);
			logger.info("Exiting...");
			chimera = null;
			// TODO: clear objects
			// if (mnDialog != null)
			// mnDialog.lostChimera();
		}
		if (!reply) {
			return null;
		}
		return chimeraListenerThreads.getResponse(command);
	}

	private boolean isChimeraLaunched() {
		if (chimera != null) {
			return true;
		}
		return false;
	}

	// TODO: maybe this should be a private method
	private boolean launchChimera() {
		// Do nothing if Chimera is already launched
		if (isChimeraLaunched()) {
			return true;
		}

		// Try to launch Chimera (eventually using one of the possible paths)
		// TODO: get the path from cytoscape, e.g. user input and save it in the
		// StructureManager
		// String chiemraPath = structureManager.getChimeraPath();
		List<String> chimeraPaths = new ArrayList<String>(1);
		chimeraPaths.add("/usr/bin/chimera");
		String error = "";
		// iterate over possible paths for starting Chimera
		for (String chimeraPath : chimeraPaths) {
			try {
				List<String> args = new ArrayList<String>();
				args.add(chimeraPath);
				args.add("--start");
				args.add("ReadStdin");
				ProcessBuilder pb = new ProcessBuilder(args);
				chimera = pb.start();
			} catch (Exception e) {
				// Chimera could not be started
				error = e.getMessage();
			}
		}
		// If no error, then Chimera was launched successfully
		if (error.length() == 0) {
			// Initialize the listener threads
			chimeraListenerThreads = new ListenerThreads(chimera);
			chimeraListenerThreads.start();
			// Ask Chimera to give us updates
			sendChimeraCommand("listen start models; listen start selection", true);
			return true;
		}

		// Tell the suer that Chimera could not be started because of an error
		System.out.println(error);
		return false;
	}

}
