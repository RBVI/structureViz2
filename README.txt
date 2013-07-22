structureViz and RINalyzer
---------------------------

1. Introduction
1.1. General description
Here, we present a computational framework that combines network and structural biology by means of our tools structureViz and RINalyzer. Starting with a protein interaction network in Cytoscape, the user can visualize the experimentally derived structures of selected proteins in UCSF Chimera and then use these to generate and explore residue interaction networks in Cytoscape. structureViz provides a rich interface for the interactive communication with UCSF Chimera, while RINalyzer supports the advanced visualization and analysis of residue interaction networks (RINs).
1.2. structureViz
Website: http://www.rbvi.ucsf.edu/Research/cytoscape/structureViz/
Tutorial: http://www.rbvi.ucsf.edu/Research/cytoscape/structureViz/tutorial.html
1.3. RINalyzer
Website: http://www.rinalyzer.de/
Tutorial: http://www.rinalyzer.de/tutorials.php
Nature Protocol: http://dx.doi.org/doi:10.1038/nprot.2012.004

2. Installation
2.1. Requirements
- Cytoscape 3.0.1 (http://www.cytoscape.org/download.html)
- Cytoscape apps: structureViz2, RINalyzer2, setsApp
- UCSF Chimera 1.8 and above (http://www.cgl.ucsf.edu/chimera/download.html)
- The daily build of UCSF Chimera for generating trajectory networks
2.2. Installation
- Download and install Cytoscape and UCSF Chimera.
- Copy the Cytoscape apps to the directory CytoscapeConfiguration\3\apps\installed in your user's directory.


3. Generate RINs from a single structure
3.1. Description
structureViz supports the generation of RINs from a selection of residues in UCSF Chimera.
In the resulting RIN, nodes represent amino acid residues, solvent molecules, ligands, etc., while the edges correspond to (non-) covalent interactions between these entities. Currently, 5 types of interactions can be retrieved: contacts, clashes, hydrogen bonds, connectivity (backbone), distances. There are different interaction subtypes depending on whether an interaction occurs between the atoms in the main chain (mc), side chain (sc), water, etc. For each interaction, the interacting atoms identifiers as well as the distance/overlap are also included as attributes. For contact edges this equals to the (minimum) distance between the closest atoms, for hydrogen bonds to the distance between the H donor and acceptor, for distance edges to the distance between the atoms, and for clashes to the (maximum) overlap between the atoms.
After a RIN is generated, the preferred layout is applied to the network view and a new visual style is applied, so that the nodes and edges are colored according to secondary structure and interaction subtype, respectively. In addition, all residue attributes available in Chimera are automatically transferred as node attributes in Cytoscape. Usually, they include secondary structure, hydrophobicity, residue coordinates, backbone and side chain angles, average bfactor, average occupancy and others.
3.2. How-to (from a PDB file)
- Launch Cytoscape.
- Launch Chimera from Cytoscape by selecting Apps -> structureViz -> Launch Chimera.
- Open a PDB file in Chimera.
- In UCSF Chimera (or in the Molecular Structure Navigator dialog), select the residues, for which a RIN should be generated.
- In the Molecular Structure Navigator dialog, go to Chimera -> Residue Network Generation.
- Choose which interaction types should be included in the RIN and click OK to generate it.
3.3. How-to (from a PPI network)
- Launch Cytoscape.
- Open a network session with PDB annotations (see XX for specifications).
- Select a node and right-click to open its context menu.
- Go to Apps -> structureViz -> Open Structures for Node(s).
- Select the structures to be opened and click OK.
- In UCSF Chimera (or in the Molecular Structure Navigator dialog), select the residues, for which a network should be generated.
- In the Molecular Structure Navigator dialog, go to Chimera -> Residue Network Generation.
- Choose which interaction types should be included in the RIN and click OK to generate it.


4. Generate RINs from a trajectory
4.1. Description
The MD Movie tool of Chimera supports the generation of RINs from an MD trajectory. First, Analysis -> Cluster has to be used to cluster the trajectory based on pairwise best-fit root-mean-square deviations (RMSDs). A representative frame is identified for each cluster. Then, a RIN can be generated for each cluster of for the difference between two clusters. In both cases, the network nodes represent the amino acid residues and the edges their van der Waals contacts. In the single cluster RIN, the edge weights correspond to the average number of contacts between amino acids formed in each frame of the cluster. In the cluster difference RIN, the edge weights correspond to the difference of the average number of contacts in each cluster. In the network view, the edge weights are visualized by a color gradient, which is user-defined.
4.2. How-to
- Launch Cytoscape.
- Go to Apps -> structureViz -> Settings... and set the path to the Chimera executable at the bottom of the dialog.
- Launch Chimera from Cytoscape by selecting Apps -> structureViz -> Launch Chimera.
- Open the MD Movie tool (Tools -> MD/Ensemble Analysis) in Chimera.
- Select the GROMACS trajectory format and load the corresponding files.
- To cluster the trajectory, go to Analysis -> Cluster... and click OK.
- In the Clustering dialog, select one or two clusters to enable the generate RIN buttons.
- In the Calculate Residue-Interaction Network dialog, click OK to generate the RIN with the default settings.
- Select the whole structure or a subset of residues, for which the contacts should be evaluated. Designate the selected atoms, choose "themselves" and click OK.
- In the Residue Interaction Computation and Display, adjust the color scheme, edge width and network name, and click OK to load the RIN in Cytoscape.
- Switch back to Cytoscape to explore the network.


5. Import RINs from RINdata
5.1. Description
RINs generated for most PDB structures in the PDB with the RINerator package (http://rinalyzer.de/docu/rindata_gen.php) can be directly imported in Cytoscape.
5.2. How-to
- Go to File -> Import -> Network -> Public databases.
- In the Data Source drop-down menu, select the RINdata Web Service Client.
- In the Query window, type a PDB identifier and click search to import the RIN for this PDB.
- Click Cancel to close the window or repeat the last steps to import another RIN.


6. Annotate RINs
6.1. Description
structureViz provides the functionality to import all residue attributes in Chimera as node attributes in Cytoscape. All attribute names except for secondary structure and coordinates are retrieved automatically. Thus, user-defined residue attributes in Chimera are also listed and can be imported in Cytoscape. The secondary structure (SS) attribute contains information about the the secondary structure of the corresponding residue. The residue coordinates are represented by three attributes in Cytoscape (resCoord.x, resCoord.y, resCoord.z).
6.2. How-to
- Go to Apps -> StructureViz -> Annotate Residue Network.
- Select attributes and click OK to transfer them as node attributes.


7. Visualize RINs
structureViz and RINalyzer provide diverse options for enhancing the visualization of RINs. Among others, edge/node coloring, color synchronization between network and structure views, network layout based on the current structure position, and pre-defined visual styles.

7.1. Node/edge visual properties
- Go to Apps -> RINalyzer -> Visual Properties.
- Show/hide edges of same type, e.g. contacts, hydrogen bonds, by selecting or unselecting the corresponding box in the Edge tab.
- Adjust the edge colors in the Edge tab if necessary.
- Convert curved to straight edge line by clicking the corresponding box in the Edge tab.
- Change secondary structure node coloring in the Node tab if necessary.
- Adapt the node labels in the Node tab.
- Click Apply to apply changes.

7.2. Color synchronization
- Go to Apps -> structureViz -> Synchronize colors.
- "Apply colors from associated Chimera models to current network view" will transfer the ribbon colors of all residues (even in different models) associated with the nodes in the current view.
- "Apply colors from current network view to associated Chimera models" will transfer the colors of the nodes in the current view to the residues they are associated with.
* To transfer the colors of the residues in only one model to the corresponding network nodes, select the model in the Molecular Structure Navigator dialog and go to Chimera -> Color synchronization

7.3. Layout
- Go to Apps -> structureViz -> Annotate residue network, select Coordinates and click OK to get the current 3D coordinates (only until Cytoscape release 3.1.)
- Go to Layout -> RINLayout and click OK to apply a layout using the 3D coordinates and the default layout parameters.

7.4. Pre-defined visual styles
To come.

8. Automatic node/edge to structures association
structureViz automatically associated each new node in Cytoscape with the corresponding structure and vice versa, given that the nodes are correctly annotated with PDB structures and residue identifiers. A node is associated with a PDB structure, if the structure is contained in any of the string (list) attributes: "Structure", "pdb", "pdbFileName", "PDB ID", "structure", "biopax.xref.PDB", "pdb_ids", "ModelName", "ModelNumber". A node is associated with a chain or residue, if it has a string node attribute called "ChimeraResidue" with the following format: [modelName[.modelNumber]#][residueID][.chainID], whereas modelName should be either 1) a 4-character PDB identifie; 2) a file path enclosed by quotation marks; 3) an URL enclosed by quotation marks. For some of the RINalyzer tasks, an additional node attribute called "RINalyzerResidue" is needed and it is formatted as follows: modelName:chainID:residueIndex:insertionCode:residueType, whereas missing values are substituted by an underscore. A node is also associated with a smiles structure, if it has a node attribute called "Smiles", "smiles", or "SMILES" containing the corresponding smiles string.
In addition, structureViz checks for new associations, whenever a new network is created, the above listed node attributes are changed, or a new structure is opened in Chimera.

9. Network comparison
9.1. Description
RINalyzer supports the comparison of two RINs and generated a comparison network, in which edges and nodes belonging to either or both networks are highlighted (see RINalyzer webpage). The comparison is performed using
(1) the Chimera MatchMaker tool and the resulting sequence and/or structure alignment (http://www.cgl.ucsf.edu/chimera/docs/ContributedSoftware/matchmaker/matchmaker.html)
(2) an XML structure alignment file as generated by the RCSB PDB Protein Comparison Tool (http://www.rcsb.org/pdb/workbench/workbench.do)
(3) an AFASTA alignment file as generated by UCSF Chimera (header should be formatted as in this example: >1PTA, chain A/36-362)
(4) a node attribute mapping based on an attribute selected by the user (recommended are node name or RINalyzer identifier)

9.2. How-to
- Make sure you have two RINs that could be aligned.
- Go to Apps -> RINalyzer -> Network Comparison.
- Enter a name for the comparison network, select the networks to be compared and the node attribute containing the RINalyzer identifiers (needed for the correct matching of nodes and residues).
- If the models corresponding to these RINs are open in Chimera, they will be listed. Decide whether to use Chimera for the alignment and adjust the settings.
- If there are no models open in Chimera or you prefer to use your own alignment file, choose the file by clicking the Open a File button.
- If neither Chimera is used for the mapping nor an external alignment file is supplied, the values in the first and second name attribute will be matched.
- Click OK and a new network should appear in a few seconds. If the Chimera alignment is chosen, the selected chains will also be aligned in the Chimera window.

10. Extract interfaces and single chains
10.1. Description
RINalyzer provides the functionality to extract single chain(s) or interface(s) between chains in the same RIN. When generating a new RIN from a single or multiple chains, RINalyzer includes the interactions between residues in the chain(s) as well as on the interface between them. In a RIN for the interface of one chain, all residues are included that have interactions with other chains. In a RIN for the interface between multiple chains, all residues having interactions with residues in other chains are included as well as their interactions. In this case, the user can select whether or not interactions between residues in the same chain should be considered.

10.2. How-to
- Go to Apps -> RINalyzer -> Subnetwork Generation.
- Adjust the settings, enter a name for the new network and click OK.

