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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import ca.sqlpower.testutil.NewValueMaker;
import ca.sqlpower.wabit.WabitChildEvent.EventType;
import ca.sqlpower.wabit.dao.CountingWabitPersister;
import ca.sqlpower.wabit.dao.WabitSessionPersister;
import ca.sqlpower.wabit.dao.WabitPersister.DataType;
import ca.sqlpower.wabit.dao.WabitSessionPersister.WabitObjectProperty;
import ca.sqlpower.wabit.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.wabit.dao.session.WorkspacePersisterListener;

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
     * Returns a list of JavaBeans property names that should be ignored when
     * testing that all of the properties of an object are persisted when the
     * object itself is being persisted.
     */
    public Set<String> getPropertiesToNotPersistOnObjectPersist() {
    	Set<String> ignore = new HashSet<String>();
    	ignore.add("class");
    	ignore.add("children");
    	ignore.add("parent");
    	ignore.add("dependencies");
    	ignore.add("UUID");
    	return ignore;
    }
    
    /**
     * Returns a list of properties that must be persisted on top of the
     * properties that can be set on the object. These properties will be
     * properties of objects contained by the object under test. In the 
     * future we may want to reflectively go through each object in the
     * object under test and find all of the setters and getters to check
     * that they are persisted but at current we only need this for a few
     * specific places. This returns an empty list.
     */
    public Set<String> getAdditionalPropertiesToPersistOnObjectPersist() {
    	return Collections.emptySet();
    }
    
    /**
     * Uses reflection to find all the settable properties of the object under test,
     * and fails if any of them can be set without an event happening.
     */
    public void testSettingPropertiesFiresEvents() throws Exception {
        
        CountingWabitListener listener = new CountingWabitListener();
        WabitObject wo = getObjectUnderTest();
        wo.addWabitListener(listener);

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
                            listener.getLastPropertyEvent().getPropertyName());
                    assertEquals("New value for "+property.getName()+" was wrong",
                            newVal,
                            listener.getLastPropertyEvent().getNewValue());  
                }
            } catch (InvocationTargetException e) {
                System.out.println("(non-fatal) Failed to write property '"+property.getName()+" to type "+wo.getClass().getName());
            }
        }

    }

	/**
	 * This will reflectively iterate over all of the properties in the Wabit
	 * object and set each value that has a setter and getter. When the property
	 * is set it should cause the property to be persisted through the
	 * {@link WorkspacePersisterListener}.
	 */
    public void testPropertiesArePersisted() throws Exception {
    	
    	CountingWabitPersister countingPersister = new CountingWabitPersister();
    	WorkspacePersisterListener listener = new WorkspacePersisterListener(
    			new StubWabitSession(new StubWabitSessionContext()), countingPersister);
    	
        WabitObject wo = getObjectUnderTest();
        wo.addWabitListener(listener);

        SessionPersisterSuperConverter converterFactory = new SessionPersisterSuperConverter(
        		new StubWabitSession(new StubWabitSessionContext()));
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
            int oldChangeCount = countingPersister.getPersistPropertyCount();
            
            try {
                System.out.println("Setting property '"+property.getName()+"' to '"+newVal+"' ("+newVal.getClass().getName()+")");
                BeanUtils.copyProperty(wo, property.getName(), newVal);

                assertTrue("Did not persist property " + property.getName(), 
                		oldChangeCount + 1 == countingPersister.getPersistPropertyCount());
                assertEquals(wo.getUUID(), countingPersister.getLastUUID());
                assertEquals(property.getName(), countingPersister.getLastPropertyName());
                DataType oldValType;
                if (oldVal != null) {
                	 oldValType = DataType.getTypeByClass(oldVal.getClass());
                } else {
                	 oldValType = DataType.getTypeByClass(null);
                }
				Object oldConvertedType = converterFactory.convertToBasicType(oldVal, oldValType);
				assertEquals("Old value of property " + property.getName() + " was wrong, value expected was  " + oldConvertedType + 
						" but is " + countingPersister.getLastOldValue(), oldConvertedType, 
                		countingPersister.getLastOldValue());
                assertEquals(converterFactory.convertToBasicType(newVal, DataType.getTypeByClass(newVal.getClass())), 
                		countingPersister.getLastNewValue());
                Class<? extends Object> classType;
                if (oldVal != null) {
                	classType = oldVal.getClass();
                } else {
                	classType = newVal.getClass();
                }
                assertEquals(DataType.getTypeByClass(classType), countingPersister.getLastDataType());
            } catch (InvocationTargetException e) {
                System.out.println("(non-fatal) Failed to write property '"+property.getName()+" to type "+wo.getClass().getName());
            }
        }
		
	}
    
    /**
     * Reflective test that the wabit object can be persisted as an object and all of
     * its properties are persisted with it.
     */
    public void testPersistsObjectAsChild() throws Exception {

    	//This may need to actually have the wabit object as a child to itself.
    	WabitObject parent = new StubWabitObject(); 
    	
    	CountingWabitPersister persister = new CountingWabitPersister();
    	WorkspacePersisterListener listener = new WorkspacePersisterListener(
    			new StubWabitSession(new StubWabitSessionContext()), persister);
    	WabitObject wo = getObjectUnderTest();
    	wo.setParent(parent);
    	
    	listener.wabitChildAdded(new WabitChildEvent(parent, wo.getClass(), wo, 0, EventType.ADDED));
    	
    	assertEquals(1, persister.getPersistObjectCount());
    	assertEquals(wo.getClass().getSimpleName(), persister.getLastPersistObject().getType());
    	assertEquals(0, persister.getLastPersistObject().getIndex());
    	assertEquals(wo.getUUID(), persister.getLastPersistObject().getUUID());
    	
    	//confirm we get one persist property for each getter/setter pair
    	//confirm we get one persist property for each value in one of the constructors in the object.
    	
    	List<PropertyDescriptor> settableProperties = new ArrayList<PropertyDescriptor>(
    			Arrays.asList(PropertyUtils.getPropertyDescriptors(wo.getClass())));
    	List<WabitObjectProperty> allPropertyChanges = persister.getAllPropertyChanges();
    	Set<String> ignorableProperties = getPropertiesToNotPersistOnObjectPersist();
    	
    	List<String> settablePropertyNames = new ArrayList<String>();
    	for (PropertyDescriptor pd : settableProperties) {
    		settablePropertyNames.add(pd.getName());
    	}
    	
    	settablePropertyNames.removeAll(ignorableProperties);
    	settablePropertyNames.addAll(getAdditionalPropertiesToPersistOnObjectPersist());
    	
    	if (settablePropertyNames.size() != allPropertyChanges.size()) {
    		for (String descriptor : settablePropertyNames) {
        		WabitObjectProperty foundChange = null;
        		for (WabitObjectProperty propertyChange : allPropertyChanges) {
        			if (propertyChange.getPropertyName().equals(descriptor)) {
        				foundChange = propertyChange;
        				break;
        			}
        		}
        		assertNotNull("The property " + descriptor + " was not persisted", foundChange);
    		}
    	}
    	assertEquals(settablePropertyNames.size(), allPropertyChanges.size());
    	assertEquals(settablePropertyNames.size(), persister.getPersistPropertyUnconditionallyCount());
    	
    	SessionPersisterSuperConverter factory = new SessionPersisterSuperConverter(
    			new StubWabitSession(new StubWabitSessionContext()));
    	for (String descriptor : settablePropertyNames) {
    		WabitObjectProperty foundChange = null;
    		for (WabitObjectProperty propertyChange : allPropertyChanges) {
    			if (propertyChange.getPropertyName().equals(descriptor)) {
    				foundChange = propertyChange;
    				break;
    			}
    		}
    		assertNotNull("The property " + descriptor + " was not persisted", foundChange);
    		assertTrue(foundChange.isUnconditional());
    		assertEquals(wo.getUUID(), foundChange.getUUID());
    		String[] properties = descriptor.split("\\" + WabitSessionPersister.PROPERTY_SEPARATOR);
    		Object value = wo;
    		for (String property : properties) {
    			value = PropertyUtils.getSimpleProperty(value, property);
    		}
    		System.out.println("Property \"" + descriptor + "\": expected \"" + value + "\" but was \"" + foundChange.getNewValue() + "\" of type " + foundChange.getDataType());
    		DataType dataTypeForValue;
    		if (value != null) {
    			dataTypeForValue = DataType.getTypeByClass(value.getClass());
    		} else {
    			dataTypeForValue = DataType.getTypeByClass(null);
    		}
			Object valueConvertedToBasic = factory.convertToBasicType(value, dataTypeForValue);
			assertEquals(valueConvertedToBasic, foundChange.getNewValue());
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

        CountingWabitListener listener = new CountingWabitListener();
        wo.addWabitListener(listener);
        
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
                assertEquals("Add child event for " + method.getName() + " didn't fire!",
                        oldAddCount + 1, listener.getAddedCount());
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
    
    /**
     * No WabitObject is allowed to return a null dependency. Objects with no dependencies
     * should return an empty list.
     * @throws Exception
     */
    public void testDependenciesNotNull() throws Exception {
        assertNotNull(getObjectUnderTest().getDependencies());
        assertFalse(getObjectUnderTest().getDependencies().contains(null));
    }
}
