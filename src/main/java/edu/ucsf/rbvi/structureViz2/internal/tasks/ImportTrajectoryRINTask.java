package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.structureViz2.internal.model.CytoUtils;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class ImportTrajectoryRINTask extends AbstractTask {

	private StructureManager structureManager;
	private String fileNames;
	private CyNetwork newNetwork;
	private Map<String, CyNode> nodesMap;

	public ImportTrajectoryRINTask(StructureManager structureManager, String fileNames) {
		this.structureManager = structureManager;
		this.fileNames = fileNames;
		newNetwork = null;
		nodesMap = null;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (fileNames == null) {
			return;
		}
		System.out.println(fileNames);
		String[] fileNamesParts = fileNames.split(",");
		if (fileNamesParts.length == 2) {
			String networkFile = fileNamesParts[0].trim();
			networkFile = networkFile.substring(networkFile.indexOf("'") + 1, networkFile.length() - 1);
			System.out.println(networkFile);
			if (networkFile.endsWith("network.txt")) {
				// import network data
				importNetwork(networkFile);
			}
			String tableFile = fileNamesParts[1].trim();
			tableFile = tableFile.substring(tableFile.indexOf("'") + 1, tableFile.length() - 2);
			System.out.println(tableFile);
			if (tableFile.endsWith("nattr.txt") && newNetwork != null) {
				// import node attributes
				importTable(tableFile);
			}
		}
	}

	private void importNetwork(String file) {
		System.out.println("Import network " + file);
		CyNetworkFactory netFactory = (CyNetworkFactory) structureManager
				.getService(CyNetworkFactory.class);
		newNetwork = netFactory.createNetwork();
		newNetwork.getDefaultEdgeTable().createColumn("Weight", Double.class, false);
		newNetwork.getRow(newNetwork).set(CyNetwork.NAME, file);
		BufferedReader br = null;
		nodesMap = new HashMap<String, CyNode>();
		try {
			br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] words = line.split("\t");
				if (words.length != 4) {
					continue;
				}
				CyNode source = null;
				CyNode target = null;
				if (!nodesMap.containsKey(words[0])) {
					source = newNetwork.addNode();
					nodesMap.put(words[0], source);
					newNetwork.getRow(source).set(CyNetwork.NAME, words[0]);
				} else {
					source = nodesMap.get(words[0]);
				}
				if (!nodesMap.containsKey(words[2])) {
					target = newNetwork.addNode();
					nodesMap.put(words[2], target);
					newNetwork.getRow(target).set(CyNetwork.NAME, words[2]);
				} else {
					target = nodesMap.get(words[2]);
				}
				if (source != null && target != null) {
					CyEdge edge = newNetwork.addEdge(source, target, true);
					newNetwork.getRow(edge).set(
							CyNetwork.NAME,
							CytoUtils.getNodeName(newNetwork, source) + " (" + words[1] + ") "
									+ CytoUtils.getNodeName(newNetwork, target));
					newNetwork.getRow(edge).set(CyEdge.INTERACTION, words[1]);
					newNetwork.getRow(edge).set("Weight", new Double(words[3]));
				}
			}
			br.close();
		} catch (Exception ex) {
			// ignore, parsing failed
			ex.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ex) {
				// ignore
			}
		}
		if (newNetwork.getNodeCount() > 0) {
			finalizeNetwork();
		}
	}

	private void importTable(String file) {
		System.out.println("Import table " + file);
		CyTable table = newNetwork.getDefaultNodeTable();
		BufferedReader br = null;
		String attr1 = "ChimeraResidue";
		String attr2 = "ModelName";
		table.createColumn(attr1, String.class, false);
		table.createColumn(attr2, String.class, false);
		try {
			br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] words = line.split("\t");
				if (words.length != 3) {
					continue;
				}
				if (nodesMap.containsKey(words[0])) {
					CyNode node = nodesMap.get(words[0]);
					table.getRow(node.getSUID()).set(attr1, words[1]);
					table.getRow(node.getSUID()).set(attr2, words[2]);
				}
			}
			br.close();
		} catch (Exception ex) {
			// ignore, parsing failed
			ex.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ex) {
				// ignore
			}
		}
	}

	private void finalizeNetwork() {
		// get factories and managers
		CyNetworkManager cyNetworkManager = (CyNetworkManager) structureManager
				.getService(CyNetworkManager.class);
		CyNetworkViewFactory cyNetworkViewFactory = (CyNetworkViewFactory) structureManager
				.getService(CyNetworkViewFactory.class);
		CyNetworkViewManager cyNetworkViewManager = (CyNetworkViewManager) structureManager
				.getService(CyNetworkViewManager.class);
		ApplyPreferredLayoutTaskFactory layoutTaskFactory = (ApplyPreferredLayoutTaskFactory) structureManager
				.getService(ApplyPreferredLayoutTaskFactory.class);
		TaskManager<?, ?> taskManager = (TaskManager<?, ?>) structureManager
				.getService(TaskManager.class);

		// register network
		cyNetworkManager.addNetwork(newNetwork);

		// Create network view
		CyNetworkView view = cyNetworkViewFactory.createNetworkView(newNetwork);
		cyNetworkViewManager.addNetworkView(view);
		Set<CyNetworkView> views = new HashSet<CyNetworkView>();
		views.add(view);

		// Do a layout
		taskManager.execute(layoutTaskFactory.createTaskIterator(views));

		// Set vizmap
		// ...
		view.updateView();
	}

}
