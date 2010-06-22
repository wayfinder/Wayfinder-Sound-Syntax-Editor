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

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Vector;
import java.util.Stack;
import java.util.EventListener;
import javax.swing.event.EventListenerList;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.lang.String;
import java.lang.Throwable;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.IOException;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphUndoManager;
import com.wayfinder.syntaxedit.MacroModel.GenericVertexCell;
import com.wayfinder.syntaxedit.MacroModel.MacroCell;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

public class SyntaxTree implements SemanticChecker {
    static public interface SyntaxTreeEventListener extends EventListener {
        void modelListChanged();
        void soundListChanged();
    }
    
    protected EventListenerList listenerList = new EventListenerList();
    protected Map<String, MacroModel> models;
    protected String                 lastSemanticError;
    protected Map<String, SoundClip> soundClips;
    protected GraphUndoManager undoManager;
    protected String voiceName;
    protected String subVoiceName;
    protected String language;
    protected Vector<String> soundDirs = new Vector<String>();
    
    protected AbstractTableModel distanceTableModel = new AbstractTableModel() {
        public String getColumnName(int col) {
            if (col == 0) return "Dist 1";
            if (col == 1) return "Dist 2";
            return "Unknown";
        }
        public int getRowCount() { return 3; }
        public int getColumnCount() { return 2; }
        public Object getValueAt(int row, int col) {
            return row*col;
        }
        public boolean isCellEditable(int row, int col) {
            return true; }
        public void setValueAt(Object value, int row, int col) {
            fireTableCellUpdated(row, col);
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    protected AbstractTableModel soundTableModel = new AbstractTableModel() {
        public String getColumnName(int col) {
            if (col == 0) return "Sound name";
            if (col == 1) return "File name";
            return "Unknown";
        }
        public int getRowCount() { return soundClips.size()+1; }
        public int getColumnCount() { return 2; }
        public Object getValueAt(int row, int col) {
            if (col < 0 || col > 1)
                return "";
            SortedSet<String> sounds = new TreeSet<String>(soundClips.keySet());
            if (row < 0 || row >= sounds.size())
                return "";
            Iterator<String> i = sounds.iterator();
            for ( ; row > 0 ; --row) {
                i.next();
            }
            if (col == 0) {
                return i.next();
            } else if (col == 1) {
                return soundClips.get(i.next()).getFileName();
            } else {
                return "";
            }
        }
        public boolean isCellEditable(int row, int col) {
            return true; }
        public void setValueAt(Object value, int row, int col) {
            if (row < 0)
                return;
            if (row > getRowCount())
                return;
            if (row == (getRowCount()-1)) {
                // Add a new sound if this was the first column
                if (col != 0)
                    return;
                // Check for duplicate entries
                if (soundClips.get(value) != null)
                    return;
                soundClips.put((String)value, new SoundClip());
                fireTableDataChanged();
                fireSoundListChanged();
            } else {
                // Changing an existing sound - find the sound
                SortedSet<String> sounds = new TreeSet<String>(soundClips.keySet());
                Iterator<String> i = sounds.iterator();
                for ( ; row > 0 ; --row) {
                    i.next();
                }
                String changingKey = i.next();
                
                if (col == 0) {
                    // Cannot change the name yet
                    return;
                } else if (col == 1) {
                    // Update the filename
                    soundClips.get(changingKey).setFileName((String)value);
                }
            }
            fireTableCellUpdated(row, col);
            fireSoundListChanged();
        }
        /** Shut up the compiler warnings
         */
        private static final long serialVersionUID = 42L;
    };
    
    
    
    /** Creates a new instance of SyntaxTree */
    public SyntaxTree(GraphUndoManager undoManager) {
        this.undoManager = undoManager;
        setLanguage("EN");
        soundClips = new HashMap<String, SoundClip>();
        models = new HashMap<String, MacroModel>();
        soundDirs.add("Foo");
        soundDirs.add("Bar");
        SetupTestingSyntax();
    }
    
    public SyntaxTree(GraphUndoManager undoManager, File f) throws SAXException {
        this.undoManager = undoManager;
        setLanguage("EN");
        // Initialize from an XML file
        soundClips = new HashMap<String, SoundClip>();
        models = new HashMap<String, MacroModel>();
        String header;
        try {
            FileReader r = new FileReader(f);
            char raw[] = new char[19];
            r.read(raw, 0, 19);
            header = new String(raw);
            r.close();
        } catch (Throwable t) {
            throw new SAXException("Failed to read file");
        }
        if (header.equals("WF_AUDIO_SYNTAX: 1\n")) {
            new SynFileLoader(f);
        } else if (header.startsWith("<?xml")) {
            loadFromXML(f);
        } else {
            throw new SAXException("Unrecognized file format");
        }
        undoManager.discardAllEdits();
    }
    
    public MacroModel getModel(String model) {
        return models.get(model);
    }
    
    public Set<String> getModelList() {
        return models.keySet();
    }
    
    public List<String> getModelListInDependencyOrder() {
        List<String> l = new LinkedList<String>();
        /* The "done" set is used to speed up the set operations. It is
         * equivalent to the list "l". */
        Set<String> done = new HashSet<String>();
        
        Map<String, Set<String>> deps = new HashMap<String, Set<String>>();
        for (String name : getModelList()) {
            deps.put(name, getModel(name).getCalledMacros());
        }
        
        while ( ! deps.isEmpty()) {
            boolean progress = false;
            Iterator<String> i = deps.keySet().iterator();
            while (i.hasNext()) {
                String name = i.next();
                // Make sure all called macros are already
                // done, postpone the current macro otherwise.
                Set<String> calledMacros = deps.get(name);
                calledMacros.removeAll(done);
                if (calledMacros.isEmpty()) {
                    l.add(name);
                    done.add(name);
                    i.remove();
                    progress=true;
                }
            }
            if (! progress) {
                // The operation has stalled - probably a cyclic reference.
                // We really should throw something here. FIXME
                return l;
            }
        }
        
        return l;
    }
    
    /** Insert a new macro block into the model.
     * Returns null on success and an error text
     * on failure. */
    public String addProdModel(String name) {
        if (models.get(name) != null)
            return "Macro already exists";
        MacroModel model = new MacroModel(name, getSemanticChecker());
        models.put(name, model);
        model.addUndoableEditListener(undoManager);
        fireSyntaxTreeChanged();
        return null;
    }
    
    public Set<String> getSoundList() {
        return soundClips.keySet();
    }
    
    public String getSoundClipFileName(String clip) {
        return soundClips.get(clip).getFileName();
    }
    
    public TableModel getDistanceTableModel() {
        return distanceTableModel;
    }
    
    public TableModel getSoundTableModel() {
        return soundTableModel;
    }
    
    public void addSyntaxTreeEventListener(SyntaxTreeEventListener l) {
        listenerList.add(SyntaxTreeEventListener.class, l);
    }
    
    public void removeSyntaxTreeEventListener(SyntaxTreeEventListener l) {
        listenerList.remove(SyntaxTreeEventListener.class, l);
    }
    
    protected void fireSyntaxTreeChanged() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==SyntaxTreeEventListener.class) {
                ((SyntaxTreeEventListener)listeners[i+1]).modelListChanged();
            }
        }
    }
    
    protected void fireSoundListChanged() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==SyntaxTreeEventListener.class) {
                ((SyntaxTreeEventListener)listeners[i+1]).soundListChanged();
            }
        }
    }
    
    public SemanticChecker getSemanticChecker() {
        return this;
    }
    
    public String getSemanticErrorDescription() {
        return lastSemanticError;
    }
    
    /** Check that the descendants from cell does not form a cyclic
     *graph.
     *Cells found ok are added to knownGood. Cells seen so far are
     *added to seen. If any new cell is found already to be in
     *seen then the graph is cyclic.
     *parentCell is only used for error messages for macro calls and
     *may be null. */
    private boolean checkAcyclic(
            MacroModel model,
            GenericVertexCell cell,
            GenericVertexCell parentCell,
            Set<GenericVertexCell> knownGood,
            Set<GenericVertexCell> seenSoFar) {
        if (knownGood.contains(cell)) {
            return true;
        }
        seenSoFar.add(cell);
        if (cell instanceof MacroCell) {
            /* Check the lower macro level - depth first */
            MacroModel childModel = getModel(((MacroCell)cell).getMacroTarget());
            if (childModel == null) {
                lastSemanticError = "Call to unknown model " +
                        cell.getUserObject() + " in model" + model.getName();
                System.out.println(lastSemanticError);
            } else {
                GenericVertexCell child = childModel.getStartVertex();
                if (seenSoFar.contains(child)) {
                    if (parentCell != null) {
                        lastSemanticError = "Found cyclic refenrence from " +
                                parentCell.getUserObject() + " to macro " +
                                cell.getUserObject() + " in "+
                                model.getName();
                        System.out.println(lastSemanticError);
                    } else {
                        lastSemanticError = "Found cyclic refenrence to macro " +
                                cell.getUserObject() + " in "+
                                model.getName();
                        System.out.println(lastSemanticError);
                    }
                    return false;
                }
                if ( ! checkAcyclic(childModel, childModel.getStartVertex(),
                        null,
                        knownGood,
                        new HashSet<GenericVertexCell>(seenSoFar))) {
                    return false;
                }
            }
        }
        Set<GenericVertexCell> children = model.getDescendantVertices(cell);
        for (GenericVertexCell child : children) {
            if (seenSoFar.contains(child)) {
                lastSemanticError = "Found cyclic refenrence from " +
                        cell.getUserObject() + " to " +
                        child.getUserObject() + " in "+
                        model.getName();
                System.out.println(lastSemanticError);
                return false;
            }
            if ( ! checkAcyclic(model, child, cell, knownGood,
                    new HashSet<GenericVertexCell>(seenSoFar))) {
                return false;
            }
        }
        knownGood.add(cell);
        return true;
    }
    
    public boolean isValidSemantically() {
        Set<GenericVertexCell> allVertices = new HashSet<GenericVertexCell>();
        
        /*Start by checking things local to each model.
         *All macro calls must point to a valid macro body.
         *Also, store all vertices in allVertices for later
         *use.
         */
        for (String modelName : getModelList()) {
            MacroModel model = getModel(modelName);
            Object [] cells = MacroModel.getAll(model);
            for (int i = 0; i<cells.length; ++i) {
                if (cells[i] instanceof GenericVertexCell) {
                    allVertices.add((GenericVertexCell)(cells[i]));
                }
                if (cells[i] instanceof MacroCell) {
                    MacroCell m = (MacroCell)(cells[i]);
                    String calledMacro = (String)(m.getUserObject());
                    if (getModel(calledMacro) == null) {
                        lastSemanticError =
                                "Found error : call to missing macro " +
                                calledMacro + " in prod " + modelName;
                        System.out.println(lastSemanticError);
                    }
                }
            }
        }
        
        /* Check that there are no circular links or references */
        Set<GenericVertexCell> knownGood = new HashSet<GenericVertexCell>();
        for (String modelName : getModelList()) {
            MacroModel model = getModel(modelName);
            Set<GenericVertexCell> roots = model.getRootVertices();
            for (GenericVertexCell rootCell : roots) {
                if ( ! checkAcyclic(model, rootCell, null, knownGood,
                        new HashSet<GenericVertexCell>())) {
                    return false;
                }
            }
        }
        
        /* Any vertices not in knownGood at this point must form one or
         *more cyclic groups. If at least one element was a root element
         *it would have been caught in the prior test. Since all elements in
         *cyclicGroups are known to have an incoming source, and are not part
         *of chains started from any roots, they must be cyclic. */
        Set<GenericVertexCell> cyclicGroups =
                new HashSet<GenericVertexCell>(allVertices);
        cyclicGroups.removeAll(knownGood);
        if ( ! cyclicGroups.isEmpty()) {
            lastSemanticError = "Cyclic, unconnected group(s) found";
            System.out.println(lastSemanticError);
            return false;
        }
        
        return true;
    }
    
    /** A simple wrapper around checkAcyclic. Returns false if the graph
     *no longer would be acyclic after the change. The function assumes the
     *graph to be acyclic to begin with. */
    public boolean isNewEdgeAllowed(
            MacroModel model,
            GenericVertexCell source,
            GenericVertexCell target) {
        Set<GenericVertexCell> knownGood = new HashSet<GenericVertexCell>();
        Set<GenericVertexCell> seenSoFar = new HashSet<GenericVertexCell>();
        seenSoFar.add(source);
        return this.checkAcyclic(model, target, source, knownGood, seenSoFar);
    }
    
    public void saveSyntaxAsXML(File f) throws SAXException {
        XMLWriter w = new XMLWriter(f);
        AttributesImpl a = new AttributesImpl();
        a.addAttribute("", "language", "", "", getLanguage());
        a.addAttribute("", "voiceName", "", "", getVoiceName());
        a.addAttribute("", "subVoiceName", "", "", getSubVoiceName());
        w.emitOpenElement("AudioSyntax",a);
        w.emitOpenElement("SoundClips",null);
        for (String dir : getSoundDirList()) {
            a = new AttributesImpl();
            a.addAttribute("", "path", "", "", dir);
            w.emitEmptyElement("SoundPath", a);
        }
        for (String key : soundClips.keySet()) {
            SoundClip value = soundClips.get(key);
            value.saveAsXML(w, key);
        }
        w.emitCloseElement(); // SoundClips
        
        w.emitOpenElement("MacroList", null);
        for (String key : getModelListInDependencyOrder()) {
            MacroModel m = getModel(key);
            m.saveAsXML(w, key);
        }
        w.emitCloseElement(); // Macro
        w.emitCloseElement(); // AudioSyntax
        w.close();
    }
    
    public void saveSyntaxAsCompiled(File f) throws SAXException {
        SynWriter s = new SynWriter(f);
        
        // soundlist
        s.writeSoundClips(soundClips);
        
        // distancetable - TBD
        
        // macro_list
        List<String> macros = getModelListInDependencyOrder();
        s.writeInt16(macros.size());
        for (String name : macros) {
            s.writeMacro(name, getModel(name));
        }
        
        s.close();
        
        // Write out the corresponding pkg file
        File p = new File(f.getParent(), f.getName().replace(".syn",".pkg"));
        PkgWriter.writePkgFile(p, f, language, voiceName, subVoiceName, soundClips);
    }
    
    protected class XMLHandler extends DefaultHandler {
        protected MacroModel model = null;
        protected GenericVertexCell currentCell = null;
        protected Map<String, GenericVertexCell> seenVertices =
                new HashMap<String, GenericVertexCell>();
        
        public void startDocument() throws SAXException {
            System.out.println("XML got startDocument()");
        }
        
        public void endDocument() throws SAXException {
            System.out.println("XML got endDocument()");
        }
        
        protected String getAttrib(Attributes a, String qName) {
            if (a == null)
                return null;
            return a.getValue(qName);
        }
        
        protected void handleBooleanLink(boolean type, Attributes attrs)
        throws SAXException {
            String targId = getAttrib(attrs, "target");
            GenericVertexCell target = seenVertices.get(targId);
            if (target == null)
                throw new SAXException("No matching id "+targId+
                        " was found in macro "+model.getName());
            model.insertBooleanEdge((MacroModel.BooleanBranchCell)currentCell,
                    type, target);
        }
        
        public void startElement(String namespaceURI,
                String sName, // simple name (localName)
                String qName, // qualified name
                Attributes attrs)
                throws SAXException {
            String eName = sName; // element name
            if ("".equals(eName)) eName = qName; // namespaceAware = false
            //System.out.println("XML got startElement(" + eName + ")");
            
            String name = getAttrib(attrs, "name");
            String id = getAttrib(attrs, "id");
            
            if (eName.equals("AudioSyntax")) {
                setLanguage(getAttrib(attrs, "language"));
                setVoiceName(getAttrib(attrs, "voiceName"));
                setSubVoiceName(getAttrib(attrs, "subVoiceName"));
            } else if (eName.equals("SoundPath")) {
                soundDirs.add(getAttrib(attrs, "path"));
            } else if (eName.equals("Clip")) {
                soundClips.put(name,
                        new SoundClip(getAttrib(attrs, "filename")));
            } else if (eName.equals("Macro")) {
                model = new MacroModel(name, getSemanticChecker());
                models.put(name, model);
                seenVertices.clear();
                currentCell=null;
            } else if (eName.equals("PlayClip")) {
                String clipName = getAttrib(attrs, "clipName");
                if (soundClips.get(clipName) == null) {
                    throw new SAXException("No matching sound clip "+clipName+
                            " found in macro "+model.getName());
                }
                currentCell=model.insertSoundClipVertex(clipName, 10, 10);
                seenVertices.put(id, currentCell);
            } else if (eName.equals("SelectXing")) {
                String xingNo = getAttrib(attrs, "xingno");
                currentCell=model.insertSetXingVertex(Integer.decode(xingNo), 10, 10);
                seenVertices.put(id, currentCell);
            } else if (eName.equals("TimingMarker")) {
                currentCell=model.insertTimingMarkerVertex(10, 10);
                seenVertices.put(id, currentCell);
            } else if (eName.equals("ReturnCell")) {
                currentCell=model.insertReturnVertex(10, 10);
                seenVertices.put(id, currentCell);
            } else if (eName.equals("FailCell")) {
                currentCell=model.insertFailVertex(10, 10);
                seenVertices.put(id, currentCell);
            } else if (eName.equals("BooleanBranchCell")) {
                String cond = getAttrib(attrs, "condition");
                currentCell=model.insertBooleanVertex(cond, 10, 10);
                seenVertices.put(id, currentCell);
            } else if (eName.equals("TryCatchCell")) {
                currentCell=model.insertTryCatchVertex(10, 10);
                seenVertices.put(id, currentCell);
            } else if (eName.equals("MacroCell")) {
                String mname = getAttrib(attrs, "macroName");
                if ( getModel(mname) == null) {
                    throw new SAXException("No matching called macro "+mname+
                            " was found in macro "+model.getName());
                }
                currentCell=model.insertMacroVertex(mname, 10, 10);
                seenVertices.put(id, currentCell);
            } else if (eName.equals("StartCell")) {
                currentCell = model.getStartVertex();
                seenVertices.put(id, currentCell);
            } else if (eName.equals("Link")) {
                String targId = getAttrib(attrs, "target");
                GenericVertexCell target = seenVertices.get(targId);
                if (target == null)
                    throw new SAXException("No matching id "+targId+
                            " was found in macro "+model.getName());
                model.insertEdge(currentCell, target);
            } else if (eName.equals("BooleanTrueLink")) {
                handleBooleanLink(true, attrs);
            } else if (eName.equals("BooleanFalseLink")) {
                handleBooleanLink(false, attrs);
            } else if (eName.equals("TryLink")) {
                handleBooleanLink(true, attrs);
            } else if (eName.equals("CatchLink")) {
                handleBooleanLink(false, attrs);
            }
            
            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    String aName = attrs.getLocalName(i); // Attr name
                    if ("".equals(aName)) aName = attrs.getQName(i);
                    //emit(" ");
                    //emit(aName+"=\""+attrs.getValue(i)+"\"");
                }
            }
        }
        
        public void endElement(String namespaceURI,
                String sName, // simple name
                String qName  // qualified name
                )
                throws SAXException {
            String eName = sName; // element name
            if ("".equals(eName)) eName = qName; // namespaceAware = false
            //System.out.println("XML got endElement(" + eName + ")");
            //emit("</"+sName+">");
        }
        
        public void error(SAXParseException e)
        throws SAXParseException {
            throw e;
        }
        
        
    }
    
    
    public void loadFromXML(File f) throws SAXException {
        //try {
        XMLHandler handler = new XMLHandler();
        // Use the default (non-validating) parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        // Parse the input
        SAXParser saxParser;
        try {
            saxParser = factory.newSAXParser();
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            throw new SAXException("Internal error detected in xml parsing", e);
        }
        try {
            saxParser.parse( f, handler );
        } catch (IOException e) {
            throw new SAXException("I/O error reading the xml file", e);
        }
        if ( ! isValidSemantically())
            throw new SAXException("Semantically invalid xml file: " +
                    getSemanticErrorDescription());
        //} catch (Throwable t) {
        //    t.printStackTrace();
        //}
    }
    
    
    private class SynFileLoader {
        private FileInputStream r;
        private MacroModel macro;
        private String clipVector[];
        private String macroVector[];
        private Vector<GenericVertexCell> cellVector;
        
        public SynFileLoader(File f) throws SAXException {
            try {
                r = new FileInputStream(f);
            } catch (java.io.FileNotFoundException e) {
                throw new SAXException("File not found");
            }
            try {
                r.skip(19);  // Skip past header
                
                // Read out all options
                // Ignore all options for now
                while (readInt8() != 0) {
                }
                
                // Read the sound clips
                int numClips = readInt16();
                //int totalFileNameLength = readInt16();  // dummy
                clipVector = new String[numClips];
                for (int i = 0 ; i <numClips ; ++i) {
                    String name = readString();
                    String fileName = readString();
                    soundClips.put(name, new SoundClip(fileName));
                    clipVector[i]=name;
                }
                
                // Read the distance table
                
                // Read the macros
                int numMacros = readInt16();
                macroVector = new String[numMacros];
                for (int i = 0 ; i < numMacros; ++i) {
                    String macroName = readString();
                    macro = new MacroModel(macroName, getSemanticChecker());
                    models.put(macroName, macro);
                    macroVector[i] = macroName;
                    cellVector = new Vector<GenericVertexCell>(10,10);
                    int nodeType;
                    do {
                        nodeType = readInt8();
                        switch (nodeType) {
                            case 0:
                                // Start node - last in a macro
                                readStartNode();
                                break;
                            case 1:
                                // Play sound clip
                                readSoundClipNode();
                                break;
                            case 2:
                                // Macro call
                                readMacroCallNode();
                                break;
                            case 3:
                                // Conditional
                                readBooleanNode();
                                break;
                            case 4:
                                // Try / catch
                                readTryCatchNode();
                                break;
                            case 5:
                                // Select xing
                                readSelectXingNode();
                                break;
                            default:
                                throw new SAXException("File not correct");
                        }
                    } while (nodeType != 0);
                }
                
                
                r.close();
            } catch (java.io.IOException e) {
                throw new SAXException("Failed to read file");
            }
        }
        
        public void readSoundClipNode() throws IOException {
            int nextId = readInt16();
            int clipId = readInt16();
            GenericVertexCell cell;
            if (clipId == -3) {
                cell = macro.insertTimingMarkerVertex(10, 10);
            } else {
                cell = macro.insertSoundClipVertex(clipVector[clipId], 10, 10);
            }
            addLink(cell, nextId);
            cellVector.add(cell);
        }
        
        public void readMacroCallNode() throws IOException {
            int nextId = readInt16();
            int macroId = readInt16();
            GenericVertexCell cell = macro.insertMacroVertex(macroVector[macroId], 10, 10);
            addLink(cell, nextId);
            cellVector.add(cell);
        }
        
        public void readBooleanNode() throws IOException {
            int vartmp = readInt8();
            Condition.Variable variable = Condition.Variable.fromWfCode(vartmp);
            if (variable == null) {
                variable = Condition.Variable.ZERO;
            }
            int reltmp = readInt8();
            Condition.Relation relation = Condition.Relation.fromWfCode(reltmp);
            if (relation == null) {
                relation = Condition.Relation.EQ;
            }
            int limit = readInt16();
            Condition cond = new Condition(variable, relation, limit);
            int trueId = readInt16();
            int falseId = readInt16();
            GenericVertexCell cell = macro.insertBooleanVertex(cond.toString(), 10, 10);
            addLink(macro.findBooleanOutgoingPort(cell, true), trueId);
            addLink(macro.findBooleanOutgoingPort(cell, false), falseId);
            cellVector.add(cell);
        }
        
        public void readTryCatchNode() throws IOException {
            int trueId = readInt16();
            int falseId = readInt16();
            GenericVertexCell cell = macro.insertTryCatchVertex(10, 10);
            addLink(macro.findBooleanOutgoingPort(cell, true), trueId);
            addLink(macro.findBooleanOutgoingPort(cell, false), falseId);
            cellVector.add(cell);
        }
        
        public void readSelectXingNode() throws IOException {
            int nextId = readInt16();
            int xing = readInt8();
            GenericVertexCell cell = macro.insertSetXingVertex(xing, 10, 10);
            addLink(cell, nextId);
            cellVector.add(cell);
        }
        
        public void readStartNode() throws IOException {
            int nextId = readInt16();
            addLink(macro.getStartVertex(), nextId);
        }
        
        private void addLink(GenericVertexCell cell, int nextId) {
            addLink(macro.findFirstOutgoingPort(cell), nextId);
        }
        
        private void addLink(DefaultPort port, int nextId) {
            if (nextId < 0) {
                GenericVertexCell targCell = null;
                switch (nextId) {
                    case -1:
                        // Do nothing
                        break;
                    case -2:
                        targCell = macro.insertReturnVertex(10,10);
                        break;
                    case -3:
                        targCell = macro.insertFailVertex(10,10);
                        break;
                    default:
                        // Do nothing - FIXME - error handling here?
                        System.out.println("Unknown link target " +nextId);
                }
                if (targCell != null) {
                    macro.insertEdge(port, macro.findIncomingPort(targCell));
                }
            } else {
                // Find and link to an existing node
                GenericVertexCell targCell = cellVector.get(nextId);
                if (targCell == null) {
                    System.out.println("Unable to locate cell with id=" + nextId);
                    return;
                }
                macro.insertEdge(port, macro.findIncomingPort(targCell));
            }
        }
        
        public int readInt8() throws IOException {
            int low = r.read();
            if (low > 127) {
                low = 0xffffff00 | low;
            }
            return low;
        }
        
        public int readInt16() throws IOException {
            int low = r.read();
            int high = r.read();
            int val = (high<<8) + low;
            if (val > 32767) {
                val = 0xffff0000 | val;
            }
            return val;
        }
        
        public String readString() throws IOException {
            int length = readInt8();
            byte buf[] = new byte[length];
            r.read(buf, 0, length);
            return new String(buf);
        }
    }
    
    static private class CallStackEntry {
        public enum Type { Try, Call };
        
        public Type type;
        public int clipListSize;
        public MacroModel model;
        public MacroModel.GenericVertexCell callingCell;
        public int currXing;
        
        public CallStackEntry( Type type, int clipListSize,
                MacroModel m,
                MacroModel.GenericVertexCell callingCell,
                int currXing) {
            this.type = type;
            this.clipListSize = clipListSize;
            this.model = m;
            this.callingCell = callingCell;
            this.currXing = currXing;
        }
    }
    private class Executor {
        private class GraphErrorException extends Exception {
            GraphErrorException(String s) {
                super(s);
            }
            /** Shut up the compiler warnings
             */
            private static final long serialVersionUID = 42L;
        }
        
        Vector<String> clips = new Vector<String>();
        Stack<CallStackEntry> callStack = new Stack<CallStackEntry>();
        Condition.TurnCode turn1;
        int distToWpt;
        int currExitNo = 0;
        int currSide = 0;
        Condition.TurnCode turn2;
        int distToNext;
        int nextExitNo = 0;
        int nextSide = 0;
        Condition.GpsQuality gpsQ;
        
        int currXing;
        MacroModel m;
        MacroModel.GenericVertexCell node;
        
        Executor(int distToWpt, Condition.TurnCode turn1, int exitNo1,
                int distToNext, Condition.TurnCode turn2, int exitNo2,
                Condition.GpsQuality gpsQ,
                String startNode) {
            this.distToWpt = distToWpt;
            this.distToNext= distToNext;
            this.turn1 = turn1;
            this.turn2 = turn2;
            this.currExitNo = exitNo1;
            this.nextExitNo = exitNo2;
            this.gpsQ = gpsQ;
            
            try {
                run(startNode);
            } catch (GraphErrorException g) {
                System.out.println("Graph execution failed:");
            }
        }
        
        private void gotoNextNode() {
            // Node is assumed to have zero or one descendants.
            Set<GenericVertexCell> descendants = m.getDescendantVertices(node);
            GenericVertexCell targ = null;
            if (descendants.size() > 0) {
                targ = descendants.iterator().next();
                node = targ;
            } else {
                node = null;
            }
        }
        
        private void run(String startNode) throws GraphErrorException {
            // Find the start node
            m = getModel(startNode);
            if (m == null) {
                throw new GraphErrorException("No macro named " + startNode);
            }
            
            node = m.getStartVertex();
            
            while (true) {
                // Execute any side effects of this node
                if (node instanceof MacroModel.TryCatchCell) {
                    System.out.println("Found try catch cell");
                    callStack.push(new CallStackEntry(CallStackEntry.Type.Try,
                            clips.size(), m, node, currXing));
                    node = (GenericVertexCell)(m.findBooleanTarget(node, true));
                    
                } else if (node instanceof MacroModel.BooleanBranchCell) {
                    System.out.println("Found boolean cell");
                    Condition c = new Condition((String)(node.getUserObject()));
                    boolean result;
                    if (currXing == 2) {
                        result = c.evaluate(turn2, distToNext, currXing, nextExitNo, nextSide, gpsQ);
                    } else {
                        result = c.evaluate(turn1, distToWpt, currXing, currExitNo, currSide, gpsQ);
                    }
                    node = (GenericVertexCell)(m.findBooleanTarget(node, result));
                    
                } else if (node == null || node instanceof MacroModel.ReturnCell) {
                    if (node == null) {
                        System.out.println("Found implicit return cell");
                    } else {
                        System.out.println("Found return cell");
                    }
                    while (true) {
                        if (callStack.empty()) {
                            // return from the outermost call -> we are done
                            return;
                        }
                        CallStackEntry cse = callStack.pop();
                        if (cse.type == CallStackEntry.Type.Call) {
                            m = cse.model;
                            node = cse.callingCell;
                            gotoNextNode();
                            currXing = cse.currXing;
                            break;
                        }
                    }
                } else if (node instanceof MacroModel.FailCell) {
                    System.out.println("Found fail cell");
                    while (true) {
                        if (callStack.empty()) {
                            // return from the outermost call -> nothing to say
                            clips.clear();
                            return;
                        }
                        CallStackEntry cse = callStack.pop();
                        if (cse.type == CallStackEntry.Type.Try) {
                            while (clips.size() > cse.clipListSize) {
                                clips.remove(clips.size()-1);
                            }
                            m = cse.model;
                            node = (GenericVertexCell)(m.findBooleanTarget(cse.callingCell, false));
                            currXing = cse.currXing;
                            break;
                        }
                    }
                } else if (node instanceof MacroModel.MacroCell) {
                    System.out.println("Found macro call - " + node.getUserObject().toString());
                    callStack.push(new CallStackEntry(CallStackEntry.Type.Call,
                            clips.size(), m, node, currXing));
                    m = getModel(node.getUserObject().toString());
                    if (m == null) {
                        throw new GraphErrorException("Unknown macro called:"+
                                node.getUserObject().toString());
                    }
                    node = m.getStartVertex();
                } else {
                    // Not a fork and not a return means this is a regular
                    // node that will continue to the next node once done.
                    if (node instanceof MacroModel.StartCell) {
                        // Do nothing - simply proceed to the next node
                    } else if (node.getUserObject().toString().startsWith("SelectXing(")) {
                        String userObj = (String)(node.getUserObject());
                        String xingNo = userObj.substring(userObj.indexOf("(")+1,
                                userObj.indexOf(")"));
                        currXing = Integer.decode(xingNo);
                        System.out.println("Found selectxing: " + currXing);
                    } else if (node.getUserObject().toString().startsWith("TimingMarker")) {
                        System.out.println("Found timingmarker - ignored");
                    } else {
                        // Sound clip
                        String name = node.getUserObject().toString();
                        System.out.println("Found sound clip - " + name);
                        clips.add(name);
                    }
                    gotoNextNode();
                }
            }
        }
        
        Vector<String> getClips() {
            return clips;
        }
    }
    
    public Vector<String> execute(
            int distToWpt, Condition.TurnCode turn1, int exitNo1, 
            int distToNext, Condition.TurnCode turn2, int exitNo2,
            Condition.GpsQuality gpsQ, 
            String startNode) {
        Executor e = new Executor(
                distToWpt, turn1, exitNo1, 
                distToNext, turn2, exitNo2,
                gpsQ,
                startNode);
        return e.getClips();
    }
    
    protected void SetupTestingSyntax() {
        MacroModel csl=new MacroModel("SoundListNormal", this);
        csl.addUndoableEditListener(undoManager);
        models.put("SoundListNormal", csl);
        isValidSemantically();
        undoManager.discardAllEdits();
    }
    
    public String getVoiceName() {
        return voiceName;
    }
    
    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
    }
    
    public String getSubVoiceName() {
        return subVoiceName;
    }
    
    public void setSubVoiceName(String subVoiceName) {
        this.subVoiceName = subVoiceName;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public List<String> getSoundDirList() {
        return new Vector<String>(soundDirs);
    }
    
    public void setSoundDirList(List<String> dirs) {
        this.soundDirs = new Vector<String>(dirs);
    }
    
}
