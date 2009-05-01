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

package ca.sqlpower.wabit.swingui.olap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.metadata.Catalog;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;
import org.olap4j.metadata.NamedSet;
import org.olap4j.metadata.Property;
import org.olap4j.metadata.Schema;

/**
 * Models the metadata structure under a list of olap4j database connections.
 * <p>
 * As specified by the olap4j api, here is the hierarchy modeled by this tree:
 *   <li type="circle">{@link org.olap4j.OlapConnection}<ul>
 *     <li type="circle">{@link Catalog}<ul>
 *         <li type="circle">{@link Schema}<ul>
 *             <li type="circle">{@link Cube}<ul>
 *                 <li type="circle">{@link Dimension}<ul>
 *                     <li type="circle">{@link Hierarchy}<ul>
 *                         <li type="circle">{@link Level}<ul>
 *                             <li type="circle">{@link Member}</li>
 *                             <li type="circle">{@link Property}</li>
 *                         </ul></li>
 *                     </ul></li>
 *                 </ul></li>
 *             <li type="circle">{@link NamedSet}</li>
 *             </ul></li>
 *         <li type="circle">Dimension (shared)</li>
 *         </ul></li>
 *     </ul></li>
 */
public class Olap4jTreeModel implements TreeModel {


    private class OlapTreeRoot {
        
        /** These are the children of the root object. */
        private final List<OlapConnection> connections;

        /**
         * @param connections
         */
        public OlapTreeRoot(List<OlapConnection> connections) {
            super();
            this.connections = connections;
        }
        
        public List<OlapConnection> getConnections() {
            return connections;
        }
    }
    
    private final OlapTreeRoot root;
    
    public Olap4jTreeModel(List<OlapConnection> connections) {
        root = new OlapTreeRoot(connections);
    }


    private List<? extends Object> getChildren(Object parent) {
        try {
            if (parent instanceof OlapTreeRoot) {
                return ((OlapTreeRoot) parent).connections;
            } else if (parent instanceof OlapConnection) {
                return ((OlapConnection) parent).getCatalogs();
            } else if (parent instanceof Catalog) {
                return ((Catalog) parent).getSchemas();
            } else if (parent instanceof Schema) {
                Schema s = (Schema) parent;
                NamedList<Cube> cubes = s.getCubes();
                NamedList<Dimension> sharedDimensions = s.getSharedDimensions();

                // This could be cached, but it probably wouldn't make a big difference
                List<Object> schemaKids = new ArrayList<Object>(cubes.size() + sharedDimensions.size());

                schemaKids.addAll(cubes);
                
                // There can apparently be duplicate dimensions (in FoodMart, the Time dimension appears twice)
                Set<Object> alreadyAdded = new HashSet<Object>();
                for (Dimension d : sharedDimensions) {
                    boolean isNew = alreadyAdded.add(d);
                    if (isNew) {
                        schemaKids.add(d);
                    }
                }
                return schemaKids;
            } else if (parent instanceof Cube) {
                Cube c = (Cube) parent;
                List<Measure> measures = c.getMeasures();
                NamedList<Dimension> dimensions = c.getDimensions();
                NamedList<NamedSet> sets = c.getSets();
                
                List<Object> cubeKids = new ArrayList<Object>(measures.size() + dimensions.size() + sets.size());
                cubeKids.addAll(measures);
                cubeKids.addAll(dimensions.subList(1, dimensions.size()));
                cubeKids.addAll(sets);
                return cubeKids;
            } else if (parent instanceof Dimension) {
                return ((Dimension) parent).getHierarchies();
            } else if (parent instanceof Measure) {
                return Collections.emptyList();
            } else if (parent instanceof Hierarchy) {
                return ((Hierarchy) parent).getLevels();
            } else if (parent instanceof Level) {
                return ((Level) parent).getMembers();
                // note Level also has Properties children, but they appear to be useless in the GUI
            } else if (parent instanceof Member) {
                return ((Member) parent).getChildMembers();
            } else if (parent instanceof Property) {
                return Collections.emptyList();
            } else if (parent instanceof NamedSet) {
                return Collections.emptyList();
            } else {
                throw new IllegalArgumentException("Unknown node type " + parent);
            }
        } catch (OlapException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addTreeModelListener(TreeModelListener l) {
        // this tree doesn't make events
    }

    public void removeTreeModelListener(TreeModelListener l) {
        // this tree doesn't make events
    }

    public Object getChild(Object parent, int index) {
        return getChildren(parent).get(index);
    }
    
    public int getChildCount(Object parent) {
        return getChildren(parent).size();
    }

    public int getIndexOfChild(Object parent, Object child) {
        return getChildren(parent).indexOf(child);
    }

    public Object getRoot() {
        return root;
    }

    public boolean isLeaf(Object node) {
        return getChildren(node).isEmpty();
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("OLAP Tree is not editable");
    }
    
    
}
