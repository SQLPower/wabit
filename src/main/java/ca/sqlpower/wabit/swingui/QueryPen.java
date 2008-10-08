package ca.sqlpower.wabit.swingui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolox.pswing.PSwing;
import edu.umd.cs.piccolox.pswing.PSwingCanvas;
import edu.umd.cs.piccolox.swing.PScrollPane;

/**
 * The pen where users can graphically create sql queries.
 */
public class QueryPen {
	
	protected static final double ZOOM_CONSTANT = 0.1;

	/**
	 * The scroll pane that contains the visual query a user is building.
	 */
	private final JScrollPane scrollPane;

	/**
	 * The Piccolo canvas that allows zooming and the JComponents are placed in.
	 */
	private final PSwingCanvas canvas;
	
	private final JButton zoomInButton;
	private final JButton zoomOutButton;
	
	public static JPanel createQueryPen() {
		JPanel panel = new JPanel();
		QueryPen pen = new QueryPen();
        panel.setLayout(new BorderLayout());
        panel.add(pen.getScrollPane(), BorderLayout.CENTER);
        ButtonStackBuilder buttonStack = new ButtonStackBuilder();
        buttonStack.addGridded(pen.getZoomInButton());
        buttonStack.addRelatedGap();
        buttonStack.addGridded(pen.getZoomOutButton());
        panel.add(buttonStack.getPanel(), BorderLayout.EAST);
		return panel;
	}

	public QueryPen() {
		canvas = new PSwingCanvas();
		scrollPane = new PScrollPane(canvas);
        PLayer l = canvas.getLayer();

        JSlider js = new JSlider( 0, 100 );
        js.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                System.out.println( "e = " + e );
            }
        } );
        js.setBorder( BorderFactory.createTitledBorder( "Test JSlider" ) );
        PSwing pSwing = new PSwing( js );
        pSwing.translate( 100, 100 );
        l.addChild( pSwing );

        canvas.setPanEventHandler( null );
        
        zoomInButton = new JButton(new AbstractAction("Zoom In") {
        	public void actionPerformed(ActionEvent e) {
        		PCamera camera = canvas.getCamera();
        		camera.setViewScale(camera.getViewScale() + ZOOM_CONSTANT);
        	}
        });
        zoomOutButton = new JButton(new AbstractAction("Zoom Out"){
			public void actionPerformed(ActionEvent e) {
				PCamera camera = canvas.getCamera();
        		camera.setViewScale(camera.getViewScale() - ZOOM_CONSTANT);
			}
		});
	}
	
	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public JButton getZoomInButton() {
		return zoomInButton;
	}

	public JButton getZoomOutButton() {
		return zoomOutButton;
	}
}
