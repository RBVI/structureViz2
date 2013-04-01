package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;

public class CySelectionListener implements RowsSetListener {

	StructureManager structureManager;

	public CySelectionListener(StructureManager manager) {
		this.structureManager = manager;
	}

	public void handleEvent(RowsSetEvent e) {
		Map<Long, Boolean> selectedRows = new HashMap<Long, Boolean>();
		if (e.containsColumn(CyNetwork.SELECTED)) {
			Collection<RowSetRecord> records = e.getColumnRecords(CyNetwork.SELECTED);
			for (RowSetRecord record : records) {
				CyRow row = record.getRow();
				// This is a hack to avoid double selection...
				if (row.toString().indexOf("FACADE") >= 0)
					continue;
				selectedRows.put(row.get(CyIdentifiable.SUID, Long.class), (Boolean)record.getValue());
			}
		}
		if (selectedRows.size() != 0) {
			structureManager.cytoscapeSelectionChanged(selectedRows);
		}
	}
}
