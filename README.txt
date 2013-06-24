structureViz and RINalyzer

1. Introduction
1.1. General description
Here, we present a computational framework that combines network and structural biology by means of our tools structureViz and RINalyzer. Starting with a protein interaction network in Cytoscape, the user can visualize the experimentally derived structures of selected proteins in UCSF Chimera and then use these to generate and explore residue interaction networks in Cytoscape. structureViz provides a rich interface for the interactive communication with UCSF Chimera, while RINalyzer supports the advanced visualization and analysis of residue interaction networks (RINs).
1.2. structureViz
1.3. RINalyzer


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
RINs generated for most PDB structures in the PDB with the RINerator pakcage can be directly imported in Cytoscape. 
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
structureViz and RInalyzer provide diverse options for enhancing the visualization of RINs. Among others, edge/node coloring, color synchronization, layout, and pre-defined visual styles.

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
* To transfer the colors of the residues in only one model to the corresponding network nodes, select the model in the Molecular Structure Navigator dialog and select the menu Chimera -> Color synchronization

7.3. Layout
- Go to Apps -> structureViz -> Annotate residue network, select Coordinates and click OK to get the current 3D coordinates.
- Go to Layout -> RINLayout and click OK to apply a layout using the 3D coordinates and the default layout parameters.

7.4. Pre-defined visual styles
To come.

8. Automatic node/edge to structures association
structureViz automatically associated each new node in Cytoscape with the corresponding structure and vice versa given that the node annotations are correct. A node is associated with a whole structure if the structure is listed in one one of the attributes with the name: pdb, pdbFileName, .... A node is associated with a chain or residue, if it has a string node attribute ChimeraResidue with the following format: ....

9. Network comparison
