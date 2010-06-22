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

import java.io.*;
import org.xml.sax.*;
import java.util.Stack;

public class XMLWriter {
    protected OutputStreamWriter out;
    protected Stack<String> openElem;
    
    /** Creates a new instance of XMLWriter */
    public XMLWriter(File fn) {
        openElem = new Stack<String>();
        try {
            // Set up output stream
            FileOutputStream f = new FileOutputStream(fn);
            out = new OutputStreamWriter(f, "UTF8");
            emit("<?xml version='1.0' encoding='UTF-8'?>");  nl();
            emit("<!DOCTYPE AudioSyntax [");                                              nl();
            emit("  <!ELEMENT AudioSyntax (SoundClips?, MacroList?)>");                   nl();
            emit("  <!ATTLIST AudioSyntax ");                                             nl();
            emit("        language      CDATA   #REQUIRED");                              nl();
            emit("        voiceName     CDATA   #REQUIRED");                              nl();
            emit("        subVoiceName  CDATA   #REQUIRED");                              nl();
            emit("  >");                                                                  nl();
            emit("  ");                                                                   nl();
            emit("  <!ELEMENT SoundClips (SoundPath*, Clip*)>");                          nl();
            emit("  <!ELEMENT SoundPath EMPTY>");                                         nl();
            emit("  <!ATTLIST SoundPath ");                                               nl();
            emit("        path      CDATA   #REQUIRED");                                  nl();
            emit("  >");                                                                  nl();
            emit("  <!ELEMENT Clip EMPTY>");                                              nl();
            emit("  <!ATTLIST Clip ");                                                    nl();
            emit("        name      CDATA   #REQUIRED");                                  nl();
            emit("        filename  CDATA   #IMPLIED");                                   nl();
            emit("  >");                                                                  nl();
            emit("  ");                                                                   nl();
            emit("  <!ELEMENT MacroList (Macro*)>");                                      nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT Macro ((PlayClip | SelectXing | MacroCell | BooleanBranchCell | ReturnCell | FailCell | TryCatchCell | TimingMarker)*, ");         nl();
            emit("                   StartCell) >");                                      nl();
            emit("  <!ATTLIST Macro ");                                                   nl();
            emit("        name      ID      #REQUIRED");                                  nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT StartCell (Link?)>");                                       nl();
            emit("  <!ATTLIST StartCell");                                                nl();
            emit("        id        ID      #REQUIRED");                                  nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT PlayClip (Link?)>");                                        nl();
            emit("  <!ATTLIST PlayClip");                                                 nl();
            emit("        id        ID      #REQUIRED");                                  nl();
            emit("        clipName  CDATA   #IMPLIED");                                   nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT SelectXing (Link?)>");                                      nl();
            emit("  <!ATTLIST SelectXing");                                               nl();
            emit("        id        ID      #REQUIRED");                                  nl();
            emit("        xingno    CDATA   #IMPLIED");                                   nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT TimingMarker (Link?)>");                                    nl();
            emit("  <!ATTLIST TimingMarker");                                             nl();
            emit("        id        ID      #REQUIRED");                                  nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT ReturnCell EMPTY>");                                        nl();
            emit("  <!ATTLIST ReturnCell");                                               nl();
            emit("        id        ID      #REQUIRED");                                  nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT FailCell EMPTY>");                                          nl();
            emit("  <!ATTLIST FailCell");                                                 nl();
            emit("        id        ID      #REQUIRED");                                  nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT MacroCell (Link?)>");                                       nl();
            emit("  <!ATTLIST MacroCell");                                                nl();
            emit("        id        ID      #REQUIRED");                                  nl();
            emit("        macroName IDREF   #REQUIRED");                                  nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT BooleanBranchCell (BooleanTrueLink?, BooleanFalseLink?)>"); nl();
            emit("  <!ATTLIST BooleanBranchCell");                                        nl();
            emit("        id        ID      #REQUIRED");                                  nl();
            emit("        condition CDATA   #REQUIRED");                                  nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT TryCatchCell (TryLink?, CatchLink?)>");                     nl();
            emit("  <!ATTLIST TryCatchCell");                                             nl();
            emit("        id        ID      #REQUIRED");                                  nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT Link EMPTY>");                                              nl();
            emit("  <!ATTLIST Link");                                                     nl();
            emit("        target    IDREF   #REQUIRED");                                  nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT BooleanTrueLink EMPTY>");                                   nl();
            emit("  <!ATTLIST BooleanTrueLink");                                          nl();
            emit("        target    IDREF   #REQUIRED");                                  nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT BooleanFalseLink EMPTY>");                                  nl();
            emit("  <!ATTLIST BooleanFalseLink");                                         nl();
            emit("        target    IDREF   #REQUIRED");                                  nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT TryLink EMPTY>");                                           nl();
            emit("  <!ATTLIST TryLink");                                                  nl();
            emit("        target    IDREF   #REQUIRED");                                  nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("  <!ELEMENT CatchLink EMPTY>");                                         nl();
            emit("  <!ATTLIST CatchLink");                                                nl();
            emit("        target    IDREF   #REQUIRED");                                  nl();
            emit("  >");                                                                  nl();
            emit("");                                                                     nl();
            emit("]>");                                                                   nl();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public void close()
    throws SAXException {
        try {
            out.close();
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }
    
    protected StringBuffer substAttrEntities(final String attr) {
        StringBuffer buf;
        if (attr == null)
            buf = new StringBuffer();
        else
            buf = new StringBuffer(attr);
        // Replace all & with &amp;
        int pos=-1;
        while ((pos=buf.indexOf("&", pos+1)) >= 0) {
            buf.replace(pos, pos+1, "&amp;");
            pos = pos + 4;
        }
        pos=-1;
        while ((pos=buf.indexOf("<", pos+1)) >= 0) {
            buf.replace(pos, pos+1, "&lt;");
            pos = pos + 3;
        }
        return buf;
    }
    
    protected StringBuffer constructAttr(final Attributes attr) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < attr.getLength(); ++i) {
            b.append(" " + attr.getLocalName(i) + "=\"" +
                    substAttrEntities(attr.getValue(i)) + "\"");
        }
        return b;
    }
    
    public void emitEmptyElement(String elem, final Attributes attr, String comment)
    throws SAXException {
        StringBuffer indent = new StringBuffer();
        for (int i = 0 ; i < openElem.size() ; ++i)
            indent.append("  ");
        if (attr != null)
            emit(indent + "<" + elem + constructAttr(attr) + " />");
        else
            emit(indent + "<" + elem + "/>");
        if (comment != null)
            emit(" <!-- " + comment + "-->");
        nl();
    }
    
    public void emitEmptyElement(String elem, final Attributes attr)
    throws SAXException {
        emitEmptyElement(elem, attr, null);
    }
    
    public void emitOpenElement(String elem, final Attributes attr, String comment)
    throws SAXException {
        StringBuffer indent = new StringBuffer();
        for (int i = 0 ; i < openElem.size() ; ++i)
            indent.append("  ");
        if (attr != null)
            emit(indent + "<" + elem + constructAttr(attr) + ">");
        else
            emit(indent + "<" + elem + ">");
        if (comment != null)
            emit(" <!-- " + comment + "-->");
        nl();
        openElem.push(elem);
    }
    
    public void emitOpenElement(String elem, final Attributes attr)
    throws SAXException {
        emitOpenElement(elem, attr, null);
    }
    public void emitCloseElement()
    throws SAXException {
        StringBuffer indent = new StringBuffer();
        for (int i = 0 ; i < (openElem.size()-1) ; ++i)
            indent.append("  ");
        emit(indent + "</" + openElem.pop() + ">");
        nl();
    }
    
    protected void emit(String s)
    throws SAXException {
        try {
            out.write(s);
            out.flush();
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }
    
    
    
    protected void nl()
    throws SAXException {
        String lineEnd =  System.getProperty("line.separator");
        try {
            out.write(lineEnd);
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }
    
}
