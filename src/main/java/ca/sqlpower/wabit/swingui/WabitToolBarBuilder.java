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

package ca.sqlpower.wabit.swingui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.JToolBar.Separator;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

/**
 * A lot of the JToolBars that were being added to Wabit were coded with a lot
 * of boilerplate code. In particular, buttons were having their text set to be
 * at the bottom and centred, and having a client property set to that they
 * would render without button borders on OS X (Leopard and later (hopefully))
 * Also, each toolbar was being placed with a 'Wabit Logo' button on the right
 * that opens to the support forum. This class is intended to centralize most of
 * the boilerplate code so that making a JToolBar that looks like all the other
 * JToolBars in the Wabit application doesn't require so much repeated code.
 */
public class WabitToolBarBuilder {
    
    private static final Logger logger = Logger.getLogger(WabitToolBarBuilder.class);
    
    private static final ImageIcon MORE_ICON = 
        new ImageIcon(WabitToolBarBuilder.class.getClassLoader().getResource("icons/more-16.png"));
    
    /**
    * Taken from http://forums.sun.com/thread.jspa?forumID=256&threadID=405848
    * posted by camickr
    * <p>
    * Modifications made to improve component layout. A component can be added with
    * the constraint {@value #RIGHT_IMAGE_CONSTRAINT} to be placed on the far right of the
    * tool bar. 
    * 
    * @author subanark
    */
    private class ExpandLayout implements java.awt.LayoutManager {
        public static final String RIGHT_IMAGE_CONSTRAINT = "RIGHT_IMAGE_CONSTRAINT";
        
        private Component rightComponent;
        
        private JPopupMenu extenderPopup = new JPopupMenu();
        private JButton extenderButton = new JButton(new PopupAction());
     
        /** Creates a new instance of ExpandLayout */
        public ExpandLayout() {
            extenderPopup.setLayout(new MigLayout("wrap 1, align 50% 50%"));
        }
     
        /** If the layout manager uses a per-component string,
        * adds the component <code>comp</code> to the layout,
        * associating it
        * with the string specified by <code>name</code>.
        *
        * @param name the string to be associated with the component
        * @param comp the component to be added
        */
        public void addLayoutComponent(String name, Component comp) {
            if (RIGHT_IMAGE_CONSTRAINT.equals(name)) {
                rightComponent = comp;
            }
        }
     
        /**
        * Lays out the specified container.
        * @param parent the container to be laid out
        */
        public void layoutContainer(Container parent) {
            //  Position all buttons in the container
            
            resetParent(parent);
     
            Insets insets = parent.getInsets();
            int x = insets.left;
            int spaceUsed = insets.right + insets.left;
            
            if (rightComponent != null) {
                spaceUsed += rightComponent.getPreferredSize().getWidth();
            }
            
            //Find what components fit in the available space
            int componentCountThatFits = 0;
            for (; componentCountThatFits < parent.getComponentCount(); componentCountThatFits++) {
                Component aComponent = parent.getComponent(componentCountThatFits);
                if (aComponent == rightComponent) continue; //don't add to space estimate
                int componentWidth = aComponent.getPreferredSize().width;
                spaceUsed += componentWidth;  
                if (spaceUsed > parent.getSize().getWidth()) {
                    spaceUsed -= componentWidth;
                    break;
                }
            }
            if (componentCountThatFits < parent.getComponentCount()
                    && spaceUsed + extenderButton.getWidth() > parent.getSize().getWidth()) {
                componentCountThatFits--;
            }
     
            //position buttons being added to the tool bar.
            for (int i = 0; i < componentCountThatFits; i++ ) {
                Component aComponent = parent.getComponent(i);
                if (aComponent == rightComponent) continue; //add last
                if (logger.isDebugEnabled()) {
                    logger.debug("Component " + aComponent.getClass() + " number " + i + " has " +
                    		"preferred size " + aComponent.getPreferredSize());
                }
                aComponent.setSize(aComponent.getPreferredSize());
                if (aComponent instanceof Separator) {
                    aComponent.setSize(aComponent.getWidth(), parent.getHeight());
                }
                aComponent.setLocation(x, (parent.getHeight() - aComponent.getHeight()) / 2);
                int componentWidth = aComponent.getPreferredSize().width;
                x += componentWidth;
            }
            
            if (rightComponent != null) {
                rightComponent.setSize(rightComponent.getPreferredSize());
                rightComponent.setLocation(parent.getWidth() - insets.right - rightComponent.getWidth(), 
                        (parent.getHeight() - rightComponent.getHeight()) / 2);
            }
     
            //  All the buttons won't fit, add extender button
     
            if (componentCountThatFits < parent.getComponentCount()) {
                add(extenderButton);
                extenderButton.setSize( extenderButton.getPreferredSize() );
                extenderButton.setLocation(x, 
                        parent.getHeight() - extenderButton.getHeight() - insets.bottom);
            }
     
            //  Remove buttons that don't fit and add to the popup menu
     
            for (int i = parent.getComponentCount() - 2; i >= componentCountThatFits; i--) {
                Component aComponent = parent.getComponent(i);
                if (aComponent == rightComponent) continue;
                parent.remove(i);
                extenderPopup.add(aComponent, "align 50% 50%", 0);
            }
        }
     
