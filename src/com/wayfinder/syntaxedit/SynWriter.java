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

import java.util.Map;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Set;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.xml.sax.SAXException;
import java.lang.String;
import java.lang.Throwable;
import com.wayfinder.syntaxedit.MacroModel.*;
import org.jgraph.graph.GraphCell;

public class SynWriter {
    
    private FileOutputStream os;
    private Map<String, Integer> knownSounds = new HashMap<String, Integer>();
    private Map<String, Integer> knownMacros = new HashMap<String, Integer>();
    
    /** Creates a new instance of SynWriter */
    public SynWriter(File f) throws SAXException {
        try {
            os = new FileOutputStream(f);
            os.write("WF_AUDIO_SYNTAX: 1\n".getBytes());  // header
            writeInt8(0x00);
        } catch (Throwable t) {
            throw new SAXException("Unable to write to file "+f.getName());
        }
    }
    
    protected void writeInt8(int token) throws SAXException {
        try {
            os.write(token);
        } catch (IOException t) {
            throw new SAXException("Write failed", t);
        }
    }
    
    protected void writeInt16(int token) throws SAXException {
        try {
            os.write(token & 0xff);
            os.write((token >> 8) & 0xff);
        } catch (IOException t) {
            throw new SAXException("Write failed", t);
        }
    }
    
    protected void writeString(String s) throws SAXException {
        try {
            writeInt8(s.length());
            os.write(s.getBytes());
        } catch (IOException t) {
            throw new SAXException("Write failed", t);
        }
    }
    
    protected void close() throws SAXException {
        try {
            os.close();
        } catch (IOException t) {
            throw new SAXException("Close failed", t);
        }
    }
    
    public void writeSoundClips(Map<String, SoundClip> soundClips)
    throws SAXException {
        writeInt16(soundClips.size());
        if (false) {
            int totlen = 0;
            for (String name : soundClips.keySet()) {
                totlen = totlen + soundClips.get(name).getFileName().length();
            }
            writeInt16(totlen);
        }
        for (String name : soundClips.keySet()) {
            writeString(name);
            writeString(soundClips.get(name).getFileName());
            knownSounds.put(name, knownSounds.size());
        }
    }
    
    protected class MacroWriter {
        MacroModel model;
        Map<GenericVertexCell, Integer> knownIds;
        int num_ids = 0;
        
        public MacroWriter(String name, MacroModel model) throws SAXException {
            this.model = model;
            knownIds = new IdentityHashMap<GenericVertexCell, Integer>();
            knownMacros.put(name, knownMacros.size());
            writeString(name);
            for (GenericVertexCell r : model.getRootVertices()) {
                if (r != model.getStartVertex()) {
                    saveVertexAndDescendants(r);
                }
            }
            saveVertexAndDescendants(model.getStartVertex());
        }
        
        public Integer saveVertexAndDescendants(GenericVertexCell c) throws SAXException {
            Integer id = knownIds.get(c);
            if (id != null) {
                // Already seen node - return the last id
                return id;
            }
            if (c instanceof BooleanBranchCell) {
                GraphCell trueTarg  = model.findBooleanTarget(c, true);
                GraphCell falseTarg = model.findBooleanTarget(c, false);
                Integer trueId = null;
                Integer falseId = null;
                if (trueTarg != null && trueTarg instanceof GenericVertexCell) {
                    trueId = saveVertexAndDescendants((GenericVertexCell)trueTarg);
                }
                if (falseTarg != null && falseTarg instanceof GenericVertexCell) {
                    falseId = saveVertexAndDescendants((GenericVertexCell)falseTarg);
                }
                if (c instanceof TryCatchCell) {
                    id = writeTryCatchCell(trueId, falseId);
                } else {
                    // Regular BranchCell
                    Object condition = c.getUserObject();
                    id = writeBooleanCell(condition, trueId, falseId);
                }
            } else if (c instanceof ReturnCell) {
                id = new Integer(-2);
            } else if (c instanceof FailCell) {
                id = new Integer(-3);
            } else if (c instanceof ProcessCell) {
                Set<GenericVertexCell> descendants = model.getDescendantVertices(c);
                GenericVertexCell targ = null;
                if (descendants.size() > 0) {
                    targ = descendants.iterator().next();
                }
                Integer targId = null;
                if (targ != null && targ instanceof GenericVertexCell) {
                    targId = saveVertexAndDescendants((GenericVertexCell)targ);
                }
                {
                    if (c instanceof StartCell) {
                        id = writeStartCell(targId);
                    } else if (c instanceof MacroCell) {
                        id = writeMacroCall(c.getUserObject().toString(), targId);
                    } else if (c instanceof SetXingCell) {
                            id = writeSelectXing(((SetXingCell)c).getXingNo(), targId);
                    } else if (c instanceof TimingMarkerCell) {
                            id = writeTimingMarkerClip(targId);
                    } else if (c instanceof PlayClipCell) {
                            id = writeSoundClip(c.getUserObject().toString(), targId);
                    } else {
                        // Unknown cell - what to do= FIXME
                    }
                }
            } else {
                // Unknown cell - what to do? FIXME
            }
            knownIds.put(c, id);
            return id;
            
        }
        
        public Integer writeTryCatchCell(Integer tryLink, Integer failLink)
        throws SAXException {
            writeInt8(0x04);
            writeBooleanLinks(tryLink, failLink);
            return allocateId();
        }
        
        public Integer writeBooleanCell(Object condObj, Integer trueLink, Integer falseLink)
        throws SAXException {
            int var; // Id of the tested variable
            Condition cond = new Condition((String)condObj);
            writeInt8(0x03);
            writeInt8(cond.getVariable().toWfCode());
            writeInt8(cond.getRelation().toWfCode());
            writeInt16(cond.getLimit());
            writeBooleanLinks(trueLink, falseLink);
            return allocateId();
        }
        
        public Integer writeMacroCall(String mname, Integer link)
        throws SAXException {
            writeInt8(0x02);
            writeLink(link);
            writeInt16(knownMacros.get(mname));
            return allocateId();
        }
        
        public Integer writeStartCell(Integer link)
        throws SAXException {
            writeInt8(0x00);
            writeLink(link);
            return allocateId();
        }
        
        public Integer writeSoundClip(String sname, Integer link)
        throws SAXException {
            writeInt8(0x01);
            writeLink(link);
            writeInt16(knownSounds.get(sname));
            return allocateId();
        }
        
        public Integer writeTimingMarkerClip(Integer link)
        throws SAXException {
            writeInt8(0x01);
            writeLink(link);
            writeInt16(-3);
            return allocateId();
        }
        
        public Integer writeSelectXing(int xing, Integer link)
        throws SAXException {
            writeInt8(0x05);
            writeLink(link);
            writeInt8(xing);
            return allocateId();
        }
        
        public void writeLink(Integer link)
        throws SAXException {
            if (link != null) {
                writeInt16(link);
            } else {
                writeInt16(-1);
            }
        }
        
        public void writeBooleanLinks(Integer trueLink, Integer falseLink)
        throws SAXException {
            writeLink(trueLink);
            writeLink(falseLink);
        }
        
        public Integer allocateId() {
            Integer id = new Integer(num_ids);
            num_ids = num_ids + 1;
            return id;
        }
    }
    
    public void writeMacro(String name, MacroModel model)
    throws SAXException {
        new MacroWriter(name, model);
    }
    
}
