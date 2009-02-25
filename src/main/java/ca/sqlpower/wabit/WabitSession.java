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

import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;

/**
 * The basic interface for a Wabit session. This interface provides all the
 * UI-independent state and behaviour of a Wabit session. 
 */
public interface WabitSession {

	public void addSessionLifecycleListener(SessionLifecycleListener<WabitSession> l);

	public void removeSessionLifecycleListener(SessionLifecycleListener<WabitSession> l);

	/**
	 * Returns the context this session belongs to.
	 */
	public WabitSessionContext getContext();

	/**
	 * Ends this session, disposing its frame and releasing any system
	 * resources that were obtained explicitly by this session. Also
	 * fires a sessionClosing lifecycle event, so any resources used up
	 * by subsystems dependent on this session can be freed by the appropriate
	 * parties.
	 * 
	 * @return True if the session was successfully closed. False if the
	 * session did not close due to an error or user intervention.
	 */
	public boolean close();

	/**
	 * Returns the project associated with this session.
	 */
	public WabitProject getProject();

	/**
	 * This will create a UserPrompter to let users decide the given question.
	 * Questions can be yes/no questions if the response type is
	 * {@link UserPromptType#BOOLEAN}, a question about data sources letting the
	 * user create or select a database if the response type is
	 * {@link UserPromptType#DATA_SOURCE}, or a question about files, allowing
	 * the user to select or create a file if the response type is
	 * {@link UserPromptType#FILE}.
	 * 
	 * @param question
	 *            The question the user is being asked. This can be a template
	 *            with format arguments given in the promptUser of the
	 *            UserPrompter.
	 * @param okText
	 *            The text for the ok button defined by the question and the
	 *            type of response expected.
	 * @param newText
	 *            The text for the new button defined by the question and the
	 *            type of response. This action should create a new instance of
	 *            the response type if this option is selected. This is not used
	 *            for boolean type prompters.
	 * @param notOkText
	 *            The text for the not ok button. This button should define the
	 *            current question should not be done but does not mean to
	 *            cancel the entire operation if the question is only about part
	 *            of the action which created this prompt.
	 * @param cancelText
	 *            The text for the cancel button. This button should stop the
	 *            action this prompter is asking about and undo what the action
	 *            was doing.
	 * @param responseType
	 *            The type of response the UserPrompter should be returning.
	 *            This depends on what the question is asking about.
	 * @param defaultResponseType
	 *            The default response type to be selected by the user.
	 * @param defaultResponse
	 *            The default response to be given if the default response is
	 *            used.
	 */
	public UserPrompter createUserPrompter(String question, UserPromptType responseType, UserPromptOptions optionType,
			UserPromptResponse defaultResponseType, Object defaultResponse, String ... buttonNames);
	
}
