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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.SwingUtilities;

import org.jgraph.JGraph;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.VertexView;
import org.jgraph.graph.PortView;

import com.wayfinder.syntaxedit.MacroModel.MacroCell;

/**
 *
 * Custom MarqueeHandler from GraphEd in the JGraph examples
 */
public class EdgeAddMarquee extends BasicMarqueeHandler {
    
    // MarqueeHandler that Connects Vertices and Displays PopupMenus
    JGraph graph;
    
    SyntaxEditor ownerEditor;
    
    // Holds the Start and the Current Point
    protected Point2D start, current;
    
    // Holds the First and the Current Port
    protected PortView port, firstPort;
    
    public EdgeAddMarquee(JGraph graph, SyntaxEditor ownerEditor) {
        this.graph = graph;
        this.ownerEditor = ownerEditor;
    }
    
    // Override to Gain Control (for PopupMenu and ConnectMode)
    public boolean isForceMarqueeEvent(MouseEvent e) {
        if (e.isShiftDown())
            return false;
        // If Right Mouse Button we want to Display the PopupMenu
        if (SwingUtilities.isRightMouseButton(e))
            // Return Immediately
            return true;
        // If double-click over a macro cell, inhibit the default
        // edit action.
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()>1) {
            GraphCell vertex = getVertexAt(e.getPoint());
            if ( vertex != null && 
                    ! GraphConstants.isEditable(vertex.getAttributes()) &&
                    vertex instanceof MacroCell)
                return true;
        }
        // Find and Remember Port
        port = getSourcePortAt(e.getPoint());
        // If Port Found and in ConnectMode (=Ports Visible)
        if (port != null && graph.isPortsVisible())
            return true;
        // Else Call Superclass
        return super.isForceMarqueeEvent(e);
    }
    
    // Display PopupMenu or Remember Start Location and First Port
    public void mousePressed(final MouseEvent e) {
        // If Right Mouse Button
        if (SwingUtilities.isRightMouseButton(e)) {
            // Find Cell in Model Coordinates
            Object cell = graph.getFirstCellForLocation(e.getX(), e.getY());
            //// Create PopupMenu for the Cell
            //JPopupMenu menu = createPopupMenu(e.getPoint(), cell);
            //// Display PopupMenu
            //menu.show(graph, e.getX(), e.getY());
            // Else if in ConnectMode and Remembered Port is Valid
        } else if (	port != null && graph.isPortsVisible() &&
                graph.getModel().acceptsSource(new DefaultEdge(), port.getCell())) {
            // Remember Start Location
            start = graph.toScreen(port.getLocation(null));
            // Remember First Port
            firstPort = port;
        } else {
            // Call Superclass
            super.mousePressed(e);
        }
    }
    
    // Find Port under Mouse and Repaint Connector
    public void mouseDragged(MouseEvent e) {
        // If remembered Start Point is Valid
        if (start != null) {
            // Fetch Graphics from Graph
            Graphics g = graph.getGraphics();
            // Reset Remembered Port
            PortView newPort = getTargetPortAt(e.getPoint());
            // Check that the port is permissible
            DefaultEdge tmpEdge = new DefaultEdge();
            tmpEdge.setSource(firstPort.getCell());
            if ( newPort != null && 
                    ! graph.getModel().acceptsTarget(tmpEdge, newPort.getCell()))
                newPort = null;
            // Do not flicker (repaint only on real changes)
            if (newPort == null || newPort != port) {
                // Xor-Paint the old Connector (Hide old Connector)
                paintConnector(Color.black, graph.getBackground(), g);
                // If Port was found then Point to Port Location
                port = newPort;
                if (port != null)
                    current = graph.toScreen(port.getLocation(null));
                // Else If no Port was found then Point to Mouse Location
                else
                    current = graph.snap(e.getPoint());
                // Xor-Paint the new Connector
                paintConnector(graph.getBackground(), Color.black, g);
            }
        }
        // Call Superclass
        super.mouseDragged(e);
    }
    
    public PortView getSourcePortAt(Point2D point) {
        // Disable jumping
        graph.setJumpToDefaultPort(false);
        PortView result;
        try {
            // Find a Port View in Model Coordinates and Remember
            result = graph.getPortViewAt(point.getX(), point.getY());
        } finally {
            graph.setJumpToDefaultPort(true);
        }
        return result;
    }
    
    public GraphCell getVertexAt(Point2D point) {
        // Disable jumping
        graph.setJumpToDefaultPort(false);
        GraphCell result = null;
        try {
            // Find a Port View in Model Coordinates and Remember
            CellView view = graph.getNextViewAt(null, point.getX(), point.getY());
            if (view != null && view instanceof VertexView) {
                Object tmp = view.getCell();
                if (tmp instanceof GraphCell) 
                    result = (GraphCell) tmp;
            }
        } finally {
            graph.setJumpToDefaultPort(true);
        }
        return result;
    }
        // Find a Cell at point and Return its first Port as a PortView
    protected PortView getTargetPortAt(Point2D point) {
        // Find a Port View in Model Coordinates and Remember
        return graph.getPortViewAt(point.getX(), point.getY());
    }
    
    // Connect the First Port and the Current Port in the Graph or Repaint
    public void mouseReleased(MouseEvent e) {
        if (e != null 
                && e.getClickCount() > 1) {
            GraphCell vertex = getVertexAt(e.getPoint());
            if (vertex != null && vertex instanceof MacroCell) {
                MacroCell macrocell = (MacroCell) vertex;
                System.out.println("Got doubleclick on " + macrocell.getUserObject());
                ownerEditor.gotoProdModel((String)(macrocell.getUserObject()));
            }            
        }
        // If Valid Event, Current and First Port
        if (e != null
                && port != null
                && firstPort != null
                && firstPort != port) {
            // Then Establish Connection
            MacroModel model = (MacroModel)(graph.getModel());
            // "port" is valid here, guaranteed by mouseDragged().
            model.insertEdge((DefaultPort) firstPort.getCell(), (DefaultPort) port.getCell());
            // Else Repaint the Graph
        } else
            graph.repaint();
        // Reset Global Vars
        firstPort = port = null;
        start = current = null;
        // Call Superclass
        super.mouseReleased(e);
    }
    
    // Show Special Cursor if Over Port
    public void mouseMoved(MouseEvent e) {
        // Check Mode and Find Port
        if (e != null
                && getSourcePortAt(e.getPoint()) != null
                && graph.isPortsVisible()) {
            // Set Cusor on Graph (Automatically Reset)
            graph.setCursor(new Cursor(Cursor.HAND_CURSOR));
            // Consume Event
            // Note: This is to signal the BasicGraphUI's
            // MouseHandle to stop further event processing.
            e.consume();
        } else
            // Call Superclass
            super.mouseMoved(e);
    }
    
    // Use Xor-Mode on Graphics to Paint Connector
    protected void paintConnector(Color fg, Color bg, Graphics g) {
        // Set Foreground
        g.setColor(fg);
        // Set Xor-Mode Color
        g.setXORMode(bg);
        // Highlight the Current Port
        paintPort(graph.getGraphics());
        // If Valid First Port, Start and Current Point
        if (firstPort != null && start != null && current != null)
            // Then Draw A Line From Start to Current Point
            g.drawLine((int) start.getX(),
                    (int) start.getY(),
                    (int) current.getX(),
                    (int) current.getY());
    }
    
    // Use the Preview Flag to Draw a Highlighted Port
    protected void paintPort(Graphics g) {
        // If Current Port is Valid
        if (port != null) {
            // If Not Floating Port...
            boolean o =
                    (GraphConstants.getOffset(port.getAttributes()) != null);
            // ...Then use Parent's Bounds
            Rectangle2D r =
                    (o) ? port.getBounds() : port.getParentView().getBounds();
            // Scale from Model to Screen
            r = graph.toScreen((Rectangle2D) r.clone());
            // Add Space For the Highlight Border
            r.setFrame(r.getX() - 3, r.getY() - 3, r.getWidth() + 6, r.getHeight() + 6);
            // Paint Port in Preview (=Highlight) Mode
            graph.getUI().paintCell(g, port, r, true);
        }
    }
    
} // End of Editor.MyMarqueeHandler




