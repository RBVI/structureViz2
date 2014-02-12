package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraManager;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraModel;
import edu.ucsf.rbvi.structureViz2.internal.model.RINManager;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

// TODO: [Old bug] Different number of nodes and edges in consecutive runs (only when adding hydrogens?)
// TODO: [Old bug] Selection disappears sometimes upon RIN creation
// TODO: [Optional] Add menus for adding each type of edges to an already existing network?
// TODO: [Release] Decide what to do with edges of the same type?
public class CreateStructureNetworkTask extends AbstractTask {

	@Tunable(description = "Name of new network", groups = "General", gravity = 1.1)
	public String networkName;

	@Tunable(description = "Include interactions", groups = "General", gravity = 1.2)
	public ListSingleSelection<String> includeInteracions;

	@Tunable(description = "Add hydrogens", groups = "General", gravity = 1.3)
	public boolean addHydrogens;

	@Tunable(description = "Ignore water", groups = "General", gravity = 1.4)
	public boolean ignoreWater;

	@Tunable(description = "Include combined edges", groups = "General", gravity = 1.5)
	public boolean includeCombiEdges;

	@Tunable(description = "Include contacts", groups = "Contacts", gravity = 2.1)
	public boolean includeContacts;

	@Tunable(description = "Overlap cutoff", groups = "Contacts", dependsOn = "includeContacts=true", gravity = 2.2)
	public double overlapCutoffCont;

	@Tunable(description = "HBond allowance", groups = "Contacts", dependsOn = "includeContacts=true", gravity = 2.3)
	public double hbondAllowanceCont;

	@Tunable(description = "Bond separation", groups = "Contacts", dependsOn = "includeContacts=true", gravity = 2.4)
	public int bondSepCont;

	@Tunable(description = "Include clashes", groups = "Clashes", gravity = 3.1)
	public boolean includeClashes;

	@Tunable(description = "Overlap cutoff", groups = "Clashes", dependsOn = "includeClashes=true", gravity = 3.2)
	public double overlapCutoffClash;

	@Tunable(description = "HBond allowance", groups = "Clashes", dependsOn = "includeClashes=true", gravity = 3.3)
	public double hbondAllowanceClash;

	@Tunable(description = "Bond separation", groups = "Clashes", dependsOn = "includeClashes=true", gravity = 3.4)
	public int bondSepClash;

	@Tunable(description = "Include hydrogen bonds", groups = "Hydrogen bonds", gravity = 4.1)
	public boolean includeHBonds;

	@Tunable(description = "Remove redundant contacts", groups = "Hydrogen bonds", dependsOn = "includeHBonds=true", gravity = 4.2)
	public boolean removeRedContacts;

	@Tunable(description = "Add tolerances to strict criteria", groups = "Hydrogen bonds", dependsOn = "includeHBonds=true", gravity = 4.3)
	public boolean relaxHBonds;

	@Tunable(description = "Distance tolerance", groups = "Hydrogen bonds", dependsOn = "relaxHBonds=true", gravity = 4.4)
	public double distSlop;

	@Tunable(description = "Angle tolerance", groups = "Hydrogen bonds", dependsOn = "relaxHBonds=true", gravity = 4.5)
	public double angleSlop;

	@Tunable(description = "Include (backbone) connectivity", groups = "Connectivity", gravity = 5.1)
	public boolean includeConnectivity;

	@Tunable(description = "Include distances between CA atoms", groups = "Distance", gravity = 6.1)
	public boolean includeDistance;

	@Tunable(description = "Distance cutoff (in angstoms)", groups = "Distance", gravity = 6.2)
	public double distCutoff;

	// @Tunable(description =
	// "Calculate connectivity distances (more time consuming)", groups =
	// "Connectivity", dependsOn = "includeConnectivity=true")
	// public boolean includeConnectivityDistance;

	private static final String withinSelection = "Within selection";
	private static final String betweenSelection = "Between selection and other atoms";
	private static final String allSelection = "All of the above";

	private StructureManager structureManager;
	private ChimeraManager chimeraManager;
	private RINManager rinManager;

	public CreateStructureNetworkTask(StructureManager structureManager) {
		this.structureManager = structureManager;
		this.chimeraManager = structureManager.getChimeraManager();
		this.rinManager = structureManager.getRINManager();
		networkName = getRINName();
		includeInteracions = new ListSingleSelection<String>(withinSelection, betweenSelection,
				allSelection);
		includeInteracions.setSelectedValue(withinSelection);
		addHydrogens = false;
		ignoreWater = true;
		includeCombiEdges = false;
		includeContacts = true;
		overlapCutoffCont = -0.4;
		hbondAllowanceCont = 0.0;
		bondSepCont = 4;
		includeClashes = false;
		overlapCutoffClash = 0.6;
		hbondAllowanceClash = 0.4;
		bondSepClash = 4;
		includeHBonds = false;
		removeRedContacts = true;
		relaxHBonds = false;
		distSlop = 0.4;
		angleSlop = 20;
		includeConnectivity = false;
		includeDistance = false;
		distCutoff = 5.0;
	}

