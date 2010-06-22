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

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.Edge;
import org.jgraph.graph.AttributeMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.Color;
import javax.swing.BorderFactory;
import java.util.Set;
import java.util.HashSet;
import javax.swing.tree.TreeNode;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class MacroModel extends org.jgraph.graph.DefaultGraphModel{
    
    public class BasicPort extends DefaultPort {
        protected boolean canBeSource;
        protected boolean canBeTarget;
        public BasicPort(boolean isSrc, boolean isTrg) {
            canBeSource = isSrc;
            canBeTarget = isTrg;
        };
        public BasicPort(Object userObject, boolean isSrc, boolean isTrg) {
            super(userObject);
            canBeSource = isSrc;
            canBeTarget = isTrg;
        }
        public boolean isTarget() { return canBeTarget; }
        public boolean isSource() { return canBeSource; }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    public class BoolSourcePort extends BasicPort {
        final private boolean value;
        public BoolSourcePort(boolean v, String portName) {
            super(portName, true, false);
            value = v;
        }
        public boolean getValue() {
            return value;
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    
    public class GenericVertexCell extends DefaultGraphCell {
        public GenericVertexCell(String name) {
            super(name);
        }
        public String getCellDescription() {
            return "GenericVertexCell" + getUserObject().toString();
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    public class ProcessCell extends GenericVertexCell {
        public ProcessCell(String name) {
            super(name);
        }
        public String getCellDescription() {
            return "ProcessCell" + getUserObject().toString();
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    public class TimingMarkerCell extends ProcessCell {
        public TimingMarkerCell() {
            super("TimingMarker");
        }
        public String getCellDescription() {
            return "TimingMarker";
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    public class PlayClipCell extends ProcessCell {
        public PlayClipCell(String name) {
            super(name);
        }
        public String getClipName() {
            return getUserObject().toString();
        }
        public String getCellDescription() {
            return "Clip "+getClipName();
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    public class SetXingCell extends ProcessCell {
        public SetXingCell(int xing) {
            super("SelectXing(" + xing + ")");
        }
        public int getXingNo() {
            String tmp = getUserObject().toString();
            tmp = tmp.substring("SelectXing(".length(), tmp.length()-1);
            return Integer.decode(tmp);
        }
        public String getCellDescription() {
            return "Set Xing= "+getXingNo();
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    public class StartCell extends ProcessCell {
        public StartCell(String name) {
            super(name);
        }
        public String getCellDescription() {
            return "Start";
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    public class MacroCell extends ProcessCell {
        public MacroCell(String name) {
            super(name);
        }
        public String getMacroTarget() { return (String)getUserObject(); }
        public String getCellDescription() {
            return "Macro call:"+getMacroTarget();
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    public class ReturnCell extends ProcessCell {
        public ReturnCell() {
            super("continue");
        }
        public String getCellDescription() {
            return "Return";
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    public class FailCell extends ProcessCell {
        public FailCell() {
            super("fail");
        }
        public String getCellDescription() {
            return "Fail-return";
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    public class BooleanBranchCell extends GenericVertexCell {
        public BooleanBranchCell(String name) {
            super(name);
        }
        public Condition getCondition() {
            return new Condition(getUserObject().toString());
        }
        public String getCellDescription() {
            return "Branch with cond:" + getCondition().toString();
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    public class TryCatchCell extends BooleanBranchCell {
        public TryCatchCell() {
            super("TRY");
        }
        public String getCellDescription() {
            return "Try/catch";
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    
    
    /** An edge class that gets it's label from the source
     *port it is connected to. */
    public class BasicEdge extends DefaultEdge {
        public BasicEdge()                  { super(); }
        public BasicEdge(Object userObject) { super(userObject); }
        public BasicEdge(Object userObject, AttributeMap storageMap) {
            super(userObject, storageMap);
        }
        public void setSource(Object port) {
            super.setSource(port);
            if (port instanceof BoolSourcePort) {
                setUserObject(((BoolSourcePort)port).getUserObject());
            } else {
                setUserObject("");
            }
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    
    private final boolean useFloatingPorts = true;
    private String name;
    private SemanticChecker semantic;
    private StartCell startVertex;
    private StatusManager statusManager;
    
    /** Creates a new instance of ProdModel */
    public MacroModel(String prodName, SemanticChecker semCheck) {
        name = prodName;
        semantic = semCheck;
        
        startVertex = insertStartVertex(name, 20, 20);
        statusManager = null;
    }
    
    public void setStatusManager(StatusManager mgr) {
        statusManager = mgr;
    }
    
    /** Insert the incoming and outgoing ports of a process vertex. */
    protected void insertProcessPorts(GenericVertexCell vertex,
            Map<GraphCell, Map<Object, Object>> attributes,
            boolean hasSource,
            boolean hasTarget) {
        // Add in and out ports
        // Start with the source or combined port
        if (hasSource || (useFloatingPorts && hasTarget)) {
            DefaultPort hp;
            if (! useFloatingPorts || !hasTarget) {
                hp = new BasicPort(true, false);
            } else if (hasTarget && hasSource) {
                hp = new BasicPort(true, true);
            } else {
                hp = new BasicPort(false, true);
            }
            Map<Object,Object> portAttrib = new Hashtable<Object,Object>();
            attributes.put(hp, portAttrib);
            if (! useFloatingPorts) {
                GraphConstants.setOffset(portAttrib, new Point2D.Double(1*GraphConstants.PERMILLE,0.5*GraphConstants.PERMILLE));
            }
            vertex.add(hp);
        }
        if (! useFloatingPorts && hasTarget) {
            // Target port
            DefaultPort hp;
            hp = new BasicPort(false, true);
            Map<Object,Object> portAttrib = new Hashtable<Object,Object>();
            attributes.put(hp, portAttrib);
            GraphConstants.setOffset(portAttrib, new Point2D.Double(0*GraphConstants.PERMILLE,0.5*GraphConstants.PERMILLE));
            vertex.add(hp);
        }
        
        
    }
    
    /** Inserts a start vertex into the model. Only called
     *  from the constructor.
     *  FIXME - enforce that constraint */
    public StartCell insertStartVertex(String name, int x, int y) {
        final int dx = 50;
        final int dy = 20;
        
        // Create Nested Map (from Cells to Attributes)
        //
        Map<GraphCell, Map<Object, Object>> attributes = new Hashtable<GraphCell, Map<Object,Object>>();
        
        // Create Start Vertex
        //
        StartCell vertex = new StartCell(name);
        
        // Create Vertex Attributes
        //
        Map<Object,Object> vertexAttrib = new Hashtable<Object,Object>();
        attributes.put(vertex, vertexAttrib);
        // Set bounds
        Rectangle2D vertexBounds = new Rectangle2D.Double(x, y, dx, dy);
        GraphConstants.setBounds(vertexAttrib, vertexBounds);
        // Set fill color
        GraphConstants.setBackground(vertexAttrib, Color.orange);
        GraphConstants.setOpaque(vertexAttrib, true);
        // Set raised border
        GraphConstants.setBorder(vertexAttrib,
                BorderFactory.createRaisedBevelBorder());
        
        
        // Create the ports
        insertProcessPorts(vertex, attributes, true, false);
        
        // Insert into Model
        //
        Object[] cells = new Object[]{vertex};
        insert(cells, attributes, null, null, null);
        
        return vertex;
    }
    
    /** Inserts a process vertex into the model.
     */
    private GenericVertexCell insertProcessVertex(GenericVertexCell vertex, int x, int y) {
        final int dx = 50;
        final int dy = 20;
        
        // Create Nested Map (from Cells to Attributes)
        //
        Map<GraphCell, Map<Object, Object>> attributes = new Hashtable<GraphCell, Map<Object,Object>>();
        
        // Create Vertex Attributes
        //
        Map<Object,Object> vertexAttrib = new Hashtable<Object,Object>();
        attributes.put(vertex, vertexAttrib);
        // Set bounds
        Rectangle2D vertexBounds = new Rectangle2D.Double(x, y, dx, dy);
        GraphConstants.setBounds(vertexAttrib, vertexBounds);
        // Set black border
        GraphConstants.setBorderColor(vertexAttrib, Color.black);
        // No editing allowed
        GraphConstants.setEditable(vertexAttrib, false);
        
        // Create the ports
        insertProcessPorts(vertex, attributes, true, true);
        
        // Insert into Model
        //
        Object[] cells = new Object[]{vertex};
        insert(cells, attributes, null, null, null);
        
        return vertex;
    }
    
    public GenericVertexCell insertSoundClipVertex(String name, int x, int y) {
        return insertProcessVertex(new PlayClipCell(name), x, y);
    }
    
    public GenericVertexCell insertTimingMarkerVertex(int x, int y) {
        return insertProcessVertex(new TimingMarkerCell(), x, y);
    }
    
    public GenericVertexCell insertSetXingVertex(int xing, int x, int y) {
        return insertProcessVertex(new SetXingCell(xing),x ,y);
    }
    
    /** Inserts a macro vertex into the model.
     */
    public GenericVertexCell insertMacroVertex(String name, int x, int y) {
        final int dx = 50;
        final int dy = 20;
        
        // Create Nested Map (from Cells to Attributes)
        //
        Map<GraphCell, Map<Object, Object>> attributes = new Hashtable<GraphCell, Map<Object,Object>>();
        
        // Create Start Vertex
        //
        GenericVertexCell vertex = new MacroCell(name);
        
        // Create Vertex Attributes
        //
        Map<Object,Object> vertexAttrib = new Hashtable<Object,Object>();
        attributes.put(vertex, vertexAttrib);
        // Set bounds
        Rectangle2D vertexBounds = new Rectangle2D.Double(x, y, dx, dy);
        GraphConstants.setBounds(vertexAttrib, vertexBounds);
        // Set black border
        GraphConstants.setBorderColor(vertexAttrib, Color.blue);
        // No editing allowed
        GraphConstants.setEditable(vertexAttrib, false);
        
        // Create the ports
        insertProcessPorts(vertex, attributes, true, true);
        
        // Insert into Model
        //
        Object[] cells = new Object[]{vertex};
        insert(cells, attributes, null, null, null);
        
        return vertex;
    }
    
    /** Inserts a boolean choise vertex into the model.
     */
    public BooleanBranchCell insertBooleanVertex(String name, int x, int y) {
        final int dx = 70;
        final int dy = 70;
        
        // Create Nested Map (from Cells to Attributes)
        //
        Map<GraphCell, Map<Object, Object>> attributes = new Hashtable<GraphCell, Map<Object,Object>>();
        
        // Create Start Vertex
        //
        BooleanBranchCell vertex = new BooleanBranchCell(name);
        
        // Create Vertex Attributes
        //
        Map<Object,Object> vertexAttrib = new Hashtable<Object,Object>();
        attributes.put(vertex, vertexAttrib);
        // Set bounds
        Rectangle2D vertexBounds = new Rectangle2D.Double(x, y, dx, dy);
        GraphConstants.setBounds(vertexAttrib, vertexBounds);
        // Set black border
        GraphConstants.setBorderColor(vertexAttrib, Color.black);
        
        
        // Add in and out ports
        // Start with the source ports
        DefaultPort hp = new BoolSourcePort(true, "true");
        Map<Object,Object> portAttrib = new Hashtable<Object,Object>();
        attributes.put(hp, portAttrib);
        // Never use floating ports
        GraphConstants.setOffset(portAttrib, new Point2D.Double(1*GraphConstants.PERMILLE,0.5*GraphConstants.PERMILLE));
        vertex.add(hp);
        hp = new BoolSourcePort(false, "false");
        portAttrib = new Hashtable<Object,Object>();
        attributes.put(hp, portAttrib);
        GraphConstants.setOffset(portAttrib, new Point2D.Double(0.5*GraphConstants.PERMILLE,1*GraphConstants.PERMILLE));
        vertex.add(hp);
        
        // Target port
        insertProcessPorts(vertex, attributes, false, true);
        
        // Insert into Model
        //
        Object[] cells = new Object[]{vertex};
        insert(cells, attributes, null, null, null);
        
        return vertex;
    }
    
    /** Inserts a try-catch vertex into the model.
     */
    public TryCatchCell insertTryCatchVertex(int x, int y) {
        final int dx = 70;
        final int dy = 70;
        
        // Create Nested Map (from Cells to Attributes)
        //
        Map<GraphCell, Map<Object, Object>> attributes = new Hashtable<GraphCell, Map<Object,Object>>();
        
        // Create Start Vertex
        //
        TryCatchCell vertex = new TryCatchCell();
        
        // Create Vertex Attributes
        //
        Map<Object,Object> vertexAttrib = new Hashtable<Object,Object>();
        attributes.put(vertex, vertexAttrib);
        // Set bounds
        Rectangle2D vertexBounds = new Rectangle2D.Double(x, y, dx, dy);
        GraphConstants.setBounds(vertexAttrib, vertexBounds);
        // Set black border
        GraphConstants.setBorderColor(vertexAttrib, Color.black);
        
        
        // Add in and out ports
        // Start with the source ports
        DefaultPort hp = new BoolSourcePort(true, "");
        Map<Object,Object> portAttrib = new Hashtable<Object,Object>();
        attributes.put(hp, portAttrib);
        // Never use floating ports
        GraphConstants.setOffset(portAttrib, new Point2D.Double(1*GraphConstants.PERMILLE,0.5*GraphConstants.PERMILLE));
        vertex.add(hp);
        hp = new BoolSourcePort(false, "failed");
        portAttrib = new Hashtable<Object,Object>();
        attributes.put(hp, portAttrib);
        GraphConstants.setOffset(portAttrib, new Point2D.Double(0.5*GraphConstants.PERMILLE,1*GraphConstants.PERMILLE));
        vertex.add(hp);
        
        // Target port
        insertProcessPorts(vertex, attributes, false, true);
        
        // Insert into Model
        //
        Object[] cells = new Object[]{vertex};
        insert(cells, attributes, null, null, null);
        
        return vertex;
    }
    
    /** Inserts a return vertex into the model.
     */
    public ReturnCell insertReturnVertex(int x, int y) {
        final int dx = 50;
        final int dy = 20;
        
        // Create Nested Map (from Cells to Attributes)
        //
        Map<GraphCell, Map<Object, Object>> attributes = new Hashtable<GraphCell, Map<Object,Object>>();
        
        // Create Start Vertex
        //
        ReturnCell vertex = new ReturnCell();
        
        // Create Vertex Attributes
        //
        Map<Object,Object> vertexAttrib = new Hashtable<Object,Object>();
        attributes.put(vertex, vertexAttrib);
        // Set bounds
        Rectangle2D vertexBounds = new Rectangle2D.Double(x, y, dx, dy);
        GraphConstants.setBounds(vertexAttrib, vertexBounds);
        // Set black border
        GraphConstants.setBorderColor(vertexAttrib, Color.black);
        // No editing allowed
        GraphConstants.setEditable(vertexAttrib, false);
        
        // Target port
        insertProcessPorts(vertex, attributes, false, true);
        
        // Insert into Model
        //
        Object[] cells = new Object[]{vertex};
        insert(cells, attributes, null, null, null);
        
        return vertex;
    }
    
    /** Inserts a fail vertex into the model.
     */
    public FailCell insertFailVertex(int x, int y) {
        final int dx = 50;
        final int dy = 20;
        
        // Create Nested Map (from Cells to Attributes)
        //
        Map<GraphCell, Map<Object, Object>> attributes = new Hashtable<GraphCell, Map<Object,Object>>();
        
        // Create Start Vertex
        //
        FailCell vertex = new FailCell();
        
        // Create Vertex Attributes
        //
        Map<Object,Object> vertexAttrib = new Hashtable<Object,Object>();
        attributes.put(vertex, vertexAttrib);
        // Set bounds
        Rectangle2D vertexBounds = new Rectangle2D.Double(x, y, dx, dy);
        GraphConstants.setBounds(vertexAttrib, vertexBounds);
        // Set black border
        GraphConstants.setBorderColor(vertexAttrib, Color.black);
        // No editing allowed
        GraphConstants.setEditable(vertexAttrib, false);
        
        // Target port
        insertProcessPorts(vertex, attributes, false, true);
        
        // Insert into Model
        //
        Object[] cells = new Object[]{vertex};
        insert(cells, attributes, null, null, null);
        
        return vertex;
    }
    
    /** Create an edge between two vertices. This will use the
     *first source port found. For vetices with several source ports
     *such as branches the port-taking overloaded version should be used.
     */
    public DefaultEdge insertEdge(DefaultPort sourcePort, DefaultPort targetPort) {
        if (!contains(sourcePort) || !contains(targetPort)) {
            return null;
        }
        // Allow only one source edge.
        Object edges[] = getOutgoingEdges(this, sourcePort);
        if (edges.length != 0) {
            return null;
        }
        
        // Create Nested Map (from Cells to Attributes)
        //
        Map<GraphCell, Map<Object, Object>> attributes = new Hashtable<GraphCell, Map<Object,Object>>();
        
        // Create Edge
        //
        DefaultEdge edge = new BasicEdge();
        
        // Create Edge Attributes
        //
        Map<Object,Object> edgeAttrib = new Hashtable<Object,Object>();
        attributes.put(edge, edgeAttrib);
        // Set Arrow
        int arrow = GraphConstants.ARROW_CLASSIC;
        GraphConstants.setLineEnd(edgeAttrib , arrow);
        GraphConstants.setEndFill(edgeAttrib, true);
        
        // Connect Edge
        //
        ConnectionSet cs = new ConnectionSet(edge, sourcePort, targetPort);
        
        // Insert into Model
        //
        Object[] cells = new Object[]{edge};
        insert(cells, attributes, cs, null, null);
        return edge;
    }
    
    /** Create an edge between two vertices. This will use the
     *first source port found. For vetices with several source ports
     *such as branches the port-taking overloaded version should be used.
     */
    public DefaultEdge insertEdge(GraphCell source, GraphCell target) {
        if (!contains(source) || !contains(target)) {
            return null;
        }
        DefaultPort sourcePort = findFirstOutgoingPort(source);
        DefaultPort targetPort = findIncomingPort(target);
        if (sourcePort == null || targetPort == null) {
            return null;
        }
        return insertEdge(sourcePort, targetPort);
    }
    
    
    /** Create an edge from a BooleanBranchCell to any GenericVertexCell
     * with a target port. */
    public DefaultEdge insertBooleanEdge(BooleanBranchCell source,
            boolean val,
            GraphCell target) {
        DefaultPort sourcePort = findBooleanOutgoingPort(source, val);
        if (sourcePort == null) return null;
        DefaultPort targetPort = findIncomingPort(target);
        if (targetPort == null) return null;
        DefaultEdge edge = insertEdge(sourcePort, targetPort);
        //edge.setUserObject(val ? "true" : "false");
        return edge;
    }
    
    /** Return a Set of all source-less vertices in the model.
     */
    public Set<GenericVertexCell> getRootVertices() {
        Set<GenericVertexCell> retval = new HashSet<GenericVertexCell>();
        Object cells[] = getAll(this);
        int i;
        for (i=0; i<cells.length; ++i) {
            if (cells[i] instanceof GenericVertexCell) {
                GenericVertexCell cell = (GenericVertexCell)(cells[i]);
                if (getIncomingEdges(this, cell).length == 0) {
                    retval.add(cell);
                }
            }
        }
        return retval;
    }
    
    /** Return a Set of all vertices in the model that descend from cell
     */
    public Set<GenericVertexCell> getDescendantVertices(GenericVertexCell cell) {
        Object children[] = getOutgoingEdges(this, cell);
        Set<GenericVertexCell> retval = new HashSet<GenericVertexCell>();
        int i;
        for (i=0; i<children.length; ++i) {
            if (children[i] instanceof Edge) {
                Edge edge = (Edge)(children[i]);
                if ((edge.getTarget()) instanceof TreeNode) {
                    TreeNode childPort = (TreeNode)(edge.getTarget());
                    if (childPort.getParent() instanceof GenericVertexCell) {
                        retval.add((GenericVertexCell)(childPort.getParent()));
                    }
                }
            }
        }
        return retval;
    }
    
    /** Return a Set of all macros called from this model
     */
    public Set<String> getCalledMacros() {
        Set<String> retval = new HashSet<String>();
        Object cells[] = getAll(this);
        int i;
        for (i=0; i<cells.length; ++i) {
            if (cells[i] instanceof MacroCell) {
                MacroCell cell = (MacroCell)(cells[i]);
                retval.add((String)(cell.getUserObject()));
            }
        }
        return retval;
    }
    
    
    
    /** Find the source (outgoing) port for a given vertex
     *  FIXME - expensive implementation
     */
    public BasicPort findFirstOutgoingPort(GraphCell cell) {
        for (int i=0; i<getChildCount(cell); i++) {
            Object child = getChild(cell, i);
            if (child instanceof BasicPort && ((BasicPort)child).isSource()) {
                return (BasicPort) child;
            }
        }
        return null;
    }
    
    /** Find the the requested boolean source (outgoing) port for
     *  a given vertex
     *  FIXME - expensive implementation
     */
    public BasicPort findBooleanOutgoingPort(GraphCell cell, boolean val) {
        for (int i=0; i<getChildCount(cell); i++) {
            Object child = getChild(cell, i);
            if (child instanceof BoolSourcePort) {
                if (val == ((BoolSourcePort)child).getValue()) {
                    return (BasicPort) child;
                }
            }
        }
        return null;
    }
    
    /** Find the (first) outgoing edge that matches the boolean
     *value val. Returns the cell that is the target of the edge */
    public GenericVertexCell findBooleanTarget(GraphCell cell, boolean val) {
        DefaultPort port = findBooleanOutgoingPort(cell, val);
        if (port == null)
            return null;
        Set edges = port.getEdges();
        if (edges.isEmpty())
            return null;
        DefaultEdge edge = (DefaultEdge)(edges.iterator().next());
        GraphCell targetCell = (GraphCell)(edge.getTarget());
        if (targetCell instanceof DefaultPort)
            targetCell = (DefaultGraphCell)(((DefaultPort)targetCell).getParent());
        if (targetCell instanceof GenericVertexCell) 
            return (GenericVertexCell)targetCell;
        else 
            return null;
    }
    
    /** Return the start vertex.
     */
    public StartCell getStartVertex() {
        return startVertex;
    }
    
    /** Find the source (outgoing) port for a given vertex
     *  FIXME - expensive implementation
     */
    public DefaultPort findIncomingPort(GraphCell cell) {
        for (int i=0; i<getChildCount(cell); i++) {
            Object child = getChild(cell, i);
            if (child instanceof BasicPort && ((BasicPort)child).isTarget()) {
                return (DefaultPort) child;
            }
        }
        return null;
    }
    
    protected void setStatusMsg(String msg) {
        if (statusManager != null) {
            statusManager.setStatusMessage(msg);
        }
    }
    
    protected void clearStatusMsg() {
        if (statusManager != null) {
            statusManager.clearStatusMessage();
        }
    }
    
    /** Only allow edge sources to be connected to source ports
     */
    public boolean acceptsSource(Object edge, Object port) {
        if (port == null) {
            //clearStatusMsg();
            return true;
        }
        if (port instanceof BasicPort && ((BasicPort)port).isSource()) {
            // Only one edge can be connected to a source, except
            // for the current edge, which is allowed.
            Object edges[] = getOutgoingEdges(this, port);
            for (int i = 0 ; i<edges.length ; ++i) {
                if (edges[i] != edge) {
                    setStatusMsg("Outgoing edge already attached");
                    return false;
                }
            }
            // Check this edge will not violate any invariants
            DefaultEdge realedge = (DefaultEdge)edge;
            if (realedge.getTarget() != null) {
                GenericVertexCell newSource =
                        (GenericVertexCell)(((DefaultPort)port).getParent());
                GenericVertexCell target =
                        (GenericVertexCell)(
                        ((DefaultPort)(realedge.getTarget())).getParent());
                if (semantic.isNewEdgeAllowed(this, newSource, target)) {
                    clearStatusMsg();
                    return true;
                } else {
                    setStatusMsg(semantic.getSemanticErrorDescription());
                    return false;
                }
            }
            clearStatusMsg();
            return true;
        }
        setStatusMsg("Not a source port");
        return false;
    }
    
    /** Only allow edge targets to be connected to target ports
     */
    public boolean acceptsTarget(Object edge, Object port) {
        if (port == null) {
            //clearStatusMsg();
            return true;
        }
        if (port instanceof BasicPort && ((BasicPort)port).isTarget()) {
            DefaultEdge realedge = (DefaultEdge)edge;
            if (realedge.getSource() != null) {
                GenericVertexCell newTarget =
                        (GenericVertexCell)(((DefaultPort)port).getParent());
                GenericVertexCell source =
                        (GenericVertexCell)(
                        ((DefaultPort)(realedge.getSource())).getParent());
                if ( semantic.isNewEdgeAllowed(this, source, newTarget)) {
                    clearStatusMsg();
                    return true;
                } else {
                    setStatusMsg(semantic.getSemanticErrorDescription());
                    return false;
                }
            }
            clearStatusMsg();
            return true;
        }
        setStatusMsg("Not a target port");
        return false;
    }
    
    /** Write the specified node and all descendant nodes.
     *This function is called recursivly. Return the id allocated
     *for this node. If the node is already written (already has an id)
     *th old id is returned.
     */
    protected String saveVertexAndDescendantsAsXML(XMLWriter w,
            GenericVertexCell c,
            Map<GenericVertexCell, String> knownIds)
            throws SAXException {
        String id = knownIds.get(c);
        if (id != null) {
            // Already seen node - return the last id
            return id;
        }
        if (c instanceof BooleanBranchCell) {
            GenericVertexCell trueTarg  = findBooleanTarget(c, true);
            GenericVertexCell falseTarg = findBooleanTarget(c, false);
            String trueId = null;
            String falseId = null;
            if (trueTarg != null && trueTarg instanceof GenericVertexCell) {
                trueId = saveVertexAndDescendantsAsXML(w, (GenericVertexCell)trueTarg, knownIds);
            }
            if (falseTarg != null && falseTarg instanceof GenericVertexCell) {
                falseId = saveVertexAndDescendantsAsXML(w, (GenericVertexCell)falseTarg, knownIds);
            }
            id = "Macro_"+getName()+"_"+knownIds.size();
            if (c instanceof TryCatchCell) {
                AttributesImpl a = new AttributesImpl();
                a.addAttribute("", "id", "", "", id);
                w.emitOpenElement("TryCatchCell", a);
            } else {
                // Regular BranchCell
                AttributesImpl a = new AttributesImpl();
                a.addAttribute("", "condition", "", "", c.getUserObject().toString());
                a.addAttribute("", "id", "", "", id);
                w.emitOpenElement("BooleanBranchCell", a);
            }
            if (trueId != null) {
                AttributesImpl a = new AttributesImpl();
                a.addAttribute("", "target", "", "", trueId);
                String targDescriptionComment = "Next cell: " + trueTarg.getCellDescription();
                if (c instanceof TryCatchCell) {
                    w.emitEmptyElement("TryLink", a, targDescriptionComment);
                } else {
                    w.emitEmptyElement("BooleanTrueLink", a, targDescriptionComment);
                }
            }
            if (falseId != null) {
                AttributesImpl a = new AttributesImpl();
                a.addAttribute("", "target", "", "", falseId);
                String targDescriptionComment = "Next cell: " + falseTarg.getCellDescription();
                if (c instanceof TryCatchCell) {
                    w.emitEmptyElement("CatchLink", a, targDescriptionComment);
                } else {
                    w.emitEmptyElement("BooleanFalseLink", a, targDescriptionComment);
                }
            }
            w.emitCloseElement();
        } else if (c instanceof ReturnCell) {
            id = "Macro_"+getName()+"_"+knownIds.size();
            AttributesImpl a = new AttributesImpl();
            a.addAttribute("", "id", "", "", id);
            w.emitEmptyElement("ReturnCell", a);
        } else if (c instanceof FailCell) {
            id = "Macro_"+getName()+"_"+knownIds.size();
            AttributesImpl a = new AttributesImpl();
            a.addAttribute("", "id", "", "", id);
            w.emitEmptyElement("FailCell", a);
        } else if (c instanceof ProcessCell) {
            Set<GenericVertexCell> descendants = getDescendantVertices(c);
            GenericVertexCell targ = null;
            if (descendants.size() > 0) {
                targ = descendants.iterator().next();
            }
            String targId = null;
            String targDescriptionComment = null;
            if (targ != null && targ instanceof GenericVertexCell) {
                targId = saveVertexAndDescendantsAsXML(w, (GenericVertexCell)targ, knownIds);
                targDescriptionComment = "Next cell: " + targ.getCellDescription();
            }
            id = "Macro_"+getName()+"_"+knownIds.size();
            {
                AttributesImpl a = new AttributesImpl();
                if (c instanceof MacroCell) {
                    a.addAttribute("", "macroName", "", "", c.getUserObject().toString());
                } else if (c instanceof StartCell) {
                } else if (c instanceof TimingMarkerCell) {
                } else if (c instanceof SetXingCell) {
                    int xingno = ((SetXingCell)c).getXingNo();
                    a.addAttribute("", "xingno", "", "", ""+xingno);
                } else if (c instanceof PlayClipCell) {
                    a.addAttribute("", "clipName", "", "", ((PlayClipCell)c).getClipName());
                } else {
                    // Unknown cell type - what to do? FIXME
                }
                a.addAttribute("", "id", "", "", id);
                if (c instanceof StartCell) {
                    w.emitOpenElement("StartCell", a, targDescriptionComment);
                } else if (c instanceof MacroCell) {
                    w.emitOpenElement("MacroCell", a, targDescriptionComment);
                } else if (c instanceof TimingMarkerCell) {
                    w.emitOpenElement("TimingMarker", a, targDescriptionComment);
                } else if (c instanceof SetXingCell) {
                    w.emitOpenElement("SelectXing", a, targDescriptionComment);
                } else if (c instanceof PlayClipCell) {
                    w.emitOpenElement("PlayClip", a, targDescriptionComment);
                } else {
                    // Uniknown cell type - what to do? FIXME
                }
            }
            if (targId != null) {
                AttributesImpl a = new AttributesImpl();
                a.addAttribute("", "target", "", "", targId);
                w.emitEmptyElement("Link", a);
            }
            w.emitCloseElement();
        } else {
            // Unknown cell type, what to do?
        }
        knownIds.put(c, id);
        return id;
    }
    
    public void saveAsXML(XMLWriter w, String name) throws SAXException {
        AttributesImpl a = new AttributesImpl();
        a.addAttribute("", "name", "", "", name);
        w.emitOpenElement("Macro", a, null);
        Map<GenericVertexCell, String> knownIds = new HashMap<GenericVertexCell, String>();
        for (GenericVertexCell r : getRootVertices()) {
            if (r != getStartVertex()) {
                saveVertexAndDescendantsAsXML(w, r, knownIds);
            }
        }
        saveVertexAndDescendantsAsXML(w, getStartVertex(), knownIds);
        w.emitCloseElement();
    }
    
    /** Return the name of the model. The same as the name of the
     *start vertex. */
    public String getName() {return name;}
    
    /** Shut up the compiler warnings
     */
    private static final long serialVersionUID = 42L;
}
