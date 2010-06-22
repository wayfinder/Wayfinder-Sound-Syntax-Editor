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


import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JButton;

import java.util.Vector;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.lang.Exception;


public class GuideView extends javax.swing.JFrame {
    
    enum IconSize { SMALL, LARGE };
    
    static class TurnMapping {
        public final int turnCode;
        public final String description;
        public final ImageIcon smallIcon;
        public final ImageIcon bigIcon;
        
        public TurnMapping(int turnCode, final String description,
                final String bigIconName, final String smlIconName) {
            this.turnCode = turnCode;
            this.description = description;
            this.smallIcon = new ImageIcon(getClass().getResource("/res/images/"+smlIconName));
            this.bigIcon = new ImageIcon(getClass().getResource("/res/images/"+bigIconName));
        }
    }
    
    static Vector<TurnMapping> turnMap = new Vector<TurnMapping>();
    static {
        try {
            turnMap.add(new TurnMapping(0x0002, "Ahead",       "straight_ahead.png", "small_straight_arrow.gif"));
            turnMap.add(new TurnMapping(0x0003, "Left",        "left_arrow.png",     "small_left_arrow.gif"));
            turnMap.add(new TurnMapping(0x0004, "Right",       "right_arrow.png",    "small_right_arrow.gif"));
            turnMap.add(new TurnMapping(0x0005, "UTurn",       "u_turn.png",         "small_u_turn.gif"));
            //turnMap.add(new TurnMapping(0x0006, "StartAt" );
            turnMap.add(new TurnMapping(0x0007, "Finally",     "finish_arrow.png",   "small_flag.gif"));
            //turnMap.add(new TurnMapping(0x0008, "Enter roundab.");
            turnMap.add(new TurnMapping(0x0009, "Roundab. Exit",  "multiway_rdbt.png", "small_multiway_rdbt.gif"));
            turnMap.add(new TurnMapping(0x000a, "Roundab. Ahead", "rdbt_straight.png", "small_straight_arrow.gif"));
            turnMap.add(new TurnMapping(0x000b, "Roundab. Left",  "rdbt_left.png", "small_left_arrow.gif"));
            turnMap.add(new TurnMapping(0x000c, "Roundab. Right", "rdbt_right.png", "small_right_arrow.gif"));
            turnMap.add(new TurnMapping(0x0013, "Roundab. Uturn", "rdbt_uturn.png", "small_u_turn.gif"));
            turnMap.add(new TurnMapping(0x000d, "Exit ramp",      "exit_highway.png", "small_keep_right.gif"));
            turnMap.add(new TurnMapping(0x000e, "Enter ramp",     "enter_highway.png", "small_keep_right.gif"));
            turnMap.add(new TurnMapping(0x000f, "Park car",       "park_car.png", "park_car.png"));
            turnMap.add(new TurnMapping(0x0010, "Keep left",      "keep_left.png", "keep_left.png"));
            turnMap.add(new TurnMapping(0x0011, "Keep right",     "keep_right.png", "keep_right.png"));
            turnMap.add(new TurnMapping(0x0012, "Start uturn",    "u_turn.png", "small_u_turn.gif"));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    
    static class DistanceMapping {
        public final int dist;
        public final String label;
        public String toString() {
            return label;
        }
        DistanceMapping(int dist, String label) {
            this.dist=dist;
            this.label=label;
        }
    }
    static Vector<DistanceMapping> distanceMap = new Vector<DistanceMapping>();
    static {
        try {
            distanceMap.add(new DistanceMapping(3219, "2 miles"));
            distanceMap.add(new DistanceMapping(1609, "1 mile"));
            distanceMap.add(new DistanceMapping(803, "0.5 miles"));
            distanceMap.add(new DistanceMapping(402, "0.25 miles"));
            distanceMap.add(new DistanceMapping(152, "500 feet"));
            distanceMap.add(new DistanceMapping(61, "200 feet"));
            distanceMap.add(new DistanceMapping(31, "100 feet"));

            distanceMap.add(new DistanceMapping(182, "200 yards"));
            distanceMap.add(new DistanceMapping(91, "100 yards"));
            distanceMap.add(new DistanceMapping(46, "50 yards"));
            distanceMap.add(new DistanceMapping(23, "25 yards"));
            
            distanceMap.add(new DistanceMapping(2000, "2000 m"));
            distanceMap.add(new DistanceMapping(1000, "1000 m"));
            distanceMap.add(new DistanceMapping(500,  "500 m"));
            distanceMap.add(new DistanceMapping(200,  "200 m"));
            distanceMap.add(new DistanceMapping(100,  "100 m"));
            distanceMap.add(new DistanceMapping(50,   "50 m"));
            distanceMap.add(new DistanceMapping(25,   "25 m"));
            distanceMap.add(new DistanceMapping(0,    "0 m"));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    
    static class GpsQualityMapping {
        public final Condition.GpsQuality qual;
        public String toString() {
            return qual.toString();
        }
        GpsQualityMapping(Condition.GpsQuality qual) {
            this.qual=qual;
        }
    }
    static Vector<GpsQualityMapping> gpsQualityMap = new Vector<GpsQualityMapping>();
    static {
        try {
            gpsQualityMap.add(new GpsQualityMapping(Condition.GpsQuality.Missing));
            gpsQualityMap.add(new GpsQualityMapping(Condition.GpsQuality.Searching));
            gpsQualityMap.add(new GpsQualityMapping(Condition.GpsQuality.Useless));
            gpsQualityMap.add(new GpsQualityMapping(Condition.GpsQuality.Ok));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    static Vector<Integer> exitNoMap = new Vector<Integer>();
    static {
        for (int i=1; i<=10; ++i) {
            exitNoMap.add(i);
        }
    }
    
    SyntaxTree syntax;
    TurnMapping currTurn;
    TurnMapping nextTurn;
    
    private void setSelectedTurn(int turnNo, TurnMapping t) {
        switch (turnNo) {
            case 1:
                currTurn = t;
                break;
            case 2:
                nextTurn = t;
                break;
        }
    }
    
    
    /** Creates new form GuideView */
    public GuideView(SyntaxTree syntax) {
        initComponents();
        //setSize(300, 300);
        
        setupPopupMenu(jCurrXingList, 1, jCurrTurnButton, IconSize.LARGE);
        jCurrTurnButton.setText(turnMap.get(0).description);
        jCurrTurnButton.setIcon(turnMap.get(0).bigIcon);
        setSelectedTurn(1, turnMap.get(0));
        
        setupPopupMenu(jNextXingList, 2, jNextTurnButton, IconSize.SMALL);
        jNextTurnButton.setText(turnMap.get(0).description);
        jNextTurnButton.setIcon(turnMap.get(0).smallIcon);
        setSelectedTurn(2, turnMap.get(0));
        
        this.syntax = syntax;
    }
    
    /** Initialized the popup menus */
    private void setupPopupMenu(JPopupMenu menu,
            final int xingNo,
            final JButton button, final IconSize iconSize) {
        menu.removeAll();
        for (final TurnMapping t : turnMap) {
            Action a = new AbstractAction(t.description, t.smallIcon) {
                public void actionPerformed(ActionEvent e) {
                    setSelectedTurn(xingNo, t);
                    button.setText(t.description);
                    switch (iconSize) {
                        case LARGE:
                            button.setIcon(t.bigIcon);
                            break;
                        case SMALL:
                            button.setIcon(t.smallIcon);
                            break;
                    }
                }
                private static final long serialVersionUID = 42L;
            };
            JMenuItem m = new JMenuItem(a);
            menu.add(m);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 0;
        
        // Top bar - the next turn
        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());


        
        jNextExitNo = new javax.swing.JComboBox();
        jNextExitNo.setModel(new javax.swing.DefaultComboBoxModel(exitNoMap));
        topPanel.add(jNextExitNo);
        
        jNextTurnButton = new javax.swing.JButton();
        jNextTurnButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/images/small_right_arrow.gif")));
        jNextTurnButton.setText("rdbright");
        jNextTurnButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jNextTurnButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jNextTurnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jNextTurnButtonActionPerformed(evt);
            }
        });
        topPanel.add(jNextTurnButton);
        
        jNextDist = new javax.swing.JComboBox();
        jNextDist.setModel(new javax.swing.DefaultComboBoxModel(distanceMap));
        topPanel.add(jNextDist);
        
        
        getContentPane().add(topPanel, c);
        
        // Middle bar - the current turn
        final JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new FlowLayout());
        
        jCurrExitNo = new javax.swing.JComboBox();
        jCurrExitNo.setModel(new javax.swing.DefaultComboBoxModel(exitNoMap));
        middlePanel.add(jCurrExitNo);
        
        jCurrTurnButton = new javax.swing.JButton();
        jCurrTurnButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/images/rdbt_left.png")));
        jCurrTurnButton.setText("Roundab. left");
        jCurrTurnButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCurrTurnButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCurrTurnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCurrTurnButtonActionPerformed(evt);
            }
        });
        middlePanel.add(jCurrTurnButton);
        
