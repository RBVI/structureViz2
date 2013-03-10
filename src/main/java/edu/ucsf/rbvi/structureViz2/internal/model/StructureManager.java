package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

/**
 * This object maintains the relationship between Chimera
 * objects and Cytoscape objects.
 */
public class StructureManager {
	public static final String[] defaultStructureKeys = {"Structure","pdb","pdbFileName","PDB ID",
			                                                "structure","biopax.xref.PDB","pdb_ids",null};
	public static final String[] defaultChemStructKeys = {"Smiles","smiles","SMILES",null};
	public static final String[] defaultResidueKeys = {"FunctionalResidues","ResidueList",null};

	// Add Tunables to get the column names to search
	@Tunable(description="Columns to search for PDB identifiers")
	public ListMultipleSelection<String> structureColumns = null;

	@Tunable(description="Columns to search for Compound (small molecule) identifiers")
	public ListMultipleSelection<String> chemColumns = null;

	@Tunable(description="Columns to search for key or functional residue identifiers")
	public ListMultipleSelection<String> residueColumns = null;

	public StructureManager() {
		// Initialize our tunables
		structureColumns = new ListMultipleSelection<String>(Arrays.asList(defaultStructureKeys));
		structureColumns.setSelectedValues(Arrays.asList(defaultStructureKeys));

		chemColumns = new ListMultipleSelection<String>(Arrays.asList(defaultChemStructKeys));
		chemColumns.setSelectedValues(Arrays.asList(defaultChemStructKeys));

		residueColumns = new ListMultipleSelection<String>(Arrays.asList(defaultResidueKeys));
		residueColumns.setSelectedValues(Arrays.asList(defaultChemStructKeys));
	}

	public boolean hasNodeStructures(CyNetwork network, Collection<CyNode> nodeSet) {
		if (network == null) return false;
		CyTable nodeTable = network.getDefaultNodeTable();
		List<String> attrsFound = getMatchingAttributes(nodeTable, getStructureAttributes());
		Collection idSet = nodeSet;
		return hasStructures(idSet, nodeTable, attrsFound);
	}

	public boolean hasEdgeStructures(CyNetwork network, Collection<CyEdge> edgeSet) {
		if (network == null) return false;
		CyTable edgeTable = network.getDefaultEdgeTable();
		List<String> attrsFound = getMatchingAttributes(edgeTable, getStructureAttributes());
		Collection idSet = edgeSet;
		return hasStructures(idSet, edgeTable, attrsFound);
	}

	private List<String> getStructureAttributes() {
		return structureColumns.getSelectedValues();
	}

	private boolean hasStructures(Collection<CyIdentifiable> objs, CyTable table, List<String> columns) {
		if (columns == null || columns.size() == 0) return false;
		if (objs == null) return true;

		for (CyIdentifiable obj: objs) {
			if (table.rowExists(obj.getSUID())) {
				CyRow row = table.getRow(obj.getSUID());
				for (String column: columns) {
					if (row.getRaw(column) != null) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private List<String> getMatchingAttributes(CyTable table, List<String>columns) {
		Set<String> columnNames = CyTableUtil.getColumnNames(table);

		List<String> columnsFound = new ArrayList<String>();
		for (String attribute: columns) {
			if (columnNames.contains(attribute)) 
				columnsFound.add(attribute);
		}

		return columnsFound;
	}


}
