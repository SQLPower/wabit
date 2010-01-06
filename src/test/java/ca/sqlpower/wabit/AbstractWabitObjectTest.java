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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import ca.sqlpower.dao.PersistedSPOProperty;
import ca.sqlpower.dao.PersistedSPObject;
import ca.sqlpower.dao.PersisterUtils;
import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPPersister;
import ca.sqlpower.dao.StubSPPersister;
import ca.sqlpower.dao.SPPersister.DataType;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPChildEvent.EventType;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.testutil.NewValueMaker;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.wabit.dao.CountingWabitPersister;
import ca.sqlpower.wabit.dao.WabitSessionPersister;
import ca.sqlpower.wabit.dao.session.WabitSessionPersisterSuperConverter;
import ca.sqlpower.wabit.dao.session.WorkspacePersisterListener;
import ca.sqlpower.wabit.rs.olap.OlapConnectionPool;
import ca.sqlpower.wabit.rs.olap.OlapQuery;

/**
 * A baseline test that all tests for WabitObject implementations should pass.
 * The intention is that those test classes will extend this class, thereby
 * inheriting the baseline tests. While this test can be run with any SPObject,
 * it is only intended to be run by those that can be handled by the
 * {@link WabitSessionPersister}.
 */
public abstract class AbstractWabitObjectTest extends TestCase {
	
	/**
	 * Small implementation of the WabitPersister that will throw an exception on commit
	 * when its error state is set to true.
	 */
	public static class ErrorWabitPersister extends StubSPPersister {
		private int transactionCount = 0;
		
		private boolean throwError = false;
		@Override
		public void begin() throws SPPersistenceException {
			transactionCount++;
		}
		public void commit() throws SPPersistenceException {
			transactionCount--;
			if (transactionCount == 0 && throwError) {
				throw new SPPersistenceException(null, "Cause everything to rollback");
			}
		}
		
		public void setThrowError(boolean willThrowError) {
			throwError = willThrowError;
		}
	};

    private static final Logger logger = Logger.getLogger(AbstractWabitObjectTest.class);
    
    /**
     * Returns the object being tested. This will typically have been
     * created by the subclass's setUp method. The object returned
     * by this method must be in the workspace returned by {@link #getWorkspace()}.
     */
    public abstract SPObject getObjectUnderTest();
    
    /**
     * A session that is hooked up to a pl.ini that is used for regression testing.
     */
    private WabitSession session;
    
    private NewValueMaker valueMaker;

    /**
     * A converter for use in tests involving persisting objects.
     */
	private WabitSessionPersisterSuperConverter converterFactory;
    
    public WabitWorkspace getWorkspace() {
    	return session.getWorkspace();
    }
    
    /**
     * Returns a list of JavaBeans property names that should be ignored when
     * testing for proper events.
     */
    public Set<String> getPropertiesToIgnoreForEvents() {
        Set<String> ignore = new HashSet<String>();
        ignore.add("class");
        ignore.add("session");
        ignore.add("magicEnabled");
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
    	ignore.add("session");
    	ignore.add("allowedChildTypes");
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
    		
    		private final WabitWorkspace workspace = new WabitWorkspace();
    		
    		@Override
    		public DataSourceCollection<SPDataSource> getDataSources() {
    			return plIni;
    		}
    		
    		@Override
    		public WabitWorkspace getWorkspace() {
    			workspace.setSession(this);
    			return workspace;
    		}
    	};
    	
    	valueMaker = new WabitNewValueMaker(getWorkspace(), plIni);
    	
