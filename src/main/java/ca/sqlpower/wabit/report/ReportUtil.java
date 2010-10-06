/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.wabit.report;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.FontSelector;

/**
 * This class contains a collection of static methods that are useful
 * when creating report classes. 
 */
public class ReportUtil {

    /**
     * This string is used to describe the default format when selecting a format.
     */
    public static final String DEFAULT_FORMAT_STRING = "Default Format";

    private ReportUtil() {
        //Cannot construct an instance of this class
    }

    /**
     * This returns a button to choose a font type. The font of the component
     * passed in will be used as the default font of the {@link FontSelector} when it
     * is opened. The font chosen will also be placed on this {@link JComponent} when a
     * font is selected.
     * 
     * @param fontTarget
     *            The component that will have its font modified when the user
     *            chooses a new font. The parent window of this component will
     *            be used as a parent for the font selector.
     * @return A button that will display a {@link FontSelector} to choose a new font
     *         for the JComponent.
     */
    public static JButton createFontButton(final JComponent fontTarget) {
        JButton button = new JButton("Choose...");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FontSelector fs = new FontSelector(fontTarget.getFont());
                Window dialogParent = SwingUtilities.getWindowAncestor(fontTarget);
                JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(fs, dialogParent, "Choose a Font", "OK");
                d.setModal(true);
                d.setVisible(true);
                fontTarget.setFont(fs.getSelectedFont());
            }
        });
        return button;
    }

    /**
     * This returns a list of several different {@link DecimalFormat}s that can
     * be used in number formatting.
     */
    public static List<DecimalFormat> getNumberFormats() {
        List<DecimalFormat> numberFormats = new ArrayList<DecimalFormat>();
        
        numberFormats.add(new DecimalFormat("0"));
        numberFormats.add(new DecimalFormat("#,##0.00"));
        numberFormats.add(new DecimalFormat("#,##0.00%"));
        
        numberFormats.add(new DecimalFormat("(#,000.00)"));
        numberFormats.add(new DecimalFormat("(#,000)"));
        
        numberFormats.add(new DecimalFormat("$#,##0.00"));
        numberFormats.add(new DecimalFormat("€#,##0.00"));
        numberFormats.add(new DecimalFormat("£#,##0.00"));
        numberFormats.add(new DecimalFormat("¥#,##0.00"));
        
        numberFormats.add(new DecimalFormat("$#,##0"));
        numberFormats.add(new DecimalFormat("€#,##0"));
        numberFormats.add(new DecimalFormat("£#,##0"));
        numberFormats.add(new DecimalFormat("¥#,##0"));
        

        numberFormats.add(new DecimalFormat("##0.##E0"));
        
        numberFormats.add((DecimalFormat)NumberFormat.getCurrencyInstance());
        numberFormats.add((DecimalFormat)NumberFormat.getInstance());
        numberFormats.add((DecimalFormat)NumberFormat.getPercentInstance());
        
        return numberFormats;
    }
    
}
