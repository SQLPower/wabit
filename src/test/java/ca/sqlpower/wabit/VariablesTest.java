/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Wabit.
 *
 * Wabit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wabit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.wabit;

import junit.framework.TestCase;

public class VariablesTest extends TestCase {

    private TestingVariableContext varContext;
    
    protected void setUp() throws Exception {
        super.setUp();
        varContext = new TestingVariableContext();
        varContext.setVariable("animal", "cow");
        varContext.setVariable("name", "Bessie");
        varContext.setVariable("animal.cow.plural", "cows");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSubstituteSeveral() {
        String substituted = Variables.substitute("I own a ${animal} named ${name}", varContext);
        assertEquals("I own a cow named Bessie", substituted);
    }

    public void testSubstituteMiddle() {
        String substituted = Variables.substitute("I own a ${animal} who goes moo", varContext);
        assertEquals("I own a cow who goes moo", substituted);
    }

    public void testSubstituteMissingVars() {
        String substituted = Variables.substitute("I own a ${car} named ${name}", varContext);
        assertEquals("I own a MISSING_VAR:car named Bessie", substituted);
    }

    public void testSubstituteEscapedVars() {
        String substituted = Variables.substitute("I own a ${$}{car} named ${name}", varContext);
        assertEquals("I own a ${car} named Bessie", substituted);
    }

    public void testSubstituteOnlyVariable() {
        String substituted = Variables.substitute("${name}", varContext);
        assertEquals("Bessie", substituted);
    }

    public void testSubstituteOnlyEscapedVariable() {
        String substituted = Variables.substitute("${$}{name}", varContext);
        assertEquals("${name}", substituted);
    }

    public void testSubstituteNoVariableRefs() {
        String substituted = Variables.substitute("just a plain message", varContext);
        assertEquals("just a plain message", substituted);
    }

    public void testSubstituteEscapedDollarsign() {
        String substituted = Variables.substitute("bowling for ${$} escaped", varContext);
        assertEquals("bowling for $ escaped", substituted);
    }
    
    public void testDottedName() {
    	String substituted = Variables.substitute("bowling for ${animal.cow.plural}", varContext);
        assertEquals("bowling for cows", substituted);
    }

}
