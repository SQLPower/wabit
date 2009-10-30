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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;

import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompterFactory;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;

public class WabitUtils {
    
    private static final Logger logger = Logger.getLogger(WabitUtils.class);

    /**
     * Adds the given listeners to the hierarchy of Wabit objects rooted at
     * <code>root</code>.
     * 
     * @param root
     *            The object at the top of the subtree to listen to. Must not be
     *            null.
     * @param wcl
     *            The Wabit child listener to add to root and all its
     *            WabitObject descendants. If you do not want Wabit child
     *            events, you can provide null for this parameter.
     */
    public static void listenToHierarchy(WabitObject root, WabitListener wcl) {
        root.addWabitListener(wcl);
        for (WabitObject wob : root.getChildren()) {
            listenToHierarchy(wob, wcl);
        }
    }

    /**
     * Removes the given listeners from the hierarchy of Wabit objects rooted at
     * <code>root</code>.
     * 
     * @param root
     *            The object at the top of the subtree to unlisten to. Must not
     *            be null.
     * @param wcl
     *            The Wabit child listener to remove from root and all its
     *            WabitObject descendants. If you do not want to unlisten to
     *            Wabit child events, you can provide null for this parameter.
     */
    public static void unlistenToHierarchy(WabitObject root, WabitListener wcl) {
        root.removeWabitListener(wcl);
        for (WabitObject wob : root.getChildren()) {
            unlistenToHierarchy(wob, wcl);
        }
    }

    /**
     * Returns the human-readable summary of the given service info object.
     * Anywhere a server is referred to within the Wabit, this method should be
     * used to convert the service info object into the string the user sees.
     * 
     * @param si
     *            The service info to summarize.
     * @return The Wabit's canonical human-readable representation of the given
     *         service info.
     */
    public static String serviceInfoSummary(WabitServerInfo si) {
        return si.getName() + " (" + si.getServerAddress() + ":" + si.getPort() + ")";
    }

    /**
     * Checks if the two arguments o1 and o2 are equal to each other, either because
     * both are null, or because o1.equals(o2).
     * 
     * @param o1 One object or null reference to compare
     * @param o2 The other object or null reference to compare
     */
    public static boolean nullSafeEquals(Object o1, Object o2) {
        if (o1 == null) return o2 == null;
        return o1.equals(o2);
    }

    /**
     * This method returns a list of all of the ancestors of the given
     * {@link WabitObject}. The order of the ancestors is such that the highest
     * ancestor is at the start of the list and the parent of the object itself
     * is at the end of the list.
     */
    public static List<WabitObject> getAncestorList(WabitObject o) {
        List<WabitObject> ancestors = new ArrayList<WabitObject>();
        WabitObject parent = o.getParent();
        while (parent != null) {
            ancestors.add(0, parent);
            parent = parent.getParent();
        }
        return ancestors;
    }

    /**
     * This method will recursively clean up this object and all of its
     * descendants.
     * 
     * @param o
     *            The object to clean up, including its dependencies.
     * @return A collection of exceptions and errors that occurred during
     *         cleanup if any occurred.
     */
    public static CleanupExceptions cleanupWabitObject(WabitObject o) {
        CleanupExceptions exceptions = new CleanupExceptions();
        exceptions.add(o.cleanup());
        for (WabitObject child : o.getChildren()) {
            exceptions.add(cleanupWabitObject(child));
        }
        return exceptions;
    }
    
    /**
     * This method will display the cleanup errors to the user. If the
     * user prompter factory given is null the errors will be logged instead.
     */
    public static void displayCleanupErrors(@Nonnull CleanupExceptions cleanupObject, 
            UserPrompterFactory upf) {
        if (upf != null) {
            if (!cleanupObject.isCleanupSuccessful()) {
                StringBuffer message = new StringBuffer();
                message.append("The following errors occurred during closing\n");
                for (String error : cleanupObject.getErrorMessages()) {
                    message.append("   " + error + "\n");
                }
                for (Exception exception : cleanupObject.getExceptions()) {
                    message.append("   " + exception.getMessage() + "\n");
                    logger.error("Exception during cleanup", exception);
                }
                UserPrompter up = upf.createUserPrompter(
                        message.toString(),
                        UserPromptType.MESSAGE, UserPromptOptions.OK, UserPromptResponse.OK,
                        null, "OK");
                up.promptUser();
            }
        } else {
            logCleanupErrors(cleanupObject);
        }
    }

    /**
     * Logs the exceptions and errors. This is useful if there is no available
     * user prompter.
     */
    public static void logCleanupErrors(@Nonnull CleanupExceptions cleanupObject) {
        for (String error : cleanupObject.getErrorMessages()) {
            logger.debug("Exception during cleanup, " + error);
        }
        for (Exception exception : cleanupObject.getExceptions()) {
            logger.error("Exception during cleanup", exception);
        }
    }

