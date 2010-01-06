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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.text.JTextComponent;

import ca.sqlpower.object.SPVariableHelper;

/**
 * A button that shows a popup menu of variables when it is clicked.
 * If any item in the resulting popup menu is selected, it will result
 * in the variable's name being inserted into a document.
 */
public class InsertVariableButton extends JButton {

    private static final char DOWN_ARROW = '\u25be';
    private final SPVariableHelper variableHelper;
    private final JTextComponent insertInto;
	private final String variableNamespace;
	private final InsertVariableButton reference;

    public InsertVariableButton(SPVariableHelper variableHelper, JTextComponent insertInto, String variableNamespace) {
        super("Variable " + DOWN_ARROW);
        this.variableHelper = variableHelper;
        this.insertInto = insertInto;
		this.variableNamespace = variableNamespace;
		this.reference = this;
        addActionListener(clickHandler);
    }
    
    private final ActionListener clickHandler = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            variableHelper.promptAndInsertVariable(variableNamespace, reference, insertInto);
        }
    };
}