    	converterFactory = new WabitSessionPersisterSuperConverter(session, getWorkspace());
    }

	/**
	 * For the persister tests to work the object under test must be in the
	 * workspace in this session.
	 */
    public void testObjectUnderTestInWorkspace() throws Exception {
		assertNotNull(getWorkspace().findByUuid(
				getObjectUnderTest().getUUID(), getObjectUnderTest().getClass()));
	}
    
    /**
     * Uses reflection to find all the settable properties of the object under test,
     * and fails if any of them can be set without an event happening.
     */
    public void testSettingPropertiesFiresEvents() throws Exception {
        
        CountingWabitListener listener = new CountingWabitListener();
        SPObject wo = getObjectUnderTest();
        wo.addSPListener(listener);

        List<PropertyDescriptor> settableProperties;
        settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(wo.getClass()));
        
        Set<String> propertiesToIgnoreForEvents = getPropertiesToIgnoreForEvents();
        for (PropertyDescriptor property : settableProperties) {
            Object oldVal;
            if (propertiesToIgnoreForEvents.contains(property.getName())) continue;
            
            try {
                oldVal = PropertyUtils.getSimpleProperty(wo, property.getName());

                // check for a setter
                if (property.getWriteMethod() == null) continue;
                
            } catch (NoSuchMethodException e) {
            	logger.debug("Skipping non-settable property "+property.getName()+" on "+wo.getClass().getName());
                continue;
            }
            
            int oldChangeCount = listener.getPropertyChangeCount();
            Object newVal = valueMaker.makeNewValue(property.getPropertyType(), oldVal, property.getName());
            
            try {
                logger.debug("Setting property '"+property.getName()+"' to '"+newVal+"' ("+newVal.getClass().getName()+")");
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
                logger.debug("(non-fatal) Failed to write property '"+property.getName()+" to type "+wo.getClass().getName());
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
    	
        SPObject wo = getObjectUnderTest();
        wo.addSPListener(listener);

        WabitSessionPersisterSuperConverter converterFactory = new WabitSessionPersisterSuperConverter(
        		new StubWabitSession(new StubWabitSessionContext()), new WabitWorkspace());
        List<PropertyDescriptor> settableProperties;
        settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(wo.getClass()));

        //Ignore properties that are not in events because we won't have an event
        //to respond to.
        Set<String> propertiesToIgnoreForEvents = getPropertiesToIgnoreForEvents();
        
        Set<String> propertiesToIgnoreForPersisting = getPropertiesToIgnoreForPersisting();
        
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
                logger.debug("Skipping non-settable property "+property.getName()+" on "+wo.getClass().getName());
                continue;
            }
            
            Object newVal = valueMaker.makeNewValue(property.getPropertyType(), oldVal, property.getName());
            int oldChangeCount = countingPersister.getPersistPropertyCount();
            
            try {
                logger.debug("Setting property '"+property.getName()+"' to '"+newVal+"' ("+newVal.getClass().getName()+")");
                BeanUtils.copyProperty(wo, property.getName(), newVal);

                assertTrue("Did not persist property " + property.getName(), 
                		oldChangeCount < countingPersister.getPersistPropertyCount());
                
                //The first property change at current is always the property change we are
                //looking for, this may need to be changed in the future to find the correct
                //property.
                PersistedSPOProperty propertyChange = null;
                
                for (PersistedSPOProperty nextPropertyChange : countingPersister.getAllPropertyChanges()) {
                	if (nextPropertyChange.getPropertyName().equals(property.getName())) {
                		propertyChange = nextPropertyChange;
                		break;
                	}
                }
                assertNotNull("A property change event cannot be found for the property " + property.getName(), propertyChange);
                
                assertEquals(wo.getUUID(), propertyChange.getUUID());
                assertEquals(property.getName(), propertyChange.getPropertyName());
                
                //XXX will replace this later
                List<Object> additionalVals = new ArrayList<Object>();
                if (wo instanceof OlapQuery && property.getName().equals("currentCube")) {
                	additionalVals.add(((OlapQuery) wo).getOlapDataSource());
                }
                
				Object oldConvertedType = converterFactory.convertToBasicType(oldVal, additionalVals.toArray());
				assertEquals("Old value of property " + property.getName() + " was wrong, value expected was  " + oldConvertedType + 
						" but is " + countingPersister.getLastOldValue(), oldConvertedType, 
                		propertyChange.getOldValue());
				
				//Input streams from images are being compared by hash code not values
				if (Image.class.isAssignableFrom(property.getPropertyType())) {
					logger.debug(propertyChange.getNewValue().getClass());
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
                assertEquals(PersisterUtils.getDataType(classType), propertyChange.getDataType());
            } catch (InvocationTargetException e) {
                logger.debug("(non-fatal) Failed to write property '"+property.getName()+" to type "+wo.getClass().getName());
            }
        }
		
	}
    
    /**
     * Returns the specific class type that is the parent of this WabitObject.
     * This returns {@link WabitObject} which works for most cases but some specific
     * instances have a tighter parent type.
     */
    public Class<? extends SPObject> getParentClass() {
    	return WabitObject.class;
    }

	/**
	 * This test uses the object under test to ensure that the
	 * {@link WabitSessionPersister} updates each property appropriately on
	 * persistence.
	 */
    public void testPersisterUpdatesProperties() throws Exception {
    	
    	SPObject wo = getObjectUnderTest();
    	
    	WabitSessionPersister persister = new WabitSessionPersister("secondary test persister", session, getWorkspace());
		
    	WabitSessionPersisterSuperConverter converterFactory = new WabitSessionPersisterSuperConverter(
        		new StubWabitSession(new StubWabitSessionContext()), new WabitWorkspace());
        List<PropertyDescriptor> settableProperties;
        settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(wo.getClass()));

        //Ignore properties that are not in events because we won't have an event
        //to respond to.
        Set<String> propertiesToIgnoreForEvents = getPropertiesToIgnoreForEvents();
        
        Set<String> propertiesToIgnoreForPersisting = getPropertiesToIgnoreForPersisting();
    	
    	for (PropertyDescriptor property : settableProperties) {
            Object oldVal;
            
            if (propertiesToIgnoreForEvents.contains(property.getName())) continue;
            
            if (propertiesToIgnoreForPersisting.contains(property.getName())) continue;

            try {
                oldVal = PropertyUtils.getSimpleProperty(wo, property.getName());

                // check for a setter
                if (property.getWriteMethod() == null) continue;
                
            } catch (NoSuchMethodException e) {
                logger.debug("Skipping non-settable property "+property.getName()+" on "+wo.getClass().getName());
                continue;
            }
            
            //special case for parent types. If a specific wabit object has a tighter parent then
            //WabitObject the getParentClass should return the parent type.
            Class<?> propertyType = property.getPropertyType();
            if (property.getName().equals("parent")) {
            	propertyType = getParentClass();
            }
            Object newVal = valueMaker.makeNewValue(propertyType, oldVal, property.getName());
            
            logger.debug("Persisting property \"" + property.getName() + "\" from oldVal \"" + oldVal + "\" to newVal \"" + newVal + "\"");
            
            //XXX will replace this later
            List<Object> additionalVals = new ArrayList<Object>();
            if (wo instanceof OlapQuery && property.getName().equals("currentCube")) {
            	additionalVals.add(((OlapQuery) wo).getOlapDataSource());
            }
            
            DataType type = PersisterUtils.getDataType(property.getPropertyType());
			Object basicNewValue = converterFactory.convertToBasicType(newVal, additionalVals.toArray());
			persister.begin();
			persister.persistProperty(wo.getUUID(), property.getName(), type, 
					converterFactory.convertToBasicType(oldVal, additionalVals.toArray()), 
					basicNewValue);
			persister.commit();
			
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
			assertEquals("Persist failed for type " + valueType, basicValueBeforePersist, basicValueAfterPersist);
		}
    }

	/**
	 * Tests that calling
	 * {@link SPPersister#persistObject(String, String, String, int)} for a
	 * session persister will create a new object and set all of the properties
	 * on the object.
	 */
    public void testPersisterAddsNewObject() throws Exception {
    	
    	SPObject wo = getObjectUnderTest();
    	
    	WabitSessionPersister persister = new WabitSessionPersister("test persister", session, session.getWorkspace());
    	WorkspacePersisterListener listener = new WorkspacePersisterListener(session, persister);
		
    	WabitSessionPersisterSuperConverter converterFactory = new WabitSessionPersisterSuperConverter(
        		new StubWabitSession(new StubWabitSessionContext()), new WabitWorkspace());
        
    	List<PropertyDescriptor> settableProperties;
        settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(wo.getClass()));
        
        //Set all possible values to new values for testing.
        Set<String> propertiesToIgnoreForEvents = getPropertiesToIgnoreForEvents();
        for (PropertyDescriptor property : settableProperties) {
            Object oldVal;
            if (propertiesToIgnoreForEvents.contains(property.getName())) continue;
            if (property.getName().equals("parent")) continue; //Changing the parent causes headaches.
            
            try {
                oldVal = PropertyUtils.getSimpleProperty(wo, property.getName());

                // check for a setter
                if (property.getWriteMethod() == null) continue;
                
            } catch (NoSuchMethodException e) {
                logger.debug("Skipping non-settable property "+property.getName()+" on "+wo.getClass().getName());
                continue;
            }
            
            Object newVal = valueMaker.makeNewValue(property.getPropertyType(), oldVal, property.getName());
            
            try {
                logger.debug("Setting property '"+property.getName()+"' to '"+newVal+"' ("+newVal.getClass().getName()+")");
                BeanUtils.copyProperty(wo, property.getName(), newVal);
                
            } catch (InvocationTargetException e) {
                logger.debug("(non-fatal) Failed to write property '"+property.getName()+" to type "+wo.getClass().getName());
            }
        }
        
        SPObject parent = wo.getParent();
        int oldChildCount = parent.getChildren().size();
  
        listener.transactionStarted(null);
        listener.childRemoved(new SPChildEvent(parent, wo.getClass(), wo, parent.getChildren().indexOf(wo), EventType.REMOVED));
        listener.transactionEnded(null);
        
        //persist the object
        wo.setParent(parent);
        listener.transactionStarted(null);
        listener.childAdded(new SPChildEvent(parent, wo.getClass(), wo, 0, EventType.ADDED));
        listener.transactionEnded(null);
        
        //the object must now be added to the super parent
        assertEquals(oldChildCount, parent.getChildren().size());
        SPObject persistedObject = parent.getChildren().get(parent.childPositionOffset(wo.getClass()));
        
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
    		
    		logger.debug("Persisted object is of type " + persistedObject.getClass());
    		Object oldVal = PropertyUtils.getSimpleProperty(wo, persistedPropertyName);
    		Object newVal = PropertyUtils.getSimpleProperty(persistedObject, persistedPropertyName);
    		
            //XXX will replace this later
            List<Object> additionalVals = new ArrayList<Object>();
            if (wo instanceof OlapQuery && persistedPropertyName.equals("currentCube")) {
            	additionalVals.add(((OlapQuery) wo).getOlapDataSource());
            }
            
            Object basicOldVal = converterFactory.convertToBasicType(oldVal, additionalVals.toArray());
            Object basicNewVal = converterFactory.convertToBasicType(newVal, additionalVals.toArray());
            
            logger.debug("Property " + persistedPropertyName + ". oldVal is \"" + basicOldVal + "\" but newVal is \"" + basicNewVal + "\"");
    		
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
    	SPObject wo = getObjectUnderTest();
    	if (wo.getParent() == null) {
    		wo.setParent(parent);
    	}
    	
    	listener.childAdded(new SPChildEvent(parent, wo.getClass(), wo, 0, EventType.ADDED));
    	
    	assertTrue(persister.getPersistObjectCount() > 0);
    	PersistedSPObject persistedWabitObject = persister.getAllPersistedObjects().get(0);
    	assertEquals(wo.getClass().getSimpleName(), persistedWabitObject.getType());
    	assertEquals(wo.getUUID(), persistedWabitObject.getUUID());
    	
    	//confirm we get one persist property for each getter/setter pair
    	//confirm we get one persist property for each value in one of the constructors in the object.
    	
    	List<PropertyDescriptor> settableProperties = new ArrayList<PropertyDescriptor>(
    			Arrays.asList(PropertyUtils.getPropertyDescriptors(wo.getClass())));
    	List<PersistedSPOProperty> allPropertyChanges = persister.getAllPropertyChanges();
    	Set<String> ignorableProperties = getPropertiesToNotPersistOnObjectPersist();
    	ignorableProperties.addAll(getPropertiesToIgnoreForEvents());
    	
    	List<PersistedSPOProperty> changesOnObject = new ArrayList<PersistedSPOProperty>();
    	for (int i = allPropertyChanges.size() - 1; i >= 0; i--) {
    		if (allPropertyChanges.get(i).getUUID().equals(wo.getUUID())) {
    			changesOnObject.add(allPropertyChanges.get(i));
    		}
    	}
    	
    	List<String> settablePropertyNames = new ArrayList<String>();
    	for (PropertyDescriptor pd : settableProperties) {
    		settablePropertyNames.add(pd.getName());
    	}
    	
    	settablePropertyNames.removeAll(ignorableProperties);
    	
    	if (settablePropertyNames.size() != changesOnObject.size()) {
    		for (String descriptor : settablePropertyNames) {
        		PersistedSPOProperty foundChange = null;
        		for (PersistedSPOProperty propertyChange : changesOnObject) {
        			if (propertyChange.getPropertyName().equals(descriptor)) {
        				foundChange = propertyChange;
        				break;
        			}
        		}
        		assertNotNull("The property " + descriptor + " was not persisted", foundChange);
    		}
    	}
    	logger.debug("Property names" + settablePropertyNames);
    	assertTrue(settablePropertyNames.size()<=changesOnObject.size());
    	
    	WabitSessionPersisterSuperConverter factory = new WabitSessionPersisterSuperConverter(
    			new StubWabitSession(new StubWabitSessionContext()), new WabitWorkspace());
    	for (String descriptor : settablePropertyNames) {
    		PersistedSPOProperty foundChange = null;
    		for (PersistedSPOProperty propertyChange : changesOnObject) {
    			if (propertyChange.getPropertyName().equals(descriptor)) {
    				foundChange = propertyChange;
    				break;
    			}
    		}
    		assertNotNull("The property " + descriptor + " was not persisted", foundChange);
    		assertTrue(foundChange.isUnconditional());
    		assertEquals(wo.getUUID(), foundChange.getUUID());
    		Object value = PropertyUtils.getSimpleProperty(wo, descriptor);
    		
    		//XXX will replace this later
    		List<Object> additionalVals = new ArrayList<Object>();
    		if (wo instanceof OlapQuery && descriptor.equals("currentCube")) {
    			additionalVals.add(((OlapQuery) wo).getOlapDataSource());
    		}
    		Object valueConvertedToBasic = factory.convertToBasicType(value, additionalVals.toArray());
    		logger.debug("Property \"" + descriptor + "\": expected \"" + valueConvertedToBasic + "\" but was \"" + foundChange.getNewValue() + "\" of type " + foundChange.getDataType());
    		
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
        SPObject wo = getObjectUnderTest();

        CountingWabitListener listener = new CountingWabitListener();
        wo.addSPListener(listener);
        
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
     * This test will set all of the properties in a WabitObject in one transaction then
     * after committing the next persister after it will throw an exception causing the
     * persister to undo all of the changes it just made.
     */
    public void testPersisterCommitCanRollbackProperties() throws Exception {
    	
    	SPObject wo = getObjectUnderTest();
    	
		WabitSessionPersister persister = 
			new WabitSessionPersister("test persister", session, getWorkspace());
		
		CountingWabitListener countingListener = new CountingWabitListener();
		
		ErrorWabitPersister errorPersister = new ErrorWabitPersister();
		
		WorkspacePersisterListener listener = new WorkspacePersisterListener(session, errorPersister);
		
		SQLPowerUtils.listenToHierarchy(getWorkspace(), listener);
		wo.addSPListener(countingListener);
		
		List<PropertyDescriptor> settableProperties;
        settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(wo.getClass()));

        Set<String> propertiesToIgnore = new HashSet<String>(getPropertiesToIgnoreForEvents());
        
        propertiesToIgnore.addAll(getPropertiesToIgnoreForPersisting());
        
        //Track old and new property values to test they are set properly
        Map<String, Object> propertyNameToOldVal = new HashMap<String, Object>();
    	
        //Set all of the properties of the object under test in one transaction.
        persister.begin();
        
        int propertyChangeCount = 0;
    	for (PropertyDescriptor property : settableProperties) {
            Object oldVal;
            
            if (propertiesToIgnore.contains(property.getName())) continue;
            
            try {
                oldVal = PropertyUtils.getSimpleProperty(wo, property.getName());

                // check for a setter
                if (property.getWriteMethod() == null) continue;
                
            } catch (NoSuchMethodException e) {
                logger.debug("Skipping non-settable property "+property.getName()+" on "+wo.getClass().getName());
                continue;
            }
            
            propertyNameToOldVal.put(property.getName(), oldVal);
            
            //special case for parent types. If a specific wabit object has a tighter parent then
            //WabitObject the getParentClass should return the parent type.
            Class<?> propertyType = property.getPropertyType();
            if (property.getName().equals("parent")) {
            	propertyType = getParentClass();
            }
            Object newVal = valueMaker.makeNewValue(propertyType, oldVal, property.getName());
            
            logger.debug("Persisting property \"" + property.getName() + "\" from oldVal \"" + oldVal + "\" to newVal \"" + newVal + "\"");
            
            //XXX will replace this later
            List<Object> additionalVals = new ArrayList<Object>();
            if (wo instanceof OlapQuery && property.getName().equals("currentCube")) {
            	additionalVals.add(((OlapQuery) wo).getOlapDataSource());
            }
            
            DataType type = PersisterUtils.getDataType(property.getPropertyType());
			Object basicNewValue = converterFactory.convertToBasicType(newVal, additionalVals.toArray());
			persister.persistProperty(wo.getUUID(), property.getName(), type, 
					converterFactory.convertToBasicType(oldVal, additionalVals.toArray()), 
					basicNewValue);
			propertyChangeCount++;
    	}
    	
    	//Commit the transaction causing the rollback to occur
    	errorPersister.setThrowError(true);
    	
    	try {
    		persister.commit();
    		fail("The commit method should have an error sent to it and it should rethrow the exception.");
    	} catch (SPPersistenceException t) {
    		//continue
    	}
    	
    	for (PropertyDescriptor property : settableProperties) {
            Object currentVal;
            
            if (propertiesToIgnore.contains(property.getName())) continue;
            
            try {
                currentVal = PropertyUtils.getSimpleProperty(wo, property.getName());

                // check for a setter
                if (property.getWriteMethod() == null) continue;
                
            } catch (NoSuchMethodException e) {
                logger.debug("Skipping non-settable property "+property.getName()+" on "+wo.getClass().getName());
                continue;
            }
            
            Object oldVal = propertyNameToOldVal.get(property.getName());
            //XXX will replace this later
    		List<Object> additionalVals = new ArrayList<Object>();
    		if (wo instanceof OlapQuery && property.getName().equals("currentCube")) {
    			additionalVals.add(((OlapQuery) wo).getOlapDataSource());
    		}
            logger.debug("Checking property " + property.getName() + " was set to " + oldVal + ", actual value is " + currentVal);
			assertEquals(converterFactory.convertToBasicType(oldVal, additionalVals.toArray()), 
					converterFactory.convertToBasicType(currentVal, additionalVals.toArray()));
    	}
    	
    	logger.debug("Received " + countingListener.getPropertyChangeCount() + " change events.");
    	assertTrue(propertyChangeCount * 2 <= countingListener.getPropertyChangeCount());
	}

	/**
	 * Tests that if an error occurs while adding a child to a parent the child
	 * will be removed from the parent when rolled back.
	 */
    public void testPersisterCommitCanRollbackNewChild() throws Exception {
    	SPObject wo = getObjectUnderTest();
		SPObject parent = wo.getParent();
		
		//Removing the object under test from the parent but setting the object's parent
		//back to have an object that we can add to the parent through the persister but
		//is not currently a child of the object as that would cause two objects with the
		//same UUID to exist under the parent causing exceptions.
		wo.getParent().removeChild(wo);
		wo.setParent(parent);
		
		WabitSessionPersister persister = 
			new WabitSessionPersister("test persister", session, getWorkspace());
		
		CountingWabitListener countingListener = new CountingWabitListener();
		
		ErrorWabitPersister errorPersister = new ErrorWabitPersister();
		
		WorkspacePersisterListener listener = new WorkspacePersisterListener(session, errorPersister);
		
		SQLPowerUtils.listenToHierarchy(getWorkspace(), listener);
		parent.addSPListener(countingListener);
		
		int childrenBefore = parent.getChildren().size();
		
		persister.begin();
		
		class PublicListener extends WorkspacePersisterListener {
			public PublicListener(WabitSession session, SPPersister persister) {
				super(session, persister);
			}
			
			@Override
			public void persistChild(SPObject parent, SPObject child,
					Class<? extends SPObject> childClassType,
					int indexOfChild) {
				super.persistChild(parent, child, childClassType, indexOfChild);
			}
		};
		
		PublicListener listenerToPeristObject = new PublicListener(session, persister);
		listenerToPeristObject.persistChild(parent, wo, wo.getClass(), 0);
		
		errorPersister.setThrowError(true);
		boolean exceptionThrown;
		try {
			persister.commit();
			exceptionThrown = false;
		} catch (Throwable t) {
			//an error that made the commit failed was successfully passed on.
			exceptionThrown = true;
		}
		if (!exceptionThrown) fail("The exception from the errorPersister should be rethrown.");
		
		assertEquals("Incorrect number of children", childrenBefore, parent.getChildren().size());
		
		assertFalse(parent.getChildren().contains(wo));
		
		assertEquals("Child added event was not fired", 1, countingListener.getAddedCount());
		
		assertEquals("Child removed event was not fired", 1, countingListener.getRemovedCount());
	}
    
    /**
	 * Tests that if an error occurs while removing a child on a parent the child
	 * will be added to the parent when rolled back.
	 */
    public void testPersisterCommitCanRollbackRemovedChild() throws Exception {
    	SPObject wo = getObjectUnderTest();
		SPObject parent = wo.getParent();
		
		WabitSessionPersister persister = 
			new WabitSessionPersister("test persister", session, getWorkspace());
		
		CountingWabitListener countingListener = new CountingWabitListener();
		
		ErrorWabitPersister errorPersister = new ErrorWabitPersister();
		
		WorkspacePersisterListener listener = new WorkspacePersisterListener(session, errorPersister);
		
		SQLPowerUtils.listenToHierarchy(getWorkspace(), listener);
		parent.addSPListener(countingListener);
		
		int childrenBefore = parent.getChildren().size();
		
		persister.begin();
		
		persister.removeObject(parent.getUUID(), wo.getUUID());
		
		errorPersister.setThrowError(true);
		boolean exceptionThrown;
		try {
			persister.commit();
			exceptionThrown = false;
		} catch (Throwable t) {
			//an error that made the commit failed was successfully passed on.
			exceptionThrown = true;
		}
		if (!exceptionThrown) fail("The exception from the errorPersister should be rethrown.");
		
		assertEquals("Incorrect number of children", childrenBefore, parent.getChildren().size());
		
		assertTrue(parent.getChildren().contains(wo));
		
		assertEquals("Child added event was not fired", 1, countingListener.getAddedCount());
		
		assertEquals("Child removed event was not fired", 1, countingListener.getRemovedCount());
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
