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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

/**
 * The Align class provides the interface to Chimera for processing requests to align structures.
 */
public class AlignManager {
	public static final String[] attributeKeys = { "RMSD", "AlignmentScore", "AlignedResidues" };

	/**
	 * Array offset to the RMSD result
	 */
	public static final int RMSD = 0;

	/**
	 * Array offset to the Alignment Score result
	 */
	public static final int SCORE = 1;

	/**
	 * Array offset to the number of aligned pairs result
	 */
	public static final int PAIRS = 2;

	/**
	 * The name of the Cytoscape interaction type to use if we're asked to create an edge with the
	 * results
	 */
	public static final String structureInteraction = "structuralSimilarity";
	private StructureManager structureManager = null;
	private ChimeraManager chimeraManager = null;
	private HashMap results = null;
	private boolean assignResults = false;
	private boolean showSequence = false;
	private boolean createNewEdges = false;

	/**
	 * Create a new Align object
	 * 
	 * @param chimeraManager
	 *          the Chimera interface object that provides our link to Chimera
	 */
	public AlignManager(StructureManager structureManager) {
		this.structureManager = structureManager;
		chimeraManager = structureManager.getChimeraManager();
	}

	/**
	 * Set the flag that tells us whether to assign the results to existing edges or not.
	 * 
	 * @param val
	 *          the flag to set
	 */
	public void setAssignResults(boolean val) {
		this.assignResults = val;
	};

	/**
	 * Set the flag that tells us whether to create a new edge based on the results or not.
	 * 
	 * @param val
	 *          the flag to set
	 */
	public void setCreateNewEdges(boolean val) {
		this.createNewEdges = val;
	};

	/**
	 * Set the flag that tells us whether to show the sequence results when an alignment is performed.
	 * 
	 * @param val
	 *          the flag to set
	 */
	public void setShowSequence(boolean val) {
		this.showSequence = val;
	};

	/**
	 * Get the results
	 * 
	 * @param modelName
	 *          the name of the model to return the results for
	 * @return the array of 3 float results
	 */
	public float[] getResults(String modelName) {
		if (results.containsKey(modelName))
			return (float[]) results.get(modelName);
		return null;
	}

	/**
	 * This method calls Chimera to perform a pairwise alignment between the <i>reference</i> model
	 * and all other currently open Chimera models.
	 * 
	 * @param reference
	 *          the reference model
	 */
	public void alignAll(ChimeraModel reference) {
		Collection<ChimeraModel> modelList = chimeraManager.getChimeraModels();
		ArrayList<ChimeraStructuralObject> matchList = new ArrayList<ChimeraStructuralObject>();
		for (ChimeraModel match : modelList) {
			if (match != reference)
				matchList.add((ChimeraStructuralObject) match);
		}
		align((ChimeraStructuralObject) reference, matchList);
		chimeraManager.focus();
		if (assignResults)
			setAllAttributes(reference, matchList);
	}

	/**
	 * This method calls Chimera to perform a pairwise alignment between the <i>reference</i> model
	 * and a List of models.
	 * 
	 * @param reference
	 *          the reference model
	 * @param models
	 *          a List of ChimeraModels to align to the reference
	 */
	public void align(ChimeraStructuralObject reference, List<ChimeraStructuralObject> models) {
		results = new HashMap<String, float[]>();

		for (ChimeraStructuralObject match : models) {
			List<String> matchResult = singleAlign(reference, match);
			if (matchResult != null) {
				results.put(match.toString(), parseResults(matchResult));
			}
		}
		chimeraManager.focus();
		if (assignResults)
			setAllAttributes(reference, models);
	}

	/**
	 * Ask Chimera to align a single ChimeraModel to a reference ChimeraModel
	 * 
	 * @param reference
	 *          the ChimeraModel to use as a reference model
	 * @param match
	 *          the ChimeraModel to align to the reference
	 * @return an Iterator over the results
	 */
	private List<String> singleAlign(ChimeraStructuralObject reference, ChimeraStructuralObject match) {
		String command = "matchmaker " + reference.toSpec() + " " + match.toSpec();
		if (reference instanceof ChimeraChain || match instanceof ChimeraChain)
			command = command + " pair ss";
		if (showSequence) {
			command = command + " show true";
		}
		return chimeraManager.sendChimeraCommand(command, true);
	}