    /**
     * Walks up the parent chain of WabitObjects and returns the WabitSession
     * that these objects belong to. This can throw a
     * {@link SessionNotFoundException} if the object is not attached to a
     * session.
     * 
     * @param o
     *            The object to follow the parent chain.
     * @return A WabitSession that contains the given WabitObject and all of its
     *         children.
     */
    public static WabitSession getSession(WabitObject o) {
        WabitObject ancestor = o;
        while (ancestor.getParent() != null) {
            ancestor = ancestor.getParent();
        }
        if (ancestor instanceof WabitWorkspace && ((WabitWorkspace) ancestor).getSession() != null) 
            return ((WabitWorkspace) ancestor).getSession();
        throw new SessionNotFoundException("No session exists for " + o.getName() + " of type " +
                o.getClass());
    }

	/**
	 * Locates the WabitObject inside the root wabit object which has the given
	 * UUID, returning null if the item is not found. Throws ClassCastException
	 * if in item is found, but it is not of the expected type.
	 * 
	 * @param <T>
	 *            The expected type of the item
	 * @param uuid
	 *            The UUID of the item
	 * @param expectedType
	 *            The type of the item with the given UUID. If you are uncertain
	 *            what type of object it is, or you do not want a
	 *            ClassCastException in case the item is of the wrong type, use
	 *            <tt>WabitObject.class</tt> for this parameter.
	 * @return The item, or null if no item with the given UUID exists in the
	 *         descendent tree rooted at the given root object.
	 */
    public static <T extends WabitObject> T findByUuid(WabitObject root, String uuid, Class<T> expectedType) {
        return expectedType.cast(findRecursively(root, uuid));
    }
    
    /**
     * Performs a preorder traversal of the given WabitObject and its
     * descendants, returning the first WabitObject having the given UUID.
     * Returns null if no such WabitObject exists under startWith.
     * 
     * @param startWith
     *            The WabitObject to start the search with.
     * @param uuid
     *            The UUID to search for
     * @return the first WabitObject having the given UUID in a preorder
     *         traversal of startWith and its descendants. Returns null if no
     *         such WabitObject exists.
     */
    private static WabitObject findRecursively(WabitObject startWith, String uuid) {
    	if (startWith == null) {
    		throw new IllegalArgumentException("Cannot search a null object for children with the uuid " + uuid);
    	}
        if (uuid.equals(startWith.getUUID())) {
            return startWith;
        }
        for (WabitObject child : startWith.getChildren()) {
            WabitObject found = findRecursively(child, uuid);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    /**
     * This inserts a child into it's parent and does that for all the hierarchy recursively.
     * Wraps everything in a transaction.
     * @param parent
     * @param child
     */
    public static void addRecursivelyWithTransaction(WabitObject parent, WabitObject child) {
    	parent.begin(null);
    	parent.addChild(child, parent.getChildren().size());
    	for (WabitObject grandChild : child.getChildren()) {
    		addRecursivelyWithTransaction(parent, grandChild);
    	}
    	parent.commit();
    }

	/**
	 * Generates a new UUID in the format suitable for use with any
	 * WabitObject's UUID property.
	 */
	public static String randomWabitUUID() {
		return "w" + UUID.randomUUID().toString();
	}

	/**
	 * Prints the subtree rooted at the given WabitObject to the given output
	 * stream. This is only intended for debugging; any machine parsing of the
	 * output of this method is incorrect!
	 * 
	 * @param out the target of the debug information (often System.out)
	 * @param startWith the root object for the dump
	 */
	public static void printSubtree(PrintWriter out, WabitObject startWith) {
		printSubtree(out, startWith, 0);
	}

	/**
	 * Recursive subroutine of {@link #printSubtree(PrintWriter, WabitObject)}.
	 * 
	 * @param out
	 *            The print stream to print to
	 * @param startWith
	 *            The object to print (and whose children to process
	 *            recursively)
	 * @param indentDepth
	 *            The amount of indent to print before printing the object
	 *            information
	 */
	private static void printSubtree(PrintWriter out, WabitObject startWith, int indentDepth) {
		out.printf("%s%s \"%s\" (%s)\n",
				spaces(indentDepth * 2), startWith.getClass().getSimpleName(),
				startWith.getName(), startWith.getUUID());
		for (WabitObject child : startWith.getChildren()) {
			printSubtree(out, child, indentDepth + 1);
		}
	}

	/**
	 * Creates a string consisting of the desired number of spaces.
	 * 
	 * @param n
	 *            The number of spaces in the string.
	 * @return A string of length n which consists entirely of spaces.
	 */
	private static String spaces(int n) {
		StringBuilder sb = new StringBuilder(n);
		for (int i = 0; i < n; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}
}
