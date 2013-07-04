/* vim: set ts=2: */
/**
 * Copyright (c) 2006 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package edu.ucsf.rbvi.structureViz2.internal.model;

// System imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import edu.ucsf.rbvi.structureViz2.internal.ui.AlignStructuresDialog;
// Cytoscape imports
// StructureViz imports

/**
 * The AlignmentTableModel class provides the table model used by the AlignStructuresDialog
 * 
 * @author scooter
 * @see AlignStructuresDialog
 */
public class AlignmentTableModel extends AbstractTableModel implements ListSelectionListener {

	public static final int NOREFERENCE = 0;
	public static final int REFERENCE = 1;
	public static final int MATCHLIST = 2;
	public static final int RESULTS = 3;
	private static final String[] columnNames = { "Match Structures", "Aligned Pairs", "RMSD",
			"Score" };

	private String referenceModel = null;
	private List<String> allModels = null;
	private List<ChimeraStructuralObject> matchModels = null;
	private List<ChimeraStructuralObject> selectedModels = null;
	private HashMap<String, float[]> resultsMap;
	private int state = NOREFERENCE;
	AlignStructuresDialog asDialog = null;

	/**
	 * Create the table model
	 * 
	 * @param chimeraObject
	 *            the Chimera object that links to Chimera
	 * @param models
	 *            the list of Structures involved
	 * @param asDialog
	 *            a back-pointer to the dialog itself
	 */
	public AlignmentTableModel(List<String> models, AlignStructuresDialog asDialog) {
		this.allModels = models;
		this.asDialog = asDialog;
	}

	/**
	 * Return the number of rows in the table
	 * 
	 * @return number of rows as an integer
	 */
	public int getRowCount() {
		if (referenceModel == null)
			return 0;
		return matchModels.size();
	}

	/**
	 * Return the number of columns in the table
	 * 
	 * @return 4
	 */
	public int getColumnCount() {
		return 4;
	}

	/**
	 * Return the value at the requested row and column. In our case the row provides information
	 * about our Structure and the column indicates the specific data we want.
	 * 
	 * @param row
	 *            the row number
	 * @param col
	 *            the column number
	 * @return an Object which represents the value at the requested row and column
	 */
	public Object getValueAt(int row, int col) {
		if (referenceModel == null)
			return null;

		ChimeraStructuralObject match = matchModels.get(row);
		String matchValue = ChimUtils.getAlignName(match);
		if (col == 0) {
			return matchValue;
		} else {
			if (resultsMap.containsKey(matchValue)) {
				float[] results = (float[]) resultsMap.get(matchValue);
				if (col == 1) {
					return new Integer((int) results[AlignManager.PAIRS]);
				} else if (col == 2) {
					return new Double(results[AlignManager.RMSD]);
				} else if (col == 3) {
					return new Double(results[AlignManager.SCORE]);
				}
			}
		}
		return null;
	}

	/**
	 * This method indicates whether this cell is editable. We always return false.
	 * 
	 * @param row
	 *            row number as an integer
	 * @param col
	 *            column number as an integer
	 * @return false
	 */
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	/**
	 * Return the name of a column.
	 * 
	 * @param col
	 *            column number as an integer
	 * @return column name as a String
	 */
	public String getColumnName(int col) {
		return columnNames[col];
	}

	/**
	 * Get the object class of a column. This is used to determine how the columns will be displayed
	 * 
	 * @param c
	 *            the column number as an integer
	 * @return object Class of this column
	 */
	public Class<?> getColumnClass(int c) {
		if (c == 0)
			return String.class;
		if (c == 1)
			return Integer.class;
		return Double.class;
	}

	/**
	 * Set the reference structure to use for the alignments
	 * 
	 * @param refModel
	 *            the name of the structure
	 */
	public void setReferenceModel(ChimeraStructuralObject refModel) {
		if (refModel == null) {
			this.matchModels = null;
			this.resultsMap = null;
			this.referenceModel = null;
		} else {
			String refName = ChimUtils.getAlignName(refModel);
			String refModelName = ChimUtils.getAlignName(refModel.getChimeraModel());

			this.referenceModel = refName;
			this.matchModels = new ArrayList<ChimeraStructuralObject>();
			this.resultsMap = new HashMap<String, float[]>();
			for (String chimObjectName : allModels) {
				if (refName.equals(chimObjectName)
						|| refModelName.equals(ChimUtils.getAlignName(asDialog.getChimObj(
								chimObjectName).getChimeraModel()))) {
					continue;
				}
				matchModels.add(asDialog.getChimObj(chimObjectName));
			}
		}
		// Update the table
		fireTableDataChanged();
	}

	/**
	 * Force the table to update
	 */
	public void updateTable() {
		fireTableDataChanged();
	}

	/**
	 * Sets the results from an alignment in the table.
	 * 
	 * @param matchStruct
	 *            the name of the structure that was aligned
	 * @param results
	 *            the 3 result values from the alignment
	 */
	public void setResults(String matchStruct, float[] results) {
		resultsMap.put(matchStruct, results);
	}

	/**
	 * Get the Structures that have been selected by the user
	 * 
	 * @return a List of selected structures
	 */
	public List<ChimeraStructuralObject> getSelectedModels() {
		return selectedModels;
	}

	/**
	 * This method is called whenever a value in the table is changed. It is used to detect
	 * selection and add the selection to the list of structures to be used for the alignment
	 * 
	 * @param e
	 *            a ListSelectionEvent
	 */
	public void valueChanged(ListSelectionEvent e) {
		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		// Get the list of selected structures
		if (lsm.isSelectionEmpty()) {
			// Tell the dialog
			selectedModels = null;
			asDialog.setAlignEnabled(false);
		} else {
			selectedModels = new ArrayList<ChimeraStructuralObject>();
			for (int i = 0; i < matchModels.size(); i++) {
				if (lsm.isSelectedIndex(i)) {
					selectedModels.add(matchModels.get(i));
				}
			}
			asDialog.setAlignEnabled(true);
		}
	}
}
