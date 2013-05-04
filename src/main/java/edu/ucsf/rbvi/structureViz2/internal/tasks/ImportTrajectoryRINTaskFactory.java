package edu.ucsf.rbvi.structureViz2.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager;

public class ImportTrajectoryRINTaskFactory extends AbstractTaskFactory implements TaskFactory {

	private StructureManager structureManager;
	private String fileNames;
	
	public ImportTrajectoryRINTaskFactory(StructureManager structureManager, String fileNames) {
		this.structureManager = structureManager;
		this.fileNames = fileNames;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ImportTrajectoryRINTask(structureManager, fileNames));
	}

}
