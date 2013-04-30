package edu.ucsf.rbvi.structureViz2.internal.tasks;

import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class SendComandTask extends AbstractTask {

	private StructureManager structureManager;

	@Tunable(description = "Command")
	public String command;

	public SendComandTask(StructureManager structureManager) {
		this.structureManager = structureManager;
		this.command = "";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		List<String> reply = structureManager.getChimeraManager().sendChimeraCommand(command, true);
		if (reply != null) {
			structureManager.addChimReply(command, reply);
		}
	}
}
