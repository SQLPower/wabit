package ca.sqlpower.wabit.swingui;

/**
 * Classes that contain a mouse state should implement this interface.
 */
public interface MouseStatePane {

	/**
	 * The states a mouse can be on the query pen.
	 */
	public enum MouseStates {READY, CREATE_JOIN}
	
	public MouseStates getMouseState();
	
	public void setMouseState(MouseStates state);
}
