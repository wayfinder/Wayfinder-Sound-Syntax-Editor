/*******************************************************************************
 * Copyright (c) 1999-2010, Vodafone Group Services
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above 
 *       copyright notice, this list of conditions and the following 
 *       disclaimer in the documentation and/or other materials provided 
 *       with the distribution.
 *     * Neither the name of Vodafone Group Services nor the names of its 
 *       contributors may be used to endorse or promote products derived 
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 ******************************************************************************/

package com.wayfinder.syntaxedit;

import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.VertexView;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.GraphUndoManager;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphConstants;
import org.jgraph.cellview.JGraphRoundRectView;
import java.util.Hashtable;
import java.awt.Component;
import java.awt.geom.Rectangle2D;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import javax.swing.event.UndoableEditEvent;
import javax.swing.TransferHandler;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JScrollPane;
import javax.swing.Action;
import org.xml.sax.SAXException;

/**
 * Graphical element that provides the main window (manu bar,
 * tabbed views, owns the JGraph editors.
 *
 * The graph model is owned by the SyntaxTree passed in the
 * constructor. The models are passed from the SyntaxTree to
 * the JGraph in createNewGraphTab().
 *
 */
public class SyntaxEditor extends javax.swing.JFrame
        implements StatusManager {
    protected class ViewFactory extends DefaultCellViewFactory {
        /**
         * Creates and returns a default <code>GraphView</code>.
         *
         * @return the default <code>GraphView</code>
         */
        protected VertexView createVertexView(Object v) {
                /*
                if (v instanceof EllipseCell)
                    return new JGraphEllipseView(v);
                else if (v instanceof DiamondCell)
                    return new JGraphDiamondView(v);
                else if (v instanceof RoundRectangleCell)
                    return new JGraphRoundRectView(v);
                else if (v instanceof ImageCell)
                    return new ScaledVertexView(v);
                else if ((v instanceof TextCell) &&
                        ((TextCell) v).isMultiLined())
                    return new JGraphMultilineView(v);
                 */
            if (v instanceof MacroModel.StartCell)
                return new JGraphRoundRectView(v);
            else if (v instanceof MacroModel.BooleanBranchCell)
                //return new JGraphDiamondView(v);
                return super.createVertexView(v);
            return super.createVertexView(v);
        }
        
    };
    
    protected GuideView guideView;
    protected SyntaxTree synTree;
    protected Map<String, JScrollPane> graphViews;
    protected final int layout_dy = 80;
    JFileChooser fc;
    protected SyntaxTree.SyntaxTreeEventListener synListener =
            new SyntaxTree.SyntaxTreeEventListener() {
        public void modelListChanged() {
            updateBlockMenus();
        }
        public void soundListChanged() {
            updateInsertSoundMenu();
        }
    };
    protected GraphUndoManager undoManager = new GraphUndoManager() {
        public void undoableEditHappened(UndoableEditEvent e) {
            // First invoke Superclass
            super.undoableEditHappened(e);
            // Update Undo/Redo buttons and menu entries
            updateHistoryButtons();
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    
    /** Shut up the compiler warnings
     */
    private static final long serialVersionUID = 42L;
    
    /** Creates new form ProdEdit */
    public SyntaxEditor(JFileChooser fileChooser) throws Throwable {
        this(null, fileChooser);
    }
    
    public SyntaxEditor(File f, JFileChooser fileChooser) throws Throwable {
        if (f != null) {
            try {
                synTree = new SyntaxTree(undoManager, f);
            } catch (Throwable t) {
                JOptionPane.showMessageDialog(null, "Failed to parse xml file",
                        "File corrupt", JOptionPane.ERROR_MESSAGE);
                t.printStackTrace();
                throw t;
            }
        } else {
            synTree = new SyntaxTree(undoManager);
        }
        fc = fileChooser;
        synTree.addSyntaxTreeEventListener(synListener);
        graphViews = new HashMap<String, JScrollPane>();
        
        initComponents();
        updateBlockMenus();
        updateInsertSoundMenu();
        gotoProdModel("SoundListNormal");
    }
    
    /** Open a graph for a specified model in a
     *  new tab
     */
    
    private void createNewGraphTab(String prodName) {
        MacroModel model = synTree.getModel(prodName);
        assert (model != null) : "no such model found";
        if (model == null)
            return;
        JGraph j = new JGraph();
        j.getGraphLayoutCache().setFactory(new ViewFactory());
        j.setModel(model);
        j.setPortsVisible(true);
        j.setMarqueeHandler(new EdgeAddMarquee(j, this));
        model.setStatusManager(this);
        layoutGraph(j);
        
        JScrollPane scroller = new JScrollPane(j);
        jTabbedPane1.addTab(prodName, scroller);
        graphViews.put(prodName, scroller);
    }
    
    /**
     * Handle changes to the undo buffer.
     * Mostly this implies graying out the invalid buttons / menu entries
     */
    protected void updateHistoryButtons() {
        
    }
    
    /**
     *  Create a menu item for each SyntaxTree.
     **/
    private void updateBlockMenus() {
        for (int i=jBlockMenu.getMenuComponentCount()-1; i>=0 ; --i) {
            Component c = jBlockMenu.getMenuComponent(i);
            if (c instanceof JSeparator)
                break;
            jBlockMenu.remove(i);
        }
        jInsertMacroMenu.removeAll();
        for (String ms : synTree.getModelList()) {
            //createNewGraphTab(ms);
            
            javax.swing.JMenuItem m = new javax.swing.JMenuItem();
            m.setText(ms);
            m.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menuPressedProdName(evt);
                }
            });
            jBlockMenu.add(m);
            // Add to Insert->Macro call menu
            m = new javax.swing.JMenuItem();
            m.setText(ms);
            m.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menuPressedInsertMacro(evt);
                }
            });
            jInsertMacroMenu.add(m);
        }
    }
    
    /**
     *  Create a menu item for each SyntaxTree.
     **/
    private void updateInsertSoundMenu() {
        jInsertSoundMenu.removeAll();
        for (String snd : new TreeSet<String>(synTree.getSoundList())) {
            javax.swing.JMenuItem m = new javax.swing.JMenuItem();
            m.setText(snd);
            m.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menuPressedInsertSound(evt);
                }
            });
            jInsertSoundMenu.add(m);
        }
    }
    
    private void layoutGraph(JGraph j) {
        MacroModel model = (MacroModel)(j.getModel());
        int i;
        int x = 50;
        int y = 50;
        for (MacroModel.GenericVertexCell cell : model.getRootVertices()) {
            y = layoutCellView(j, cell, x, y);
            y += layout_dy;
        }
    }
    
    /** Layout a cell at x,y and all descendants as a tree beneath it.
     * return the last used y value.
     */
    private int layoutCellView(JGraph j, GraphCell cell, int x, int y) {
        GraphLayoutCache cache = j.getGraphLayoutCache();
        MacroModel model = (MacroModel)(j.getModel());
        final int dx=130;
        
        // Set up an undoable transaction
        model.beginUpdate();
        
        // Create Nested Map (from Cells to Attributes)
        //
        Map<GraphCell, Map<Object, Object>> attributes = new Hashtable<GraphCell, Map<Object,Object>>();
        
        // Create Vertex Attributes
        //
        Map<Object,Object> vertexAttrib = new Hashtable<Object,Object>();
        attributes.put(cell, vertexAttrib);
        // Set bounds
        Rectangle2D vertexBounds;
        if (cell instanceof MacroModel.BooleanBranchCell) {
//            vertexBounds = new Rectangle2D.Double(x, y-20, 60, 60);
            vertexBounds = new Rectangle2D.Double(x, y-20, 100, 60);
        } else {
//            vertexBounds = new Rectangle2D.Double(x, y, 90, 20);
            vertexBounds = new Rectangle2D.Double(x, y, 100, 20);
        }
        GraphConstants.setBounds(vertexAttrib, vertexBounds);
        cache.edit(attributes, null, null, null);
        
        // Layout the children
        if (cell instanceof MacroModel.BooleanBranchCell) {
            GraphCell trueCell  = model.findBooleanTarget(cell, true);
            GraphCell falseCell = model.findBooleanTarget(cell, false);
            if (trueCell != null) {
                y = layoutCellView(j, trueCell, x+dx, y);
            }
            y += layout_dy;
            if (falseCell != null) {
                if (falseCell instanceof MacroModel.BooleanBranchCell) {
                    y = layoutCellView(j, falseCell, x, y);
                } else {
                    y = layoutCellView(j, falseCell, x+dx, y);
                }
            }
        } else if (cell instanceof MacroModel.GenericVertexCell) {
            MacroModel.GenericVertexCell gcell = (MacroModel.GenericVertexCell) cell;
            boolean firstChild = true;
            for (MacroModel.GenericVertexCell childCell : model.getDescendantVertices(gcell)) {
                if (! firstChild) {
                    y += layout_dy;
                }
                y = layoutCellView(j, childCell, x+dx, y);
                firstChild = false;
            }
        }
        
        // End the undoable transaction
        model.endUpdate();
        
        
        return y;
    }
    
    /** Handle menu selection of a Prod name
     */
    private void menuPressedProdName(java.awt.event.ActionEvent evt) {
        gotoProdModel(evt.getActionCommand());
    }
    
    /** Handle menu to insert a sound clip into the graph
     */
    private void menuPressedInsertSound(java.awt.event.ActionEvent evt) {
        MacroModel model = getCurrentMacroModel();
        if (model == null) {
            return;
        }
        model.insertSoundClipVertex(evt.getActionCommand(), 10, 10);
    }
    
    /** Handle menu to insert a  macro call into the graph
     */
    private void menuPressedInsertMacro(java.awt.event.ActionEvent evt) {
        MacroModel model = getCurrentMacroModel();
        if (model == null) {
            return;
        }
        model.insertMacroVertex(evt.getActionCommand(), 10, 10);
    }
    
    /** Go to the tab of a specified Prod name, possibly
     *creating the tab first.
     */
    public void gotoProdModel(String prodName) {
        Component tabbedGraph = graphViews.get(prodName);
        if (tabbedGraph == null) {
            createNewGraphTab(prodName);
            tabbedGraph = graphViews.get(prodName);
            if (tabbedGraph == null) {
                // No such macro
                return;
            }
        }
        jTabbedPane1.setSelectedComponent(tabbedGraph);
        
    }
    
    /** Delete a specific tab holding a Prod block editor
     */
    private void closeCurrentGraphTab() {
        java.awt.Component c = jTabbedPane1.getSelectedComponent();
        if (c == null) {
            return;
        }
        Iterator<Map.Entry<String,JScrollPane>> it = graphViews.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,JScrollPane> entry = it.next();
            if (entry.getValue() == c) {
                jTabbedPane1.remove(c);
                Component view = entry.getValue().getViewport().getView();
                if (view instanceof JGraph) {
                    GraphModel model = ((JGraph)view).getModel();
                    if (model instanceof MacroModel) {
                        ((MacroModel)model).setStatusManager(null);
                    }
                }
                it.remove();
                return;
            }
        }
    }
    
    /** Layout the graph in the current tab holding a Prod block editor
     */
    private void layoutCurrentGraphTab() {
        JGraph graph = getCurrentMacroGraph();
        if (graph != null) {
            layoutGraph(graph);
        }
    }
    
    private JGraph getCurrentMacroGraph() {
        Component c = jTabbedPane1.getSelectedComponent();
        if ( ! (c instanceof JScrollPane))
            return null;
        Component c2= ((JScrollPane)c).getViewport().getView();
        if (c2 instanceof JGraph)
            return (JGraph)c2;
        return null;
    }
    
    private MacroModel getCurrentMacroModel() {
        JGraph g = getCurrentMacroGraph();
        if (g == null) {
            return null;
        }
        GraphModel m = g.getModel();
        if (m instanceof MacroModel) {
            return (MacroModel)m;
        }
        return null;
    }
    
    public void setStatusMessage(String msg) {
        jStatusBar.setText(msg);
    }
    public void clearStatusMessage() {
        jStatusBar.setText("");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jDistanceTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jSoundTable = new javax.swing.JTable();
        jStatusBar = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jFileMenu = new javax.swing.JMenu();
        jSemanticCheckMenuItem = new javax.swing.JMenuItem();
        jPropertiesMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jLoadItem = new javax.swing.JMenuItem();
        jSaveItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jExitMenuItem = new javax.swing.JMenuItem();
        jEditMEnu = new javax.swing.JMenu();
        jDeleteItem = new javax.swing.JMenuItem();
        jUndoMenuItem = new javax.swing.JMenuItem();
        jRedoMenuItem = new javax.swing.JMenuItem();
        jCutMenuItem = new javax.swing.JMenuItem();
        jPasteMenuItem = new javax.swing.JMenuItem();
        jGuideViewMenuItem = new javax.swing.JMenuItem();
        jBlockMenu = new javax.swing.JMenu();
        jLayoutProdlMenuItem = new javax.swing.JMenuItem();
        jCloseTabMenuItem = new javax.swing.JMenuItem();
        jNewProdMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jInsertMenu = new javax.swing.JMenu();
        jInsertSoundMenu = new javax.swing.JMenu();
        jInsertMacroMenu = new javax.swing.JMenu();
        jInsertConditionalMenuItem = new javax.swing.JMenuItem();
        jInsertReturnItem = new javax.swing.JMenuItem();
        jInsertFailMenuItem = new javax.swing.JMenuItem();
        jInsertTryCatchMenuItem = new javax.swing.JMenuItem();
        jInsertTimingMarkerItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Block editor");
        jDistanceTable.setModel(synTree.getDistanceTableModel());
        jScrollPane1.setViewportView(jDistanceTable);

        jTabbedPane1.addTab("[Distance Table]", jScrollPane1);

        jSoundTable.setModel(synTree.getSoundTableModel());
        jScrollPane2.setViewportView(jSoundTable);

        jTabbedPane1.addTab("[Sound Table]", jScrollPane2);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jStatusBar.setText("StatusBar");
        jStatusBar.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jStatusBar.setFocusable(false);
        jStatusBar.setRequestFocusEnabled(false);
        jStatusBar.setVerifyInputWhenFocusTarget(false);
        getContentPane().add(jStatusBar, java.awt.BorderLayout.SOUTH);

        jFileMenu.setText("File");
        jSemanticCheckMenuItem.setText("Semantic check");
        jSemanticCheckMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSemanticCheckMenuItemActionPerformed(evt);
            }
        });

        jFileMenu.add(jSemanticCheckMenuItem);

        jPropertiesMenuItem.setText("Properties");
        jPropertiesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPropertiesMenuItemActionPerformed(evt);
            }
        });

        jFileMenu.add(jPropertiesMenuItem);

        jFileMenu.add(jSeparator3);

        jLoadItem.setText("Load");
        jLoadItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLoadItemActionPerformed(evt);
            }
        });

        jFileMenu.add(jLoadItem);

        jSaveItem.setText("Save");
        jSaveItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveItemActionPerformed(evt);
            }
        });

        jFileMenu.add(jSaveItem);

        jFileMenu.add(jSeparator1);

        jExitMenuItem.setText("Exit");
        jExitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jExitMenuItemActionPerformed(evt);
            }
        });

        jFileMenu.add(jExitMenuItem);

        jMenuBar1.add(jFileMenu);

        jEditMEnu.setText("Edit");
        jDeleteItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        jDeleteItem.setText("Delete");
        jDeleteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDeleteItemActionPerformed(evt);
            }
        });

        jEditMEnu.add(jDeleteItem);

        jUndoMenuItem.setText("Undo");
        jUndoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jUndoMenuItemActionPerformed(evt);
            }
        });

        jEditMEnu.add(jUndoMenuItem);

        jRedoMenuItem.setText("Redo");
        jEditMEnu.add(jRedoMenuItem);

        jCutMenuItem.setText("Cut");
        jCutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCutMenuItemActionPerformed(evt);
            }
        });

        jEditMEnu.add(jCutMenuItem);

        jPasteMenuItem.setText("Paste");
        jPasteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPasteMenuItemActionPerformed(evt);
            }
        });

        jEditMEnu.add(jPasteMenuItem);

        jGuideViewMenuItem.setText("Show Guide view");
        jGuideViewMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jGuideViewMenuItemActionPerformed(evt);
            }
        });

        jEditMEnu.add(jGuideViewMenuItem);

        jMenuBar1.add(jEditMEnu);

        jBlockMenu.setText("Macro");
        jLayoutProdlMenuItem.setText("Arrange graph");
        jLayoutProdlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLayoutProdlMenuItemActionPerformed(evt);
            }
        });

        jBlockMenu.add(jLayoutProdlMenuItem);

        jCloseTabMenuItem.setText("Close macro");
        jCloseTabMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCloseTabMenuItemActionPerformed(evt);
            }
        });

        jBlockMenu.add(jCloseTabMenuItem);

        jNewProdMenuItem.setText("Create macro");
        jNewProdMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jNewProdMenuItemActionPerformed(evt);
            }
        });

        jBlockMenu.add(jNewProdMenuItem);

        jBlockMenu.add(jSeparator2);

        jMenuBar1.add(jBlockMenu);

        jInsertMenu.setText("Insert");
        jInsertSoundMenu.setText("Sounds");
        jInsertMenu.add(jInsertSoundMenu);

        jInsertMacroMenu.setText("Macro call");
        jInsertMenu.add(jInsertMacroMenu);

        jInsertConditionalMenuItem.setText("Conditional");
        jInsertConditionalMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jInsertConditionalMenuItemActionPerformed(evt);
            }
        });

        jInsertMenu.add(jInsertConditionalMenuItem);

        jInsertReturnItem.setText("Return");
        jInsertReturnItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jInsertReturnItemActionPerformed(evt);
            }
        });

        jInsertMenu.add(jInsertReturnItem);

        jInsertFailMenuItem.setText("Fail");
        jInsertFailMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jInsertFailMenuItemActionPerformed(evt);
            }
        });

        jInsertMenu.add(jInsertFailMenuItem);

        jInsertTryCatchMenuItem.setText("Try/Catch");
        jInsertTryCatchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jInsertTryCatchMenuItemActionPerformed(evt);
            }
        });

        jInsertMenu.add(jInsertTryCatchMenuItem);

        jInsertTimingMarkerItem.setText("Timing marker");
        jInsertTimingMarkerItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jInsertTimingMarkerItemActionPerformed(evt);
            }
        });

        jInsertMenu.add(jInsertTimingMarkerItem);

        jMenuBar1.add(jInsertMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }//GEN-END:initComponents
    
    private void jPropertiesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPropertiesMenuItemActionPerformed
        new ProjSettings(synTree).setVisible(true);
    }//GEN-LAST:event_jPropertiesMenuItemActionPerformed
    
    private void jGuideViewMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jGuideViewMenuItemActionPerformed
        if (guideView == null) {
            guideView = new GuideView(synTree);
            guideView.setVisible(true);
        } else {
        }
    }//GEN-LAST:event_jGuideViewMenuItemActionPerformed
    
    private void jPasteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasteMenuItemActionPerformed
        JGraph graph = getCurrentMacroGraph();
        if (graph == null) {
            return;
        }
        Action action = graph.getTransferHandler().getPasteAction();
        ActionEvent e = new ActionEvent(graph, evt.getID(),
                evt.getActionCommand(), evt.getModifiers());
        action.actionPerformed(evt);
    }//GEN-LAST:event_jPasteMenuItemActionPerformed
    
    private void jCutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCutMenuItemActionPerformed
        JGraph graph = getCurrentMacroGraph();
        if (graph == null) {
            return;
        }
        TransferHandler h = graph.getTransferHandler();
        Action action = graph.getTransferHandler().getCutAction();
        ActionEvent e = new ActionEvent(graph, evt.getID(),
                evt.getActionCommand(), evt.getModifiers());
        action.actionPerformed(evt);
    }//GEN-LAST:event_jCutMenuItemActionPerformed
    
    private void jUndoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jUndoMenuItemActionPerformed
        MacroModel m = getCurrentMacroModel();
        if (m == null) {
            return;
        }
        try {
            undoManager.undo(m);
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            updateHistoryButtons();
        }
    }//GEN-LAST:event_jUndoMenuItemActionPerformed
    
    private void jInsertTimingMarkerItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jInsertTimingMarkerItemActionPerformed
        MacroModel m = getCurrentMacroModel();
        if (m == null) {
            return;
        }
        GraphCell v = m.insertTimingMarkerVertex(100,100);
    }//GEN-LAST:event_jInsertTimingMarkerItemActionPerformed
    
    private void jInsertTryCatchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jInsertTryCatchMenuItemActionPerformed
        MacroModel m = getCurrentMacroModel();
        if (m == null) {
            return;
        }
        GraphCell v = m.insertTryCatchVertex(100,100);
    }//GEN-LAST:event_jInsertTryCatchMenuItemActionPerformed
    
    private void jInsertFailMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jInsertFailMenuItemActionPerformed
        MacroModel m = getCurrentMacroModel();
        if (m == null) {
            return;
        }
        GraphCell v = m.insertFailVertex(100,100);
    }//GEN-LAST:event_jInsertFailMenuItemActionPerformed
    
    private void jInsertConditionalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jInsertConditionalMenuItemActionPerformed
        MacroModel m = getCurrentMacroModel();
        if (m == null) {
            return;
        }
        GraphCell v = m.insertBooleanVertex("???=?",100,100);
    }//GEN-LAST:event_jInsertConditionalMenuItemActionPerformed
    
    private void jNewProdMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jNewProdMenuItemActionPerformed
        String newName = JOptionPane.showInputDialog(this, "Name of new macro block?",
                "New macro", JOptionPane.QUESTION_MESSAGE);
        if (newName == null)
            return;
        String errorMsg = synTree.addProdModel(newName);
        if (errorMsg == null) {
            gotoProdModel(newName);
        } else {
            JOptionPane.showMessageDialog(this, errorMsg,
                    "Failed to create new macro",JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("Got new name: " + newName);
    }//GEN-LAST:event_jNewProdMenuItemActionPerformed
    
    private void jLoadItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoadItemActionPerformed
        int res = fc.showOpenDialog(null);
        if (res != JFileChooser.APPROVE_OPTION)
            return;
        File f = fc.getSelectedFile();
        if ( ! f.exists()) {
            JOptionPane.showMessageDialog(null, "File does not exist",
                    "No such file", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            new SyntaxEditor(f, fc).setVisible(true);
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(null, "Failed to parse xml file",
                    "File corrupt", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jLoadItemActionPerformed
    
    private void jSaveItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveItemActionPerformed
        while (true) {
            int res = fc.showSaveDialog(null);
            if (res != JFileChooser.APPROVE_OPTION)
                return;
            File f = fc.getSelectedFile();
            if (f.exists()) {
                int res2 = JOptionPane.showConfirmDialog(null,
                        "Overwrite?",
                        "File already exists",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (res2 == JOptionPane.NO_OPTION)
                    continue;
                if (res2 == JOptionPane.CANCEL_OPTION)
                    return;
            }
            String filter = fc.getFileFilter().getDescription();
            String fname = f.getName();
            String xml = Utils.synxml;
            String syn = Utils.syn;
            if (filter.endsWith("(xml)") ||
                    ((filter.equals(fc.getAcceptAllFileFilter().getDescription())) &&
                    fname.endsWith("." + xml))) {
                try {
                    synTree.saveSyntaxAsXML(fc.getSelectedFile());
                } catch (SAXException t) {
                    JOptionPane.showMessageDialog(null, "Failed to save the file",
                            "Save failed", JOptionPane.ERROR_MESSAGE);
                }
            } else if (filter.endsWith("(compiled)") ||
                    ((filter.equals(fc.getAcceptAllFileFilter().getDescription())) &&
                    fname.endsWith("." + syn))) {
                try {
                    synTree.saveSyntaxAsCompiled(fc.getSelectedFile());
                } catch (SAXException t) {
                    JOptionPane.showMessageDialog(null, "Failed to save the file",
                            "Save failed", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                    JOptionPane.showMessageDialog(null, "Failed to save the file",
                            "Unable to determine file format.", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
    }//GEN-LAST:event_jSaveItemActionPerformed
    
    private void jDeleteItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDeleteItemActionPerformed
        JGraph g = getCurrentMacroGraph();
        g.getGraphLayoutCache().remove(g.getSelectionCells(),true,false);
    }//GEN-LAST:event_jDeleteItemActionPerformed
    
    private void jInsertReturnItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jInsertReturnItemActionPerformed
        MacroModel m = getCurrentMacroModel();
        if (m == null) {
            return;
        }
        GraphCell v = m.insertReturnVertex(100,100);
    }//GEN-LAST:event_jInsertReturnItemActionPerformed
    
    private void jSemanticCheckMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSemanticCheckMenuItemActionPerformed
        synTree.isValidSemantically();
    }//GEN-LAST:event_jSemanticCheckMenuItemActionPerformed
    
    private void jLayoutProdlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLayoutProdlMenuItemActionPerformed
        layoutCurrentGraphTab();
    }//GEN-LAST:event_jLayoutProdlMenuItemActionPerformed
    
    private void jExitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jExitMenuItemActionPerformed
        for (java.awt.Frame f : java.awt.Frame.getFrames()) {
            //f.dispose();
            f.dispatchEvent(new java.awt.event.WindowEvent(f, java.awt.event.WindowEvent.WINDOW_CLOSING));
        }
    }//GEN-LAST:event_jExitMenuItemActionPerformed
    
    private void jCloseTabMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCloseTabMenuItemActionPerformed
        closeCurrentGraphTab();
    }//GEN-LAST:event_jCloseTabMenuItemActionPerformed
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jBlockMenu;
    private javax.swing.JMenuItem jCloseTabMenuItem;
    private javax.swing.JMenuItem jCutMenuItem;
    private javax.swing.JMenuItem jDeleteItem;
    private javax.swing.JTable jDistanceTable;
    private javax.swing.JMenu jEditMEnu;
    private javax.swing.JMenuItem jExitMenuItem;
    private javax.swing.JMenu jFileMenu;
    private javax.swing.JMenuItem jGuideViewMenuItem;
    private javax.swing.JMenuItem jInsertConditionalMenuItem;
    private javax.swing.JMenuItem jInsertFailMenuItem;
    private javax.swing.JMenu jInsertMacroMenu;
    private javax.swing.JMenu jInsertMenu;
    private javax.swing.JMenuItem jInsertReturnItem;
    private javax.swing.JMenu jInsertSoundMenu;
    private javax.swing.JMenuItem jInsertTimingMarkerItem;
    private javax.swing.JMenuItem jInsertTryCatchMenuItem;
    private javax.swing.JMenuItem jLayoutProdlMenuItem;
    private javax.swing.JMenuItem jLoadItem;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jNewProdMenuItem;
    private javax.swing.JMenuItem jPasteMenuItem;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JMenuItem jPropertiesMenuItem;
    private javax.swing.JMenuItem jRedoMenuItem;
    private javax.swing.JMenuItem jSaveItem;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JMenuItem jSemanticCheckMenuItem;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTable jSoundTable;
    private javax.swing.JLabel jStatusBar;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JMenuItem jUndoMenuItem;
    // End of variables declaration//GEN-END:variables
    
}
