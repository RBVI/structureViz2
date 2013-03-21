package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import edu.ucsf.rbvi.structureViz2.internal.ui.ModelNavigatorDialog;

/**
 * This object maintains the relationship between Chimera objects and Cytoscape
 * objects.
 */
public class StructureManager {
	static final String[] defaultStructureKeys = { "Structure", "pdb", "pdbFileName", "PDB ID",
			"structure", "biopax.xref.PDB", "pdb_ids" };
	static final String[] defaultChemStructKeys = { "Smiles", "smiles", "SMILES" };
	static final String[] defaultResidueKeys = { "FunctionalResidues", "ResidueList" };

	public enum ModelType {
		PDB_MODEL, MODBASE_MODEL, SMILES
	};

	private ChimeraManager chimeraManager = null;
	private Map<CyNetwork, StructureSettings> settings = null;
	private Map<CyIdentifiable, Structure> currentStructuresMap = null;

	static private ModelNavigatorDialog mnDialog = null;
	static private List<ChimeraStructuralObject> selectionList;

	// static private AlignStructuresDialog alDialog = null;

	public StructureManager() {
		settings = new HashMap<CyNetwork, StructureSettings>();
		currentStructuresMap = new HashMap<CyIdentifiable, Structure>();
		// Create the Chimera interface
		chimeraManager = new ChimeraManager(this);
		selectionList = new ArrayList<ChimeraStructuralObject>();
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

	public ChimeraManager getChimeraManager() {
		return chimeraManager;
	}

	/**
	 * Return all open structures for a set of CyObjects. Invoked by
	 * CloseStructuresTask.
	 * 
	 * @param cyObjSet
	 * @return
	 */
	public Map<CyIdentifiable, List<String>> getNodeOpenChimObjNames(Collection<CyNode> cyObjSet) {
		Map<CyIdentifiable, List<String>> matchingNames = new HashMap<CyIdentifiable, List<String>>();
		for (CyNode obj : cyObjSet) {
			List<String> nodeMatchingNames = new ArrayList<String>();
			if (currentStructuresMap.containsKey(obj)) {
				Structure structure = currentStructuresMap.get(obj);
				System.out.println("structure found");
				nodeMatchingNames.addAll(structure.getChimeraModelNames());
				if (nodeMatchingNames.size() > 0) {
					matchingNames.put(obj, nodeMatchingNames);
				}
			}
		}
		return matchingNames;
	}

	public boolean hasNodeChimObjNames(CyNetwork network, Collection<CyNode> nodeSet) {
		if (network == null)
			return false;
		CyTable nodeTable = network.getDefaultNodeTable();
		// TODO: Return not only the structure keys but also all other types?
		List<String> attrsFound = CyUtils.getMatchingAttributes(nodeTable,
				getCurrentStructureKeys(network));
		Collection idSet = nodeSet;
		return hasChimObjNames(idSet, nodeTable, attrsFound);
	}

	/**
	 * Return the names of structures or smiles that can be opened in Chimera from
	 * the selected attribute. Invoked by openStructuresTask.
	 * 
	 * @param network
	 * @param nodeSet
	 * @return
	 */
	public Map<CyIdentifiable, List<String>> getNodeChimObjNames(CyNetwork network,
			Collection<CyNode> nodeSet) {
		if (network == null)
			return new HashMap<CyIdentifiable, List<String>>();
		CyTable nodeTable = network.getDefaultNodeTable();
		List<String> attrsFound = CyUtils.getMatchingAttributes(nodeTable,
				getCurrentStructureKeys(network));
		Collection idSet = nodeSet;
		return getChimObjNames(idSet, nodeTable, attrsFound);
	}

	public boolean hasEdgeChimObjNames(CyNetwork network, Collection<CyEdge> edgeSet) {
		if (network == null)
			return false;
		CyTable edgeTable = network.getDefaultEdgeTable();
		List<String> attrsFound = CyUtils.getMatchingAttributes(edgeTable,
				getCurrentChemStructKeys(network));
		Collection idSet = edgeSet;
		return hasChimObjNames(idSet, edgeTable, attrsFound);
	}

	public void openStructures(CyNetwork network, Map<CyIdentifiable, List<String>> chimObjNames) {
		if (!chimeraManager.isChimeraLaunched()) {
			chimeraManager.launchChimera();
		}
		for (CyIdentifiable cyObj : chimObjNames.keySet()) {
			Structure currentStructure = null;
			if (currentStructuresMap.containsKey(cyObj)) {
				currentStructure = currentStructuresMap.get(cyObj);
			} else {
				currentStructure = new Structure(network, cyObj);
				currentStructuresMap.put(cyObj, currentStructure);
			}
			System.out.println("structures for cyObj " + cyObj.getSUID() + ": "
					+ chimObjNames.get(cyObj).size());
			for (String chimObjName : chimObjNames.get(cyObj)) {
				List<ChimeraModel> currentModels = chimeraManager.getChimeraModels(chimObjName);
				if (currentModels.size() == 0) {
					// map and open
					currentModels = chimeraManager.openModel(chimObjName, ModelType.PDB_MODEL);
				}
				for (ChimeraModel currentModel : currentModels) {
					System.out.println("add new model to structure");
					currentStructure.addChimeraObject(currentModel);
				}
			}
		}
		if (mnDialog != null) {
			mnDialog.modelChanged();
		}
	}

	public void closeStructures(CyNetwork network, Map<CyIdentifiable, List<String>> chimObjNames) {
		for (CyIdentifiable cyObj : chimObjNames.keySet()) {
			if (!currentStructuresMap.containsKey(cyObj)) {
				// should not be the case
				System.out.println("Could not find structure for cyObj");
				continue;
			}
			Structure currentStructure = currentStructuresMap.get(cyObj);
			System.out.println("models for cyObj " + cyObj.getSUID() + ": "
					+ chimObjNames.get(cyObj).size());
			for (String chimObjName : chimObjNames.get(cyObj)) {
				List<ChimeraModel> currentModels = currentStructure.getChimeraModels(chimObjName);
				for (ChimeraModel currentModel : currentModels) {
					if (currentModel != null) {
						chimeraManager.closeModel(currentModel);
						currentStructure.removeChimeraObject(currentModel);
					}
				}
			}
			// TODO: Remove structure when empty?
			if (currentStructure.getChimeraObjects().size() == 0) {
				currentStructuresMap.remove(cyObj);
			}
		}
	}

	public void exitChimera() {
		// exit chimera, invokes clearOnExitChimera
		if (mnDialog != null) {
			mnDialog.setVisible(false);
			mnDialog = null;
		}
		chimeraManager.exitChimera();
		// if (alDialog != null)
		// alDialog.setVisible(false);

	}

	// invoked by ChimeraManager whenever Chimera exits
	public void clearOnChimeraExit() {
		// clear structures
		currentStructuresMap.clear();
		if (mnDialog != null) {
			mnDialog.lostChimera();
		}
	}

	/**
	 * This is called by the selectionListener to let us know that the user has
	 * changed their selection in Chimera. We need to go back to Chimera to find
	 * out what is currently selected and update our list.
	 */
	// TODO: Move code to ChimeraManager?
	public void updateSelection() {
		System.out.println("updateSelection");
		clearSelectionList();
		// Execute the command to get the list of models with selections
		Map<Integer, ChimeraModel> selectedModelsMap = chimeraManager.getSelectedModels();
		System.out.println("selected models: " + selectedModelsMap.size());
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
						selectionList.add(dataModel);
						dataModel.setSelected(true);
					} else {
						for (ChimeraChain selectedChain : selectedModel.getChains()) {
							ChimeraChain dataChain = dataModel.getChain(selectedChain.getChainId());
							if (selectedChain.getResidueCount() == dataChain.getResidueCount()) {
								selectionList.add(dataChain);
								dataChain.setSelected(true);
							} else {
								// Need to select individual residues
								for (ChimeraResidue res : selectedChain.getResidues()) {
									String residueIndex = res.getIndex();
									ChimeraResidue residue = dataChain.getResidue(residueIndex);
									if (residue == null) {
										continue;
									}
									selectionList.add(residue);
									residue.setSelected(true);
								} // resIter.hasNext
							}
						} // chainIter.hasNext()
					}
				}
			} // modelIter.hasNext()
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("selected elements: " + selectionList.size());
		// Finally, update the navigator panel
		selectionChanged();
	}

	public List<ChimeraStructuralObject> getSelectionList() {
		return selectionList;
	}

	/**
	 * Add a selection to the selection list. This is called primarily by the
	 * Model Navigator Dialog to keep the selections in sync
	 * 
	 * @param selectionToAdd
	 *          the selection to add to our list
	 */
	public void addSelection(ChimeraStructuralObject selectionToAdd) {
		if (selectionToAdd != null && !selectionList.contains(selectionToAdd))
			selectionList.add(selectionToAdd);
	}

	/**
	 * Remove a selection from the selection list. This is called primarily by the
	 * Model Navigator Dialog to keep the selections in sync
	 * 
	 * @param selectionToRemove
	 *          the selection to remove from our list
	 */
	public void removeSelection(ChimeraStructuralObject selectionToRemove) {
		if (selectionToRemove != null && selectionList.contains(selectionToRemove))
			selectionList.remove(selectionToRemove);
	}

	/**
	 * Clear the list of selected objects
	 */
	public void clearSelectionList() {
		for (ChimeraStructuralObject cso : selectionList) {
			if (cso != null)
				cso.setSelected(false);
		}
		selectionList.clear();
	}

	/**
	 * Dump and refresh all of our model/chain/residue info
	 */
	// TODO: Move code to ChimeraManager?
	public void refresh() {
		// Get a new model list
		// HashMap<Integer, ChimeraModel> newHash = new HashMap<Integer,
		// ChimeraModel>();

		// Stop all of our listeners while we try to handle this
		chimeraManager.stopListening();

		// Get all of the open models
		List<ChimeraModel> newModelList = chimeraManager.getModelList();

		// Match them up -- assume that the model #'s haven't changed
		for (ChimeraModel model : newModelList) {
			// Get the color (for our navigator)
			model.setModelColor(chimeraManager.getModelColor(model));

			// Get our model info
			int modelNumber = model.getModelNumber();
			int subModelNumber = model.getSubModelNumber();

			// If we already know about this model number, get the Structure,
			// which tells us about the associated CyNode
			if (chimeraManager.hasChimeraModel(modelNumber, subModelNumber)) {
				ChimeraModel oldModel = chimeraManager.getChimeraModel(modelNumber, subModelNumber);
				if (oldModel.getModelType() == ModelType.SMILES) {
					model.setModelName(oldModel.getModelName());
				}
				// remove old model from ChimeraManager
				chimeraManager.removeChimeraModel(oldModel.getModelNumber(), oldModel.getSubModelNumber());
				// TODO: Associate new model with the correct structure
				// Maybe we need to keep a reference to the structure in the model?
				// model.setStructure(s);
			} else {
				// TODO: Associate new model with the correct structure
				// This will return a new Structure if we don't know about it
				// Structure s = CyChimera.findStructureForModel(networkView,
				// model.getModelName(), true);
				// s.setModelNumber(model.getModelNumber(), model.getSubModelNumber());
				// model.setStructure(s);
			}
			// add new model to ChimeraManager
			chimeraManager.addChimeraModel(modelNumber, subModelNumber, model);

			// TODO: Why check the Structure? 
			// model.getStructure() != null
			if (model.getModelType() != ModelType.SMILES) {
				// Get the residue information
				chimeraManager.getResidueInfo(model);
			}
		}

		// Restart all of our listeners
		chimeraManager.startListening();

		// Done
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
		if (mnDialog != null)
			mnDialog.updateSelection(selectionList);
	}

	public void launchDialog() {
		if (mnDialog == null) {
			mnDialog = ModelNavigatorDialog.LaunchModelNavigator(null, this);
		}
		mnDialog.setVisible(true);
	}

	public ModelNavigatorDialog getModelNavigatorDialog() {
		return mnDialog;
	}

	private boolean hasChimObjNames(Collection<CyIdentifiable> objs, CyTable table,
			List<String> columns) {
		if (getChimObjNames(objs, table, columns).size() > 0) {
			return true;
		}
		return false;
	}

	private Map<CyIdentifiable, List<String>> getChimObjNames(Collection<CyIdentifiable> cyObjs,
			CyTable table, List<String> columns) {
		Map<CyIdentifiable, List<String>> mapChimObjNames = new HashMap<CyIdentifiable, List<String>>();
		// if something is null, just return an empty map
		if (columns == null || columns.size() == 0 || cyObjs == null)
			return mapChimObjNames;
		// iterate over cytoscape objects
		for (CyIdentifiable cyObj : cyObjs) {
			// skip if node/edge does not exist anymore
			if (!table.rowExists(cyObj.getSUID())) {
				continue;
			}
			CyRow row = table.getRow(cyObj.getSUID());
			// iterate over attributes that contain structures
			for (String column : columns) {
				// TODO: Consider attributes that contain lists?
				// TODO: Split by comma if more than one attribute?
				String cell = row.get(column, String.class, "").trim();
				// skip if the cell is empty
				if (cell.equals("")) {
					continue;
				}
				// skip if the structure is already open
				if (currentStructuresMap.containsKey(cyObj)
						&& currentStructuresMap.get(cyObj).hasChimeraModelName(cell)) {
					continue;
				}
				// add strcture name to map
				if (!mapChimObjNames.containsKey(cyObj)) {
					mapChimObjNames.put(cyObj, new ArrayList<String>());
				}
				mapChimObjNames.get(cyObj).add(cell);
			}
		}
		return mapChimObjNames;
	}

}
