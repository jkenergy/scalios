package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/DriverManager.java $
 * 
 */


import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * A DriverManager that allows dynamic access and creation of drivers and devices.<br><br>
 * 
 * This class is responsible for loading and instantiating classes derived from {@link TargetDriver}.
 * 
 * 
 * @author Mark Dixon
 *
 */
public class DriverManager {
 
	/**
	 * The value to be used as a prefix to a driver name is order to create the full driver Class name.
	 * At the moment this prefix is the name of this class followed by an underscore (_).
	 * e.g. if a driver is named "timer", then its implementing class must be called -
	 * TargetDriver_timer
	 * 
	 * @see #getDriverName(Class) since this assumes use of this format.
	 */
	private final static String DRIVER_PREFIX = TargetDriver.class.getName()+"_";	
	
	
	/**
	 * The {@link TargetCpu} that owns this manager
	 */
	TargetCpu	targetCpu;
	
	/**
	 * Collection of {@link TargetDriver} instances created by the driver manager.
	 */
	private Collection<TargetDriver> targetDrivers = new LinkedHashSet<TargetDriver>();


	/**
	 * Returns an existing {@link TargetDriver} that has the given name.
	 * If no driver has the given name then returns null.
	 * 
	 * @param <E> type of TargetDriver to be returned.
	 * @param name the name of the required driver.
	 * @return the {@link TargetDriver} instance (subtype of TargetDriver), null if named driver does not exist.
	 */
	@SuppressWarnings("unchecked")
	private <E extends TargetDriver> E getNamedDriver(String name) {
	
		for ( TargetDriver nextDriver : targetDrivers ) {
			if ( nextDriver.getName().equals(name) ) {
				return (E)nextDriver;
			}
		}
		return null;	// named driver does not exist
	}	
		
	
	/**
	 * Returns a named {@link TargetDriver}. If the named driver already exists
	 * then that is returned, otherwise attempts to load the driver's Class
	 * and create a new instance of the driver. 
	 *
	 * @param <E> type of TargetDriver to be returned.
	 * @param driverName the name of the required driver.
	 * @return the {@link TargetDriver} instance (subtype of TargetDriver), null if unable to load driver Class.
	 */
	@SuppressWarnings("unchecked")
	public <E extends TargetDriver> E getDriver(String driverName) {
         
		// attempt to get driver, if already loaded and instantiated
		E driver = getNamedDriver(driverName);

		if ( driver == null ) {
       
			// driver not yet loaded
	        try {
	        	 	// TODO: must extract package name in some other way
	        		// this may depend on how the driver class is loaded, i.e. in future may use URL to load the class
	        	
	        	// Construct the name of the class
	        	String className = DRIVER_PREFIX+driverName;
	        		
	        	// get existing driver Class instance, loading Class if necessary
	        	Class<?> driverClass = Class.forName(className);
	              
	        	if ( driverClass != null && TargetDriver.class.isAssignableFrom(driverClass)) {
	        		
	        		try {
	        			// driver Class now exists, so can create the instance using the class
	        			driver = (E)driverClass.newInstance();
			             
	        			// instance created ok, so setup attributes
	        			driver.setName(driverName);
	        			driver.setTargetCpu(targetCpu);     			
	        			targetDrivers.add(driver);
	        			
	        		} catch (InstantiationException e) {
	        			driver = null;
			        } catch (IllegalAccessException e) {
			        	driver = null;
			        }
	        	}    
	        }
	        catch (ClassNotFoundException e) {
	             
	        }
		}
		
        return driver;
      }


	/**
	 * Gets all the driver #include names.
	 * 
	 * @return the collection of driver #include names.
	 */
	public Collection<String> getDriverIncludeNames() {
      
 		Collection<String> includeNames = new HashSet<String>();
	  
 		for (TargetDriver next : targetDrivers) {
 			String nextName = next.getIncludeName();
 			
 			if ( nextName != null ) {
 				includeNames.add(nextName);
 			}
 		}
 		
 		return includeNames;  
	}
	
	
	/**
	 * @return all the {@link TargetDriver} instances created by the driver manager.
	 */
	public Collection<TargetDriver> getTargetDrivers() {
		return targetDrivers;
	}
	
	
	/**
	 * @return all the {@link TargetDevice} instances owned by all the created TargetDrivers
	 */
	public Collection<TargetDevice> getTargetDevices() {
		
		Collection<TargetDevice> targetDevices = new LinkedHashSet<TargetDevice>();
		
		for (TargetDriver next : targetDrivers) {
			targetDevices.addAll(next.getTargetDevices());
		}
		return targetDevices;
	}

	
	/**
	 * Static method that when given a {@link TargetDriver} derived "Driver" Class, returns a textual name for the driver.
	 * 
	 * The name is derived by extracting all characters in the class name following the first underscore ("_") character.
	 * If no underscore is present in the name then the class name is returned.
	 * 
	 * @param driverClass the {@link TargetDriver} derived class for which the driver name is required
	 * @return the name for the driver
	 */
	public static String getDriverName(Class<? extends TargetDriver> driverClass) {
		
		String name = driverClass.getSimpleName();
		
		// attempt to return driver name as anything following "_" in the class name
		int index = name.indexOf('_');
		
		if ( index >=0 ) {
			return name.substring(index+1);
		}
		
		return name;	// use the class name if no underscore present
	}	
	
	/**
	 * @return the targetCpu
	 */
	public TargetCpu getTargetCpu() {
		return targetCpu;
	}

	/**
	 * 
	 * @param targetCpu the {@link TargetCpu} that owns this manager
	 */
	DriverManager(TargetCpu	targetCpu) {
		this.targetCpu = targetCpu;
	}
}
