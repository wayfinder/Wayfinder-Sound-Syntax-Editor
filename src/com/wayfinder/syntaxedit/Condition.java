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

import java.lang.Character;

/**
 * A condition-based descicion element.
 * Can be initialized from a string, a graph element or from
 * the components.
 * A condition is made up of three elements:
 *   - a varaible (dist, xing, turn etc)
 *   - a relation ( =, <, >, >= != etc)
 *   - a limit in the form of an integer
 */
public class Condition {
    
    public enum Variable {
        ZERO(0, "zero"),
                DIST(1, "dist"),
                TURN(2, "turn"),
                XING(3, "xing"),
                EXIT(4, "exit"),
                SIDE(5, "side"),
                GPSQ(6, "gpsq");
        
        private int wfCode;
        private String graphName;
        Variable(int wfCode, final String graphName) {
            this.wfCode = wfCode;
            this.graphName = graphName;
        }
        
        int toWfCode() {
            return wfCode;
        }
        final String toGraphName() {
            return graphName;
        }
        
        static Variable fromWfCode(int c) {
            for (Variable v : Variable.values()) {
                if (v.toWfCode() == c) {
                    return v;
                }
            }
            return null;
        }
        
        static Variable fromGraphName(final String n) {
            for (Variable v : Variable.values()) {
                if (v.toGraphName().equals(n)) {
                    return v;
                }
            }
            return null;
        }
        
    };
    public enum Relation {
        EQ(0, new String[] {"==", "="} ),
                NE(1, "!="),
                LT(2, "<"),
                GT(3, ">"),
                LE(4, "<="),
                GE(5, ">=");
        
        private int wfCode;
        private String[] graphNames;
        Relation(int wfCode, String graphName) {
            this.wfCode = wfCode;
            this.graphNames = new String[] {graphName} ;
        }
        Relation(int wfCode, String[] graphNames) {
            this.wfCode = wfCode;
            this.graphNames = graphNames;
        }
        
        int toWfCode() {
            return wfCode;
        }
        final String toGraphName() {
            return graphNames[0];
        }
        
        static Relation fromWfCode(int c) {
            for (Relation v : Relation.values()) {
                if (v.toWfCode() == c) {
                    return v;
                }
            }
            return null;
        }
        
        static Relation fromGraphName(final String n) {
            for (Relation v : Relation.values()) {
                for (final String s : v.graphNames ) {
                    if (s.equals(n)) {
                        return v;
                    }
                }
            }
            return null;
        }
    }
    public enum TurnCode {
        None         (-1, "none"),
                Ahead        (0x0002, "ahead"),
                Left         (0x0003, "left"),
                Right        (0x0004, "right"),
                UTurn        (0x0005, "uturn"),
                StartAt      (0x0006, "startat"),
                Finally      (0x0007, "finally"),
                EnterRdbt    (0x0008, "enter_rdbt"),
                ExitRdbt     (0x0009, "exit_rdbt"),
                AheadRdbt    (0x000a, "ahead_rdbt"),
                LeftRdbt     (0x000b, "left_rdbt"),
                RightRdbt    (0x000c, "right_rdbt"),
                ExitAt       (0x000d, "exit_ramp"),
                On           (0x000e, "enter_ramp"),
                ParkCar      (0x000f, "park_car"),
                KeepLeft     (0x0010, "keep_left"),
                KeepRight    (0x0011, "keep_right"),
                StartWithUturn (0x0012, "start_uturn"),
                UTutrnRdbt   (0x0013, "uturn_rdbt"),
                //FollowRoad   (0x0014, "follow_road"),
                EnterFerry   (0x0015, "enter_ferry"),
                ExitFerry    (0x0016, "exit_ferry"),
                ChangeFerry  (0x0017, "change_ferry"),
                //EndOfRoadLeft (0x0018, "end_of_road_left"),
                //EndOfRoadRight (0x0019, "end_of_road_right"),
                OffRampLeft  (0x001a, "offramp_left"),
                OffRampRight(0x001b, "offramp_right");
        
        private int wfCode;
        private String graphName;
        TurnCode(int wfCode, String graphName) {
            this.wfCode = wfCode;
            this.graphName = graphName;
        }
        
        int toWfCode() {
            return wfCode;
        }
        final String toGraphName() {
            return graphName;
        }
        
        static TurnCode fromWfCode(int c) {
            for (TurnCode v : TurnCode.values()) {
                if (v.toWfCode() == c) {
                    return v;
                }
            }
            return null;
        }
        
        static TurnCode fromGraphName(final String n) {
            for (TurnCode v : TurnCode.values()) {
                if (v.toGraphName().equals(n)) {
                    return v;
                }
            }
            return null;
        }
    }
    public enum GpsQuality {
        Missing         (0, "missing"),
        Searching       (1, "searching"),
        Useless         (2, "useless"),
        Ok              (3, "ok");
        
