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

import java.lang.String;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class SoundClip {
    
    /** fileName may be null */
    protected String fileName;
    
    /** Creates a new instance of SoundClip without a corresponding file */
    public SoundClip() {
        fileName = null;
    }
    
    /** Creates a new instance of SoundClip, pointing to the optional
     * fileName. */
    public SoundClip(String fileName) {
        this.fileName = fileName;
    }
    
    /** Return the length of the clip in milliseconds, or
     * 0 if unknown. */
    public int getLength() {
        if (fileName == null) {
            return 0;
        } else {
            return 10;
        }
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fn) {
        fileName=fn;
    }
    
    public void saveAsXML(XMLWriter w, String name) throws SAXException {
        AttributesImpl a = new AttributesImpl();
        a.addAttribute("", "name", "", "", name);
        a.addAttribute("", "filename", "", "", fileName);
        w.emitEmptyElement("Clip", a);
    }
    
    
}
