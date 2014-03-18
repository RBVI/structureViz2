package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.structureViz2.internal.model.CytoUtils;
import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class SendComandTask extends AbstractTask implements ObservableTask {

	private StructureManager structureManager;

	@Tunable(description = "Command")
	public String command;

	public List<String> result;

	public SendComandTask(StructureManager structureManager) {
		this.structureManager = structureManager;
		this.command = "";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Sending Command to Chimera");
		result = structureManager.getChimeraManager().sendChimeraCommand(command, true);
		if (result != null) {
			structureManager.addChimReply(command, result);
		}
	}

	public Object getResults(Class expectedClass) {
		if (expectedClass.equals(String.class) && result != null) {
			result.add(0, command);
			return CytoUtils.join(result, "\n");
		}
		return result;
	}

	@ProvidesTitle
	public String getTitle() {
		return "Send command to Chimera";
	}
}