        jCurrDist = new javax.swing.JComboBox();
        jCurrDist.setModel(new javax.swing.DefaultComboBoxModel(distanceMap));
        middlePanel.add(jCurrDist);
        
        c.gridy = 1;
        getContentPane().add(middlePanel, c);
        
        
        
        // bottom bar - misc stuff
        final JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        
        jStartNode = new javax.swing.JComboBox();
        jStartNode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SoundListNormal", "SoundListXing", "NewCrossing", "SoundListAtDest", "SoundListOffTrack", "SoundListSpeedCam", "SoundListGpsChange" }));
        bottomPanel.add(jStartNode);
        
        
        jGpsQuality = new javax.swing.JComboBox();
        jGpsQuality.setModel(new javax.swing.DefaultComboBoxModel(gpsQualityMap));
        bottomPanel.add(jGpsQuality);
        
        c.gridy = 2;
        getContentPane().add(bottomPanel, c);
        
        jExecutButton = new javax.swing.JButton();
        jExecutButton.setText("Test!");
        jExecutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jExecutButtonActionPerformed(evt);
            }
        });
        
        c.gridy = 3;
        getContentPane().add(jExecutButton, c);


        jNextXingList = new javax.swing.JPopupMenu();
        
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/images/rdbt_left.png")));
        jMenuItem1.setText("rdbt_left");

        jCurrXingList = new javax.swing.JPopupMenu();
        jCurrXingList.add(jMenuItem1);

        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/images/rdbt_right.png")));
        jMenuItem2.setText("rdbt_right");
        jCurrXingList.add(jMenuItem2);
        
        pack();

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    private void jExecutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jExecutButtonActionPerformed
        int currDist = ((DistanceMapping)(jCurrDist.getModel().getSelectedItem())).dist;
        int nextDist = ((DistanceMapping)(jNextDist.getModel().getSelectedItem())).dist;
        int firstExitNo = ((Integer)(jCurrExitNo.getModel().getSelectedItem()));
        int secondExitNo = ((Integer)(jNextExitNo.getModel().getSelectedItem()));
        Condition.TurnCode firstTurn = Condition.TurnCode.fromWfCode(currTurn.turnCode);
        Condition.TurnCode secondTurn = Condition.TurnCode.fromWfCode(nextTurn.turnCode);
        Condition.GpsQuality gpsQ = ((GpsQualityMapping)(jGpsQuality.getModel().getSelectedItem())).qual;
        String startNode = jStartNode.getModel().getSelectedItem().toString();
        Vector<String> clips = syntax.execute(currDist,
                firstTurn,
                firstExitNo,
                nextDist,
                secondTurn,
                secondExitNo,
                gpsQ,
                startNode);
        Vector<SoundClip> v = new Vector<SoundClip>();
        for (String c : clips) {
            v.add(syntax.soundClips.get(c));
        }
        new ClipPlayer(v, syntax.getSoundDirList());
    }//GEN-LAST:event_jExecutButtonActionPerformed
    
    private void jNextTurnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jNextTurnButtonActionPerformed
        jNextXingList.show(this, 10, 10);
    }//GEN-LAST:event_jNextTurnButtonActionPerformed
    
    private void jCurrTurnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCurrTurnButtonActionPerformed
        jCurrXingList.show(this, 10, 10);
    }//GEN-LAST:event_jCurrTurnButtonActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GuideView(null).setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JComboBox jCurrDist;
    public javax.swing.JComboBox jCurrExitNo;
    public javax.swing.JButton jCurrTurnButton;
    public javax.swing.JPopupMenu jCurrXingList;
    public javax.swing.JButton jExecutButton;
    public javax.swing.JComboBox jGpsQuality;
    public javax.swing.JMenuItem jMenuItem1;
    public javax.swing.JMenuItem jMenuItem2;
    public javax.swing.JComboBox jNextDist;
    public javax.swing.JComboBox jNextExitNo;
    public javax.swing.JButton jNextTurnButton;
    public javax.swing.JPopupMenu jNextXingList;
    public javax.swing.JComboBox jStartNode;
    // End of variables declaration//GEN-END:variables
    
    /** Shut up the compiler warnings
     */
    private static final long serialVersionUID = 42L;
}
