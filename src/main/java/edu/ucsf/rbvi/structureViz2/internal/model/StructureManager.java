package edu.ucsf.rbvi.structureViz2.internal.model;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import edu.ucsf.rbvi.structureViz2.internal.tasks.CreateStructureNetworkTaskFactory;
import edu.ucsf.rbvi.structureViz2.internal.ui.AlignStructuresDialog;
import edu.ucsf.rbvi.structureViz2.internal.ui.ModelNavigatorDialog;

/**
 * This object maintains the relationship between Chimera objects and Cytoscape objects.
 */
public class StructureManager {
	static final String[] defaultStructureKeys = { "Structure", "pdb", "pdbFileName", "PDB ID",
			"structure", "biopax.xref.PDB", "pdb_ids", "ModelName", "ModelNumber" };
	static final String[] defaultChemStructKeys = { "Smiles", "smiles", "SMILES" };
	static final String[] defaultResidueKeys = { "FunctionalResidues", "ResidueList", "Residues" };

	public static final Map<String, String> residueAttrCommandMap = new HashMap<String, String>();

	static {
		residueAttrCommandMap.put("SecondaryStructure", "");
		// residueAttrCommandMap.put("Color", "ribbonColor");
		residueAttrCommandMap.put("averageBFactor", "bfactor");
		residueAttrCommandMap.put("averageOccupancy", "occupancy");
		residueAttrCommandMap.put("residueCoordinates", "occupancy");
		// residueAttrCommandMap.put("kdHydrophobicity", "kdHydrophobicity");
		// residueAttrCommandMap.put("phiAngle", "phi");
		// residueAttrCommandMap.put("psiAngle", "psi");
		// residueAttrCommandMap.put("areaSAS", "areaSAS");
		// residueAttrCommandMap.put("areaSES", "areaSES");
		// residueAttrCommandMap.put("chi1Angle", "chi1");
		// residueAttrCommandMap.put("chi2Angle", "chi2");
		// residueAttrCommandMap.put("chi3Angle", "chi3");
		// residueAttrCommandMap.put("chi4Angle", "chi4");
	}

	public enum ModelType {
		PDB_MODEL, MODBASE_MODEL, SMILES
	};

	public static Properties pathProps;

	private String chimeraCommandAttr = "ChimeraCommand";
	private String chimeraOutputTable = "ChimeraTable";
	private String chimeraOutputAttr = "ChimeraOutput";
	private CyTable chimTable = null;
	private BundleContext bundleContext = null;
	private boolean haveGUI = true;
	private ChimeraManager chimeraManager = null;
	private CreateStructureNetworkTaskFactory structureNetFactory = null;
	private Map<CyNetwork, StructureSettings> settings = null;
	private Map<CyIdentifiable, Set<ChimeraStructuralObject>> currentCyMap = null;
	private Map<ChimeraStructuralObject, Set<CyIdentifiable>> currentChimMap = null;
	private Map<CyIdentifiable, CyNetwork> networkMap = null;

	private ModelNavigatorDialog mnDialog = null;
	private AlignStructuresDialog alDialog = null;
	private AssociationTask aTask = null;

	static private List<ChimeraStructuralObject> chimSelectionList;
	private boolean ignoreCySelection = false;

	public StructureManager(BundleContext bc, boolean haveGUI) {
		this.bundleContext = bc;
		this.haveGUI = haveGUI;
		settings = new HashMap<CyNetwork, StructureSettings>();
		currentCyMap = new HashMap<CyIdentifiable, Set<ChimeraStructuralObject>>();
		currentChimMap = new HashMap<ChimeraStructuralObject, Set<CyIdentifiable>>();
		networkMap = new HashMap<CyIdentifiable, CyNetwork>();
		aTask = new AssociationTask();
		aTask.start();
		// Create the Chimera interface
		chimeraManager = new ChimeraManager(this);
		chimSelectionList = new ArrayList<ChimeraStructuralObject>();
		pathProps = new Properties();
	}

	public ChimeraManager getChimeraManager() {
		return chimeraManager;
	}

	public Object getService(Class<?> serviceClass) {
		return bundleContext.getService(bundleContext.getServiceReference(serviceClass.getName()));
	}

	public Object getService(Class<?> serviceClass, String filter) {
		// TODO: [Cy3] Seems to work but may have to re revised
		try {
			ServiceReference[] services = bundleContext.getServiceReferences(serviceClass.getName(),
					filter);
			if (services != null && services.length > 0) {
				return bundleContext.getService(services[0]);
			}
		} catch (Exception ex) {
			// ignore
			// ex.printStackTrace();
		}
		return null;
	}

	public void setCreateStructureNetFactory(TaskFactory factory) {
		this.structureNetFactory = (CreateStructureNetworkTaskFactory) factory;
	}

