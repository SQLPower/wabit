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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import ca.sqlpower.testutil.CountingPropertyChangeListener;
import ca.sqlpower.testutil.NewValueMaker;

/**
 * A baseline test that all tests for WabitObject implementations should pass.
 * The intention is that those test classes will extend this class, thereby
 * inheriting the baseline tests.
 */
public abstract class AbstractWabitObjectTest extends TestCase {

    private static final Logger logger = Logger.getLogger(AbstractWabitObjectTest.class);
    
    /**
     * Returns the object being tested. This will typically have been
     * created by the subclass's setUp method.
     */
    public abstract WabitObject getObjectUnderTest();

    /**
     * Returns a list of JavaBeans property names that should be ignored when
     * testing for proper events.
     */
    public Set<String> getPropertiesToIgnoreForEvents() {
        Set<String> ignore = new HashSet<String>();
        ignore.add("class");
        return ignore;
    }
    
    /**
     * Uses reflection to find all the settable properties of the object under test,
     * and fails if any of them can be set without an event happening.
     */
    public void testSettingPropertiesFiresEvents() throws Exception {
        
        CountingPropertyChangeListener listener = new CountingPropertyChangeListener();
        WabitObject wo = getObjectUnderTest();
        wo.addPropertyChangeListener(listener);

        List<PropertyDescriptor> settableProperties;
        settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(wo.getClass()));
        
        Set<String> propertiesToIgnoreForEvents = getPropertiesToIgnoreForEvents();
        NewValueMaker valueMaker = new WabitNewValueMaker();
        for (PropertyDescriptor property : settableProperties) {
            Object oldVal;
            if (propertiesToIgnoreForEvents.contains(property.getName())) continue;
            
            try {
                oldVal = PropertyUtils.getSimpleProperty(wo, property.getName());

                // check for a setter
                if (property.getWriteMethod() == null) continue;
                
            } catch (NoSuchMethodException e) {
                System.out.println("Skipping non-settable property "+property.getName()+" on "+wo.getClass().getName());
                continue;
            }
            
            Object newVal = valueMaker.makeNewValue(property.getPropertyType(), oldVal, property.getName());
            int oldChangeCount = listener.getPropertyChangeCount();
            
            try {
                System.out.println("Setting property '"+property.getName()+"' to '"+newVal+"' ("+newVal.getClass().getName()+")");
                BeanUtils.copyProperty(wo, property.getName(), newVal);
                
                // some setters fire multiple events (they change more than one property)
                assertTrue("Event for set "+property.getName()+" on "+wo.getClass().getName()+" didn't fire!",
                        listener.getPropertyChangeCount() > oldChangeCount);
                if (listener.getPropertyChangeCount() == oldChangeCount + 1) {
                    assertEquals("Property name mismatch for "+property.getName()+ " in "+wo.getClass(),
                            property.getName(),
                            listener.getLastPropertyChange());
                    assertEquals("New value for "+property.getName()+" was wrong",
                            newVal,
                            listener.getLastNewValue());  
                }
            } catch (InvocationTargetException e) {
                System.out.println("(non-fatal) Failed to write property '"+property.getName()+" to type "+wo.getClass().getName());
            }
        }

    }

    /**
     * Reflectively discovers all the addXXX() methods which take an argument of
     * type WabitObject, then calls them to add and remove children. The test
     * passes if each of these discovered methods fires a well-formed
     * WabitChildEvent when the child is added and removed.
     */
    public void testAddChildren() throws Exception {
        WabitObject wo = getObjectUnderTest();
        
        CountingWabitChildListener listener = new CountingWabitChildListener();
        wo.addChildListener(listener);
        
        NewValueMaker valueMaker = new WabitNewValueMaker();
        
        Method[] allMethods = wo.getClass().getMethods();
        for (Method method : allMethods) {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (method.getName().matches("add.*") &&
                    paramTypes.length == 1 &&
                    WabitObject.class.isAssignableFrom(paramTypes[0])) {
                
                int oldAddCount = listener.getAddedCount();
                int oldRemoveCount = listener.getRemovedCount();
                Object newChild = valueMaker.makeNewValue(paramTypes[0], null, method.getName());
                method.invoke(wo, newChild);
                assertEquals(oldAddCount + 1, listener.getAddedCount());
                assertEquals(oldRemoveCount, listener.getRemovedCount());
                assertSame(wo, listener.getLastEvent().getSource());
                assertSame(newChild, listener.getLastEvent().getChild());
                assertSame(paramTypes[0], listener.getLastEvent().getChildType());
                
            } else {
                logger.debug("Skipped " + method.getName());
            }
        }
    }
    
    /**
     * No WabitObject is allowed to return null from getChildren(). Objects that
     * don't allow children should return an empty list.
     */
    public void testGetChildrenNotNull() throws Exception {
        assertNotNull(getObjectUnderTest().getChildren());
    }
}
