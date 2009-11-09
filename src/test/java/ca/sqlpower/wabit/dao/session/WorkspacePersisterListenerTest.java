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

package ca.sqlpower.wabit.dao.session;

import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.dao.CountingWabitPersister;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.swingui.StubWabitSwingSession;
import junit.framework.TestCase;

public class WorkspacePersisterListenerTest extends TestCase {
	
	/**
	 * Tests that persisting an object will persist the objects children as well.
	 * @throws Exception
	 */
	public void testPersistObject() throws Exception {
		WabitWorkspace workspace = new WabitWorkspace();
		CountingWabitPersister counter = new CountingWabitPersister();
		WorkspacePersisterListener listener = new WorkspacePersisterListener(
				new StubWabitSwingSession(), counter);

		WabitImage firstImage = new WabitImage();
		workspace.addImage(firstImage);
		
		WabitImage image = new WabitImage();
		workspace.addImage(image);
		
		listener.persistObject(workspace);
		
		assertEquals(3, counter.getPersistObjectCount());
		
		assertEquals(image.getUUID(), counter.getLastPersistObject().getUUID());
	}

}