	/**
	 * Parse the results returned by <b>singleAlign</b> and return an array of 3 floats with the
	 * results of an alignment.
	 * 
	 * @param lineIter
	 *          the iterator over the lines of responses from Chimera
	 * @return the array of floats containing the results from a single alignment
	 */
	private float[] parseResults(List<String> resultsList) {
		float[] results = new float[3];
		int index = -1;
		for (String line : resultsList) {
			// System.out.println(line);
			if ((index = line.indexOf("score = ")) > 0) {
				Float score = new Float(line.substring(index + 8));
				results[SCORE] = score.floatValue();
			} else if ((index = line.indexOf("RMSD between")) == 0) {
				String[] tokens = line.split(" ");
				Float pairs = new Float(tokens[2]);
				results[PAIRS] = pairs.floatValue();
				Float rmsd = new Float(tokens[6]);
				results[RMSD] = rmsd.floatValue();
			}
		}
		// System.out.println("RMSD = "+results[RMSD]+", score = "+results[SCORE]);
		return results;
	}

	/**
	 * This method is used to set all of the Cytoscape edge attributes resulting from a series of
	 * alignments.
	 * 
	 * @param source
	 *          the ChimeraModel representing the source of the edge (the reference structure)
	 * @param targetList
	 *          the list of targets (aligned structures)
	 */
	private void setAllAttributes(ChimeraStructuralObject source,
			List<ChimeraStructuralObject> targetList) {
		ChimeraModel sourceModel = source.getChimeraModel();
		for (ChimeraStructuralObject target : targetList) {
			// TODO: Check usage of model/chain names in the alignment dialog?
			// If our target is a ChimeraModel, we want the model name, otherwise
			// we want the toString
			// String modelKey = null;
			// if (target instanceof ChimeraModel)
			// modelKey = ((ChimeraModel) target).getModelName();
			// else
			String modelKey = target.toString();
			ChimeraModel targetModel = target.getChimeraModel();
			float[] results = getResults(modelKey);
			setEdgeAttributes(results, sourceModel, targetModel);
		}
	}

	/**
	 * 
	 * @param results
	 *          the results values to assign to the edge as attributes
	 * @param from
	 *          the ChimeraModel that represents the CyNode to use as the source of the edge
	 * @param to
	 *          the ChimeraModel that represents the CyNode to use as the destination of the edge
	 */
	private void setEdgeAttributes(float[] results, ChimeraModel reference, ChimeraModel match) {
		// System.out.println("From: "+from+" To: "+to+" results: "+results);
		Map<CyIdentifiable, CyNetwork> refNodes = reference.getCyObjects();
		Map<CyIdentifiable, CyNetwork> matchNodes = match.getCyObjects();
		ArrayList<CyNode> nodeList1 = new ArrayList<CyNode>();
		ArrayList<CyNode> nodeList2 = new ArrayList<CyNode>();
		Set<CyNetwork> networks = new HashSet<CyNetwork>();
		for (CyIdentifiable cyObj : refNodes.keySet()) {
			if (cyObj instanceof CyNode) {
				nodeList1.add((CyNode) cyObj);
				networks.add(refNodes.get(cyObj));
			}
		}
		for (CyIdentifiable cyObj : matchNodes.keySet()) {
			if (cyObj instanceof CyNode) {
				nodeList2.add((CyNode) cyObj);
				networks.add(matchNodes.get(cyObj));
			}
		}
		// We assume that all nodes are in the same network
		if (nodeList1.size() == 0 || nodeList2.size() == 0 || networks.size() > 1) {
			return;
		}
		CyNetwork network = networks.iterator().next();
		// create attributes if needed
		CyTable edgeTable = network.getDefaultEdgeTable();
		for (String attrKey : attributeKeys) {
			if (edgeTable.getColumn(attrKey) == null) {
				edgeTable.createColumn(attrKey, Double.class, false);
			}
		}
		// get edges between matched nodes and assign results to the attributes
		// if set by the user create a new edge
		List<CyEdge> edgeList = new ArrayList<CyEdge>();
		for (CyNode node1 : nodeList1) {
			for (CyNode node2 : nodeList2) {
				edgeList = network.getConnectingEdgeList(node1, node2, Type.ANY);
				if (edgeList.size() == 0 && createNewEdges) {
					edgeList = new ArrayList<CyEdge>();
					CyEdge edge = network.addEdge(node1, node2, true);
					network.getRow(edge).set(CyEdge.INTERACTION, structureInteraction);
					network.getRow(edge).set(
							CyNetwork.NAME,
							network.getRow(node1).get(CyNetwork.NAME, String.class) + " (" + structureInteraction
									+ ") " + network.getRow(node2).get(CyNetwork.NAME, String.class));
					edgeList.add(edge);
				}
				for (CyEdge edge : edgeList) {
					Double d;
					for (int i = 0; i < 3; i++) {
						d = new Double(results[i]);
						network.getRow(edge).set(attributeKeys[i], d);
					}
				}
			}
		}
	}
}
