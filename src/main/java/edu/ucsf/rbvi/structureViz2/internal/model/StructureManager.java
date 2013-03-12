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
 * This object maintains the relationship between Chimera objects and Cytoscape
 * objects.
 */
public class StructureManager {
	public static final String[] defaultStructureKeys = { "Structure", "pdb", "pdbFileName",
			"PDB ID", "structure", "biopax.xref.PDB", "pdb_ids" };
	public static final String[] defaultChemStructKeys = { "Smiles", "smiles", "SMILES" };
	public static final String[] defaultResidueKeys = { "FunctionalResidues", "ResidueList" };

	// Add Tunables to get the column names to search
	@Tunable(description = "Columns to search for PDB identifiers")
	public ListMultipleSelection<String> structureColumns = null;

	@Tunable(description = "Columns to search for Compound (small molecule) identifiers")
	public ListMultipleSelection<String> chemColumns = null;

	@Tunable(description = "Columns to search for key or functional residue identifiers")
	public ListMultipleSelection<String> residueColumns = null;

	public StructureManager() {
		// TODO: strange behavior: the dialog only opens once!
		// Initialize our tunables
		structureColumns = new ListMultipleSelection<String>(
				Arrays.asList(defaultStructureKeys));
		structureColumns.setSelectedValues(Arrays.asList(defaultStructureKeys));

		chemColumns = new ListMultipleSelection<String>(
				Arrays.asList(defaultChemStructKeys));
		chemColumns.setSelectedValues(Arrays.asList(defaultChemStructKeys));

		residueColumns = new ListMultipleSelection<String>(
				Arrays.asList(defaultResidueKeys));
		residueColumns.setSelectedValues(Arrays.asList(defaultResidueKeys));
	}

	// I added these methods hoping that this may undo the strange settings dialog behavior,
	// but it didn't
	public ListMultipleSelection<String> getStructureColumns() {
		return this.structureColumns;
	}

	public ListMultipleSelection<String> getChemColumns() {
		return this.chemColumns;
	}

	public ListMultipleSelection<String> getResidueColumns() {
		return this.residueColumns;
	}
	
	public void setStructureColumns(ListMultipleSelection<String> structureColumns) {
		this.structureColumns = structureColumns;
	}
	
	public void setChemColumns(ListMultipleSelection<String> chemColumns) {
		this.chemColumns = chemColumns;
	}
	
	public void setResidueColumns(ListMultipleSelection<String> residueColumns) {
		this.residueColumns = residueColumns;
	}

	
	
	public boolean hasNodeStructures(CyNetwork network, Collection<CyNode> nodeSet) {
		if (network == null)
			return false;
		CyTable nodeTable = network.getDefaultNodeTable();
		List<String> attrsFound = getMatchingAttributes(nodeTable, getStructureAttributes());
		Collection idSet = nodeSet;
		return hasStructures(idSet, nodeTable, attrsFound);
	}

	public List<String> getNodeStructures(CyNetwork network, Collection<CyNode> nodeSet) {
		if (network == null)
			return new ArrayList<String>();
		CyTable nodeTable = network.getDefaultNodeTable();
		List<String> attrsFound = getMatchingAttributes(nodeTable, getStructureAttributes());
		Collection idSet = nodeSet;
		return getStructures(idSet, nodeTable, attrsFound);
	}

	public boolean hasEdgeStructures(CyNetwork network, Collection<CyEdge> edgeSet) {
		if (network == null)
			return false;
		CyTable edgeTable = network.getDefaultEdgeTable();
		List<String> attrsFound = getMatchingAttributes(edgeTable, getStructureAttributes());
		Collection idSet = edgeSet;
		return hasStructures(idSet, edgeTable, attrsFound);
	}

	private List<String> getStructureAttributes() {
		return structureColumns.getSelectedValues();
	}

	private boolean hasStructures(Collection<CyIdentifiable> objs, CyTable table,
			List<String> columns) {
		if (columns == null || columns.size() == 0)
			return false;
		if (objs == null)
			return true;

		for (CyIdentifiable obj : objs) {
			if (table.rowExists(obj.getSUID())) {
				CyRow row = table.getRow(obj.getSUID());
				for (String column : columns) {
					if (row.getRaw(column) != null) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// TODO: duplicated code with hasStructures, do we need both?
	private List<String> getStructures(Collection<CyIdentifiable> objs, CyTable table,
			List<String> columns) {
		List<String> structures = new ArrayList<String>();
		if (columns == null || columns.size() == 0 || objs == null)
			return structures;

		for (CyIdentifiable obj : objs) {
			if (table.rowExists(obj.getSUID())) {
				CyRow row = table.getRow(obj.getSUID());
				for (String column : columns) {
					// TODO: consider attributes that contain lists?
					if (row.getRaw(column) != null) {
						structures.add(row.get(column, String.class));
					}
				}
			}
		}
		return structures;
	}

	private List<String> getMatchingAttributes(CyTable table, List<String> columns) {
		Set<String> columnNames = CyTableUtil.getColumnNames(table);

		List<String> columnsFound = new ArrayList<String>();
		for (String attribute : columns) {
			if (columnNames.contains(attribute))
				columnsFound.add(attribute);
		}

		return columnsFound;
	}

}
