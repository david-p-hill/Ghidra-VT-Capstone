/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package capstonefinal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import docking.ActionContext;
import docking.ComponentProvider;
import docking.action.DockingAction;
import docking.action.ToolBarData;
import docking.widgets.table.GTable;
import ghidra.app.plugin.ProgramPlugin;
import ghidra.app.script.GhidraScript;
import ghidra.feature.vt.api.db.VTSessionDB;
import ghidra.feature.vt.api.main.VTMatch;
import ghidra.feature.vt.api.main.VTMatchSet;
import ghidra.feature.vt.gui.plugin.VersionTrackingPluginPackage;
import ghidra.features.base.values.GhidraValuesMap;
import ghidra.framework.model.DomainFile;
import ghidra.framework.plugintool.*;
import ghidra.framework.plugintool.util.PluginStatus;
import ghidra.program.model.listing.Program;
import ghidra.util.HelpLocation;
import ghidra.util.MessageType;
import resources.Icons;

//@formatter:off
@PluginInfo(
		status = PluginStatus.STABLE,
		packageName = VersionTrackingPluginPackage.NAME,
		category = "Version Tracking",
		shortDescription = "Version Differential Visual",
		description = "This plugin is a broad-view version differential visual with two modules. The"
				+ " first module exmaines the liklihood of a selected match from the VT Match Table,"
				+ " and highlights the match in the relative location in both versions of the program."
				+ " The second module explicitly examines how different the Destination Program is from"
				+ " the Source Program by identifying the best byte-related correlator and the BSim correlator"
				+ " strength at each address in the Destination Program. The second module also provides"
				+ " the difference in Source and Destination Programs' addresses for the byte-related and"
				+ " BSim correlators to offer insight on the location of potentially added or missing code in"
				+ " the Destination Program."
)
//@formatter:on
public class CapstoneFinalPlugin extends ProgramPlugin {

	MyProvider provider;
	static GetVTSession session;

	/**
	 * Plugin constructor.
	 * 
	 * @param tool The plugin tool that this plugin is added to.
	 */
	public CapstoneFinalPlugin(PluginTool tool) {
		super(tool);

		// TODO: Customize provider (or remove if a provider is not desired)
		String pluginName = getName();
		provider = new MyProvider(this, pluginName);

		// TODO: Customize help (or remove if help is not desired)
		String topicName = this.getClass().getPackage().getName();
		String anchorName = "HelpAnchor";
		provider.setHelpLocation(new HelpLocation(topicName, anchorName));
		
		// TODO: Create instance of GetVTSession
		session = new GetVTSession();
	}

	@Override
	public void init() {
		super.init();
	}

	public class GetVTSession extends GhidraScript{

		/*
		 * Private Variables Storing VT Session
		 */
		private String sessionName;
		private Program sourceProgram;
		private Program destProgram;
		private VTSessionDB vtSessionDB;
		
		/*
		 *  GetVTSession Constructor
		 *  A constructor allows us to initialize an instance of the script in order to run
		 */
		public GetVTSession() {
			sessionName = "Default Name";
			sourceProgram = null;
			destProgram = null;
			vtSessionDB = null;
		}

