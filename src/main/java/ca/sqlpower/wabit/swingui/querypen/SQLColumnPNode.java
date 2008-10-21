package ca.sqlpower.wabit.swingui.querypen;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JEditorPane;
import javax.swing.border.LineBorder;

import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.wabit.swingui.event.ExtendedStyledTextEventHandler;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolox.event.PStyledTextEventHandler;
import edu.umd.cs.piccolox.nodes.PStyledText;

/**
 * This PNode represents a SQL column on a table.
 */
public class SQLColumnPNode extends PNode {

	/**
	 * This will become a SQLColumn later when it is refactored.
	 */
	private final SQLObject sqlColumn;
	
	/**
	 * A text area to allow showing the name of the sql column and
	 * for editing its alias.
	 */
	private final PStyledText columnText;
	
	public SQLColumnPNode(MouseStatePane mouseStates, PCanvas canvas, SQLObject sqlColumn) {
		this.sqlColumn = sqlColumn;
		columnText = new PStyledText();
		JEditorPane nameEditor = new JEditorPane();
		nameEditor.setBorder(new LineBorder(nameEditor.getForeground()));
		nameEditor.setText(sqlColumn.getName());
		columnText.setDocument(nameEditor.getDocument());
		final PStyledTextEventHandler styledTextEventHandler = new ExtendedStyledTextEventHandler(mouseStates, canvas, nameEditor);
		columnText.addInputEventListener(styledTextEventHandler);
		nameEditor.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				styledTextEventHandler.stopEditing();
			}
			public void focusGained(FocusEvent e) {
				//no-op
			}
		});
		addChild(columnText);
	}

	public SQLObject getSqlColumn() {
		return sqlColumn;
	}

	public PStyledText getColumnText() {
		return columnText;
	}

}
