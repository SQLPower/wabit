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

import java.awt.Image;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.testutil.NewValueMaker;
import ca.sqlpower.wabit.WabitChildEvent.EventType;
import ca.sqlpower.wabit.dao.CountingWabitPersister;
import ca.sqlpower.wabit.dao.PersisterUtils;
import ca.sqlpower.wabit.dao.WabitPersister;
import ca.sqlpower.wabit.dao.WabitSessionPersister;
import ca.sqlpower.wabit.dao.WabitPersister.DataType;
import ca.sqlpower.wabit.dao.WabitSessionPersister.WabitObjectProperty;
import ca.sqlpower.wabit.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.wabit.dao.session.SessionPersisterUtils;
import ca.sqlpower.wabit.dao.session.WorkspacePersisterListener;
import ca.sqlpower.wabit.olap.OlapConnectionPool;
import ca.sqlpower.wabit.olap.OlapQuery;

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
     * A session that is hooked up to a pl.ini that is used for regression testing.
     */
    private WabitSession session;
    
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
     * These properties, on top of the properties ignored for events, will be
     * ignored when checking the properties of a specific {@link WabitObject}
     * are persisted.
     */
    public Set<String> getPropertiesToIgnoreForPersisting() {
    	return new HashSet<String>();
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
    
    @Override
    protected void setUp() throws Exception {
    	super.setUp();
    	final PlDotIni plIni = new PlDotIni();
    	plIni.read(new File("src/test/java/pl.regression.ini"));
        final Olap4jDataSource olapDS = plIni.getDataSource("World Facts OLAP Connection", 
        		Olap4jDataSource.class);
        if (olapDS == null) throw new IllegalStateException("Cannot find 'World Facts OLAP Connection'");
        final OlapConnectionPool connectionPool = new OlapConnectionPool(olapDS, 
        		new SQLDatabaseMapping() {
        	private final SQLDatabase sqlDB = new SQLDatabase(olapDS.getDataSource());
        	public SQLDatabase getDatabase(JDBCDataSource ds) {
        		return sqlDB;
        	}
        });
    	
    	
    	WabitSessionContext context = new StubWabitSessionContext() {
    		public org.olap4j.OlapConnection createConnection(Olap4jDataSource dataSource) 
    			throws java.sql.SQLException ,ClassNotFoundException ,javax.naming.NamingException {
    				return connectionPool.getConnection();
    		};
    	};
    	session = new StubWabitSession(context) {
    		
    		@Override
    		public DataSourceCollection<SPDataSource> getDataSources() {
    			return plIni;
    		}
    	};
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
        		new StubWabitSession(new StubWabitSessionContext()), new WabitWorkspace());
        List<PropertyDescriptor> settableProperties;
        settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(wo.getClass()));

        //Ignore properties that are not in events because we won't have an event
        //to respond to.
        Set<String> propertiesToIgnoreForEvents = getPropertiesToIgnoreForEvents();
        
        Set<String> propertiesToIgnoreForPersisting = getPropertiesToIgnoreForPersisting();
        
        NewValueMaker valueMaker = new WabitNewValueMaker();
        for (PropertyDescriptor property : settableProperties) {
            Object oldVal;
            
            if (propertiesToIgnoreForEvents.contains(property.getName())) continue;
            if (propertiesToIgnoreForPersisting.contains(property.getName())) continue;

            countingPersister.clearAllPropertyChanges();
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
                		oldChangeCount < countingPersister.getPersistPropertyCount());
                
                //The first property change at current is always the property change we are
                //looking for, this may need to be changed in the future to find the correct
                //property.
                WabitObjectProperty propertyChange = null;
                
                for (WabitObjectProperty nextPropertyChange : countingPersister.getAllPropertyChanges()) {
                	if (nextPropertyChange.getPropertyName().equals(property.getName())) {
                		propertyChange = nextPropertyChange;
                		break;
                	}
                }
                assertNotNull("A property change event cannot be found for the property " + property.getName(), propertyChange);
                
                assertEquals(wo.getUUID(), propertyChange.getUUID());
                assertEquals(property.getName(), propertyChange.getPropertyName());
				Object oldConvertedType = converterFactory.convertToBasicType(oldVal);
				assertEquals("Old value of property " + property.getName() + " was wrong, value expected was  " + oldConvertedType + 
						" but is " + countingPersister.getLastOldValue(), oldConvertedType, 
                		propertyChange.getOldValue());
				
	            //XXX will replace this later
	            List<Object> additionalVals = new ArrayList<Object>();
	            if (wo instanceof OlapQuery && property.getName().equals("currentCube")) {
	            	additionalVals.add(((OlapQuery) wo).getOlapDataSource());
	            }
				//Input streams from images are being compared by hash code not values
				if (Image.class.isAssignableFrom(property.getPropertyType())) {
					System.out.println(propertyChange.getNewValue().getClass());
					assertTrue(Arrays.equals(PersisterUtils.convertImageToStreamAsPNG(
								(Image) newVal).toByteArray(),
							PersisterUtils.convertImageToStreamAsPNG(
								(Image) converterFactory.convertToComplexType(
										propertyChange.getNewValue(), Image.class)).toByteArray()));
				} else {
					assertEquals(converterFactory.convertToBasicType(newVal, additionalVals.toArray()), 
                		propertyChange.getNewValue());
				}
                Class<? extends Object> classType;
                if (oldVal != null) {
                	classType = oldVal.getClass();
                } else {
                	classType = newVal.getClass();
                }
                assertEquals(SessionPersisterUtils.getDataType(classType), propertyChange.getDataType());
            } catch (InvocationTargetException e) {
                System.out.println("(non-fatal) Failed to write property '"+property.getName()+" to type "+wo.getClass().getName());
            }
        }
		
	}
    
    /**
     * Returns the specific class type that is the parent of this WabitObject.
     * This returns {@link WabitObject} which works for most cases but some specific
     * instances have a tighter parent type.
     */
    public Class<? extends WabitObject> getParentClass() {
    	return WabitObject.class;
    }

	/**
	 * This test uses the object under test to ensure that the
	 * {@link WabitSessionPersister} updates each property appropriately on
	 * persistence.
	 */
    public void testPersisterUpdatesProperties() throws Exception {
    	
    	AllObjectContainer superParent = new AllObjectContainer();
    	
    	WabitObject wo = getObjectUnderTest();
    	superParent.addChild(wo, 0);
    	
    	WabitSessionPersister persister = new WabitSessionPersister("secondary test persister", session, superParent);
		
    	SessionPersisterSuperConverter converterFactory = new SessionPersisterSuperConverter(
        		new StubWabitSession(new StubWabitSessionContext()), new WabitWorkspace());
        List<PropertyDescriptor> settableProperties;
        settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(wo.getClass()));

        //Ignore properties that are not in events because we won't have an event
        //to respond to.
        Set<String> propertiesToIgnoreForEvents = getPropertiesToIgnoreForEvents();
        
        Set<String> propertiesToIgnoreForPersisting = getPropertiesToIgnoreForPersisting();
    	
        NewValueMaker valueMaker = new WabitNewValueMaker();
    	for (PropertyDescriptor property : settableProperties) {
            Object oldVal;
            
            if (propertiesToIgnoreForEvents.contains(property.getName())) continue;
            
            if (propertiesToIgnoreForPersisting.contains(property.getName())) continue;

            try {
                oldVal = PropertyUtils.getSimpleProperty(wo, property.getName());

                // check for a setter
                if (property.getWriteMethod() == null) continue;
                
            } catch (NoSuchMethodException e) {
                System.out.println("Skipping non-settable property "+property.getName()+" on "+wo.getClass().getName());
                continue;
            }
            
            //special case for parent types. If a specific wabit object has a tighter parent then
            //WabitObject the getParentClass should return the parent type.
            Class<?> propertyType = property.getPropertyType();
            if (property.getName().equals("parent")) {
            	propertyType = getParentClass();
            }
            Object newVal = valueMaker.makeNewValue(propertyType, oldVal, property.getName());
            
            if (newVal instanceof WabitObject) {
            	superParent.addChild((WabitObject) newVal, 0);
            }
            
            System.out.println("Persisting property \"" + property.getName() + "\" from oldVal \"" + oldVal + "\" to newVal \"" + newVal + "\"");
            
            //XXX will replace this later
            List<Object> additionalVals = new ArrayList<Object>();
            if (wo instanceof OlapQuery && property.getName().equals("currentCube")) {
            	additionalVals.add(((OlapQuery) wo).getOlapDataSource());
            }
            
            DataType type = SessionPersisterUtils.getDataType(property.getPropertyType());
			Object basicNewValue = converterFactory.convertToBasicType(newVal, additionalVals.toArray());
			persister.persistProperty(wo.getUUID(), property.getName(), type, 
					converterFactory.convertToBasicType(oldVal, additionalVals.toArray()), 
					basicNewValue);
			
			Object newValAfterSet = PropertyUtils.getSimpleProperty(wo, property.getName());
			Object basicExpectedValue = converterFactory.convertToBasicType(newValAfterSet, additionalVals.toArray());
			
			assertPersistedValuesAreEqual(newVal, newValAfterSet, basicNewValue, 
					basicExpectedValue, property.getPropertyType());
    	}
	}

	/**
	 * Tests that the new value that was persisted is the same as an old value
	 * that was to be persisted. This helper method for the persister tests will
	 * compare the values by their converted type or some other means as not all
	 * values that are persisted have implemented their equals method.
	 * <p>
	 * This will do the asserts to compare if the objects are equal.
	 * 
	 * @param valueBeforePersist
	 *            the value that we are expecting the persisted value to contain
	 * @param valueAfterPersist
	 *            the value that was persisted to the object. This will be
	 *            tested against the valueBeforePersist to ensure that they are
	 *            the same.
	 * @param basicValueBeforePersist
	 *            The valueBeforePersist converted to a basic type by a
	 *            converter.
	 * @param basicValueAfterPersist
	 *            The valueAfterPersist converted to a basic type by a
	 *            converter.
	 * @param valueType
	 *            The type of object the before and after values should contain.
	 */
    private void assertPersistedValuesAreEqual(Object valueBeforePersist, Object valueAfterPersist, 
    		Object basicValueBeforePersist, Object basicValueAfterPersist, 
    		Class<? extends Object> valueType) {
    	
		//Input streams from images are being compared by hash code not values
		if (Image.class.isAssignableFrom(valueType)) {
			assertTrue(Arrays.equals(PersisterUtils.convertImageToStreamAsPNG((Image) valueBeforePersist).toByteArray(),
					PersisterUtils.convertImageToStreamAsPNG((Image) valueAfterPersist).toByteArray()));
		} else {

			//Not all new values are equivalent to their old values so we are
			//comparing them by their basic type as that is at least comparable, in most cases, i hope.
			assertEquals(basicValueBeforePersist, basicValueAfterPersist);
		}
    }

	/**
	 * Tests that calling
	 * {@link WabitPersister#persistObject(String, String, String, int)} for a
	 * session persister will create a new object and set all of the properties
	 * on the object.
	 */
    public void testPersisterAddsNewObject() throws Exception {
    	
    	AllObjectContainer superParent = new AllObjectContainer();
    	
    	WabitObject wo = getObjectUnderTest();
    	
    	WabitSessionPersister persister = new WabitSessionPersister("test persister", session, superParent);
    	WorkspacePersisterListener listener = new WorkspacePersisterListener(session, persister);
		
    	SessionPersisterSuperConverter converterFactory = new SessionPersisterSuperConverter(
        		new StubWabitSession(new StubWabitSessionContext()), new WabitWorkspace());
        
    	List<PropertyDescriptor> settableProperties;
        settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(wo.getClass()));
        
        //Set all possible values to new values for testing.
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
            
            try {
                System.out.println("Setting property '"+property.getName()+"' to '"+newVal+"' ("+newVal.getClass().getName()+")");
                BeanUtils.copyProperty(wo, property.getName(), newVal);
                
                if (newVal instanceof WabitObject) {
                	superParent.addChild((WabitObject) newVal, 0);
                }
                
            } catch (InvocationTargetException e) {
                System.out.println("(non-fatal) Failed to write property '"+property.getName()+" to type "+wo.getClass().getName());
            }
        }
        
        int oldChildCount = superParent.getChildren().size();
        
        //persist the object
        listener.wabitChildAdded(new WabitChildEvent(superParent, wo.getClass(), wo, 0, EventType.ADDED));
        
        //the object must now be added to the super parent
        assertEquals(oldChildCount + 1, superParent.getChildren().size());
        WabitObject persistedObject = superParent.getChildren().get(0);
        
        //check all the properties are what we expect on the new object
    	Set<String> ignorableProperties = getPropertiesToNotPersistOnObjectPersist();
    	
    	List<String> settablePropertyNames = new ArrayList<String>();
    	for (PropertyDescriptor pd : settableProperties) {
    		settablePropertyNames.add(pd.getName());
    	}
    	
    	settablePropertyNames.removeAll(ignorableProperties);
    	
    	for (String persistedPropertyName : settablePropertyNames) {
    		Class<?> classType = null;
    		for (PropertyDescriptor propertyDescriptor : settableProperties) {
    			if (propertyDescriptor.getName().equals(persistedPropertyName)) {
    				classType = propertyDescriptor.getPropertyType();
    			}
    		}
    		
    		System.out.println("Persisted object is of type " + persistedObject.getClass());
    		Object oldVal = PropertyUtils.getSimpleProperty(wo, persistedPropertyName);
    		Object newVal = PropertyUtils.getSimpleProperty(persistedObject, persistedPropertyName);
    		
            //XXX will replace this later
            List<Object> additionalVals = new ArrayList<Object>();
            if (wo instanceof OlapQuery && persistedPropertyName.equals("currentCube")) {
            	additionalVals.add(((OlapQuery) wo).getOlapDataSource());
            }
            
            Object basicOldVal = converterFactory.convertToBasicType(oldVal, additionalVals.toArray());
            Object basicNewVal = converterFactory.convertToBasicType(newVal, additionalVals.toArray());
            
            System.out.println("Property " + persistedPropertyName + ". oldVal is \"" + basicOldVal + "\" but newVal is \"" + basicNewVal + "\"");
    		
            assertPersistedValuesAreEqual(oldVal, newVal, basicOldVal, basicNewVal, classType);
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
    	if (wo.getParent() == null) {
    		wo.setParent(parent);
    	}
    	
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
    	ignorableProperties.addAll(getPropertiesToIgnoreForEvents());
    	
    	List<String> settablePropertyNames = new ArrayList<String>();
    	for (PropertyDescriptor pd : settableProperties) {
    		settablePropertyNames.add(pd.getName());
    	}
    	
    	settablePropertyNames.removeAll(ignorableProperties);
    	
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
    	System.out.println("Property names" + settablePropertyNames);
    	assertEquals(settablePropertyNames.size(), allPropertyChanges.size());
    	assertEquals(settablePropertyNames.size(), persister.getPersistPropertyUnconditionallyCount());
    	
    	SessionPersisterSuperConverter factory = new SessionPersisterSuperConverter(
    			new StubWabitSession(new StubWabitSessionContext()), new WabitWorkspace());
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
    		Object value = PropertyUtils.getSimpleProperty(wo, descriptor);
    		System.out.println("Property \"" + descriptor + "\": expected \"" + value + "\" but was \"" + foundChange.getNewValue() + "\" of type " + foundChange.getDataType());
			Object valueConvertedToBasic = factory.convertToBasicType(value);
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
            
            //XXX Take this out once we have the wabit workspace an interface with a
            //normal and session implementation.
            if (wo instanceof WabitWorkspace && 
            		(method.getName().equals("addGroup") || method.getName().equals("addUser"))) 
            	continue;
            
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
                
                //TODO uncomment this when all objects are parented properly
                //assertSame(wo, ((WabitObject) newChild).getParent());
                
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
