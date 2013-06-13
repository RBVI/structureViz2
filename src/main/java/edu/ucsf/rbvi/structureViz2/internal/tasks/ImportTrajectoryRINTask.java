package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.io.BufferedReader;
import java.io.File;
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
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.structureViz2.internal.model.CytoUtils;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class ImportTrajectoryRINTask extends AbstractTask {

	private StructureManager structureManager;
	private String trajInfo;
	private CyNetwork newNetwork;
	private Map<String, CyNode> nodesMap;
	private VisualStyle visualStyle;

	public ImportTrajectoryRINTask(StructureManager structureManager, String trajInfo) {
		this.structureManager = structureManager;
		this.trajInfo = trajInfo;
		newNetwork = null;
		nodesMap = null;
		visualStyle = null;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (trajInfo == null) {
			return;
		}
		// Trajectory residue network info: [1, u'c:\\users\\admini~1\\appdata\\local\\temp
		// \\tmpytlcfw_network.txt', u'c:\\users\\admini~1\\appdata\\local\\temp\\tmpbth1xp
		// _nattr.txt', u'c:\\users\\admini~1\\appdata\\local\\temp\\tmplf0znj_netviz.xml']
		// remove leading and trailing brackets
		trajInfo = trajInfo.substring(trajInfo.indexOf("[") + 1, trajInfo.indexOf("]"));
		// System.out.println(trajInfo);
		// get file names
		String[] fileNames = trajInfo.split(",");
		String networkFile = null;
		String tableFile = null;
		String vizmapFile = null;
		String networkName = "";
		if (fileNames.length == 4 && fileNames[0].trim().equals("1")) {
			// get file names
			networkFile = fileNames[1].trim();
			tableFile = fileNames[2].trim();
			vizmapFile = fileNames[3].trim();
		} else if (fileNames.length == 5 && fileNames[0].trim().equals("2")) {
			networkName = fileNames[1].trim();
			networkName = networkName.substring(1, networkName.length() - 1);
			networkFile = fileNames[2].trim();
			tableFile = fileNames[3].trim();
			vizmapFile = fileNames[4].trim();
		}
		if (networkFile != null) {
			// import network
			networkFile = networkFile.substring(networkFile.indexOf("'") + 1,
					networkFile.length() - 1);
			// System.out.println(networkFile);
			if (networkFile.endsWith("network.txt")) {
				// import network data
				importNetwork(networkName, networkFile);
			}
		}
		if (tableFile != null) {
			// import table
			tableFile = tableFile.substring(tableFile.indexOf("'") + 1, tableFile.length() - 1);
			// System.out.println(tableFile);
			if (tableFile.endsWith("nattr.txt") && newNetwork != null) {
				// import node attributes
				importTable(tableFile);
			}
		}
		if (vizmapFile != null) {
			// load vizmap file
			vizmapFile = vizmapFile.substring(vizmapFile.indexOf("'") + 1, vizmapFile.length() - 1);
			if (vizmapFile.endsWith("netviz.xml") && newNetwork != null) {
				// import vizmap file
				// System.out.println("Load visual style: " + vizmapFile);
				LoadVizmapFileTaskFactory loadVizmapFileTaskFactory = (LoadVizmapFileTaskFactory) structureManager
						.getService(LoadVizmapFileTaskFactory.class);
				Set<VisualStyle> vsSet = loadVizmapFileTaskFactory.loadStyles(new File(vizmapFile));
				// we assume there is only one visual style
				// TODO: Consider multiple visual styles?
				for (VisualStyle style : vsSet) {
					// System.out.println(style.getTitle());
					// if (style.getTitle().equals("Trajectory network style")) {
					visualStyle = style;
					// }
				}
			}
		}
		if (newNetwork.getNodeCount() > 0) {
			finalizeNetwork();
		}
	}

	private void importNetwork(String name, String file) {
		// System.out.println("Import network " + file);
		if (name.equals("")) {
			name = file.substring(file.lastIndexOf("\\") + 1);
		}
		CyNetworkFactory netFactory = (CyNetworkFactory) structureManager
				.getService(CyNetworkFactory.class);
		newNetwork = netFactory.createNetwork();
		newNetwork.getDefaultEdgeTable().createColumn("Weight", Double.class, false);
		newNetwork.getRow(newNetwork).set(CyNetwork.NAME, name);
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
	}

	private void importTable(String file) {
		// System.out.println("Import table " + file);
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
		VisualMappingManager cyVmManager = (VisualMappingManager) structureManager
				.getService(VisualMappingManager.class);
		if (visualStyle == null) {
			visualStyle = cyVmManager.getDefaultVisualStyle();
		}
		visualStyle.apply(view);
		cyVmManager.setVisualStyle(visualStyle, view);

		// update view
		view.updateView();
	}

}
