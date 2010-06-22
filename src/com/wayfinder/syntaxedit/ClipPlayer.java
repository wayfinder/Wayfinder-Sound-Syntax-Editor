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

import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.IOException;

public class ClipPlayer {
    
    /** Creates a new instance of ClipPlayer */
    public ClipPlayer(Vector<SoundClip> clips, List<String> soundDirs) {
        List<AudioInputStream> streams = new ArrayList<AudioInputStream>();
        for (SoundClip c : clips) {
            File f;
            String fileName = c.getFileName();
            for (String dir : soundDirs) {
                String fullName = dir + fileName + ".wav";
                System.out.println("Checking sound file "+fullName);
                f = new File(fullName);
                if (f.canRead()) {
                    try {
                        System.out.println("Try to load sound "+fullName);
                        AudioInputStream str = AudioSystem.getAudioInputStream(f);
                        AudioFormat strForm = str.getFormat();
                        AudioFormat decForm = new AudioFormat(
                                AudioFormat.Encoding.PCM_SIGNED,
                                strForm.getSampleRate(),
                                16,
                                strForm.getChannels(),
                                strForm.getChannels()*2,
                                strForm.getSampleRate(),
                                false);
                        AudioInputStream decStr = AudioSystem.getAudioInputStream(decForm, str);
                        streams.add(decStr);
                    } catch (IOException i) {
                        System.out.println("Unable to open the file " + fileName);
                    } catch (UnsupportedAudioFileException u){
                        System.out.println("Unsupported audio file " + fileName);
                    }
                    break;
                }
            }
        }
        
        if (streams.size()<=0) {
            System.out.println("No audio clips to play");
            return;
        }
        AudioFormat format = streams.get(0).getFormat();
        SequenceAudioInputStream catStr =
                new SequenceAudioInputStream(format, streams);
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(catStr);
            //clip.open(streams.get(0));
            clip.start();
        } catch (LineUnavailableException l) {
            System.out.println("Audio line is not available");
        } catch (IOException i) {
            System.out.println("Unable to open a clip file ");
        }
    }
    
}
