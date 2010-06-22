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

import com.wayfinder.syntaxedit.SyntaxEditor;
import com.wayfinder.syntaxedit.SyntaxTree;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import org.xml.sax.SAXException;
import org.jgraph.graph.GraphUndoManager;
import java.util.Set;

public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    private static void startEditor(final File f) {
        // Start the gui
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFileChooser fc = new JFileChooser();
                fc.addChoosableFileFilter(new FileFilter() {
                    public String getDescription() { return "Syntax File (xml)"; }
                    public boolean accept(File f) {
                        if (f.isDirectory()) return true;
                        String ext = Utils.getExtension(f);
                        if (ext == null) return false;
                        String tmp = Utils.synxml;
                        boolean tmp2 = ext.equals(tmp);
                        return tmp2;
                        //return ext.equals(Utils.synxml);
                    }
                } );
                fc.addChoosableFileFilter(new FileFilter() {
                    public String getDescription() { return "Syntax File (compiled)"; }
                    public boolean accept(File f) {
                        if (f.isDirectory()) return true;
                        String ext = Utils.getExtension(f);
                        if (ext == null) return false;
                        String tmp = Utils.syn;
                        boolean tmp2 = ext.equals(tmp);
                        return tmp2;
                        //return ext.equals(Utils.syn);
                    }
                } );
                try {
                    if (f == null) {
                        new SyntaxEditor(fc).setVisible(true);
                    } else {
                        new SyntaxEditor(f, fc).setVisible(true);
                    }
                } catch (Throwable t) {
                    // Fixme - what to do?
                }
            }
        });
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SyntaxTree batchTree = null;
        File editFile = null;
        
        // Parse the command line
        for (String param : args) {
            if (param.startsWith("--edit=")) {
                editFile = new File(param.substring(7));
            }
            if (param.startsWith("--load=")) {
                try {
                    batchTree = new SyntaxTree(new GraphUndoManager(), new File(param.substring(7)));
                } catch (SAXException e) {
                }
            }
            if (param.startsWith("--savesyn=")) {
                if (batchTree == null)  {
                    System.out.println("No syntax tree loaded when trying to save");
                } else {
                    try {
                        batchTree.saveSyntaxAsCompiled(new File(param.substring(10)));
                    } catch (SAXException e) {
                    }
                }
            }
            if (param.startsWith("--savexml=")) {
                if (batchTree == null)  {
                    System.out.println("No syntax tree loaded when trying to save");
                } else {
                    try {
                        batchTree.saveSyntaxAsXML(new File(param.substring(10)));
                    } catch (SAXException e) {
                    }
                }
            }
            if (param.startsWith("--listclips")) {
                if (batchTree == null)  {
                    System.out.println("No syntax tree loaded when trying to show clips");
                } else {
                    Set<String> clips = batchTree.getSoundList();
                    for (String clip : clips) {
                        System.out.println(batchTree.getSoundClipFileName(clip));
                    }
                }
            }
            if (param.startsWith("--help")) {
                System.out.print(
                        "Usage: SyntaxEdit [--edit=filename]       for interactive edits\n" +
                        " or    SyntaxEdit --load=filename [--savexml=filename] [--savesyn=filename] [--listclips]\n" +
                        "             for batch usage.\n"+
                        "\n");  
            }
        }
        
        if (batchTree == null) {
            startEditor(editFile);
        }
    }
    
}