        /**
        * Calculates the minimum size dimensions for the specified
        * container, given the components it contains.
        * @param parent the component to be laid out
        * @see #preferredLayoutSize
        */
        public Dimension minimumLayoutSize(Container parent) {
            return extenderButton.getMinimumSize();
        }
     
        /** Calculates the preferred size dimensions for the specified
        * container, given the components it contains.
        * @param parent the container to be laid out
        *
        * @see #minimumLayoutSize
        */
        public Dimension preferredLayoutSize(Container parent) {
            //  Move all components to the container and remove the extender button
     
            resetParent(parent);
     
            //  Calculate the width of all components in the container
     
            Dimension d = new Dimension();
            d.width += parent.getInsets().right + parent.getInsets().left;
     
            for (int i = 0; i < parent.getComponents().length; i++) {
                d.width += parent.getComponent(i).getPreferredSize().width;
                d.height = Math.max(d.height,parent.getComponent(i).getPreferredSize().height);
            }
     
            d.height += parent.getInsets().top + parent.getInsets().bottom + 5;
            return d;
        }

        private void resetParent(Container parent) {
            parent.remove(extenderButton);
     
            while (extenderPopup.getComponentCount() > 0) {
                Component aComponent = extenderPopup.getComponent(0);
                extenderPopup.remove(aComponent);
                parent.add(aComponent);
            }
        }
     
        /** Removes the specified component from the layout.
        * @param comp the component to be removed
        */
        public void removeLayoutComponent(Component comp) {
            if (comp == rightComponent) {
                rightComponent = null;
            }
        }
     
        protected class PopupAction extends AbstractAction {
            public PopupAction() {
                super("", MORE_ICON);
            }
     
            public void actionPerformed(ActionEvent e) {
                JComponent component = (JComponent)e.getSource();
                extenderPopup.show(component,0,component.getHeight());
            }
        }
     
    }
    
	private JToolBar buttonBar;
	
	/**
	 * This is the Wabit logo button that takes the user to the forums.
	 */
    private JButton forumButton = new JButton(WabitSwingSessionContextImpl.FORUM_ACTION);
	
	public WabitToolBarBuilder() {
		forumButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		buttonBar = new JToolBar();
		buttonBar.setFloatable(false);
		buttonBar.setLayout(new ExpandLayout());
		buttonBar.add(forumButton, ExpandLayout.RIGHT_IMAGE_CONSTRAINT);
	}
	
	/**
	 * Get the final JToolBar component. Typically you would call this once
	 * you're done adding all the buttons and components you want.
	 * 
	 * @return An instance of JToolBar with specially formatted JButtons and a
	 *         Wabit button at the end.
	 */
	public JToolBar getToolbar() {
		return buttonBar;
	}
	
	/**
	 * Adds an Action as a button to the toolbar.
	 * 
	 * @param action
	 *             The Action to add to the toolbar. Its name will be the button's label
	 */
	public void add(Action action) {
        add(action, null);
        
    }
	
	/**
	 * Adds an Action as a button to the toolbar.
	 * 
	 * @param a
	 *            The Action to add to the toolbar
	 * @param text
	 *            The text to set as the button's label
	 *            If text is null, toolbar button will get name from <code>a</code>
	 */
    public void add(Action a, String text) {
        add(a, text, null);
    }

    /**
     * Add an Action that will be added as a button to the toolbar. If the icon
     * is not null it will be placed on the button.
     * 
     * @param a
     *            The Action which to add to the ToolBar
     * @param text
     *            The text to set as the button's label.
     *            If text is null, ToolBar button will get name from <code>a</code>
     * @param icon
     *            The icon that will be placed on the button. If this is null
     *            then the icon defined in the action will be used.
     */
	public void add(Action a, String text, Icon icon) {
		add(new JButton(a), text, icon);
	}

    /**
     * Add a button that will be added as a button to the toolbar. This method
     * will change the look of the given button to have its text below the icon
     * among other things. If the icon is not null it will be placed on the
     * button.
     * 
     * @param button
     *            The button which to add to the ToolBar
     * @param text
     *            The text to set as the button's label.
     *            If null, the button's text will not be changed.
     * @param icon
     *            The icon that will be placed on the button. If this is null
     *            then the icon defined in the action will be used.
     */
	public void add(JButton button, String text, Icon icon) {
	    if (icon != null) {
	        button.setIcon(icon);
	    }
	    button.setVerticalTextPosition(SwingConstants.BOTTOM);
	    button.setHorizontalTextPosition(SwingConstants.CENTER);
	    // Remove button borders in OS X Leopard (and hopefully future OS X releases)
        button.putClientProperty("JButton.buttonType", "toolbar");
	    if (text != null) {
	        button.setText(text);
	    }
	    buttonBar.add(button);
	}
	
	/**
	 * Add a button to the tool bar. Sets the button type for OSX to remove borders.
	 */
	public void add(JButton button) {
	    // Remove button borders in OS X Leopard (and hopefully future OS X releases)
	    button.putClientProperty("JButton.buttonType", "toolbar");
	    buttonBar.add(button);
	}
	
	/**
	 * Add a component to the toolbar. 
	 */
	public void add(Component component) {
		buttonBar.add(component);
	}
	
	/**
	 * Add a separatot to the toolbar.
	 */
	public void addSeparator() {
		buttonBar.addSeparator();
	}

    /**
     * This will remove all of the buttons from the button bar of this tool bar.
     * This will not remove the forum button.
     */
    public void clear() {
        buttonBar.removeAll();
    }

}
