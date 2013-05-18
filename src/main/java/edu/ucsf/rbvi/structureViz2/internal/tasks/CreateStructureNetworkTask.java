package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.ChimUtils;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraChain;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraManager;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraModel;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraResidue;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraStructuralObject;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class CreateStructureNetworkTask extends AbstractTask {

	@Tunable(description = "Name of new structure network", groups = "General")
	public String networkName;

	@Tunable(description = "Include interactions", groups = "General")
	public ListSingleSelection<String> includeInteracions;

	@Tunable(description = "Add hydrogens", groups = "General")
	public boolean addHydrogens;

	@Tunable(description = "Ignore water", groups = "General")
	public boolean ignoreWater;

	@Tunable(description = "Include contacts", groups = "Contacts")
	public boolean includeContacts;

	@Tunable(description = "Overlap cutoff", groups = "Contacts", dependsOn = "includeContacts=true")
	public double overlapCutoffCont;

	@Tunable(description = "HBond allowance", groups = "Contacts", dependsOn = "includeContacts=true")
	public double hbondAllowanceCont;

	@Tunable(description = "Bond separation", groups = "Contacts", dependsOn = "includeContacts=true")
	public int bondSepCont;

	@Tunable(description = "Include clashes", groups = "Clashes")
	public boolean includeClashes;

	@Tunable(description = "Overlap cutoff", groups = "Clashes", dependsOn = "includeClashes=true")
	public double overlapCutoffClash;

	@Tunable(description = "HBond allowance", groups = "Clashes", dependsOn = "includeClashes=true")
	public double hbondAllowanceClash;

	@Tunable(description = "Bond separation", groups = "Clashes", dependsOn = "includeClashes=true")
	public int bondSepClash;

	@Tunable(description = "Include hydrogen bonds (overlaps with contacts)", groups = "Hydrogen bonds")
	public boolean includeHBonds;

	@Tunable(description = "Relax criteria for hydrogen bonding", groups = "Hydrogen bonds", dependsOn = "includeHBonds=true")
	public boolean relaxHBonds;

	@Tunable(description = "Distance criteria", groups = "Hydrogen bonds", dependsOn = "relaxHBonds=true")
	public double distSlop;

	@Tunable(description = "Angle criteria", groups = "Hydrogen bonds", dependsOn = "relaxHBonds=true")
	public double angleSlop;

	@Tunable(description = "Include connectivity", groups = "Connectivity")
	public boolean includeConnectivity;

	@Tunable(description = "Calculate connectivity distances (more time consuming)", groups = "Connectivity", dependsOn = "includeConnectivity=true")
	public boolean includeConnectivityDistance;

	private static final String[] interactionArray = { "Within selection",
			"Between selection and all atoms", "Within selection and all atoms", "Between all models",
			"Within and between all models" };
	// TODO: Add "Within model" and specify number
	// TODO: [FIX] Different number of nodes and edges in consecutive runs
	// TODO: Too many Interactions between two models?
	// TODO: remove single nodes?

	// Edge attributes
	static final String DISTANCE_ATTR = "MinimumDistance";
	static final String OVERLAP_ATTR = "MaximumOverlap";
	static final String INTSUBTYPE_ATTR = "InteractionSubtype";
	static final String INTATOMS_ATTR = "InteractingAtoms";
	static final String NUMINT_ATTR = "NumberInteractions";
	// Node attributes
	static final String SMILES_ATTR = "SMILES";
	static final String STRUCTURE_ATTR = "pdbFileName";
	static final String SEED_ATTR = "SeedResidues";
	// TODO: Should we keep these two attributes?
	static final String BACKBONE_ATTR = "BackboneInteraction";
	static final String SIDECHAIN_ATTR = "SideChainInteraction";

	private StructureManager structureManager;
	private ChimeraManager chimeraManager;

	public CreateStructureNetworkTask(StructureManager structureManager) {
		this.structureManager = structureManager;
		this.chimeraManager = structureManager.getChimeraManager();

		// TODO: Create name for a new network dynamically
		networkName = "New RIN";
		includeInteracions = new ListSingleSelection<String>(interactionArray);
		includeInteracions.setSelectedValue(interactionArray[0]);
		addHydrogens = true;
		ignoreWater = true;
		includeContacts = true;
		overlapCutoffCont = -0.4;
		hbondAllowanceCont = 0.0;
		bondSepCont = 4;
		includeClashes = false;
		overlapCutoffClash = 0.6;
		hbondAllowanceClash = 0.4;
		bondSepClash = 4;
		includeHBonds = false;
		relaxHBonds = false;
		distSlop = 0.4;
		angleSlop = 20;
		includeConnectivity = false;
		includeConnectivityDistance = false;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// Save selected nodes indexed by their name
		Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();
		System.out.println("create network");
		chimeraManager.stopListening();
		CyNetwork rin = createNetwork();

		System.out.println("start getting interactions");
		// structureManager.ignoreCySelection = true;
		// add hydrogens first
		if (addHydrogens) {
			System.out.println("Adding hudrogens");
			chimeraManager.sendChimeraCommand("addh hbond true", false);
		}
		if (includeContacts) {
			System.out.println("Getting contacts");
			chimeraManager.stopListening();
			List<String> replyList = chimeraManager.sendChimeraCommand(
					getContactCommand(overlapCutoffCont, hbondAllowanceCont, bondSepCont), true);
			if (replyList != null) {
				parseContactReplies(replyList, rin, nodeMap);
			}
		}
		if (includeClashes) {
			System.out.println("Getting clashes");
			chimeraManager.stopListening();
			List<String> replyList = chimeraManager.sendChimeraCommand(
					getContactCommand(overlapCutoffClash, hbondAllowanceClash, bondSepClash), true);
			if (replyList != null) {
				parseContactReplies(replyList, rin, nodeMap);
			}
		}
		if (includeHBonds) {
			System.out.println("Getting Hydrogen Bonds");
			chimeraManager.stopListening();
			List<String> replyList = chimeraManager.sendChimeraCommand(getHBondCommand(), true);
			if (replyList != null) {
				parseHBondReplies(replyList, rin, nodeMap);
			}
		}
		if (includeConnectivity) {
			System.out.println("Getting Connectivity");
			String command = "listphysicalchains";
			chimeraManager.stopListening();
			List<String> replyList = chimeraManager.sendChimeraCommand(command, true);
			if (replyList != null) {
				parseConnectivityReplies(replyList, rin);
			}
		}
		addCombinedEdges();

		// register network
		CyNetworkManager cyNetworkManager = (CyNetworkManager) structureManager
				.getService(CyNetworkManager.class);
		cyNetworkManager.addNetwork(rin);

		// structureManager.ignoreCySelection = false;
		// Activate structureViz for all of our nodes
		structureManager.addStructureNetwork(rin);
		finalizeNetwork(rin);
		chimeraManager.startListening();
	}

	private String getContactCommand(double overlapCutoff, double hbondAllowance, int bondSep) {
		String atomspec1 = "";
		String atomspec2 = "";
		// "Within selection"
		if (includeInteracions.getSelectedValue() == interactionArray[0]) {
			// among the specified atoms
			atomspec1 = "sel";
			atomspec2 = "test self";
		}
		// "Between selection and all atoms"
		else if (includeInteracions.getSelectedValue() == interactionArray[1]) {
			// between the specified atoms and all other atoms
			atomspec1 = "sel";
			atomspec2 = "test other";
		}
		// "Within selection and all atoms"
		else if (includeInteracions.getSelectedValue() == interactionArray[2]) {
			// intra-model interactions between the specified atoms and all other atoms
			atomspec1 = "sel";
			atomspec2 = "test model";
		}
		// "Between models"
		else if (includeInteracions.getSelectedValue() == interactionArray[3]) {
			// between the specified atoms and all other atoms
			// TODO: Not correct, finds only contacts between the first model and its neighbors
			atomspec1 = "#" + chimeraManager.getChimeraModel().getModelNumber();
			atomspec2 = "test other";
		}
		// Between and within all models
		else {
			atomspec1 = "#*";
			atomspec2 = "test self";
		}
		// Create the command
		String command = "findclash " + atomspec1
				+ " makePseudobonds false log true namingStyle command overlapCutoff " + overlapCutoff
				+ " hbondAllowance " + hbondAllowance + " bondSeparation " + bondSep + " " + atomspec2;
		return command;
	}

	private String getHBondCommand() {
		// for which atoms to find hydrogen bonds
		String atomspec = "";
		// intermodel: whether to look for H-bonds between models
		// intramodel: whether to look for H-bonds within models.
		String modelrestr = "";
		// "Within selection"
		if (includeInteracions.getSelectedValue() == interactionArray[0]) {
			// Limit H-bond detection to H-bonds with both atoms selected
			atomspec = "selRestrict both";
			modelrestr = "intramodel true intermodel true";
		}
		// "Between selection and all atoms"
		else if (includeInteracions.getSelectedValue() == interactionArray[1]) {
			// Limit H-bond detection to H-bonds with at least one atom selected
			atomspec = "selRestrict any";
			modelrestr = "intramodel false intermodel true";
		}
		// "Within selection and all atoms"
		else if (includeInteracions.getSelectedValue() == interactionArray[2]) {
			// Limit H-bond detection to H-bonds with at least one atom selected
			atomspec = "selRestrict any";
			modelrestr = "intramodel true intermodel true";
		}
		// "Between models"
		else if (includeInteracions.getSelectedValue() == interactionArray[3]) {
			// Restrict H-bond detection to the specified model
			atomspec = "spec #*";
			modelrestr = "intramodel false intermodel true";
		}
		// Between and within models
		else {
			atomspec = "spec #*";
			modelrestr = "intramodel true intermodel true";
		}
		String command = "findhbond " + atomspec + " " + modelrestr
				+ " makePseudobonds false log true namingStyle command";
		if (relaxHBonds) {
			command += " relax true distSlop " + distSlop + " angleSlop " + angleSlop;
		}
		return command;
	}

	/**
	 * Clash replies look like: *preamble* *header line* *clash lines* where preamble is: Allowed
	 * overlap: -0.4 H-bond overlap reduction: 0 Ignore contacts between atoms separated by 4 bonds or
	 * less Ignore intra-residue contacts 44 contacts and the header line is: atom1 atom2 overlap
	 * distance and the clash lines look like: :2470.A@N :323.A@OD2 -0.394 3.454
	 */
	private List<CyEdge> parseContactReplies(List<String> replyLog, CyNetwork rin,
			Map<String, CyNode> nodeMap) {
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
			// System.out.println(replyLog.get(index));
			String[] line = replyLog.get(index).trim().split("\\s+");
			if (line.length != 4)
				continue;

			CyEdge edge = createEdge(rin, nodeMap, line[0], line[1], "contact");
			if (edge == null) {
				continue;
			}
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
		if (!foundHeader) {
			return null;
		}

		Map<CyEdge, Double> distanceMap = new HashMap<CyEdge, Double>();
		for (++index; index < replyLog.size(); index++) {
			// System.out.println(replyLog.get(index));
			String[] line = replyLog.get(index).trim().split("\\s+");
			if (line.length != 5 && line.length != 6)
				continue;

			CyEdge edge = createEdge(rin, nodeMap, line[0], line[1], "hbond");
			if (edge == null) {
				continue;
			}
			// TODO: do not create a new edge
			// TODO: if hydrogens added, take the other distance?
			String distance = line[3];
			if ((line[2].equals("no") && line[3].equals("hydrogen")) || addHydrogens) {
				distance = line[4];
			}
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
		CyNode source = createResidueNode(rin, nodeMap, sourceAlias);
		CyNode target = createResidueNode(rin, nodeMap, targetAlias);
		if (source == null || target == null) {
			return null;
		}
		String sourceAtom = ChimUtils.getAtomName(sourceAlias);
		String targetAtom = ChimUtils.getAtomName(targetAlias);
		String interactingAtoms = sourceAtom + "_" + targetAtom;
		String interactionSubtype = type + " "
				+ ChimUtils.getIntSubtype(rin.getRow(source).get(CyNetwork.NAME, String.class), sourceAtom)
				+ "_"
				+ ChimUtils.getIntSubtype(rin.getRow(target).get(CyNetwork.NAME, String.class), targetAtom);

		// Create our edge
		CyEdge edge = null;
		if (type.equals("hbond")) {
			List<CyEdge> existingEdges = rin.getConnectingEdgeList(source, target, Type.ANY);
			if (existingEdges.size() > 0) {
				for (CyEdge exEdge : existingEdges) {
					if (rin.getRow(exEdge).get(CyEdge.INTERACTION, String.class).equals("contact")
							&& rin.getRow(exEdge).get(INTATOMS_ATTR, String.class).equals(interactingAtoms)) {
						// TODO: delete overlap attribute for this edge
						edge = exEdge;
						break;
					}
				}
			}
		}
		if (edge == null) {
			edge = rin.addEdge(source, target, true);
		}
		String edgeName = rin.getRow(source).get(CyNetwork.NAME, String.class) + " "
				+ rin.getRow(target).get(CyNetwork.NAME, String.class);
		rin.getRow(edge).set(CyNetwork.NAME, edgeName);
		rin.getRow(edge).set(CyEdge.INTERACTION, type);
		rin.getRow(edge).set(INTATOMS_ATTR, interactingAtoms);
		rin.getRow(edge).set(INTSUBTYPE_ATTR, interactionSubtype);
		return edge;
	}

	private CyEdge createConnectivityEdge(CyNetwork rin, CyNode node1, CyNode node2) {
		CyEdge edge = rin.addEdge(node1, node2, true);
		String edgeName = rin.getRow(node1).get(CyNetwork.NAME, String.class) + " "
				+ rin.getRow(node2).get(CyNetwork.NAME, String.class);
		rin.getRow(edge).set(CyNetwork.NAME, edgeName);
		rin.getRow(edge).set(CyEdge.INTERACTION, "connected");

		// Get the residue for node1 and node2 and ask Chimera to calculate the distance
		String residueAttr = rin.getRow(node1).get(ChimUtils.RESIDUE_ATTR, String.class);
		ChimeraStructuralObject cso1 = ChimUtils.fromAttribute(residueAttr, chimeraManager);
		residueAttr = rin.getRow(node2).get(ChimUtils.RESIDUE_ATTR, String.class);
		ChimeraStructuralObject cso2 = ChimUtils.fromAttribute(residueAttr, chimeraManager);
		if (cso1 != null && cso2 != null && cso1 instanceof ChimeraResidue
				&& cso2 instanceof ChimeraResidue && includeConnectivityDistance) {
			String spec1 = cso1.toSpec() + "@CA";
			String spec2 = cso2.toSpec() + "@CA";
			System.out.println("Getting distance between " + spec1 + " and " + spec2);

			String command = "distance " + spec1 + " " + spec2 + "; ~distance " + spec1 + " " + spec2;
			List<String> replyList = chimeraManager.sendChimeraCommand(command, true);
			if (replyList != null) {
				int offset = replyList.get(0).indexOf(':');
				Double distance = Double.valueOf(replyList.get(0).substring(offset + 1));
				rin.getRow(edge).set(DISTANCE_ATTR, distance);
				// chimeraObject.chimeraSend("~distance "+spec1+" "+spec2);
			}
		}
		return edge;
	}

	private CyNode createResidueNode(CyNetwork rin, Map<String, CyNode> nodeMap, String alias) {
		// alias is a atomSpec of the form [#model]:residueNumber@atom
		// We want to convert that to a node identifier of [pdbid#]ABC nnn
		// and add FunctionalResidues and BackboneOnly attributes
		boolean singleModel = false;
		ChimeraModel model = ChimUtils.getModel(alias, chimeraManager);
		if (model == null) {
			model = chimeraManager.getChimeraModel();
			singleModel = true;
		}
		ChimeraResidue residue = ChimUtils.getResidue(alias, model);
		if (ignoreWater && residue.getType().equals("HOH")) {
			return null;
		}
		// boolean backbone = ChimUtils.isBackbone(alias);

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

		// Add attributes from Chimera
		rin.getRow(node).set(ChimUtils.RESIDUE_ATTR,
				model.getModelName() + "#" + residue.getIndex() + "." + residue.getChainId());
		rin.getRow(node).set(
				ChimUtils.RINALYZER_ATTR,
				model.getModelName() + ":" + residue.getChainId() + ":" + residue.getIndex() + ":_:"
						+ residue.getType());
		rin.getRow(node).set(SEED_ATTR, Boolean.valueOf(residue.isSelected()));
		// if (backbone)
		// rin.getRow(node).set(BACKBONE_ATTR, Boolean.TRUE);
		// else
		// rin.getRow(node).set(SIDECHAIN_ATTR, Boolean.TRUE);

		// Add structureViz attributes
		String smiles = ChimUtils.toSMILES(residue.getType());
		if (smiles != null) {
			// TODO: Check if attribute is a list?
			rin.getRow(node).set(SMILES_ATTR, smiles);
		}
		// TODO: check the same as above
		rin.getRow(node).set(STRUCTURE_ATTR, model.getModelName());

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
		String residueAttr = rin.getRow(node).get(ChimUtils.RESIDUE_ATTR, String.class);
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

	private void addCombinedEdges() {

	}

	private CyNetwork createNetwork() {
		// get factories, etc.
		CyNetworkFactory cyNetworkFactory = (CyNetworkFactory) structureManager
				.getService(CyNetworkFactory.class);

		// Create the network
		CyNetwork rin = cyNetworkFactory.createNetwork();
		rin.getRow(rin).set(CyNetwork.NAME, networkName);

		// Create new attributes
		rin.getDefaultNodeTable().createColumn(ChimUtils.RESIDUE_ATTR, String.class, false);
		rin.getDefaultNodeTable().createColumn(ChimUtils.RINALYZER_ATTR, String.class, false);
		rin.getDefaultEdgeTable().createColumn(DISTANCE_ATTR, Double.class, false);
		rin.getDefaultEdgeTable().createColumn(OVERLAP_ATTR, Double.class, false);
		rin.getDefaultEdgeTable().createColumn(INTSUBTYPE_ATTR, String.class, false);
		rin.getDefaultEdgeTable().createColumn(INTATOMS_ATTR, String.class, false);
		rin.getDefaultNodeTable().createColumn(SMILES_ATTR, String.class, false);
		rin.getDefaultNodeTable().createColumn(STRUCTURE_ATTR, String.class, false);
		rin.getDefaultNodeTable().createColumn(SEED_ATTR, Boolean.class, false);
		rin.getDefaultNodeTable().createColumn(BACKBONE_ATTR, Boolean.class, false);
		rin.getDefaultNodeTable().createColumn(SIDECHAIN_ATTR, Boolean.class, false);

		// return network
		return rin;
	}

	private void finalizeNetwork(CyNetwork network) {
		// get factories, etc.
		CyNetworkViewFactory cyNetworkViewFactory = (CyNetworkViewFactory) structureManager
				.getService(CyNetworkViewFactory.class);
		CyNetworkViewManager cyNetworkViewManager = (CyNetworkViewManager) structureManager
				.getService(CyNetworkViewManager.class);

		// Create a network view
		CyNetworkView rinView = cyNetworkViewFactory.createNetworkView(network);
		cyNetworkViewManager.addNetworkView(rinView);
		// Do a layout
		// CyLayoutAlgorithmManager cyLayoutManager = (CyLayoutAlgorithmManager) structureViz
		// .getService(CyLayoutAlgorithmManager.class);
		// CyLayoutAlgorithm layout = cyLayoutManager.getDefaultLayout();
		// insertTasksAfterCurrentTask(layout.createTaskIterator(rinView,
		// layout.getDefaultLayoutContext(), layout.ALL_NODE_VIEWS, null));
		ApplyPreferredLayoutTaskFactory layoutTaskFactory = (ApplyPreferredLayoutTaskFactory) structureManager
				.getService(ApplyPreferredLayoutTaskFactory.class);
		Set<CyNetworkView> views = new HashSet<CyNetworkView>();
		views.add(rinView);
		insertTasksAfterCurrentTask(layoutTaskFactory.createTaskIterator(views));

		// annotate
		NetworkTaskFactory annotateFactory = new AnnotateStructureNetworkTaskFactory(structureManager);
		// (NetworkTaskFactory) structureManager.getService(
		// NetworkTaskFactory.class,
		// "(&(commandNamespace=structureViz)(command=annotateStructureNetwork))");
		if (annotateFactory != null) {
			TunableSetter tunableSetter = (TunableSetter) structureManager
					.getService(TunableSetter.class);
			Map<String, Object> tunables = new HashMap<String, Object>();
			tunables.put("ssAnnot", true);
			TaskManager<?, ?> tm = (TaskManager<?, ?>) structureManager.getService(TaskManager.class);
			tm.execute(tunableSetter.createTaskIterator(annotateFactory.createTaskIterator(network),
					tunables));
		}
		// Set vizmap
		NetworkViewTaskFactory rinalyzerVisProps = (NetworkViewTaskFactory) structureManager
				.getService(NetworkViewTaskFactory.class,
						"(&(commandNamespace=rinalyzer)(command=initRinVisProps))");
		if (rinalyzerVisProps != null) {
			insertTasksAfterCurrentTask(rinalyzerVisProps.createTaskIterator(rinView));
		} else {
			VisualMappingManager cyVmManager = (VisualMappingManager) structureManager
					.getService(VisualMappingManager.class);
			VisualStyleFactory cyVsFactory = (VisualStyleFactory) structureManager
					.getService(VisualStyleFactory.class);
			VisualStyle rinStyle = null;
			for (VisualStyle vs : cyVmManager.getAllVisualStyles()) {
				if (vs.getTitle().equals("RIN style")) {
					rinStyle = vs;
				}
			}
			if (rinStyle == null) {
				rinStyle = cyVsFactory.createVisualStyle(cyVmManager.getDefaultVisualStyle());
				rinStyle.setTitle("RIN style");
				cyVmManager.addVisualStyle(rinStyle);
			}
			cyVmManager.setVisualStyle(rinStyle, rinView);
			rinStyle.apply(rinView);
		}
		rinView.updateView();
	}
}