		/*
		 *  run() executes the script which sets the private variables above
		 */
		@Override
		protected void run() throws Exception {
			// TODO: script to retrieve VT Session from file directory
					GhidraValuesMap startupValues = new GhidraValuesMap();
					
					// Query Session Name, Source Program, and Destination Program
					startupValues.defineProjectFile("Select Version Tracking Session", "/");
					
					//startupValues.defineProjectFile("Select Source Program", "/"); -- Can uncomment
					//startupValues.defineProjectFile("Select Destination Program", "/"); -- Can uncomment
					
					// Error check that there are files selected
					startupValues.setValidator((valueMap, status) -> {
						if (!valueMap.hasValue("Select Version Tracking Session")) {
							status.setStatusText("Must select a Version Tracking Session!", MessageType.ERROR);
							return false;
						}
						/* -- Can uncomment
						if (!valueMap.hasValue("Select Source Program")) {
							status.setStatusText("Must select a Source Program!", MessageType.ERROR);
							return false;
						}
						
						if (!valueMap.hasValue("Select Destination Program")) {
							status.setStatusText("Must select a Destination Program!", MessageType.ERROR);
							return false;
						}
						*/
						return true;
					});

					startupValues = askValues("Enter Version Tracking Session:", "", startupValues);

					// Get files
					DomainFile sessionFile = startupValues.getProjectFile("Select Version Tracking Session");
					//DomainFile sourceFile1 = startupValues.getProjectFile("Select Source Program");
					//DomainFile destFile1 = startupValues.getProjectFile("Select Destination Program");
					
					// Set session Name
					setSessionName(sessionFile.getName());
					// Set srcProgram
					//setSourceProgram((Program) sourceFile1.getDomainObject(this, false, false, monitor)); -- Can uncomment
					// Set destProgram
					//setDestProgram((Program) destFile1.getDomainObject(this, false, false, monitor)); -- Can uncomment
					// Set vtSessionDB
					setVtSessionDB((VTSessionDB) sessionFile.getDomainObject(this, true, true, monitor));
		}
		/*
		 *  Get and Set methods
		 */
		// Session Name
		public String getSessionName() { return sessionName;}
		public void setSessionName(String sessionName) { this.sessionName = sessionName;}
		
		// Source Program
		public Program getSourceProgram() { return sourceProgram;}
		public void setSourceProgram(Program sourceProgram) { this.sourceProgram = sourceProgram;}
		
		// Destination Program
		public Program getDestProgram() { return destProgram;}
		public void setDestProgram(Program destProgram) { this.destProgram = destProgram;}
		
		// VTSessionDB
		public VTSessionDB getVtSessionDB() { return vtSessionDB;}
		public void setVtSessionDB(VTSessionDB vtSessionDB) { this.vtSessionDB = vtSessionDB;}
	}
	
	// Plugin Provider
	private static class MyProvider extends ComponentProvider {
		// private variables
		
		private JPanel mainPanel;
		private JPanel leftPanel;
		private JPanel rightPanel;
		private JPanel centPanel;
		private GTable table;
		private List<Object[]> trueMatchList;
		private Set<String> set3L;
		private Set<String> set3R;
		private DefaultTableModel v1Src;
		private DefaultTableModel v1Des;
		private DefaultTableModel newVis;
		private DefaultTableModel hiddenData;
		private DockingAction action;

		// provider method
		public MyProvider(Plugin plugin, String owner) {
			super(plugin.getTool(), owner, owner);
			buildPanels();
			getVTFiles();
			showVTFiles();
			theGhidraTransform();
		}

		// TODO: Customize GUI
		private void buildPanels() {
			// Create Panels
			mainPanel = new JPanel(new GridLayout(1,3));
			mainPanel.setPreferredSize(new Dimension(350, 190));
			leftPanel = new JPanel(new GridLayout());
			rightPanel = new JPanel(new GridLayout());
			centPanel = new JPanel(new BorderLayout());
			setVisible(true);
		}
		