	public void openStructures(CyNetwork network, Map<CyIdentifiable, List<String>> chimObjNames,
			ModelType type) {
		if (network == null
				|| chimObjNames.size() == 0
				|| (!chimeraManager.isChimeraLaunched() && !chimeraManager
						.launchChimera(getChimeraPaths(network)))) {
			return;
		}
		// potential rins
		Set<CyNetwork> potentialRINs = new HashSet<CyNetwork>();
		// new models
		Map<String, List<ChimeraModel>> newModels = new HashMap<String, List<ChimeraModel>>();
		// for each node that has an associated structure
		for (CyIdentifiable cyObj : chimObjNames.keySet()) {
			// save node to track its selection and mapping to chimera objects
			if (!currentCyMap.containsKey(cyObj)) {
				currentCyMap.put(cyObj, new HashSet<ChimeraStructuralObject>());
			}
			// save node to network mapping to keep track of selection events
			if (!networkMap.containsKey(cyObj)) {
				networkMap.put(cyObj, network);
			}
			// for each structure that has to be opened
			for (String chimObjName : chimObjNames.get(cyObj)) {
				// get or open the corresponding models if they already exist
				List<ChimeraModel> currentModels = chimeraManager.getChimeraModels(chimObjName, type);
				if (currentModels.size() == 0) {
					// open and return models
					currentModels = chimeraManager.openModel(chimObjName, type);
					newModels.put(chimObjName, currentModels);
				}
				// for each model
				for (ChimeraModel currentModel : currentModels) {
					System.out.println("Process model: " + currentModel.getModelName());
					// check if it is a RIN
					if (currentModel.getModelType().equals(ModelType.PDB_MODEL) && cyObj instanceof CyNode
							&& network.containsNode((CyNode) cyObj)
							&& network.getRow(cyObj).isSet(ChimUtils.RESIDUE_ATTR)) {
						ChimeraStructuralObject res = ChimUtils.fromAttribute(
								network.getRow(cyObj).get(ChimUtils.RESIDUE_ATTR, String.class), chimeraManager);
						if (res != null) {
							potentialRINs.add(network);
							continue;
						}
					}
					// if not RIN then associate new model with the cytoscape
					// node
					currentCyMap.get(cyObj).add(currentModel);
					if (!currentChimMap.containsKey(currentModel)) {
						currentChimMap.put(currentModel, new HashSet<CyIdentifiable>());
					}
					currentChimMap.get(currentModel).add(cyObj);
					currentModel.addCyObject(cyObj, network);
					currentModel.setFuncResidues(ChimUtils.parseFuncRes(getResidueList(network, cyObj),
							chimObjName));
				}
			}
		}
		// networks that contain nodes associated to newly opened models
		// this will usually be of length 1
		for (CyNetwork net : potentialRINs) {
			addStructureNetwork(net);
		}
		// update dialog
		if (mnDialog != null) {
			mnDialog.modelChanged();
		}
		aTask.associate(newModels);
	}

	public void closeStructures(Map<CyIdentifiable, List<String>> chimObjNames) {
		Set<ChimeraModel> closedModels = new HashSet<ChimeraModel>();
		// for each cytoscape object and chimera model pair
		for (CyIdentifiable cyObj : chimObjNames.keySet()) {
			if (!currentCyMap.containsKey(cyObj)) {
				// should not be the case
				System.out.println("Could not find structure for cyObj");
				continue;
			}
			// get all chimera objects associated with the cytoscape object
			List<ChimeraStructuralObject> associatedChimObjects = new ArrayList<ChimeraStructuralObject>(
					currentCyMap.get(cyObj));
			for (ChimeraStructuralObject chimObject : associatedChimObjects) {
				// find the model to be closed
				if (chimObject instanceof ChimeraModel
						&& chimObjNames.get(cyObj).contains(((ChimeraModel) chimObject).getModelName())) {
					closedModels.add((ChimeraModel) chimObject);
				}
			}
		}
		// iterate over closed models and remove associations of residues in the
		// model
		for (ChimeraModel model : closedModels) {
			closeModel(model);
		}
		if (mnDialog != null) {
			mnDialog.modelChanged();
		}
	}

	public void closeModel(ChimeraModel model) {
		// close model in Chimera
		chimeraManager.closeModel(model);
		// remove all associations
		if (currentChimMap.containsKey(model)) {
			for (CyIdentifiable cyObj : model.getCyObjects().keySet()) {
				if (cyObj == null) {
					continue;
				} else if (currentCyMap.containsKey(cyObj)) {
					currentCyMap.get(cyObj).remove(model);
				} else if (cyObj instanceof CyNetwork) {
					for (ChimeraResidue residue : model.getResidues()) {
						if (currentChimMap.containsKey(residue)) {
							for (CyIdentifiable cyObjRes : currentChimMap.get(residue)) {
								if (currentCyMap.containsKey(cyObjRes)) {
									currentCyMap.get(cyObjRes).remove(residue);
								}
							}
							currentChimMap.remove(residue);
						}
					}
				}
			}
			currentChimMap.remove(model);
		}
	}

	public void addStructureNetwork(CyNetwork rin) {
		if (rin == null) {
			return;
		}
		ChimeraModel model = null;
		for (CyNode node : rin.getNodeList()) {
			networkMap.put(node, rin);
			String residueSpec = rin.getRow(node).get(ChimUtils.RESIDUE_ATTR, String.class);
			ChimeraResidue chimObj = (ChimeraResidue) ChimUtils
					.fromAttribute(residueSpec, chimeraManager);
			// chimObj.getChimeraModel().addCyObject(node, rin);
			if (chimObj == null) {
				continue;
			}
			model = chimObj.getChimeraModel();
			if (!currentCyMap.containsKey(node)) {
				currentCyMap.put(node, new HashSet<ChimeraStructuralObject>());
			}
			currentCyMap.get(node).add(chimObj);
			if (!currentChimMap.containsKey(chimObj)) {
				currentChimMap.put(chimObj, new HashSet<CyIdentifiable>());
			}
			currentChimMap.get(chimObj).add(node);
			// TODO: add rin network to model in currentChimMap?
		}
		if (model != null) {
			model.addCyObject(rin, rin);
			if (!currentCyMap.containsKey(rin)) {
				currentCyMap.put(rin, new HashSet<ChimeraStructuralObject>());
			}
			currentCyMap.get(rin).add(model);
		}
	}

	public void exitChimera() {
		// exit chimera, invokes clearOnExitChimera
		if (mnDialog != null) {
			mnDialog.setVisible(false);
			mnDialog = null;
		}
		if (alDialog != null) {
			alDialog.setVisible(false);
		}
		chimeraManager.exitChimera();
	}

