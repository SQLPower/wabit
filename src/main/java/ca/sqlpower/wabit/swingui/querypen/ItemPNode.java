package ca.sqlpower.wabit.swingui.querypen;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.border.LineBorder;

import ca.sqlpower.wabit.swingui.event.ExtendedStyledTextEventHandler;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolox.event.PStyledTextEventHandler;
import edu.umd.cs.piccolox.nodes.PStyledText;
import edu.umd.cs.piccolox.pswing.PSwing;

/**
 * This PNode represents a SQL column on a table.
 */
public class ItemPNode extends PNode {

	private final Item item;
	
	/**
	 * A text area to allow showing the name of the sql column and
	 * for editing its alias.
	 */
	private final PStyledText columnText;
	
	/**
	 * The user defined alias for this sql column.
	 */
	private String aliasText;

	/**
	 * The editor pane for editing the column alias.
	 */
	private final JEditorPane nameEditor;

	/**
	 * A focus listener to properly display the alias and column name.
	 * This focus listener for the nameEditor will show only the alias
	 * when the alias is being edited and will show the alias and column
	 * name otherwise.
	 */
	private FocusListener editorFocusListener = new FocusListener() {
		public void focusLost(FocusEvent e) {
			aliasText = nameEditor.getText();
			if (nameEditor.getText() != null && nameEditor.getText().length() > 0 && !nameEditor.getText().equals(item.getName())) {
				nameEditor.setText(aliasText + " (" + item.getName() + ")");
			} else {
				nameEditor.setText(item.getName());
			}
			styledTextEventHandler.stopEditing();
		}
		public void focusGained(FocusEvent e) {
			if (aliasText != null && aliasText.length() > 0) {
				nameEditor.setText(aliasText);
			}
		}
	};

	/**
	 * The text handler for the PStyledTextArea. This will allow click
	 * to edit on the text.
	 */
	private PStyledTextEventHandler styledTextEventHandler;
	
	public ItemPNode(MouseState mouseStates, PCanvas canvas, Item item) {
		this.item = item;
		aliasText = "";
		
		PSwing swingCheckBox = new PSwing(new JCheckBox());
		addChild(swingCheckBox);
		
		columnText = new PStyledText();
		nameEditor = new JEditorPane();
		nameEditor.setBorder(new LineBorder(nameEditor.getForeground()));
		nameEditor.setText(item.getName());
		columnText.setDocument(nameEditor.getDocument());
		styledTextEventHandler = new ExtendedStyledTextEventHandler(mouseStates, canvas, nameEditor);
		columnText.addInputEventListener(styledTextEventHandler);
		nameEditor.addFocusListener(editorFocusListener);
		columnText.translate(swingCheckBox.getFullBounds().width, (swingCheckBox.getFullBounds().height - columnText.getFullBounds().height)/2);
		addChild(columnText);
	}

	public Item getItem() {
		return item;
	}

	public PStyledText getColumnText() {
		return columnText;
	}

}
