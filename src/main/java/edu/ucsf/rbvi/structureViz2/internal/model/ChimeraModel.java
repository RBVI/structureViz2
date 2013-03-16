package edu.ucsf.rbvi.structureViz2.internal.model;

import java.util.List;

public class ChimeraModel implements ChimeraStructuralObject {

	private Integer number;
	private String name;

	public ChimeraModel(Integer number, String name) {
		this.number = number;
		this.name = name;
	}

	public Integer getNumber() {
		return number;
	}

	public String getSpecNumber() {
		return ("#" + Integer.toString(number));
	}

	public String toSpec() {
		return null;
	}

	public Object getUserData() {
		return null;
	}

	public void setUserData(Object userData) {

	}

	public ChimeraModel getChimeraModel() {
		return this;
	}

	public String getName() {
		return name;
	}
	
	
	public void setSelected(boolean selected) {

	}

	public boolean isSelected() {
		return false;
	}

	public List<ChimeraStructuralObject> getChildren() {
		return null;
	}

}