	// invoked by ChimeraManager whenever Chimera exits
	public void clearOnChimeraExit() {
		// clear structures
		currentCyMap.clear();
		currentChimMap.clear();
		networkMap.clear();
		chimSelectionList.clear();
		if (chimTable != null) {
			((CyTableManager) getService(CyTableManager.class)).deleteTable(chimTable.getSUID());
		}
		if (mnDialog != null) {
			if (mnDialog.isVisible()) {
				mnDialog.lostChimera();
				mnDialog.setVisible(false);
			}
			mnDialog = null;
			if (alDialog != null) {
				alDialog.setVisible(false);
			}
		}
	}

	// We need to do this in two passes since some parts of a structure might be
	// selected and some might not. Our selection model (unfortunately) only
	// tells
	// us that something has changed, not what...
	public void updateCytoscapeSelection() {
		// List<ChimeraStructuralObject> selectedChimObj
		ignoreCySelection = true;
		System.out.println("update Cytoscape selection");
		// find all possibly selected Cytoscape objects and unselect them
		Set<CyNetwork> networks = new HashSet<CyNetwork>();
		for (CyIdentifiable currentCyObj : currentCyMap.keySet()) {
			if (!networkMap.containsKey(currentCyObj)) {
				continue;
			}
			CyNetwork network = networkMap.get(currentCyObj);
			if (network == null) {
				continue;
			}
			if ((currentCyObj instanceof CyNode && network.containsNode((CyNode) currentCyObj))
					|| (currentCyObj instanceof CyEdge && network.containsEdge((CyEdge) currentCyObj))) {
				network.getRow(currentCyObj).set(CyNetwork.SELECTED, false);
				networks.add(network);
			}
		}

		// select only those associated with selected Chimera objects
		Set<CyIdentifiable> currentCyObjs = new HashSet<CyIdentifiable>();
		for (ChimeraStructuralObject chimObj : chimSelectionList) {
			ChimeraModel currentSelModel = chimObj.getChimeraModel();
			if (currentChimMap.containsKey(currentSelModel)) {
				currentCyObjs.addAll(currentChimMap.get(currentSelModel));
			}
			if (currentChimMap.containsKey(chimObj)) {
				currentCyObjs.addAll(currentChimMap.get(chimObj));
			}
		}
		for (CyIdentifiable cyObj : currentCyObjs) {
			if (cyObj == null || !networkMap.containsKey(cyObj)) {
				continue;
			}
			CyNetwork network = networkMap.get(cyObj);
			if (network == null) {
				continue;
			}
			if ((cyObj instanceof CyNode && network.containsNode((CyNode) cyObj))
					|| (cyObj instanceof CyEdge && network.containsEdge((CyEdge) cyObj))) {
				network.getRow(cyObj).set(CyNetwork.SELECTED, true);
				networks.add(network);
			}
		}

		CyNetworkViewManager cyNetViewManager = (CyNetworkViewManager) getService(CyNetworkViewManager.class);
		// Update network views
		for (CyNetwork network : networks) {
			Collection<CyNetworkView> views = cyNetViewManager.getNetworkViews(network);
			for (CyNetworkView view : views) {
				view.updateView();
			}
		}
		ignoreCySelection = false;
	}

	public void cytoscapeSelectionChanged(Map<Long, Boolean> selectedRows) {
		if (ignoreCySelection || currentCyMap.size() == 0) {
			return;
		}
		// clearSelectionList();
		System.out.println("cytoscape selection changed");
		// iterate over all cy objects with associated models
		for (CyIdentifiable cyObj : currentCyMap.keySet()) {
			if (!selectedRows.containsKey(cyObj.getSUID())) {
				continue;
			}
			for (ChimeraStructuralObject chimObj : currentCyMap.get(cyObj)) {
				if (selectedRows.get(cyObj.getSUID())) {
					addChimSelection(chimObj);
					if (chimObj instanceof ChimeraResidue) {

						if (chimObj.getChimeraModel().isSelected()) {
							removeChimSelection(chimObj.getChimeraModel());
						} else if (chimObj.getChimeraModel().getChain(((ChimeraResidue) chimObj).getChainId())
								.isSelected()) {
							removeChimSelection(chimObj.getChimeraModel().getChain(
									((ChimeraResidue) chimObj).getChainId()));
						}
					}
				} else {
					removeChimSelection(chimObj);
					if (chimObj.hasSelectedChildren() && chimObj instanceof ChimeraModel) {
						for (ChimeraResidue residue : ((ChimeraModel) chimObj).getSelectedResidues()) {
							removeChimSelection(residue);
						}
					}
				}
			}
		}
		System.out.println("selection list: " + getChimSelectionCount());
		updateChimeraSelection();
		selectionChanged();
	}

	// Save models in a HashMap/Set for better performance?
	public void updateChimeraSelection() {
		System.out.println("update Chimera selection");
		String selSpec = "";
		for (int i = 0; i < chimSelectionList.size(); i++) {
			ChimeraStructuralObject nodeInfo = chimSelectionList.get(i);
			// we do not care about the model anymore
			selSpec = selSpec.concat(nodeInfo.toSpec());
			if (i < chimSelectionList.size() - 1)
				selSpec.concat("|");
		}
		if (selSpec.length() > 0) {
			chimeraManager.select("sel " + selSpec);
		} else {
			chimeraManager.select("~sel");
		}
	}

