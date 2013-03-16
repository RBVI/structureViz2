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
	static private Map<String, ChimeraModel> currentModelNamesMap;
	static private Map<Integer, ChimeraModel> currentModelNumbersMap;

	static private Logger logger;

	private StructureManager structureManager;

	public ChimeraManager(StructureManager structureManager) {
		this.structureManager = structureManager;
		logger = LoggerFactory.getLogger(edu.ucsf.rbvi.structureViz2.internal.CyActivator.class);
		chimera = null;
		chimeraListenerThreads = null;
		currentModelNamesMap = new HashMap<String, ChimeraModel>();
		currentModelNumbersMap = new HashMap<Integer, ChimeraModel>();
	}

	public ChimeraModel getModel(String modelName) {
		if (currentModelNamesMap.containsKey(modelName)) {
			return currentModelNamesMap.get(modelName);
		}
		return null;
	}

	public ChimeraModel getModel(Integer modelNumber) {
		if (currentModelNumbersMap.containsKey(modelNumber)) {
			return currentModelNumbersMap.get(modelNumber);
		}
		return null;
	}

	public boolean hasChimeraModel(String modelName) {
		return currentModelNamesMap.containsKey(modelName);
	}

	public boolean hasChimeraModel(Integer modelNubmer) {
		return currentModelNumbersMap.containsKey(modelNubmer);
	}

	public ChimeraModel openModel(String modelName) {
		// if (structure.getType() == Structure.StructureType.MODBASE_MODEL)
		// chimeraSend("listen stop models; listen stop selection; open "+structure.modelNumber()+" modbase:"+structure.name());
		// else if (structure.getType() == Structure.StructureType.SMILES)
		// chimeraSend("listen stop models; listen stop selection; open "+structure.modelNumber()+" smiles:"+structure.name());
		// else
		sendChimeraCommand("listen stop models; listen stop selection", false);
		List<String> response = sendChimeraCommand("open " + modelName, true);
		if (response == null) {
			// something went wrong
			return null;
		}
		// get model number from chimera?
		int modelNumber = ChimUtils.parseModelNumber(response);
		// use a method for this
		System.out.println("model number for opened structure: " + modelNumber);
		ChimeraModel newModel = new ChimeraModel(modelNumber, modelName);
		currentModelNamesMap.put(modelName, newModel);
		currentModelNumbersMap.put(modelNumber, newModel);
		// get remaining model info and add it to object
		return newModel;
	}

	public void closeModel(ChimeraModel model) {
		// int model = structure.modelNumber();
		// int subModel = structure.subModelNumber();
		// Integer modelKey = makeModelKey(model, subModel);
		System.out.println("chimera close mdoel " + model.getName());
		if (currentModelNumbersMap.containsKey(model.getNumber())) {
			sendChimeraCommand("listen stop models; listen stop select; close " + model.getSpecNumber(),
					false);
			currentModelNamesMap.remove(model.getName());
			currentModelNumbersMap.remove(model.getNumber());
			// selectionList.remove(chimeraModel);
		} else {
			System.out.println("chimera cannot find model");
			// chimeraSend("listen stop models; listen stop select; close #" + model +
			// "." + subModel);
		}
		sendChimeraCommand("listen start models; listen start select", false);
	}

	public void clearOnChimeraExit() {
		chimera = null;
		currentModelNamesMap.clear();
		currentModelNumbersMap.clear();
		chimeraListenerThreads = null;
		// if (mnDialog != null)
		// mnDialog.lostChimera();
	}

	public void exitChimera() {
		if (isChimeraLaunched()) {
			sendChimeraCommand("stop really", false);
			chimera.destroy();
		}
		clearOnChimeraExit();
	}

	private List<String> sendChimeraCommand(String command, boolean reply) {
		// if (!isChimeraLaunched()) {
		// return null;
		// }

		chimeraListenerThreads.clearResponse(command);
		String text = command.concat("\n");
		System.out.println("send command to chimera: " + text);
		try {
			// send the command
			chimera.getOutputStream().write(text.getBytes());
			chimera.getOutputStream().flush();
		} catch (IOException e) {
			logger.info("Unable to execute command: " + text);
			logger.info("Exiting...");
			clearOnChimeraExit();
			return null;
		}
		if (!reply) {
			return null;
		}
		return chimeraListenerThreads.getResponse(command);
	}

	public boolean isChimeraLaunched() {
		// TODO: what is the best way to test if chimera is launched?
		if (chimera != null && sendChimeraCommand("test", true) != null) {
			return true;
		}
		return false;
	}

	public boolean launchChimera() {
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
			chimeraListenerThreads = new ListenerThreads(chimera, structureManager);
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
