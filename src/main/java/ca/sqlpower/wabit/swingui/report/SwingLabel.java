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

package ca.sqlpower.wabit.swingui.report;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.FontSelector;
import ca.sqlpower.swingui.LabelEditorPanel;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.enterprise.client.ServerInfoProvider;
import ca.sqlpower.wabit.report.WabitLabel;
import ca.sqlpower.wabit.report.ReportContentRenderer.BackgroundColours;
import ca.sqlpower.wabit.swingui.WabitSwingSessionImpl;
import edu.umd.cs.piccolo.event.PInputEvent;

public class SwingLabel implements SwingContentRenderer {
    
    private static final Logger logger = Logger.getLogger(SwingLabel.class);
    
    private final WabitLabel renderer;

	private final SPVariableHelper variablesHelper;

    public SwingLabel(WabitLabel renderer) {
        this.renderer = renderer;
        this.variablesHelper = new SPVariableHelper(renderer);
    }
    
    public DataEntryPanel getPropertiesPanel() {
    	return new LabelEditorPanel(renderer, true) {

			@Override
			public List<Color> getBackgroundColours() {
				List<Color> colours = new ArrayList<Color>();
				for (BackgroundColours bgColour : BackgroundColours.values()) {
		            colours.add(bgColour.getColour());
		        }
				return colours;
			}

			@Override
			public FontSelector getFontSelector() {
				if (WabitUtils.getWorkspace(renderer).isServerWorkspace()) {
		        	List<String> fonts;
					try {
						fonts = ServerInfoProvider.getServerFonts(
		    				((WabitSwingSessionImpl)WabitUtils.getWorkspace(renderer).getSession()).getEnterpriseServerInfos());
					} catch (Exception e1) {
						throw new RuntimeException("Failed to obtain a list of available fonts from the server.");
					}
					return new FontSelector(
							renderer.getFont(), 
							fonts.toArray(new String[fonts.size()]),
							WabitUtils.getWorkspace(renderer).getSession().getFontLoader());
		        } else {
		        	return new FontSelector(renderer.getFont());
		        }
			}

			@Override
			public SPVariableHelper getVariablesHelper() {
				return variablesHelper;
			}
    		
    	};
    }

    public void processEvent(PInputEvent event, int type) {
        //do something cool here later
    }
}