	/**
	 * This is called by the selectionListener to let us know that the user has changed their
	 * selection in Chimera. We need to go back to Chimera to find out what is currently selected and
	 * update our list.
	 */
	public void chimeraSelectionChanged() {
		System.out.println("Chimera selection changed");
		clearSelectionList();
		// Execute the command to get the list of models with selections
		Map<Integer, ChimeraModel> selectedModelsMap = chimeraManager.getSelectedModels();
		// Now get the residue-level data
		chimeraManager.getSelectedResidues(selectedModelsMap);
		// Get the selected objects
		try {
			for (ChimeraModel selectedModel : selectedModelsMap.values()) {
				int modelNumber = selectedModel.getModelNumber();
				int subModelNumber = selectedModel.getSubModelNumber();
				// Get the corresponding "real" model
				if (chimeraManager.hasChimeraModel(modelNumber, subModelNumber)) {
					ChimeraModel dataModel = chimeraManager.getChimeraModel(modelNumber, subModelNumber);
					if (dataModel.getResidueCount() == selectedModel.getResidueCount()
							|| dataModel.getModelType() == StructureManager.ModelType.SMILES) {
						// Select the entire model
						addChimSelection(dataModel);
						// dataModel.setSelected(true);
					} else {
						for (ChimeraChain selectedChain : selectedModel.getChains()) {
							ChimeraChain dataChain = dataModel.getChain(selectedChain.getChainId());
							if (selectedChain.getResidueCount() == dataChain.getResidueCount()) {
								addChimSelection(dataChain);
								// dataChain.setSelected(true);
							} else {
								// Need to select individual residues
								for (ChimeraResidue res : selectedChain.getResidues()) {
									String residueIndex = res.getIndex();
									ChimeraResidue residue = dataChain.getResidue(residueIndex);
									if (residue == null) {
										continue;
									}
									addChimSelection(residue);
									// residue.setSelected(true);
								} // resIter.hasNext
							}
						} // chainIter.hasNext()
					}
				}
			} // modelIter.hasNext()
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("selection list: " + getChimSelectionCount());
		// Finally, update the navigator panel
		selectionChanged();
		updateCytoscapeSelection();
	}

	public void selectFunctResidues(Collection<ChimeraModel> models) {
		clearSelectionList();
		for (ChimeraModel model : models) {
			for (ChimeraResidue residue : model.getFuncResidues()) {
				addChimSelection(residue);
			}
		}
		updateChimeraSelection();
		updateCytoscapeSelection();
		selectionChanged();
	}

	public List<ChimeraStructuralObject> getChimSelectionList() {
		return chimSelectionList;
	}

	public int getChimSelectionCount() {
		return chimSelectionList.size();
	}

	/**
	 * Add a selection to the selection list. This is called primarily by the Model Navigator Dialog
	 * to keep the selections in sync
	 * 
	 * @param selectionToAdd
	 *          the selection to add to our list
	 */
	public void addChimSelection(ChimeraStructuralObject selectionToAdd) {
		if (selectionToAdd != null && !chimSelectionList.contains(selectionToAdd)) {
			chimSelectionList.add(selectionToAdd);
			selectionToAdd.setSelected(true);
		}
	}

	/**
	 * Remove a selection from the selection list. This is called primarily by the Model Navigator
	 * Dialog to keep the selections in sync
	 * 
	 * @param selectionToRemove
	 *          the selection to remove from our list
	 */
	public void removeChimSelection(ChimeraStructuralObject selectionToRemove) {
		if (selectionToRemove != null && chimSelectionList.contains(selectionToRemove)) {
			chimSelectionList.remove(selectionToRemove);
			selectionToRemove.setSelected(false);
		}
	}

	/**
	 * Clear the list of selected objects
	 */
	public void clearSelectionList() {
		for (ChimeraStructuralObject cso : chimSelectionList) {
			if (cso != null)
				cso.setSelected(false);
		}
		chimSelectionList.clear();
	}

	public void associate(CyNetwork network) {
		if (network != null) {
			aTask.associate(network);
		} else {
			aTask.associate();
		}
	}

	/**
	 * De-associate a network from its mapping to Chimera objects.
	 * 
	 * @param network
	 */
	public void deassociate(CyNetwork network) {
		if (network == null) {
			return;
		}
		for (ChimeraStructuralObject chimObj : currentChimMap.keySet()) {
			// if (currentChimMap.get(chimObj).contains(network)) {
			// currentChimMap.get(chimObj).remove(network);
			// }
			if (chimObj instanceof ChimeraModel
					&& ((ChimeraModel) chimObj).getCyObjects().containsKey(network)) {
				((ChimeraModel) chimObj).getCyObjects().remove(network);
				if (currentCyMap.containsKey(network)) {
					currentCyMap.remove(network);
				}
			}
		}
		if (currentCyMap.containsKey(network)) {
			currentCyMap.remove(network);
		}
		deassociate(new HashSet<CyIdentifiable>(network.getNodeList()), false);
		deassociate(new HashSet<CyIdentifiable>(network.getEdgeList()), false);
		modelChanged();
	}

	/**
	 * De-associate a set of nodes or edges from their mapping to Chimera objects.
	 * 
	 * @param cyObjects
	 * @param updateDialog
	 */
	public void deassociate(Set<CyIdentifiable> cyObjects, boolean updateDialog) {
		for (CyIdentifiable cyObj : cyObjects) {
			if (currentCyMap.containsKey(cyObj)) {
				for (ChimeraStructuralObject chimObj : currentCyMap.get(cyObj)) {
					if (currentChimMap.containsKey(chimObj)) {
						currentChimMap.get(chimObj).remove(cyObj);
					}
				}
				currentCyMap.remove(cyObj);
			}
			networkMap.remove(cyObj);
		}
		if (updateDialog) {
			modelChanged();
		}
	}

	/**
	 * Associate a new network with the corresponding Chimera objects.
	 * 
	 * @param network
	 */

	/**
	 * Dump and refresh all of our model/chain/residue info
	 */
	public void updateModels() {
		// Stop all of our listeners while we try to handle this
		chimeraManager.stopListening();

		// Get all of the open models
		List<ChimeraModel> newModelList = chimeraManager.getModelList();

		// Match them up -- assume that the model #'s haven't changed
		for (ChimeraModel newModel : newModelList) {
			// Get the color (for our navigator)
			newModel.setModelColor(chimeraManager.getModelColor(newModel));

			// Get our model info
			int modelNumber = newModel.getModelNumber();
			int subModelNumber = newModel.getSubModelNumber();

			// If we already know about this model number, get the Structure,
			// which tells us about the associated CyNode
			if (chimeraManager.hasChimeraModel(modelNumber, subModelNumber)) {
				ChimeraModel oldModel = chimeraManager.getChimeraModel(modelNumber, subModelNumber);
				chimeraManager.removeChimeraModel(modelNumber, subModelNumber);
				newModel.setModelType(oldModel.getModelType());
				if (oldModel.getModelType() == ModelType.SMILES) {
					newModel.setModelName(oldModel.getModelName());
				}
				// re-assign associations to cytoscape objects
				Map<CyIdentifiable, CyNetwork> oldModelCyObjs = oldModel.getCyObjects();
				for (CyIdentifiable cyObj : oldModelCyObjs.keySet()) {
					// add cy objects to the new model
					newModel.addCyObject(cyObj, oldModelCyObjs.get(cyObj));
					if (currentCyMap.containsKey(cyObj)) {
						currentCyMap.get(cyObj).add(newModel);
						if (currentCyMap.get(cyObj).contains(oldModel)) {
							currentCyMap.get(cyObj).remove(oldModel);
						}
					}
				}
				// add new model to the chimera objects map and remove old model
				if (currentChimMap.containsKey(oldModel)) {
					currentChimMap.put(newModel, currentChimMap.get(oldModel));
					currentChimMap.remove(oldModel);
				}
			}
			// add new model to ChimeraManager
			chimeraManager.addChimeraModel(modelNumber, subModelNumber, newModel);

			// Get the residue information
			if (newModel.getModelType() != ModelType.SMILES) {
				chimeraManager.addResidues(newModel);
			}
			for (CyIdentifiable cyObj : newModel.getCyObjects().keySet()) {
				if (cyObj != null && cyObj instanceof CyNetwork) {
					addStructureNetwork((CyNetwork) cyObj);
				}
			}
		}

		// associate all models with any node or network
		aTask.associate(chimeraManager.getChimeraModelsMap());

		// Restart all of our listeners
		chimeraManager.startListening();
		// Done
	}

	public void launchModelNavigatorDialog() {
		System.out.println("launch dialog");
		if (!haveGUI) {
			return;
		}
		if (mnDialog != null) {
			mnDialog.setVisible(true);
			return;
		}
		CySwingApplication cyApplication = (CySwingApplication) getService(CySwingApplication.class);
		mnDialog = new ModelNavigatorDialog(cyApplication.getJFrame(), this);
		mnDialog.pack();
		mnDialog.setVisible(true);
	}

	/**
	 * Invoked by the listener thread.
	 */
	public void modelChanged() {
		if (mnDialog != null) {
			mnDialog.modelChanged();
		}
	}

	/**
	 * Inform our interface that the selection has changed
	 */
	public void selectionChanged() {
		if (mnDialog != null) {
			System.out.println("update dialog selection");
			mnDialog.updateSelection(new ArrayList<ChimeraStructuralObject>(chimSelectionList));
		}
	}

	public void launchAlignDialog(boolean useChains) {
		if (!haveGUI) {
			return;
		}
		if (alDialog != null) {
			alDialog.setVisible(false);
			alDialog.dispose();
		}
		List<ChimeraStructuralObject> chimObjectList = new ArrayList<ChimeraStructuralObject>();
		for (ChimeraModel model : chimeraManager.getChimeraModels()) {
			chimObjectList.add(model);
			if (useChains) {
				for (ChimeraChain chain : model.getChains()) {
					chimObjectList.add(chain);
				}
			}
		}
		// Bring up the dialog
		alDialog = new AlignStructuresDialog(mnDialog, this, chimObjectList);
		alDialog.pack();
		alDialog.setVisible(true);
	}

	public AlignStructuresDialog getAlignDialog() {
		return alDialog;
	}

	public void launchStructureNetworkDialog() {
		if (!haveGUI) {
			return;
		}
		DialogTaskManager taskManager = (DialogTaskManager) getService(DialogTaskManager.class);
		if (taskManager != null) {
			taskManager.execute(structureNetFactory.createTaskIterator());
		}
	}

	/**
	 * Return all open structures for a set of CyObjects. Invoked by CloseStructuresTask.
	 * 
	 * @param cyObjSet
	 * @return
	 */
	public Map<CyIdentifiable, List<String>> getOpenChimObjNames(List<CyIdentifiable> cyObjSet) {
		Map<CyIdentifiable, List<String>> matchingNames = new HashMap<CyIdentifiable, List<String>>();
		for (CyIdentifiable cyObj : cyObjSet) {
			List<String> nodeMatchingNames = new ArrayList<String>();
			if (currentCyMap.containsKey(cyObj)) {
				for (ChimeraStructuralObject chimObj : currentCyMap.get(cyObj)) {
					// TODO: Check
					// if (chimObj instanceof ChimeraModel) {
					String modelName = chimObj.getChimeraModel().getModelName();
					if (!nodeMatchingNames.contains(modelName)) {
						nodeMatchingNames.add(modelName);
					}
					// }
				}
				if (nodeMatchingNames.size() > 0) {
					matchingNames.put(cyObj, nodeMatchingNames);
				}
			}
		}
		return matchingNames;
	}

	/**
	 * Return the names of structures or smiles that can be opened in Chimera from the selected
	 * attribute. Invoked by openStructuresTask.
	 * 
	 * @param network
	 * @param nodeSet
	 * @return
	 */
	public Map<CyIdentifiable, List<String>> getChimObjNames(CyNetwork network,
			List<CyIdentifiable> cyObjSet, ModelType type) {

		Map<CyIdentifiable, List<String>> mapChimObjNames = new HashMap<CyIdentifiable, List<String>>();
		if (network == null || cyObjSet.size() == 0)
			return mapChimObjNames;
		CyTable table = null;
		if (cyObjSet.get(0) instanceof CyNode) {
			table = network.getDefaultNodeTable();
		} else if (cyObjSet.get(0) instanceof CyEdge) {
			table = network.getDefaultEdgeTable();
		}
		if (table == null) {
			return mapChimObjNames;
		}
		List<String> attrsFound = null;
		if (type == ModelType.PDB_MODEL)
			attrsFound = CyUtils.getMatchingAttributes(table, getCurrentStructureKeys(network));
		else if (type == ModelType.SMILES) {
			attrsFound = CyUtils.getMatchingAttributes(table, getCurrentChemStructKeys(network));
		}

		// if something is null, just return an empty map
		if (attrsFound == null || attrsFound.size() == 0)
			return mapChimObjNames;
		// iterate over cytoscape objects
		for (CyIdentifiable cyObj : cyObjSet) {
			// skip if node/edge does not exist anymore
			if (!table.rowExists(cyObj.getSUID())) {
				continue;
			}
			CyRow row = table.getRow(cyObj.getSUID());
			// iterate over attributes that contain structures
			for (String column : attrsFound) {
				CyColumn col = table.getColumn(column);
				if (col == null) {
					continue;
				}

				Class<?> colType = col.getType();
				List<String> cellList = new ArrayList<String>();
				if (colType == String.class) {
					String cell = row.get(column, String.class, "").trim();
					if (cell.equals("")) {
						continue;
					}
					String[] cellArray = cell.split(",");
					for (String str : cellArray) {
						cellList.add(str.trim());
					}
				} else if (colType == List.class && col.getListElementType() == String.class) {
					for (String str : row.getList(column, String.class)) {
						cellList.add(str.trim());
					}
				} else {
					continue;
				}

				for (String cell : cellList) {
					// skip if the structure is already open
					if (currentCyMap.containsKey(cyObj) && chimeraManager.getChimeraModels(cell).size() > 0) {
						continue;
					}
					// add structure name to map
					if (!mapChimObjNames.containsKey(cyObj)) {
						mapChimObjNames.put(cyObj, new ArrayList<String>());
					}
					mapChimObjNames.get(cyObj).add(cell);
				}
			}
		}
		return mapChimObjNames;
	}

	public void annotate(CyNetwork network, String resAttr, String command) {
		// get models
		if (currentCyMap.containsKey(network)) {
			for (ChimeraStructuralObject chimObj : currentCyMap.get(network)) {
				if (chimObj instanceof ChimeraModel) {
					// get attribute values
					Map<ChimeraResidue, Object> resValues = chimeraManager.getAttrValues(command,
							chimObj.getChimeraModel());
					if (resValues.size() == 0) {
						continue;
					}
					Object testObj = resValues.values().iterator().next();
					if (testObj == null) {
						continue;
					}
					// TODO: Do we really need this?
					// create attribute
					if (network.getDefaultNodeTable().getColumn(resAttr) != null
							&& network.getDefaultNodeTable().getColumn(resAttr).getType() != testObj.getClass()) {
						network.getDefaultNodeTable().deleteColumn(resAttr);
					} else if (network.getDefaultNodeTable().getColumn(resAttr) == null) {
						network.getDefaultNodeTable().createColumn(resAttr, testObj.getClass(), false);
					}
					// save all the values
					for (ChimeraResidue res : resValues.keySet()) {
						if (currentChimMap.containsKey(res)) {
							for (CyIdentifiable cyId : currentChimMap.get(res)) {
								if (cyId instanceof CyNode && network.containsNode((CyNode) cyId)) {
									network.getRow(cyId).set(resAttr, resValues.get(res));
								}
							}
						}
					}
				}
			}
		}
	}

	public void annotateSS(CyNetwork network) {
		// get models
		final String ssColumn = "SS";
		if (currentCyMap.containsKey(network)) {
			if (network.getDefaultNodeTable().getColumn(ssColumn) == null) {
				network.getDefaultNodeTable().createColumn(ssColumn, String.class, false, "");
			}
			for (ChimeraStructuralObject chimObj : currentCyMap.get(network)) {
				if (chimObj instanceof ChimeraModel) {
					chimeraManager.sendChimeraCommand("ksdssp", false);
					Map<ChimeraResidue, Object> hResidues = chimeraManager.getAttrValues("isHelix",
							chimObj.getChimeraModel());
					Map<ChimeraResidue, Object> sResidues = chimeraManager.getAttrValues("isSheet",
							chimObj.getChimeraModel());
					for (ChimeraResidue res : hResidues.keySet()) {
						if (currentChimMap.containsKey(res)) {
							for (CyIdentifiable cyId : currentChimMap.get(res)) {
								if (cyId instanceof CyNode && network.containsNode((CyNode) cyId)) {
									if (hResidues.get(res).equals(Boolean.TRUE)) {
										network.getRow(cyId).set(ssColumn, "Helix");
									} else if (sResidues.containsKey(res) && sResidues.get(res).equals(Boolean.TRUE)) {
										network.getRow(cyId).set(ssColumn, "Sheet");
									} else {
										network.getRow(cyId).set(ssColumn, "Loop");
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void getResidueCoord(CyNetworkView networkView) {
		
	}

	public void mapChimeraColorToCytoscape(CyNetworkView networkView) {
		// get models
		CyNetwork network = networkView.getModel();
		if (currentCyMap.containsKey(network)) {
			for (ChimeraStructuralObject chimObj : currentCyMap.get(network)) {
				if (chimObj instanceof ChimeraModel) {
					// get attribute values
					Map<ChimeraResidue, Object> resValues = chimeraManager.getAttrValues("ribbonColor",
							chimObj.getChimeraModel());
					if (resValues.size() == 0) {
						continue;
					}
					// save all the values
					for (ChimeraResidue res : resValues.keySet()) {
						if (currentChimMap.containsKey(res)) {
							for (CyIdentifiable cyId : currentChimMap.get(res)) {
								if (cyId instanceof CyNode && network.containsNode((CyNode) cyId)) {
									String[] rgb = ((String) resValues.get(res)).split(",");
									if (rgb.length == 3) {
										try {
											Paint resColor = new Color(Float.valueOf(rgb[0]), Float.valueOf(rgb[1]),
													Float.valueOf(rgb[2]));
											networkView.getNodeView((CyNode) cyId).clearValueLock(
													BasicVisualLexicon.NODE_FILL_COLOR);
											networkView.getNodeView((CyNode) cyId).setVisualProperty(
													BasicVisualLexicon.NODE_FILL_COLOR, resColor);
										} catch (NumberFormatException ex) {
											// ignore
											ex.printStackTrace();
										}
									}
								}
							}
						}
					}
					networkView.updateView();
				}
			}
		}
	}

	public void setStructureSettings(CyNetwork network, StructureSettings newSettings) {
		settings.put(network, newSettings);
	}

	public List<String> getAllStructureKeys(CyNetwork network) {
		return Arrays.asList(defaultStructureKeys);
	}

	public List<String> getCurrentStructureKeys(CyNetwork network) {
		if (!settings.containsKey(network))
			return Arrays.asList(defaultStructureKeys);
		return settings.get(network).getStructureColumns().getSelectedValues();
	}

	public List<String> getAllChemStructKeys(CyNetwork network) {
		return Arrays.asList(defaultChemStructKeys);
	}

	public List<String> getCurrentChemStructKeys(CyNetwork network) {
		if (!settings.containsKey(network))
			return Arrays.asList(defaultChemStructKeys);
		return settings.get(network).getChemStructureColumns().getSelectedValues();
	}

	public List<String> getAllResidueKeys(CyNetwork network) {
		return Arrays.asList(defaultResidueKeys);
	}

	public List<String> getCurrentResidueKeys(CyNetwork network) {
		if (!settings.containsKey(network))
			return Arrays.asList(defaultResidueKeys);
		return settings.get(network).getResidueColumns().getSelectedValues();
	}

	public List<String> getAllResidueAttributes() {
		List<String> attributes = new ArrayList<String>();
		attributes.addAll(residueAttrCommandMap.keySet());
		attributes.addAll(chimeraManager.getAttrList());
		return attributes;
	}

	public String getDefaultChimeraPath() {
		String path = "";
		CyProperty<?> defaultPathProperty = null;
		CySessionManager sessionManager = (CySessionManager) getService(CySessionManager.class);
		if (sessionManager != null) {
			CySession session = sessionManager.getCurrentSession();
			if (session != null) {
				Set<CyProperty<?>> props = session.getProperties();
				if (props != null) {
					for (CyProperty<?> prop : props) {
						if (prop.getName() != null) {
							if (prop.getName().equals("defaultChimeraPath")) {
								defaultPathProperty = prop;
								Properties pathProps = (Properties) defaultPathProperty.getProperties();
								path = pathProps.getProperty("defaultChimeraPath");
								break;
							}
						}
					}
				}
			}
		}
		return path;
	}

	public void setDefaultChimeraPath(String path) {
		// TODO: Make this work
		CyProperty<?> defaultPathProperty = null;
		CySessionManager sessionManager = (CySessionManager) getService(CySessionManager.class);
		if (sessionManager != null) {
			CySession session = sessionManager.getCurrentSession();
			if (session != null) {
				Set<CyProperty<?>> props = session.getProperties();
				if (props != null) {
					boolean propExists = false;
					for (CyProperty<?> prop : props) {
						// System.out.println("prop: " + prop.toString());
						if (prop.getName() != null) {
							// System.out.println(prop.getName());
							if (prop.getName().equals("defaultChimeraPath")) {
								defaultPathProperty = prop;
								pathProps = (Properties) defaultPathProperty.getProperties();
								if (!pathProps.getProperty("defaultChimeraPath").equals(path)) {
									pathProps.setProperty("defaultChimeraPath", path);
									// session.getProperties().add(defaultPathProperty);
									propExists = true;
									break;
								}
							}
						}
					}
					if (!propExists) {
						pathProps.setProperty("defaultChimeraPath", path);
						defaultPathProperty = new SimpleCyProperty("defaultChimeraPath", pathProps,
								String.class, CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR);
						// session.getProperties().add(defaultPathProperty);
					}
				}
			}
		}
	}

	public List<String> getChimeraPaths(CyNetwork network) {
		List<String> pathList = new ArrayList<String>();

		if (settings.containsKey(network)) {
			String path = settings.get(network).getChimeraPath();
			if (path != null) {
				pathList.add(path);
				return pathList;
			}
		}

		String os = System.getProperty("os.name");
		if (os.startsWith("Linux")) {
			pathList.add("/usr/local/chimera/bin/chimera");
			pathList.add("/usr/local/bin/chimera");
			pathList.add("/usr/bin/chimera");
		} else if (os.startsWith("Windows")) {
			pathList.add("\\Program Files\\Chimera\\bin\\chimera");
			pathList.add("C:\\Program Files\\Chimera\\bin\\chimera.exe");
		} else if (os.startsWith("Mac")) {
			pathList.add("/Applications/Chimera.app/Contents/MacOS/chimera");
		}

		return pathList;
	}

	/**
	 * Set the "active site" or "special" residues
	 * 
	 * @param residues
	 *          String representation of the residues (comma separated)
	 */
	private List<String> getResidueList(CyNetwork network, CyIdentifiable cyObj) {
		List<String> residueList = new ArrayList<String>();
		// Get from attribute
		CyTable nodeTable = network.getDefaultNodeTable();
		List<String> attrsFound = CyUtils.getMatchingAttributes(nodeTable,
				getCurrentResidueKeys(network));
		if (attrsFound == null || attrsFound.size() == 0 || !nodeTable.rowExists(cyObj.getSUID())) {
			return residueList;
		}
		CyRow row = nodeTable.getRow(cyObj.getSUID());
		// iterate over attributes that contain residues
		for (String column : attrsFound) {
			CyColumn col = nodeTable.getColumn(column);
			Class<?> colType = col.getType();
			if (colType == String.class) {
				String cell = row.get(column, String.class, "").trim();
				if (cell.equals("")) {
					continue;
				}
				String[] cellArray = cell.split(",");
				for (String str : cellArray) {
					residueList.add(str.trim());
				}
			} else if (colType == List.class && col.getListElementType() == String.class) {
				for (String str : row.getList(column, String.class)) {
					residueList.add(str.trim());
				}
			} else {
				continue;
			}
		}
		return residueList;
	}

	public void initChimTable() {
		CyTableManager manager = (CyTableManager) getService(CyTableManager.class);
		CyTableFactory factory = (CyTableFactory) getService(CyTableFactory.class);
		for (CyTable table : manager.getGlobalTables()) {
			if (table.getTitle().equals(chimeraOutputTable)) {
				manager.deleteTable(table.getSUID());
			}
		}
		chimTable = factory.createTable(chimeraOutputTable, chimeraCommandAttr, String.class, false,
				true);
		manager.addTable(chimTable);
		if (chimTable.getColumn(chimeraOutputAttr) == null) {
			chimTable.createListColumn(chimeraOutputAttr, String.class, false);
		}
	}

	public void addChimReply(String command, List<String> reply) {
		chimTable.getRow(command).set(chimeraOutputAttr, reply);
	}

	class AssociationTask extends Thread {

		public AssociationTask() {
		}

		public void run() {
		}

		public void associate(CyNetwork network) {
			associateNetwork(network, chimeraManager.getChimeraModelsMap());
		}

		public void associate(Map<String, List<ChimeraModel>> newModels) {
			CyNetworkManager netManager = (CyNetworkManager) getService(CyNetworkManager.class);
			// iterate over all networks
			for (CyNetwork network : netManager.getNetworkSet()) {
				associateNetwork(network, newModels);
			}
		}

		public void associate() {
			CyNetworkManager netManager = (CyNetworkManager) getService(CyNetworkManager.class);
			Map<String, List<ChimeraModel>> newModels = chimeraManager.getChimeraModelsMap();
			// iterate over all networks
			for (CyNetwork network : netManager.getNetworkSet()) {
				associateNetwork(network, newModels);
			}
		}

		public synchronized void associateNetwork(CyNetwork network,
				Map<String, List<ChimeraModel>> newModels) {
			ChimeraModel rinModel = null;
			List<CyIdentifiable> nodes = new ArrayList<CyIdentifiable>();
			nodes.addAll(network.getNodeList());
			// for each network get the pdb names associated with its nodes
			Map<CyIdentifiable, List<String>> chimObjNames = getChimObjNames(network, nodes,
					ModelType.PDB_MODEL);
			for (CyIdentifiable cyObj : chimObjNames.keySet()) {
				for (String modelName : chimObjNames.get(cyObj)) {
					// node should be associated with current model
					if (newModels.containsKey(modelName)) {
						// add it to the map
						if (!currentCyMap.containsKey(cyObj)) {
							currentCyMap.put(cyObj, new HashSet<ChimeraStructuralObject>());
						}
						// keep track of the network it belongs to
						if (!networkMap.containsKey(cyObj)) {
							networkMap.put(cyObj, network);
						}
						// check if it is a RIN
						ChimeraResidue residue = null;
						if (cyObj instanceof CyNode
								&& network.containsNode((CyNode) cyObj)
								&& network.getRow(cyObj).isSet(ChimUtils.RESIDUE_ATTR)
								&& network.getDefaultNodeTable().getColumn(ChimUtils.RESIDUE_ATTR).getType() == String.class) {
							residue = (ChimeraResidue) ChimUtils.fromAttribute(
									network.getRow(cyObj).get(ChimUtils.RESIDUE_ATTR, String.class), chimeraManager);
						}
						if (residue != null) {
							// if it is a RIN save only node <-> residue
							// association
							currentCyMap.get(cyObj).add(residue);
							if (!currentChimMap.containsKey(residue)) {
								currentChimMap.put(residue, new HashSet<CyIdentifiable>());
							}
							currentChimMap.get(residue).add(cyObj);
							if (rinModel == null) {
								rinModel = residue.getChimeraModel();
							}
						} else {
							// save node <-> model association
							currentCyMap.get(cyObj).addAll(newModels.get(modelName));
							for (ChimeraModel model : newModels.get(modelName)) {
								if (!currentChimMap.containsKey(model)) {
									currentChimMap.put(model, new HashSet<CyIdentifiable>());
								}
								currentChimMap.get(model).add(cyObj);
								model.addCyObject(cyObj, network);
							}
						}
					}
				}
			}
			if (rinModel != null) {
				rinModel.addCyObject(network, network);
				if (!currentCyMap.containsKey(network)) {
					currentCyMap.put(network, new HashSet<ChimeraStructuralObject>());
				}
				currentCyMap.get(network).add(rinModel);
				rinModel = null;
			}

		}

	}

}
