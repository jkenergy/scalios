package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-26 22:59:46 +0000 (Sat, 26 Jan 2008) $
 * $LastChangedRevision: 588 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetResource.java $
 * 
 */

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


import com.jkenergy.rtos.config.osmodel.Resource;


/**
 * Intermediate target element used to store information on resources to be generated.
 * 
 * @author Mark Dixon
 *
 */


public class TargetResource extends TargetElement {

	/**
	 * flag that specifies whether the TargetResource is an internal resource. 
	 */
	private boolean isInternal;

	/**
	 * The highest target priority of any Runnable that accesses the resource, either directly or indirectly
	 * (via linked resource chains). 
	 */
	private int ceilingTargetPriority = TargetPriorities.INVALID_PRIORITY;
	
	/**
	 * Set of TargetRunnable instances that access the TargetResource
	 */
	private Collection<TargetRunnable> targetRunnables = new LinkedHashSet<TargetRunnable>();	
	
	/**
	 * The TargetResource to which this resource is linked (if any)
	 */
	private TargetResource linkedResource=null;
	
	/**
	 * Set of TargetResource that are linked to this TargetResource (inverse of linkedResource)
	 */
	private Collection<TargetResource> linkedResources = new LinkedHashSet<TargetResource>();
	
	

	/**
	 * Helper method that accumulates the set of all TargetResource instances linked to a resource.
	 * 
	 * note: this method is recursive (infinite recursion avoided using contents of set).
	 * 
	 * @param resources the set of target resources in which to accumulate linked resources
	 */
	private void getAllLinkedResources(Set<TargetResource> resources) {
			
		if (linkedResource != null) {
			
			// This resource is linked to a single target resource, so add to returned resources set.
			if ( resources.add(linkedResource) ) {
				// resource was not already in the set, so ask that resource to get all its linked resources
				linkedResource.getAllLinkedResources(resources);
			}
		}
		
		// Iterate over all resources that link to this resource as a target
		for (TargetResource next : resources) {
			
			// next resource linked to this resource as a target, so add to returned resources set.
			if ( resources.add(next) ) {
				// next resource was not already in the set, so ask that resource to get all its linked resources
				next.getAllLinkedResources(resources);
			}		
		}
	}
	
	/**
	 * Sets up the ceiling priority of the TargetResource
	 */
	protected void setCeiling() {
		
		assert ceilingTargetPriority == TargetPriorities.INVALID_PRIORITY;	// can only set ceiling once
		
		// Construct a set of all resources to which this resource is linked
		Set<TargetResource> allResources = new HashSet<TargetResource>();
		
		allResources.add(this);
		
		getAllLinkedResources(allResources);
		
		// Iterate over all linked resources looking for highest priority accessor
		for (TargetResource resource : allResources) {
			
			ceilingTargetPriority = 0;
			
			for (TargetRunnable runnable : resource.getTargetRunnables()) {
				
				if (runnable.getTargetPriority() > ceilingTargetPriority) {
					ceilingTargetPriority = runnable.getTargetPriority();
				}
			}
		}
	}
	
	/**
	 * @return Returns the ceiling target priority.
	 */
	protected int getCeiling() {
		
		assert ceilingTargetPriority != TargetPriorities.INVALID_PRIORITY; // setCeiling() must be called prior to this
		
		return ceilingTargetPriority;
	}
	
	
	
	/**
	 * @return Returns the linkedResource.
	 */
	protected TargetResource getLinkedResource() {
		return linkedResource;
	}

	/**
	 * @return Returns the linkedResources.
	 */
	protected Collection<TargetResource> getLinkedResources() {
		return linkedResources;
	}

	/**
	 * @return Returns the isInternal.
	 */
	protected boolean isInternal() {
		return isInternal;
	}

	/**
	 * @return Returns the targetRunnables.
	 */
	protected Collection<TargetRunnable> getTargetRunnables() {
		return targetRunnables;
	}
	
	/**
	 * Adds the runnable to the list of TargetRunnables that access this TargetResource.
	 * 
	 * @param runnable the TargetRUnnable that accesses this TargetResource.
	 */
	protected void addTargetRunnable(TargetRunnable runnable) {
		targetRunnables.add(runnable);
	}
	
	/**
	 * 
	 * @return true if the resource is directly accessed by one or more TargetRunnable instances.
	 */
	protected boolean isAccessedDirectly() {
		
		return (targetRunnables.size() > 0);
	}
	
	/**
	 * Sets up the internal associations to referenced elements
	 *
	 */
	@Override
	protected void initialiseModelAssociations() {
		
		super.initialiseModelAssociations();
		
		Resource resource = getResource();
		
		if (resource != null) {
			
			targetRunnables = getAllTargetElements(resource.getRunnables());
			
			linkedResource = (TargetResource)getTargetElement(resource.getLinkedResource());
			
			linkedResources = getAllTargetElements(resource.getLinkedResources());
		}
	}	

	/**
	 * @return Returns the OS Model Resource on which the TargetResource is based (if any)
	 */
	public Resource getResource() {
		
		assert  getOsModelElement() == null || getOsModelElement() instanceof Resource;
		
		return (Resource)getOsModelElement();
	}
	

	
	/**
	 * Standard Constructor that creates a TargetResource that does not represent a OSModelElement.
	 * @param cpu the TargetCpu that owns the element
	 * @param name the name of the element
	 */
	protected TargetResource(TargetCpu cpu, String name) {
		super(cpu, name);
	}

	/**
	 * Copy contructor that creates a TargetResource that represents a OSModelElement.
	 * 
	 * @param cpu the TargetCpu that owns the element
	 * @param osModelElement the osModelElement which this element is to represent.
	 */
	protected TargetResource(TargetCpu cpu, Resource osModelElement) {
		
		super(cpu, osModelElement);
		
		// copy required info. from the given OSModelElement
		this.isInternal = osModelElement.isInternal();
	}		
}
