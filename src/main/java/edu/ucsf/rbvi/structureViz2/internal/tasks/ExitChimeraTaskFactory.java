package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class ExitChimeraTaskFactory extends AbstractTaskFactory implements TaskFactory {

	private StructureManager structureManager;

	public ExitChimeraTaskFactory(StructureManager structureManager) {
		this.structureManager = structureManager;
	}

	public boolean isReady() {
		// System.out.println("isReady exit chimera");
		if (structureManager.getChimeraManager().isChimeraLaunched()) {
			return true;
		}
		return false;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ExitChimeraTask(structureManager));
	}

}
