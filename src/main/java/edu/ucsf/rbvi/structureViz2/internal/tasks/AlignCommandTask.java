package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import edu.ucsf.rbvi.structureViz2.internal.model.AlignManager;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimUtils;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraChain;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraManager;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraModel;
import edu.ucsf.rbvi.structureViz2.internal.model.ChimeraStructuralObject;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class AlignCommandTask extends AbstractTask implements ObservableTask {

	private StructureManager structureManager;
	private ChimeraManager chimeraManager;
	private CyNetwork net;
	private Map<String, float[]> results;

	@Tunable(description = "Network for the selected nodes/edges", context = "nogui")
	public CyNetwork network;

	@Tunable(description = "Reference model or single chain", context = "nogui")
	public String reference = "";

	@Tunable(description = "List of models to align to the reference")
	public ListMultipleSelection<String> modelList = new ListMultipleSelection<String>("");

	@Tunable(description = "List of chains to align to the reference")
	public ListMultipleSelection<String> chainList = new ListMultipleSelection<String>("");

	@Tunable(description = "Show the sequence alignment for each aligned pair", context = "nogui")
	public boolean showSequences = false;

	@Tunable(description = "Create new edges that represent the similarity of each structure with the reference", context = "nogui")
	public boolean createEdges = false;

	@Tunable(description = "Add the RMSD, Alignment Score, and number of aligned pairs as attributes", context = "nogui")
	public boolean assignAttributes = true;

	public AlignCommandTask(StructureManager structureManager) {
		this.structureManager = structureManager;
		this.chimeraManager = structureManager.getChimeraManager();
		results = new HashMap<String, float[]>();
		if (chimeraManager.isChimeraLaunched() && chimeraManager.getChimeraModelsCount(false) > 0) {
			List<String> models = new ArrayList<String>();
			List<String> chains = new ArrayList<String>();
			for (ChimeraModel model : chimeraManager.getChimeraModels()) {
				models.add(model.getModelName());
				for (ChimeraChain chain : model.getChains()) {
					chains.add(chain.getChimeraModel().getModelName() + "#." + chain.getChainId());
				}
			}
			modelList = new ListMultipleSelection<String>(models);
			// modelList.setSelectedValues(models);
			chainList = new ListMultipleSelection<String>(chains);
			// chainList.setSelectedValues(chains);
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Aligning models...");
		if (!chimeraManager.isChimeraLaunched() || chimeraManager.getChimeraModelsCount(false) == 0) {
			taskMonitor.setStatusMessage("No structures open, aborting...");
			return;
		}
		net = network;
		if (net == null) {
			CyNetwork current = ((CyApplicationManager) structureManager
					.getService(CyApplicationManager.class)).getCurrentNetwork();
			if (current != null) {
				net = current;
			} else {
				assignAttributes = false;
				createEdges = false;
				taskMonitor.showMessage(Level.INFO,
						"No network found, setting assignAttributes and createEdges to false.");
			}
		}
		if (modelList.getSelectedValues().size() > 0 && chainList.getSelectedValues().size() > 0) {
			taskMonitor.showMessage(Level.ERROR, "Cannot align both models and chains.");
			return;
		}
		AlignManager alignment = new AlignManager(structureManager);
		alignment.setShowSequence(showSequences);
		alignment.setCreateNewEdges(createEdges);
		alignment.setAssignResults(assignAttributes);
		ChimeraStructuralObject ref = null;
		List<ChimeraStructuralObject> alignModels = new ArrayList<ChimeraStructuralObject>();
		if (reference != null && reference.length() > 0) {
			if (modelList.getSelectedValues().size() > 0) {
				ref = ChimUtils.fromAttribute(reference, chimeraManager);
				if (ref != null && ref instanceof ChimeraModel) {
					for (String val : modelList.getSelectedValues()) {
						ChimeraStructuralObject model = ChimUtils
								.fromAttribute(val, chimeraManager);
						if (model != null && model instanceof ChimeraModel && !model.equals(ref)) {
							alignModels.add(model);
						}
					}
				} else {
					taskMonitor.showMessage(Level.ERROR, "Reference expected to be a model.");
				}
			}
			if (chainList.getSelectedValues().size() > 0) {
				ref = ChimUtils.fromAttribute(reference, chimeraManager);
				if (ref != null && ref instanceof ChimeraChain) {
					for (String val : chainList.getSelectedValues()) {
						ChimeraStructuralObject chain = ChimUtils
								.fromAttribute(val, chimeraManager);
						if (chain != null && chain instanceof ChimeraChain && !chain.equals(ref)) {
							alignModels.add(chain);
						}
					}
				} else {
					taskMonitor.showMessage(Level.ERROR, "Reference expected to be a chain.");
				}
			}
		}
		if (ref == null || alignModels.size() == 0) {
			taskMonitor
					.setStatusMessage("Either the reference or the models/chains could not be found, aborting...");
			return;
		}
		alignment.align(ref, alignModels);
		for (ChimeraStructuralObject model : alignModels) {
			results.put(ChimUtils.getAlignName(model),
					alignment.getResults(ChimUtils.getAlignName(model)));
		}
	}

	public Object getResults(Class type) {
		if (type == String.class) {
			StringBuilder sb = new StringBuilder();
			for (String entry : results.keySet()) {
				sb.append(entry);
				float[] value = results.get(entry);
				for (float v : value) {
					sb.append("\t" + String.valueOf(v));
				}
				sb.append("\n");
			}
			return sb.toString();
		}
		return results;
	}

}
