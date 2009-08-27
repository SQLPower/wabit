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

package ca.sqlpower.wabit.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ColumnInfo;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.report.chart.Chart;

/**
 * This comparator orders the WabitObjects given based on the following
 * criteria.
 * <ol>
 * <li>Objects are first sorted by their ancestors. The highest ancestor that is
 * different is used to order objects in this case. If the object's immediate
 * parents are the same then the next step is used.</li>
 * <li>Objects are compared by type. WabitDataSources come before QueryCaches
 * and OlapQueries which come before Layouts. Some objects, like QueryCaches and
 * OlapQueries, are considered to be equivalent in type. If two objects are the
 * same type the next step is used.</li>
 * <li>Objects are sorted by name. If two objects have the same name then they
 * are sorted by UUID. Object names do not have to be unique but the UUIDs must
 * be unique.</li>
 * </ol>
 * This ordering is used in saving so objects that need to be loaded early should
 * compare to be less than objects that are loaded later.
 */
public class WabitObjectComparator implements Comparator<WabitObject> {

    /**
     * This list tracks the desired order of components when they are compared
     * by type. New {@link WabitObject}s will need to be placed in this map to
     * be ordered correctly. Each position in the list can have multiple classes
     * defined at that position to have different class types, like {@link QueryCache}
     * and {@link OlapQuery}, to have the same position. 
     */
    private static final List<Set<Class<? extends WabitObject>>> classOrderList = 
        new ArrayList<Set<Class<? extends WabitObject>>>();
    
    static {
        classOrderList.add(new HashSet<Class<? extends WabitObject>>(Collections.singleton(WabitWorkspace.class)));
        classOrderList.add(new HashSet<Class<? extends WabitObject>>(Collections.singleton(WabitDataSource.class)));
        Set<Class<? extends WabitObject>> equalObjectsSet = new HashSet<Class<? extends WabitObject>>();
        equalObjectsSet.add(QueryCache.class);
        equalObjectsSet.add(OlapQuery.class);
        classOrderList.add(equalObjectsSet);
        classOrderList.add(new HashSet<Class<? extends WabitObject>>(Collections.singleton(Chart.class)));
        classOrderList.add(new HashSet<Class<? extends WabitObject>>(Collections.singleton(WabitImage.class)));
        classOrderList.add(new HashSet<Class<? extends WabitObject>>(Collections.singleton(Template.class)));
        classOrderList.add(new HashSet<Class<? extends WabitObject>>(Collections.singleton(Report.class)));
        classOrderList.add(new HashSet<Class<? extends WabitObject>>(Collections.singleton(Page.class)));
        classOrderList.add(new HashSet<Class<? extends WabitObject>>(Collections.singleton(ContentBox.class)));
        equalObjectsSet = new HashSet<Class<? extends WabitObject>>();
        equalObjectsSet.add(CellSetRenderer.class);
        equalObjectsSet.add(ResultSetRenderer.class);
        classOrderList.add(equalObjectsSet);
        classOrderList.add(new HashSet<Class<? extends WabitObject>>(Collections.singleton(ColumnInfo.class)));
        classOrderList.add(new HashSet<Class<? extends WabitObject>>(Collections.singleton(ChartRenderer.class)));
        classOrderList.add(new HashSet<Class<? extends WabitObject>>(Collections.singleton(ImageRenderer.class)));
        classOrderList.add(new HashSet<Class<? extends WabitObject>>(Collections.singleton(Label.class)));
        classOrderList.add(new HashSet<Class<? extends WabitObject>>(Collections.singleton(Guide.class)));
    }

    public int compare(WabitObject o1, WabitObject o2) {
        List<WabitObject> firstAncestorList = WabitUtils.getAncestorList(o1);
        List<WabitObject> secondAncestorList = WabitUtils.getAncestorList(o2);
        int differentAncestor;
        for (differentAncestor = 0; differentAncestor < firstAncestorList.size() && differentAncestor < secondAncestorList.size(); differentAncestor++) {
            if (!firstAncestorList.get(differentAncestor).equals(secondAncestorList.get(differentAncestor))) break;
        }
        
        WabitObject compareObject1;
        WabitObject compareObject2;
        
        if (differentAncestor < firstAncestorList.size()) {
            compareObject1 = firstAncestorList.get(differentAncestor);
        } else {
            compareObject1 = o1;
        }
        if (differentAncestor < secondAncestorList.size()) {
            compareObject2 = secondAncestorList.get(differentAncestor);
        } else {
            compareObject2 = o2;
        }
        
        int object1Ordinal;
        for (object1Ordinal = 0; object1Ordinal < classOrderList.size(); object1Ordinal++) {
            if (classOrderList.get(object1Ordinal).contains(compareObject1.getClass())) break;
        }
        if (object1Ordinal == classOrderList.size()) {
            throw new IllegalStateException("Missing ordering for WabitObject type " + compareObject1.getClass());
        }
        
        int object2Ordinal;
        for (object2Ordinal = 0; object2Ordinal < classOrderList.size(); object2Ordinal++) {
            if (classOrderList.get(object2Ordinal).contains(compareObject2.getClass())) break;
        }
        if (object2Ordinal == classOrderList.size()) {
            throw new IllegalStateException("Missing ordering for WabitObject type " + compareObject2.getClass());
        }
        
        if (object1Ordinal < object2Ordinal) return -1;
        if (object1Ordinal > object2Ordinal) return 1;
        
        int nameCompare = compareObject1.getName().compareTo(compareObject2.getName());
        if (nameCompare != 0) return nameCompare;
        
        return compareObject1.getUUID().compareTo(compareObject2.getUUID());
    }

}