		// TODO: Get program files and VT Session
		private void getVTFiles() {
			action = new DockingAction("Select VT Files", getName()) {
				@Override
				public void actionPerformed(ActionContext context) {
					// TODO: Call Custom Class With VT Session Info
					try {
						session.run();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			action.setToolBarData(new ToolBarData(Icons.MAKE_SELECTION_ICON, null));
			action.setEnabled(true);
			action.markHelpUnnecessary();
			dockingTool.addLocalAction(this, action);
		}
		
		// TODO: Produce Small VT Match Table and Similarity Visual
		private void showVTFiles() {
			action = new DockingAction("Show Matches", getName()) {
				@Override
				public void actionPerformed(ActionContext context) {
					// TODO: Add information to each panel
					// add panels
					mainPanel.removeAll();
					leftPanel.removeAll();
					rightPanel.removeAll();
					mainPanel.add(leftPanel);
					mainPanel.add(centPanel);
					mainPanel.add(rightPanel);
					
					// Make the Small VT Match Table
					GhidraTable(centPanel);
				}

				private void GhidraTable(JPanel panel) {
					// Reset panel
					panel.removeAll();			
					
					// Create Table
					table = new GTable();
					DefaultTableModel model1 = new DefaultTableModel() {
						// lock editing
						@Override
						public boolean isCellEditable(int row, int column) {
			                return false;
			            }
					};
					// create columns
					model1.addColumn("Association Type");
					model1.addColumn("Similarity Score");
					model1.addColumn("Confidence Score");
					model1.addColumn("Source Address");
					model1.addColumn("Destination Address");
					model1.addColumn("Transform");
					
					// for loop to get VT MatchSet
					List<VTMatchSet> list = session.getVtSessionDB().getMatchSets();
					
					// loop through all matches
					for(VTMatchSet vtms : list) {
						// get collection of VTMatch (a sublist of VTMatchSet)
						Collection<VTMatch> sublist = vtms.getMatches();
						// save correlator name
						String correlator = vtms.getProgramCorrelatorInfo().getName();
						// loop through sublist and put match data into table
						for(VTMatch vtm : sublist) {
							// get logarithmic confidence score
							DecimalFormat df = new DecimalFormat("#.####");
							double confidence = vtm.getConfidenceScore().getLog10Score();
							// add values to table
							model1.addRow(new Object[] {correlator, vtm.getSimilarityScore(), 
									Double.parseDouble(df.format(confidence)), vtm.getSourceAddress(), 
									vtm.getDestinationAddress(), "Yurr"});
						}
					}
										
					// Send model to Table
					table.setModel(model1);
					// Row Sorter
					TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
					table.setRowSorter(sorter);
					// set minSrc, maxSrc, minDes, maxDes
					setAddressBounds();
					// add table listener
					table.addMouseListener( new MouseAdapter(){
						@Override
						public void mousePressed(MouseEvent e) {
							int Row = table.rowAtPoint(e.getPoint());
							if(Row >= 0) {
								// account for table sorter
								Row = table.convertRowIndexToModel(Row);
								String srcSelect = table.getModel().getValueAt(Row, 3).toString();
								String desSelect = table.getModel().getValueAt(Row, 4).toString();
																						
								// create a table to show relative match positions in codes
								showMatchLocationInPanel("Source Program", leftPanel, set3L, srcSelect, Row);
								showMatchLocationInPanel("Destination Program", rightPanel, set3R, desSelect, Row);
							}
						}

						
						private void showMatchLocationInPanel(String colTitle, JPanel panel1, Set<String> set1, String select, int selRow) {
							// TODO Create a table with only the selected address row colored in
							// Reset panel
							panel1.removeAll();
							
							// get index of select in set1
							int selD = 0;
							for(String address : set1) {
								if(select.equals(address))
									break;
								selD++;
							}
							
							// create a column representing every unique address from VT Match Table
							int numRows = set1.size();
							
							// array to represent which row is to be colored (default boolean array is all False)
							boolean[] coloredRows = new boolean[numRows];
							// change only selected row index to True
							coloredRows[selD] = true;
							
							// new table with custom cell renderer to change cell color
							JTable table1 = new JTable(numRows, 1) {
								// prohibit cell edits by user
								@Override
								public boolean isCellEditable(int row, int column) {
					                return false;
					            }
								// color column row
								@Override
								public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
									Component c = super.prepareRenderer(renderer, row, col);
									// change color for the corresponding selected address
									if(changeRowColor(row)) {
										String simVal = table.getModel().getValueAt(selRow, 1).toString();
										String conVal = table.getModel().getValueAt(selRow, 2).toString();
										
										// These are arbitrary discrete thresholds
										if((simVal.compareTo("0.9") > 0) && (conVal.compareTo("0.9") > 0))
											c.setBackground(Color.GREEN);
										else if((simVal.compareTo("0.8") > 0) && (conVal.compareTo("0.8") > 0))
											c.setBackground(Color.YELLOW);
										else if((simVal.compareTo("0.7") > 0) && (conVal.compareTo("0.7") > 0))
											c.setBackground(Color.ORANGE);
										else
											c.setBackground(Color.RED);
											
									}
									else {
										c.setBackground(Color.WHITE);
									}
									return c;
								}

								// returns boolean array to color specific rows in table cell renderer
								private boolean changeRowColor(int row) {
									// TODO Auto-generated method stub
									return coloredRows[row];
								}
							};
							
							// adjust size of rows to fill panel
							int panelHeight = panel1.getHeight();
							table1.setRowHeight(panelHeight/numRows);
							// change column title
							table1.getColumnModel().getColumn(0).setHeaderValue(colTitle);
							// add column to panel
							panel1.add(table1);
							table1.setShowGrid(false);
							// add scroll pane if necessary
							// add scroll pane
							JScrollPane scroll1 = new JScrollPane(table1);
							scroll1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
							panel1.add(scroll1);
							// set panel background color to white
							panel1.setOpaque(true);
							panel1.setBackground(Color.WHITE);
							// refresh panel
							panel1.revalidate();
							panel1.repaint();
						}
					});
					
					// Send Table to Panel
					panel.add(table);
					// add scroll pane
					JScrollPane scroll = new JScrollPane(table);
					scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
					panel.add(scroll);
					// Refresh panel
					panel.revalidate();
					panel.repaint();
				}

				private void setAddressBounds() {
					// create view1 Source and Destination Program Table Models
					v1Src = new DefaultTableModel();
					v1Des = new DefaultTableModel();
					v1Src.addColumn("Source Program");
					v1Des.addColumn("Destination Program");
																				
					/*
					 *  Add rows that represent each address exactly once by using a Set
					 */
					SortedSet<String> set1L = new TreeSet<>();
					SortedSet<String> set1R = new TreeSet<>();
					SortedSet<String> set2L = new TreeSet<>();
					SortedSet<String> set2R = new TreeSet<>();
					set3L = new LinkedHashSet<>();
					set3R = new LinkedHashSet<>();
					
					// sort addresses by External and non-External addresses
					for(int i=0; i<table.getModel().getRowCount(); i++) {
						String srcAddress = table.getModel().getValueAt(i, 3).toString();
						String desAddress = table.getModel().getValueAt(i, 4).toString();
						// sort source program addresses
						if(srcAddress.contains("EXTERNAL"))
							set1L.add(srcAddress);
						else
							set2L.add(srcAddress);
						// sort destination program addresses
						if(srcAddress.contains("EXTERNAL"))
							set1R.add(desAddress);
						else
							set2R.add(desAddress);
					}
					// combine sets
					set3L.addAll(set1L);
					set3L.addAll(set2L);
					set3R.addAll(set1R);
					set3R.addAll(set2R);
					
					// Create View 1 Source Program Table Model
					for(String address : set3L) {
						v1Src.addRow(new Object[] {address});
					}
					// Create View 1 Destination Program Table Model
					for(String address : set3R) {
						v1Des.addRow(new Object[] {address});
					}
					
				}
			};
			action.setToolBarData(new ToolBarData(Icons.REFRESH_ICON, null));
			action.setEnabled(true);
			action.markHelpUnnecessary();
			dockingTool.addLocalAction(this, action);
		}
		
		// TODO: Analyze Matches and Produce Differential Visual
		private void theGhidraTransform() {
			action = new DockingAction("Ghidra Transform", getName()) {
				@Override
				public void actionPerformed(ActionContext context) {
					// Remove left panel
					mainPanel.remove(leftPanel);					
					// TODO: Ghidra Transform (pull desired match correlators)
					ghidraTransform(table, centPanel);
					// TODO: Compiler visual representation
					transformVis(table, rightPanel);
				}
						
				private void transformVis(GTable table1, JPanel panel1) {
					// Reset panel
					panel1.removeAll();
					
					// new table with custom cell renderer to change cell color
					JTable newCol = new JTable() {
						// prohibit cell edits by user
						@Override
						public boolean isCellEditable(int row, int column) {
			                return false;
			            }
						// Bytes and Compiler Color Visuals
						@Override
						public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
							Component c = super.prepareRenderer(renderer, row, col);
							if(col == 1) {
								/*
								 *  show lagging or leading addresses for applicable matches
								 *  
								 *  Red   = lagging (-delta)
								 *  Blue  = leading (+delta)
								 *  White = same place
								 *  Gray  = Ignore
								 */
								String bTag = hiddenData.getValueAt(row, 2).toString();
								String cTag = hiddenData.getValueAt(row, 4).toString();
								double bDelta = (double) hiddenData.getValueAt(row, 1);
								double cDelta = (double) hiddenData.getValueAt(row, 3);

								// convert deltas to color value
								// this is a custom made relationship to darken the color for large deltas
								int bColorVal = (int) (255*Math.pow(1.02177, -Math.abs(bDelta)));
								int cColorVal = (int) (255*Math.pow(1.02177, -Math.abs(cDelta)));
								
								
								// Case 1: Neither have Applicable Match
								if(bTag.equals("Yurr") && cTag.equals("N/A")) {
									c.setBackground(Color.GRAY);
								}
								// Case 2: Only Bytes has Applicable Match
								else if(!bTag.equals("Yurr") && cTag.equals("N/A")){
									// White if 0 delta
									if(bDelta == 0)
										c.setBackground(Color.WHITE);
									// Red if -delta
									if(bDelta < 0)
										c.setBackground(new Color(bColorVal, 0, 0));
									// Blue if +delta
									if(bDelta > 0)
										c.setBackground(new Color(0, 0, bColorVal));
								}
								// Case 3: Only Compiler has Applicable Match
								else if(bTag.equals("Yurr") && !cTag.equals("N/A")){
									// White if 0 delta
									if(bDelta == 0)
										c.setBackground(Color.WHITE);
									// Red if -delta
									if(bDelta < 0)
										c.setBackground(new Color(cColorVal, 0, 0));
									// Blue if +delta
									if(bDelta > 0)
										c.setBackground(new Color(0, 0, cColorVal));
								}
								// Case 4: Both have Applicable Match
								else if(!bTag.equals("Yurr") && !cTag.equals("N/A")){
									// White if 0 delta
									if(bDelta == 0)
										c.setBackground(Color.WHITE);
									// Red if -delta
									if(bDelta < 0)
										c.setBackground(new Color((bColorVal+cColorVal)/2, 0, 0));
									// Blue if +delta
									if(bDelta > 0)
										c.setBackground(new Color(0, 0, (bColorVal+cColorVal)/2));
								}
							}
							
							// Bytes Color Visual
							else if(col == 0) {
								String cellVal = hiddenData.getValueAt(row, 2).toString();
								if(cellVal.equals("Yurr"))
									c.setBackground(Color.GRAY);
								else if(cellVal.equals("True Exact Match")){
									// True Exact Match is dark green
									c.setBackground(new Color(6, 64, 43));
								}
								else if(cellVal.equals("Instructions")){
									// Instructions Match is dark yellow
									c.setBackground(new Color(139, 128, 0));
								}
								else if(cellVal.equals("Data")){
									// Data Match is steel blue
									c.setBackground(new Color(70, 130, 180));
								}
							}
							
							// Compiler Color Visual
							else if(col == 2){
								if(hiddenData.getValueAt(row, 4).toString().equals("N/A"))
									c.setBackground(Color.GRAY);
								else {
									// convert SimVal to shade of purple
									int rVal = (int) (131*Double.parseDouble(hiddenData.getValueAt(row, 4).toString()));
									int gVal = (int) (10*Double.parseDouble(hiddenData.getValueAt(row, 4).toString()));
									int bVal = (int) (191*Double.parseDouble(hiddenData.getValueAt(row, 4).toString()));
									c.setBackground(new Color(rVal, gVal, bVal));
								}
							}
							return c;
						}
					};
					// create newVis Table Model
					newVis = new DefaultTableModel();
					newVis.addColumn("Bytes Similarity");
					newVis.addColumn(" Destination Program Address");
					newVis.addColumn("Compiler Similarity");
					
					// Create a hidden Table Model that contains analysis data
					hiddenData = new DefaultTableModel();
					hiddenData.addColumn("Address");
					hiddenData.addColumn("Bytes Delta");
					hiddenData.addColumn("Bytes Tag");
					hiddenData.addColumn("Compiler Delta");
					hiddenData.addColumn("Compiler SimVal");
										
					/*
					 *  Add rows that represent each address exactly once by using a Set
					 */
					SortedSet<String> set1 = new TreeSet<>();
					SortedSet<String> set2 = new TreeSet<>();
					Set<String> set3 = new LinkedHashSet<>();
					for(int i=0; i<table1.getModel().getRowCount(); i++) {
						String address = table1.getModel().getValueAt(i, 4).toString();
						if(address.contains("EXTERNAL"))
							set1.add(address);
						else
							set2.add(address);
					}
					set3.addAll(set1);
					set3.addAll(set2);
					
					// create newVis and hiddenData Table
					for(String address : set3) {
						
						// hiddenData
						Object[] rowData = hiddenDataTableRow(table1, address);
						hiddenData.addRow(rowData);
						// newVis
						newVis.addRow(new Object[] {rowData[1], address, rowData[3]});
					}
					
					// set model to table
					newCol.setModel(newVis);
					// add column to panel
					panel1.add(newCol);
					newCol.setShowGrid(true);
					// add scroll pane if necessary
					// add scroll pane
					JScrollPane scroll1 = new JScrollPane(newCol);
					scroll1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
					panel1.add(scroll1);
					// refresh panel
					panel1.revalidate();
					panel1.repaint();
				}

				private Object[] hiddenDataTableRow(GTable table1, String address) {
					/*
					 *  Create hiddenData Table Row
					 */
					// default values
					Double compDelta = (double) 0;
					String simVal = "N/A";
					Double byteDelta = (double) 0;
					String byteTag = "N/A";
					
					// loop through table
					for(int i=0; i<table1.getModel().getRowCount(); i++) {
						// get Destination address
						String desA = table1.getModel().getValueAt(i, 4).toString();
						// check if desired address matches address at iterated row
						if(desA.equals(address)) {
							// get source program address for associated match
							String srcA = table1.getModel().getValueAt(i, 3).toString();
							// get match tag
							String tag = table1.getModel().getValueAt(i, 5).toString();
							// get compiler information
							if(tag.equals("Compiler")) {
								// Compiler Delta
								compDelta = convertHex2Dec(desA) - convertHex2Dec(srcA); 
								// Compiler SimVal
								simVal = table1.getModel().getValueAt(i, 1).toString();
							}
							// get byte-related match information
							else {
								// Bytes delta
								byteDelta = convertHex2Dec(desA) - convertHex2Dec(srcA);
								// Bytes Tag
								byteTag = tag;
							}
						}
					}
					
					// return hidden data row
					Object[] row = new Object[]{address, byteDelta, byteTag, compDelta, simVal} ;
					return row;
				}

				private void ghidraTransform(GTable table1, JPanel panel1) {
					// TODO Identify the true matches and non-matches in the Transform Column
					// Look through the match associations and mark every exact match as a "True Exact Match" or "Compiler"
						
					int numRows = table1.getModel().getRowCount();
					trueMatchList = new ArrayList<Object[]>();
					
					// Identify compiler similarity
					// "Compiler" Tag only applies to "BSim Function Matching"
					for(int i=0; i< numRows; i++) {
						if(table1.getModel().getValueAt(i, 0).toString().equals("BSim Function Matching")) {
							table1.getModel().setValueAt("Compiler", i, 5);
						}
					}
					
					// True Exact Matches
					tagRows(table1, numRows, "Exact Function Bytes Match", "True Exact Match");
					// Same Instructions
					tagRows(table1, numRows, "Exact Function Instructions Match", "Instructions");
					// Same Instructions
					tagRows(table1, numRows, "Exact Data Match", "Data");
					
					// copy table1 to table where table does not have rows marked "delete"
					table = makeTable(table1);
					
					// add table2 to panel
					panel1.removeAll();
					panel1.add(table);
					// add scroll pane
					JScrollPane scroll1 = new JScrollPane(table);
					scroll1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
					panel1.add(scroll1);
					// Refresh panel
					panel1.revalidate();
					panel1.repaint();
				}

				private GTable makeTable(GTable table1) {
					// Create Table
					GTable table2 = new GTable();
					DefaultTableModel model2 = new DefaultTableModel() {
						// lock editing
						@Override
						public boolean isCellEditable(int row, int column) {
			                return false;
			            }
					};
					// create columns
					model2.addColumn("Association Type");
					model2.addColumn("Similarity Score");
					model2.addColumn("Confidence Score");
					model2.addColumn("Source Address");
					model2.addColumn("Destination Address");
					model2.addColumn("Transform");
					
					// add rows to table2 that are not marked "delete" in table1
					TableModel t1 = table1.getModel(); 
					int numRows = t1.getRowCount();
										
					// Effectively Delete Rows
					// Make a new table and add rows not marked "Delete"
					for(int i=0; i< numRows; i++) {
						if(!table1.getModel().getValueAt(i, 5).toString().equals("Delete")) {
							model2.addRow(new Object[] {t1.getValueAt(i, 0), t1.getValueAt(i, 1), t1.getValueAt(i, 2),
									t1.getValueAt(i, 3), t1.getValueAt(i, 4), t1.getValueAt(i, 5)});
						}
					}
															
					// Send model to Table
					table2.setModel(model2);
					// Row Sorter
					TableRowSorter<TableModel> sorter = new TableRowSorter<>(table2.getModel());
					table2.setRowSorter(sorter);
					
					// return table
					return table2;
				}

				private void tagRows(GTable table1, int numRows, String target, String newTag) {
					// assign a tag to target row
					for(int i=0; i< numRows; i++) {
						// Tag targeted Match Correlator						
						if(table1.getModel().getValueAt(i, 0).toString().equals(target) && !table1.getModel().getValueAt(i, 5).toString().equals("Delete")) {
							table1.getModel().setValueAt(newTag, i, 5);
							trueMatchList.add(new Object[] {table1.getModel().getValueAt(i, 3), table1.getModel().getValueAt(i, 4)});
						}
					}			
					// delete redundant matches for targeted Match Correlator
					for(int i=0; i< numRows; i++) {
						// Delete Matches which address pairs at same location as targeted Match Correlator
						if(table1.getModel().getValueAt(i, 5).toString().equals("Yurr")) {
							Object[] addresses = new Object[] {table1.getModel().getValueAt(i, 3), table1.getModel().getValueAt(i, 4)};
							
							for(Object[] iterator : trueMatchList) {
								// check if address pair has been already classified as targeted Match Correlator
								if(Arrays.equals(iterator, addresses))
									table1.getModel().setValueAt("Delete", i, 5); // actually delete row
								// outlier test case
								if(iterator[1].toString().equals(addresses[1].toString()))
									table1.getModel().setValueAt("Delete", i, 5);
							}
						}
					}
					
				}
			};
			action.setToolBarData(new ToolBarData(Icons.CUT_ICON, null));
			action.setEnabled(true);
			action.markHelpUnnecessary();
			dockingTool.addLocalAction(this, action);
		}

		protected Double convertHex2Dec(String address) {
			// TODO Convert the string that represents an Address in hex to a decimal value
			// each address has 8 hex values (0-F)
			
			// check if address has "EXTERNAL:"
			if(address.contains("EXTERNAL:"))
				address = address.substring(9);
			
			// a string of hex values 0-F
			String hex = "0123456789abcdef";
			// decimal value
			double dec = 0;
			// use index of hex to multiply inverse indices of address by 16*index
			for(int i=0; i<=7; i++) {
				char hexAtI = address.charAt(7-i);
				int hexIndex = hex.indexOf(hexAtI);
				// update dec
				dec = dec + hexIndex*Math.pow(16, i);
			}
			
			return dec;
		}
		@Override
		public JComponent getComponent() {
			return mainPanel;
		}
	}
}