	@ProvidesTitle
	public String getTitle() {
		return "Residue Interaction Network Options";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Creating Residue Interaction Network");
		taskMonitor.setStatusMessage("Creating network ...");
		// Save selected nodes indexed by their name
		Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();
		chimeraManager.stopListening();
		// get rin initialized with all attributes and nodes in the selection
		final CyNetwork rin = rinManager.createRIN(nodeMap, networkName, ignoreWater,
				includeCombiEdges);

		// add hydrogens first
		if (addHydrogens) {
			// System.out.println("Adding hydrogens");
			taskMonitor.setStatusMessage("Adding hydrogens ...");
			chimeraManager.sendChimeraCommand("addh hbond true", false);
		}
		if (includeContacts) {
			taskMonitor.setStatusMessage("Getting contacts ...");
			chimeraManager.stopListening();
			rinManager.includeContacts(rin, nodeMap, getIncludeInteractions(), ignoreWater,
					removeRedContacts, overlapCutoffCont, hbondAllowanceCont, bondSepCont);
		}
		if (includeClashes) {
			taskMonitor.setStatusMessage("Getting clashes ...");
			chimeraManager.stopListening();
			rinManager.includeClashes(rin, nodeMap, getIncludeInteractions(), ignoreWater,
					removeRedContacts, overlapCutoffClash, hbondAllowanceClash, bondSepClash);
		}
		if (includeHBonds) {
			taskMonitor.setStatusMessage("Getting hydrogen bonds ...");
			chimeraManager.stopListening();
			rinManager.includeHBonds(rin, nodeMap, getIncludeInteractions(), ignoreWater,
					removeRedContacts, addHydrogens, relaxHBonds, angleSlop, distSlop);
		}
		if (includeConnectivity) {
			taskMonitor.setStatusMessage("Getting connectivity ...");
			chimeraManager.stopListening();
			rinManager.includeConnectivity(rin);
		}
		if (includeDistance) {
			taskMonitor.setStatusMessage("Getting distances ...");
			rinManager.includeDistances(rin, nodeMap, getIncludeInteractions(), ignoreWater,
					removeRedContacts, distCutoff);
		}
		if (includeCombiEdges) {
			taskMonitor.setStatusMessage("Adding combined edges ...");
			rinManager.addCombinedEdges(rin);
		}

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

	private String getRINName() {
		String name = "RIN ";
		Map<Integer, ChimeraModel> models = chimeraManager.getSelectedModels();
		for (ChimeraModel model : models.values()) {
			name += model.getModelName() + " ";
		}
		return name;
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
		// annotate
		NetworkTaskFactory annotateFactory = new AnnotateStructureNetworkTaskFactory(
				structureManager);
		if (annotateFactory != null) {
			TunableSetter tunableSetter = (TunableSetter) structureManager
					.getService(TunableSetter.class);
			Map<String, Object> tunables = new HashMap<String, Object>();
			List<String> resAttr = structureManager.getAllChimeraResidueAttributes();
			ListMultipleSelection<String> resAttrTun = new ListMultipleSelection<String>(resAttr);
			resAttrTun.setSelectedValues(resAttr);
			tunables.put("residueAttributes", resAttrTun);
			insertTasksAfterCurrentTask(tunableSetter.createTaskIterator(
					annotateFactory.createTaskIterator(network), tunables));
		}

		// Apply RIN Layout and if not found, do a preferred
		CyLayoutAlgorithmManager manager = (CyLayoutAlgorithmManager) structureManager
				.getService(CyLayoutAlgorithmManager.class);
		CyLayoutAlgorithm rinlayout = manager.getLayout("rin-layout");
		if (rinlayout != null) {
			TaskManager<?, ?> taskManager = (TaskManager<?, ?>) structureManager
					.getService(TaskManager.class);
			taskManager.execute(rinlayout.createTaskIterator(rinView,
					rinlayout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, null));
		} else {
			ApplyPreferredLayoutTaskFactory layoutTaskFactory = (ApplyPreferredLayoutTaskFactory) structureManager
					.getService(ApplyPreferredLayoutTaskFactory.class);
			Set<CyNetworkView> views = new HashSet<CyNetworkView>();
			views.add(rinView);
			insertTasksAfterCurrentTask(layoutTaskFactory.createTaskIterator(views));
		}

		// Set vizmap
		NetworkTaskFactory rinalyzerVisProps = (NetworkTaskFactory) structureManager.getService(
				NetworkTaskFactory.class,
				"(&(commandNamespace=rinalyzer)(command=initRinVisProps))");
		if (rinalyzerVisProps != null) {
			insertTasksAfterCurrentTask(rinalyzerVisProps.createTaskIterator(network));
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

	private int getIncludeInteractions() {
		if (withinSelection.equals(includeInteracions.getSelectedValue())) {
			return 0;
		} else if (betweenSelection.equals(includeInteracions.getSelectedValue())) {
			return 1;
		} else {
			return 2;
		}
	}
}