        private int wfCode;
        private String graphName;
        GpsQuality(int wfCode, String graphName) {
            this.wfCode = wfCode;
            this.graphName = graphName;
        }
        
        int toWfCode() {
            return wfCode;
        }
        final String toGraphName() {
            return graphName;
        }
        
        static GpsQuality fromWfCode(int c) {
            for (GpsQuality v : GpsQuality.values()) {
                if (v.toWfCode() == c) {
                    return v;
                }
            }
            return null;
        }
        
        static GpsQuality fromGraphName(final String n) {
            for (GpsQuality v : GpsQuality.values()) {
                if (v.toGraphName().equals(n)) {
                    return v;
                }
            }
            return null;
        }
    }
    
    private Variable variable;
    private Relation relation;
    private int limit;
    private TurnCode turnCode;
    private GpsQuality gpsQuality;
    
    /** Creates a new instance of Condition */
    public Condition(final String relationString) {
        if (relationString == null) {
            variable = Variable.ZERO;
            relation = Relation.EQ;
            limit    = 1;
            return;
        }
        // Try to split the string into the constituent parts
        String str = relationString.trim();
        int i;
        for (i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if ( ! Character.isLetter(c)) {
                break;
            }
        }
        String varName = str.substring(0,  i);
        // FIXME - add error handling. What if there is no relation or no
        // limit?
        
        // Eat all space before the relation
        str = str.substring(i).trim();
        // The relation ends with the first character or digit
        for (i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if ( Character.isLetterOrDigit(c)) {
                break;
            }
        }
        String relName = str.substring(0, i).trim();
        String limitName = str.substring(i).trim();
        
        System.out.println("Got condition: var=\"" + varName + "\", rel=\""+relName+"\", limit=\""+limitName+"\"");
        setVariable(varName);
        setRelation(relName);
        setLimit(limitName);
        System.out.println("Found var:"+variable.ordinal()+", rel="+relation.ordinal()+", limit="+limit);
    }
    
    public Condition(Variable v, Relation r, int l) {
        variable = v;
        relation = r;
        limit = l;
        if (variable == Variable.TURN) {
            turnCode = TurnCode.fromWfCode(l);
        } else if (variable == Variable.GPSQ) {
            gpsQuality = GpsQuality.fromWfCode(l);
        }
    }
    
    private void setVariable(String v) {
        variable = Variable.fromGraphName(v);
        if (variable == null) {
            variable=Variable.ZERO;
        }
    }
    
    private void setRelation(String r) {
        relation = Relation.fromGraphName(r);
        if (relation == null) {
            relation=Relation.EQ;
        }
    }
    
    private void setLimit(String l) {
        if (variable==Variable.TURN) {
            // Look up allowed turn types
            turnCode = TurnCode.fromGraphName(l);
            if (turnCode == null) {
                turnCode=TurnCode.None;
            }
            limit = turnCode.toWfCode();
        } else if (variable==Variable.GPSQ) {
            // Look up allowed gps quality types
            gpsQuality = GpsQuality.fromGraphName(l);
            if (gpsQuality == null) {
                gpsQuality=GpsQuality.Missing;
            }
            limit = gpsQuality.toWfCode();
        } else {
            // Numeric limit
            limit = Integer.decode(l);
        }
    }
    
    public Variable getVariable() {
        return variable;
    }
    
    public Relation getRelation() {
        return relation;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public boolean evaluate(TurnCode turn, int distance, int xingNo, int exitNo,
            int side, GpsQuality gpsQ) {
        int testVal = 0;
        
        switch (variable) {
            case GPSQ:
                testVal = gpsQ.toWfCode();
                break;
            case DIST:
                testVal = distance;
                break;
            case EXIT:
                testVal = exitNo;
                break;
            case SIDE:
                testVal = side;
                break;
            case TURN:
                testVal = turn.toWfCode();
                break;
            case XING:
                testVal = xingNo;
                break;
            case ZERO:
                testVal = 0;
                break;
        }
        switch (relation) {
            case EQ:
                return testVal == limit;
            case NE:
                return testVal != limit;
            case GE:
                return testVal >= limit;
            case LE:
                return testVal <= limit;
            case GT:
                return testVal > limit;
            case LT:
                return testVal < limit;
        }
        return false; // Generic catch all - should never be needed. The
        // java compiler is not really smart enough.
    }
    
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append(variable.toGraphName());
        b.append(relation.toGraphName());
        if (variable == Variable.TURN) {
            b.append(turnCode.toGraphName());
        } else if (variable == Variable.GPSQ) {
            b.append(gpsQuality.toGraphName());
        } else {
            b.append(limit);
        }
        return new String(b);
    }
}
