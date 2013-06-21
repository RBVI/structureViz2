structureViz and RINalyzer

1. Introduction

2. Installation
2.1. Requirements
- Cytoscape 3.0.1 (http://www.cytoscape.org/download.html)
- Cytoscape apps: structureViz2, RINalyzer2, setsApp
- UCSF Chimera 1.8 and above (http://www.cgl.ucsf.edu/chimera/download.html)
- For generating trajectory networks, get the daily build of UCSF Chimera

2.2. Installation
- Download and install Cytoscape and UCSF Chimera
- Copy the Cytoscape apps to the directory CytoscapeConfiguration\3\apps\installed in your user's directory.

3. Generate residue interaction networks from a selection in UCSF Chimera
3.1. Description

3.2. How-to (1)
- Launch Cytoscape.
- Launch Chimera from Cytoscape by selecting Apps -> structureViz -> Launch Chimera.
- Open a PDB file in Chimera.
- In UCSF Chimera (or in the Molecular Structure Navigator dialog), select the residues, for which a network should be generated.
- In the Molecular Structure Navigator dialog, go to Chimera -> Residue Network Generation.
- Select what type of edges should be included in the RIN and click OK to generate it.

3.3. How-to (2)
- Launch Cytoscape.
- Open a protein network with nodes annotated with PDB identifiers.
- Select a node and right-click to get its context menu.
- Go to Apps -> structureViz -> Open Structures for Node(s).
- Select the structures to be opened and click OK.
- In UCSF Chimera (or in the Molecular Structure Navigator dialog), select the residues, for which a network should be generated.
- In the Molecular Structure Navigator dialog, go to Chimera -> Residue Network Generation.
- Select what type of edges should be included in the RIN and click OK to generate it.

4. Import residue interaction networks from RINdata

5. Annotate residue interaction networks

6. Visualize RINs
6.1. Visual properties
- Go to Apps -> RINalyzer -> Visual Properties
- Change edge colors
- Show/hide edges of same type, e.g. contacts, hydrogen bonds
- Change secondary structure node coloring
- Convert curved edge lines to straight
- Adapt node labels

6.2. Color synchronization
- Go to Apps -> structureViz -> Synchronize colors.
- "Apply colors from associated Chimera models to current network view" will transfer the  ribbon colors of all residues (even in different models) associated with the nodes in the current view.
- "Apply colors from current network view to associated Chimera models" will transfer the colors of the nodes in the current view to the residues they are associated with.
* To transfer the colors of the residues in only one model to the corresponding network nodes, select the model in the Molecular Structure Navigator dialog and select the menu Chimera -> Color synchronization

6.3. Layout
- Go to Apps -> structureViz -> Annotate residue network, select Coordinates and click OK to get the current 3D coordinates.
- Go to Layout -> RINLayout and click OK to apply a layout using the 3D coordinates and the default layout parameters.

7. Generate trajectory networks
7.1. Description
The MD Movie tool of Chimera supports the generation of RINs from an MD trajectory. First, Analysis -> Cluster has to be used to cluster the trajectory based on pairwise best-fit root-mean-square deviations (RMSDs). A representative frame is identified for each cluster. Then, a RIN can be generated for each cluster of for the difference between two clusters. In both cases, the network nodes represent the amino acid residues and the edges their van der Waals contacts. In the single cluster RIN, the edge weights correspond to the average number of contacts between amino acids formed in each frame of the cluster. In the cluster difference RIN, the edge weights correspond to the difference of the average number of contacts in each cluster. In the network view, the edge weights are visualized by a color gradient, which is user-defined.
7.2. How-to
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

8. Node/Edge association

9. Network comparison