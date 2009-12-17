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

package ca.sqlpower.wabit.swingui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import ca.sqlpower.object.SPVariableHelper;

/**
 * A button that shows a popup menu of variables when it is clicked.
 * If any item in the resulting popup menu is selected, it will result
 * in the variable's name being inserted into a document.
 */
public class InsertVariableButton extends JButton {

    private static final Logger logger = Logger.getLogger(InsertVariableButton.class);
    
    private static final char DOWN_ARROW = '\u25be';
    
    private final SPVariableHelper variableHelper;
    private final JTextComponent insertInto;

	private final String variableNamespace;

    public InsertVariableButton(SPVariableHelper variableHelper, JTextComponent insertInto, String variableNamespace) {
        super("Variable " + DOWN_ARROW);
        this.variableHelper = variableHelper;
        this.insertInto = insertInto;
		this.variableNamespace = variableNamespace;
        addActionListener(clickHandler);
    }
    
    private final ActionListener clickHandler = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            JPopupMenu menu = new JPopupMenu();
            for (String varname : variableHelper.keySet(variableNamespace)) {
                menu.add(new InsertVariableAction(SPVariableHelper.stripNamespace(varname), varname));
                logger.debug("Added new item for " + varname);
            }
            Component invoker = (Component) e.getSource();
            logger.debug("Popup invoked by " + invoker);
            menu.show(invoker, invoker.getHeight(), 0);
        }
    };
    
    private class InsertVariableAction extends AbstractAction {
        
        private final String varName;

        InsertVariableAction(String label, String varName) {
            super(label);
            this.varName = varName;
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
                insertInto.getDocument().insertString(insertInto.getCaretPosition(), "${" + varName + "}", null);
            } catch (BadLocationException ex) {
                throw new RuntimeException("Unexpected bad location exception", ex);
            }
        }
    }
}
