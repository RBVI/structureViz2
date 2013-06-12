package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;

public class CySelectionListener implements RowsSetListener {

	private StructureManager structureManager;

	public CySelectionListener(StructureManager manager) {
		this.structureManager = manager;
	}

	public void handleEvent(RowsSetEvent e) {
		if (!structureManager.getChimeraManager().isChimeraLaunched()) {
			return;
		}
		if (e.getSource().toString().indexOf("network") >= 0) {
			return;
		}
		if (e.containsColumn(CyNetwork.SELECTED)) {
			Map<Long, Boolean> selectedRows = new HashMap<Long, Boolean>();
			Collection<RowSetRecord> records = e.getColumnRecords(CyNetwork.SELECTED);
			for (RowSetRecord record : records) {
				CyRow row = record.getRow();
				// This is a hack to avoid double selection...
				if (row.toString().indexOf("FACADE") >= 0)
					continue;
				selectedRows.put(row.get(CyIdentifiable.SUID, Long.class),
						(Boolean) record.getValue());
			}
			if (selectedRows.size() != 0) {
				structureManager.cytoscapeSelectionChanged(selectedRows);
			}
		} else {
			boolean update = false;
			// TODO: Is it fine if we check all attributes?
			List<String> defaultstructurekeys = structureManager.getAllStructureKeys();
			for (int i = 0; i < defaultstructurekeys.size(); i++) {
				String structureKey = defaultstructurekeys.get(i);
				if (e.containsColumn(structureKey)) {
					Collection<RowSetRecord> records = e.getColumnRecords(structureKey);
					for (RowSetRecord record : records) {
						CyRow row = record.getRow();
						// This is a hack to avoid double selection...
						if (row.toString().indexOf("FACADE") >= 0 || !row.isSet(structureKey)) {
							continue;
						} else {
							update = true;
							break;
						}
					}
				}
			}
			if (update) {
				structureManager.associate(null);
			}
		}
	}
}
