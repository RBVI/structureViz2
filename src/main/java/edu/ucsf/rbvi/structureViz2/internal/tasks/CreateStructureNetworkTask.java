package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.ChimUtils;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraChain;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraManager;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraModel;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraResidue;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraStructuralObject;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class CreateStructureNetworkTask extends AbstractTask {

	@Tunable(description = "Include contacts")
	public boolean includeContacts;

	@Tunable(description = "Include clashes")
	public boolean includeClashes;

	@Tunable(description = "Include hydrogen bonds (overlaps with contacts)")
	public boolean includeHBonds;

	@Tunable(description = "Include connectivity")
	public boolean includeConnectivity;

	@Tunable(description = "Calculate connectivity distances (more time consuming)")
	public boolean includeConnectivityDistance;

	@Tunable(description = "Include interactions")
	public ListSingleSelection<String> includeInteracions;

	private static final String[] interactionArray = { "Between models",
			"Between selection & other models", "Between selection and all atoms" };

	static final String DISTANCE_ATTR = "MinimumDistance";
	static final String OVERLAP_ATTR = "MaximumOverlap";
	static final String RESIDUE_ATTR = "FunctionalResidues";
	static final String SEED_ATTR = "SeedResidue";
	static final String BACKBONE_ATTR = "BackboneInteraction";
	static final String SIDECHAIN_ATTR = "SideChainInteraction";
	static final String SMILES_ATTR = "SMILES";

	private StructureManager structureManager;
	private ChimeraManager chimeraManager;
	private CyNetworkFactory cyNetworkFactory;
	private CyNetworkManager cyNetworkManager;

	public CreateStructureNetworkTask(StructureManager structureManager,
			CyNetworkFactory cyNetworkFactory, CyNetworkManager cyNetworkManager) {
		this.structureManager = structureManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkManager = cyNetworkManager;
		this.chimeraManager = structureManager.getChimeraManager();
		includeContacts = false;
		includeClashes = false;
		includeHBonds = false;
		includeConnectivity = false;
		includeConnectivityDistance = false;
		includeInteracions = new ListSingleSelection<String>(interactionArray);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// Save selected nodes indexed by their name
		Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();
		// Create the network
		CyNetwork rin = cyNetworkFactory.createNetwork();
		rin.getRow(rin).set(CyNetwork.NAME, "rin");
		// create new attributes
		rin.getDefaultEdgeTable().createColumn(DISTANCE_ATTR, Double.class, false);
		rin.getDefaultEdgeTable().createColumn(OVERLAP_ATTR, Double.class, false);		
		rin.getDefaultNodeTable().createColumn(RESIDUE_ATTR, String.class, false);
		rin.getDefaultNodeTable().createColumn(SMILES_ATTR, String.class, false);
		rin.getDefaultNodeTable().createColumn(SEED_ATTR, Boolean.class, false);
		rin.getDefaultNodeTable().createColumn(BACKBONE_ATTR, Boolean.class, false);
		rin.getDefaultNodeTable().createColumn(SIDECHAIN_ATTR, Boolean.class, false);
		
		String cutoff = "";
		String type = "Clashes";
		if (includeContacts) {
			System.out.println("Getting contacts");
			type = "Contacts";
			cutoff = "overlapCutoff -0.4 hbondAllowance 0.0";
		} else {
			System.out.println("Getting clashes");
		}
		if (includeClashes || includeContacts) {
			String command = "findclash sel makePseudobonds false log true namingStyle command " + cutoff;
			if (includeInteracions.getSelectedValue() == interactionArray[0]) {
				// Get the first model
				ChimeraModel model = chimeraManager.getChimeraModel(0, 0);
				int modelNumber = model.getModelNumber();
				// Create the command
				command = "findclash #" + modelNumber
						+ " makePseudobonds false log true namingStyle command test other " + cutoff;
			} else if (includeInteracions.getSelectedValue() == interactionArray[1]) {
				command = command.concat(" test other");
			} else if (includeInteracions.getSelectedValue() == interactionArray[2])
				command = command.concat(" test model");

			List<String> replyList = chimeraManager.sendChimeraCommand(command, true);
			parseClashReplies(replyList, rin, nodeMap, type);
		}
		if (includeHBonds) {
			System.out.println("Getting Hydrogen Bonds");
			// Get the first model
			ChimeraModel model = chimeraManager.getChimeraModel(0, 0);
			int modelNumber = model.getModelNumber();
			String command = "findhbond spec #" + modelNumber
					+ " intramodel false intermodel true makePseudobonds false log true namingStyle command";
			if (includeInteracions.getSelectedValue() == interactionArray[0]) {
				command = "findhbond selRestrict any intermodel true makePseudobonds false log true namingStyle command";
			} else if (includeInteracions.getSelectedValue() == interactionArray[1]) {
				command = command.concat(" intramodel false");
			} else if (includeInteracions.getSelectedValue() == interactionArray[2])
				command = command.concat(" intramodel true");
			List<String> replyList = chimeraManager.sendChimeraCommand(command, true);
			parseHBondReplies(replyList, rin, nodeMap);
		}
		if (includeConnectivity) {
			System.out.println("Getting Connectivity");
			String command = "listphysicalchains";
			List<String> replyList = chimeraManager.sendChimeraCommand(command, true);
			parseConnectivityReplies(replyList, rin);
		}

		// add entwork to manager
		cyNetworkManager.addNetwork(rin);
		// Create a network view

		// Set vizmap

		// Do a layout

		// Activate structureViz for all of our nodes
		structureManager.addStructureNetwork(rin, RESIDUE_ATTR);
	}

	/**
	 * Clash replies look like: *preamble* *header line* *clash lines* where preamble is: Allowed
	 * overlap: -0.4 H-bond overlap reduction: 0 Ignore contacts between atoms separated by 4 bonds or
	 * less Ignore intra-residue contacts 44 contacts and the header line is: atom1 atom2 overlap
	 * distance and the clash lines look like: :2470.A@N :323.A@OD2 -0.394 3.454
	 */
	private List<CyEdge> parseClashReplies(List<String> replyLog, CyNetwork rin,
			Map<String, CyNode> nodeMap, String type) {
		// Scan for our header line
		boolean foundHeader = false;
		int index = 0;
		for (index = 0; index < replyLog.size(); index++) {
			String str = replyLog.get(index);
			if (str.trim().startsWith("atom1")) {
				foundHeader = true;
				break;
			}
		}
		if (!foundHeader)
			return null;

		Map<CyEdge, Double> distanceMap = new HashMap<CyEdge, Double>();
		Map<CyEdge, Double> overlapMap = new HashMap<CyEdge, Double>();
		for (++index; index < replyLog.size(); index++) {
			String[] line = replyLog.get(index).trim().split("\\s+");
			if (line.length != 4)
				continue;

			CyEdge edge = createEdge(rin, nodeMap, line[0], line[1], type);

			updateMap(distanceMap, edge, line[3], -1); // We want the smallest distance
			updateMap(overlapMap, edge, line[2], 1); // We want the largest overlap
		}

		// OK, now update the edge attributes we want
		for (CyEdge edge : distanceMap.keySet()) {
			rin.getRow(edge).set(DISTANCE_ATTR, distanceMap.get(edge));
			rin.getRow(edge).set(OVERLAP_ATTR, overlapMap.get(edge));
		}

		return new ArrayList<CyEdge>(distanceMap.keySet());
	}

	// H-bonds (donor, acceptor, hydrogen, D..A dist, D-H..A dist):
	/**
	 * Finding acceptors in model '1tkk' Building search tree of acceptor atoms Finding donors in
	 * model '1tkk' Matching donors in model '1tkk' to acceptors Finding intermodel H-bonds Finding
	 * intramodel H-bonds Constraints relaxed by 0.4 angstroms and 20 degrees Models used: #0 1tkk
	 * H-bonds (donor, acceptor, hydrogen, D..A dist, D-H..A dist): ARG 24.A NH1 GLU 2471.A OE1 no
	 * hydrogen 3.536 N/A LYS 160.A NZ GLU 2471.A O no hydrogen 2.680 N/A LYS 162.A NZ ALA 2470.A O no
	 * hydrogen 3.022 N/A LYS 268.A NZ GLU 2471.A O no hydrogen 3.550 N/A ILE 298.A N GLU 2471.A OE2
	 * no hydrogen 3.141 N/A ALA 2470.A N THR 135.A OG1 no hydrogen 2.814 N/A ALA 2470.A N ASP 321.A
	 * OD1 no hydrogen 2.860 N/A ALA 2470.A N ASP 321.A OD2 no hydrogen 3.091 N/A ALA 2470.A N ASP
	 * 323.A OD1 no hydrogen 2.596 N/A ALA 2470.A N ASP 323.A OD2 no hydrogen 3.454 N/A GLU 2471.A N
	 * SER 296.A O no hydrogen 2.698 N/A HOH 2541.A O GLU 2471.A OE1 no hydrogen 2.746 N/A HOH 2577.A
	 * O GLU 2471.A O no hydrogen 2.989 N/A
	 */
	private List<CyEdge> parseHBondReplies(List<String> replyLog, CyNetwork rin,
			Map<String, CyNode> nodeMap) {
		// Scan for our header line
		boolean foundHeader = false;
		int index = 0;
		for (index = 0; index < replyLog.size(); index++) {
			String str = replyLog.get(index);
			if (str.trim().startsWith("H-bonds")) {
				foundHeader = true;
				break;
			}
		}
		if (!foundHeader)
			return null;

		Map<CyEdge, Double> distanceMap = new HashMap<CyEdge, Double>();
		for (++index; index < replyLog.size(); index++) {
			String[] line = replyLog.get(index).trim().split("\\s+");
			if (line.length != 6 && line.length != 7)
				continue;

			CyEdge edge = createEdge(rin, nodeMap, line[0], line[1], "HBond");

			String distance = line[3];
			if (line[2].equals("no") && line[3].equals("hydrogen"))
				distance = line[4];
			updateMap(distanceMap, edge, distance, -1); // We want the smallest distance
		}

		// OK, now update the edge attributes we want
		for (CyEdge edge : distanceMap.keySet()) {
			rin.getRow(edge).set(DISTANCE_ATTR, distanceMap.get(edge));
		}

		return new ArrayList<CyEdge>(distanceMap.keySet());
	}

	/**
	 * Parse the connectivity information from Chimera. The data is of the form: physical chain
	 * #0:283.A #0:710.A physical chain #0:283.B #0:710.B physical chain #0:283.C #0:710.C
	 * 
	 * We don't use this data to create new nodes -- only new edges. If two nodes are within the same
	 * physical chain, we connect them with a "Connected" edge
	 */
	private List<CyEdge> parseConnectivityReplies(List<String> replyLog, CyNetwork rin) {
		List<CyEdge> edgeList = new ArrayList<CyEdge>();
		List<ChimeraResidue[]> rangeList = new ArrayList<ChimeraResidue[]>();
		for (String line : replyLog) {
			String[] tokens = line.split(" ");
			if (tokens.length != 4)
				continue;
			String start = tokens[2];
			String end = tokens[3];

			ChimeraResidue[] range = new ChimeraResidue[2];

			// Get the residues from the reside spec
			range[0] = ChimUtils.getResidue(start, chimeraManager);
			range[1] = ChimUtils.getResidue(end, chimeraManager);
			rangeList.add(range);
		}

		// If we don't have any nodes, get all of the residues in the connectivity
		// list and create them as nodes

		// For each node pair, figure out if the pair is connected
		List<CyNode> nodes = rin.getNodeList();
		for (int i = 0; i < nodes.size(); i++) {
			CyNode node1 = nodes.get(i);
			// System.out.println("Getting the range for the first node..."+node1);
			ChimeraResidue[] range = getRange(rangeList, node1, rin);
			if (range == null)
				continue;
			for (int j = i + 1; j < nodes.size(); j++) {
				CyNode node2 = nodes.get(j);
				// System.out.println("Seeing if node2 "+node2+" is in the range...");
				if (inRange(range, node2, rin)) {
					// System.out.println("....it is");
					// These two nodes are connected
					edgeList.add(createConnectivityEdge(rin, node1, node2));
				}
			}
		}

		// Now, make the edges based on whether any pair of nodes are in the same range
		return edgeList;
	}

	private CyEdge createEdge(CyNetwork rin, Map<String, CyNode> nodeMap, String sourceAlias,
			String targetAlias, String type) {
		// Create our two nodes. Note that makeResidueNode also adds three attributes:
		// 1) FunctionalResidues; 2) Seed; 3) SideChainOnly
		CyNode source = makeResidueNode(rin, nodeMap, sourceAlias);
		CyNode target = makeResidueNode(rin, nodeMap, targetAlias);

		// Create our edge
		CyEdge edge = rin.addEdge(source, target, true);
		String edgeName = rin.getRow(source).get(CyNetwork.NAME, String.class) + " "
				+ rin.getRow(target).get(CyNetwork.NAME, String.class);
		rin.getRow(edge).set(CyNetwork.NAME, edgeName);
		rin.getRow(edge).set(CyEdge.INTERACTION, type);
		return edge;
	}

	private CyEdge createConnectivityEdge(CyNetwork rin, CyNode node1, CyNode node2) {
		CyEdge edge = rin.addEdge(node1, node2, true);
		String edgeName = rin.getRow(node1).get(CyNetwork.NAME, String.class) + " "
				+ rin.getRow(node2).get(CyNetwork.NAME, String.class);
		rin.getRow(edge).set(CyNetwork.NAME, edgeName);
		rin.getRow(edge).set(CyEdge.INTERACTION, "connected");

		// Get the residue for node1 and node2 and ask Chimera to calculate the distance
		String residueAttr = rin.getRow(node1).get(RESIDUE_ATTR, String.class);
		ChimeraStructuralObject cso1 = ChimUtils.fromAttribute(residueAttr, chimeraManager);
		residueAttr = rin.getRow(node2).get(RESIDUE_ATTR, String.class);
		ChimeraStructuralObject cso2 = ChimUtils.fromAttribute(residueAttr, chimeraManager);
		if (cso1 instanceof ChimeraResidue && cso2 instanceof ChimeraResidue
				&& includeConnectivityDistance) {
			String spec1 = cso1.toSpec() + "@CA";
			String spec2 = cso2.toSpec() + "@CA";
			System.out.println("Getting distance between " + spec1 + " and " + spec2);

			String command = "distance " + spec1 + " " + spec2 + "; ~distance " + spec1 + " " + spec2;
			List<String> replyList = chimeraManager.sendChimeraCommand(command, true);
			int offset = replyList.get(0).indexOf(':');
			Double distance = Double.valueOf(replyList.get(0).substring(offset + 1));
			rin.getRow(edge).set(DISTANCE_ATTR, distance);
			// chimeraObject.chimeraSend("~distance "+spec1+" "+spec2);
		}
		return edge;
	}

	private CyNode makeResidueNode(CyNetwork rin, Map<String, CyNode> nodeMap, String alias) {
		// alias is a atomSpec of the form [#model]:residueNumber@atom
		// We want to convert that to a node identifier of [pdbid#]ABC nnn
		// and add FunctionalResidues and BackboneOnly attributes
		boolean singleModel = false;
		ChimeraModel model = ChimUtils.getModel(alias, chimeraManager);
		if (model == null) {
			model = chimeraManager.getChimeraModel(0, 0);
			singleModel = true;
		}
		ChimeraResidue residue = ChimUtils.getResidue(alias, model);
		boolean backbone = ChimUtils.isBackbone(alias);

		int displayType = ChimeraResidue.getDisplayType();
		ChimeraResidue.setDisplayType(ChimeraResidue.THREE_LETTER);
		// OK, now we have everything we need, create the node
		String nodeName = residue.toString().trim() + "." + residue.getChainId();
		ChimeraResidue.setDisplayType(displayType);

		if (!singleModel)
			nodeName = model.getModelName() + "#" + nodeName;

		// Create the node if it does not already exist in the network
		CyNode node = null;
		if (!nodeMap.containsKey(nodeName)) {
			node = rin.addNode();
			rin.getRow(node).set(CyNetwork.NAME, nodeName);
			nodeMap.put(nodeName, node);
		} else {
			node = nodeMap.get(nodeName);
		}

		// Add our attributes
		rin.getRow(node).set(RESIDUE_ATTR,
				model.getModelName() + "#" + residue.getIndex() + "." + residue.getChainId());
		rin.getRow(node).set(SEED_ATTR, Boolean.valueOf(residue.isSelected()));
		if (backbone) {
			rin.getRow(node).set(BACKBONE_ATTR, Boolean.TRUE);
		} else {
			rin.getRow(node).set(SIDECHAIN_ATTR, Boolean.TRUE);
		}
		return node;
	}

	private void updateMap(Map<CyEdge, Double> map, CyEdge edge, String value, int comparison) {
		// Save the minimum distance between atoms
		Double v = Double.valueOf(value);
		if (map.containsKey(edge)) {
			if (comparison < 0 && map.get(edge).compareTo(v) > 0)
				map.put(edge, v);
			else if (comparison > 0 && map.get(edge).compareTo(v) < 0)
				map.put(edge, v);
		} else {
			map.put(edge, v);
		}
	}

	private ChimeraResidue[] getRange(List<ChimeraResidue[]> rangeList, CyNode node, CyNetwork rin) {
		for (ChimeraResidue[] range : rangeList) {
			if (inRange(range, node, rin))
				return range;
		}
		return null;
	}

	private boolean inRange(ChimeraResidue[] range, CyNode node, CyNetwork rin) {
		String residueAttr = rin.getRow(node).get(RESIDUE_ATTR, String.class);
		ChimeraStructuralObject cso = ChimUtils.fromAttribute(residueAttr, chimeraManager);
		// Models can't be in a range...
		if (cso == null || cso instanceof ChimeraModel)
			return false;

		// A chain might be in a range -- check this
		if (cso instanceof ChimeraChain) {
			String chainID = ((ChimeraChain) cso).getChainId();
			return inChainRange(range, chainID);
		}

		// OK, we have a residue, but we need to be careful to make
		// sure that the chains match
		ChimeraResidue residue = (ChimeraResidue) cso;
		if (inChainRange(range, residue.getChainId())) {
			return true;
		}

		int startIndex = Integer.parseInt(range[0].getIndex());
		int endIndex = Integer.parseInt(range[1].getIndex());
		int residueIndex = Integer.parseInt(residue.getIndex());

		if (endIndex < startIndex) {
			if (endIndex <= residueIndex && residueIndex <= startIndex)
				return true;
		} else {
			if (startIndex <= residueIndex && residueIndex <= endIndex)
				return true;
		}

		return false;
	}

	private boolean inChainRange(ChimeraResidue[] range, String chainID) {
		String start = range[0].getChainId();
		String end = range[1].getChainId();

		if (start == null || end == null)
			return false;

		if (start.equals(end))
			return false;

		if (start.compareTo(end) > 0) {
			end = range[0].getChainId();
			start = range[1].getChainId();
		}

		if (start.compareTo(chainID) <= 0 && chainID.compareTo(end) <= 0)
			return true;

		return false;
	}
}
