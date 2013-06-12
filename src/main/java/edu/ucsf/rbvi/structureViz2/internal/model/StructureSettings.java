package edu.ucsf.rbvi.structureViz2.internal.model;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

/**
 * This object maintains the relationship between Chimera objects and Cytoscape objects.
 */
public class StructureSettings {
	// Add Tunables to get the column names to search
	@Tunable(description = "Columns to search for PDB identifiers", groups = "PDB Identifiers")
	public ListMultipleSelection<String> structureColumns = null;

	@Tunable(description = "Columns to search for Compound (small molecule) identifiers", groups = "Compound Identifiers")
	public ListMultipleSelection<String> chemColumns = null;

	@Tunable(description = "Columns to search for key or functional residue identifiers", groups = "Functional residues")
	public ListMultipleSelection<String> residueColumns = null;

	@Tunable(description = "Path to chimera executible")
	public String chimeraPath = null;

	public StructureSettings(CyNetwork network, StructureManager manager) {
		structureColumns = new ListMultipleSelection<String>(manager.getAllStructureKeys());
		structureColumns.setSelectedValues(manager.getCurrentStructureKeys(network));

		chemColumns = new ListMultipleSelection<String>(manager.getAllChemStructKeys());
		chemColumns.setSelectedValues(manager.getCurrentChemStructKeys(network));

		residueColumns = new ListMultipleSelection<String>(manager.getAllResidueKeys());
		residueColumns.setSelectedValues(manager.getCurrentResidueKeys(network));

		chimeraPath = manager.getCurrentChimeraPath(network);

		// This seems a little strange, but it has to do with the order of tunable interceptor
		// handling. We need to set these selectors in our structure manager and dynamically
		// pull the data out as needed....
		manager.setStructureSettings(network, this);
	}

	public ListMultipleSelection<String> getStructureColumns() {
		return structureColumns;
	}

	public ListMultipleSelection<String> getChemStructureColumns() {
		return chemColumns;
	}

	public ListMultipleSelection<String> getResidueColumns() {
		return residueColumns;
	}

	public String getChimeraPath() {
		return chimeraPath;
	}
}
