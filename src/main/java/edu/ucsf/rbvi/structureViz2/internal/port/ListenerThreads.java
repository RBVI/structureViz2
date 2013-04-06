package edu.ucsf.rbvi.structureViz2.internal.port;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

/***************************************************
 *                 Thread Classes                  *
 **************************************************/

/**
 * Reply listener thread
 */
public class ListenerThreads extends Thread {
	private InputStream readChan = null;
	private BufferedReader lineReader = null;
	private Process chimera = null;
	private Map<String, List<String>> replyLog = null;
	private Logger logger;
	private StructureManager structureManager = null;

	/**
	 * Create a new listener thread to read the responses from Chimera
	 * 
	 * @param chimera
	 *          a handle to the Chimera Process
	 * @param log
	 *          a handle to a List to post the responses to
	 * @param chimeraObject
	 *          a handle to the Chimera Object
	 */
	public ListenerThreads(Process chimera, StructureManager structureManager) {
		this.chimera = chimera;
		this.structureManager = structureManager;
		replyLog = new HashMap<String, List<String>>();
		// Get a line-oriented reader
		readChan = chimera.getInputStream();
		lineReader = new BufferedReader(new InputStreamReader(readChan));
		logger = LoggerFactory.getLogger(edu.ucsf.rbvi.structureViz2.internal.CyActivator.class);
	}

	/**
	 * Start the thread running
	 */
	public void run() {
		// System.out.println("ReplyLogListener running");
		while (true) {
			try {
				chimeraRead();
			} catch (IOException e) {
				logger.info("UCSF Chimera has exited: " + e.getMessage());
				return;
			}
		}
	}

	public List<String> getResponse(String command) {
		List<String> reply;
		// System.out.println("getResponse: "+command);
		while (!replyLog.containsKey(command)) {
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException e) {
			}
		}

		synchronized (replyLog) {
			reply = replyLog.get(command);
			// System.out.println("getResponse ("+command+") = "+reply);
			replyLog.remove(command);
		}
		return reply;
	}

	public void clearResponse(String command) {
		try {
			Thread.currentThread().sleep(100);
		} catch (InterruptedException e) {
		}
		if (replyLog.containsKey(command))
			replyLog.remove(command);
		return;
	}

	/**
	 * Read input from Chimera
	 * 
	 * @return a List containing the replies from Chimera
	 */
	private void chimeraRead() throws IOException {
		if (chimera == null)
			return;

		String line = null;
		while ((line = lineReader.readLine()) != null) {
			// System.out.println("From Chimera-->"+line);
			if (line.startsWith("CMD")) {
				chimeraCommandRead(line.substring(4));
			} else if (line.startsWith("ModelChanged: ")) {
				(new ModelUpdater()).start();
			} else if (line.startsWith("SelectionChanged: ")) {
				(new SelectionUpdater()).start();
			}
		}
		return;
	}

	private void chimeraCommandRead(String command) throws IOException {
		// Generally -- looking for:
		// CMD command
		// ........
		// END
		// We return the text in between
		List<String> reply = new ArrayList<String>();
		boolean updateModels = false;
		boolean updateSelection = false;
		String line = null;

		synchronized (replyLog) {
			while ((line = lineReader.readLine()) != null) {
				// System.out.println("From Chimera ("+command+") -->"+line);
				if (line.startsWith("CMD")) {
					logger.error("Got unexpected command from Chimera: " + line);

				} else if (line.startsWith("END")) {
					break;
				}
				if (line.startsWith("ModelChanged: ")) {
					updateModels = true;
				} else if (line.startsWith("SelectionChanged: ")) {
					updateSelection = true;
				} else if (line.length() == 0) {
					continue;
				} else if (!line.startsWith("CMD")) {
					reply.add(line);
				}
			}
			replyLog.put(command, reply);
		}
		if (updateModels)
			(new ModelUpdater()).start();
		if (updateSelection)
			(new SelectionUpdater()).start();

		return;
	}

	/**
	 * Model updater thread
	 */
	class ModelUpdater extends Thread {

		public ModelUpdater() {
		}

		public void run() {
			// System.out.println("Model updated");
			structureManager.updateModels();
			structureManager.modelChanged();
			// Now update our selection from Chimera
			// (new SelectionUpdater()).start();
		}
	}

	/**
	 * Selection updater thread
	 */
	class SelectionUpdater extends Thread {

		public SelectionUpdater() {
		}

		public void run() {
			try {
				// System.out.println("Calling updateSelection");
				structureManager.chimeraSelectionChanged();
			} catch (Exception e) {
			}
		}
	}
}
