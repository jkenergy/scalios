package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/Resource.java $
 * 
 */

import java.util.*;
import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of OSModelElement that models a Resource within the OS.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public class Resource extends OSModelElement {

	
	/**
	 * The type of the resource
	 */
	private ResourceKind resourceProperty=ResourceKind.STANDARD_LITERAL;
	
		
	/**
	 * Set of Runnables that access the Resource
	 */
	private Set<Runnable> runnables = new LinkedHashSet<Runnable>();

	/**
	 * Set of LockingTimes that relate to the Resource
	 */
	private Set<LockingTime> lockingTimes = new LinkedHashSet<LockingTime>();							/* $Req: AUTOSAR $ */
	
	/**
	 * The Resource to which this resource is linked (if any)
	 */
	private Resource linkedResource=null;
	
	/**
	 * Set of Resources that are linked to this Resource (inverse of linkedResource)
	 */
	private Set<Resource> linkedResources = new LinkedHashSet<Resource>();	
	

	
	/**
	 * Helper method that accumulates the set of all Resources linked to a resource.
	 * 
	 * note: this method is recursive (infinite recursion avoided using contents of set).
	 * 
	 * @param resources the set of resources in which to accumulate linked resources
	 * @see #getAllLinkedResources()
	 */
	private void getAllLinkedResources2(Set<Resource> resources) {
			
		if (linkedResource != null) {
			
			// This resource is linked to a single target resource, so add to returned resources set.
			if ( resources.add(linkedResource) ) {
				// resource was not already in the set, so ask that resource to get all its linked resources
				linkedResource.getAllLinkedResources2(resources);
			}
		}
		
		// Iterate over all resources that link to this resource as a target
		Iterator<Resource> iter = linkedResources.iterator();
		
		while (iter.hasNext()) {
			Resource next = iter.next();
			
			// Next resource linked to this resource as a target, so add to returned resources set.
			if ( resources.add(next) ) {
				// next resource was not already in the set, so ask that resource to get all its linked resources
				next.getAllLinkedResources2(resources);
			}
		}
	}	
	
	
	/**
	 * Sets the type of this property
	 * @param newResourceProperty the new type of the resource
	 * @see ResourceKind 
	 */
	public void setResourceProperty(ResourceKind newResourceProperty) {
		
		resourceProperty=newResourceProperty;
	}
	

	/**
	 * @return the type of the resource
	 * @see ResourceKind 
	 */
	public ResourceKind getResourceProperty() {
		return resourceProperty;
	}
	
	
	/**
	 * Adds a Runnable to the collection of runnables that access the resource.
	 * This method creates a two way relationship.
	 * 
	 * @param runnable the Runnable to be added
	 */
	public void addRunnable(Runnable runnable) {
		
		if ( runnable!=null ) {
			if ( runnables.add(runnable) )
				runnable.addResource(this);	// inform Runnable that it accesses this resource
		}
	}	
	
	/**
	 * @return the collection of runnables that access the resource.
	 */
	public Collection<Runnable> getRunnables() {
		return runnables;
	}
	
	/**
	 * Adds a LockingTime to the collection of LockingTimes that relate to the resource.
	 * This method creates a two way relationship.
	 * 
	 * @param lockingTime the LockingTime to be added
	 */
	public void addLockingTime(LockingTime lockingTime) {
		
		if ( lockingTimes!=null ) {
			if ( lockingTimes.add(lockingTime) )
				lockingTime.setResource(this);	// inform LockingTimes that it reslates to this resource
		}
	}	
	
	/**
	 * @return the collection of LockingTimes that relate to the resource.
	 */
	public Collection<LockingTime> getLockingTime() {
		return lockingTimes;
	}	
	
	/**
	 * Sets the Resource to which this resource is linked.
	 * @param newResource the resource to which this resource is linked
	 */
	public void setLinkedResource(Resource newResource) {
		
		if ( newResource!=null ) {
			
			if ( linkedResource!=newResource ) {
				
				linkedResource=newResource;
				linkedResource.addLinkedResource(this);
			}
		}
	}
	
	/**
	 * 
	 * @return the Resource to which this resource is linked
	 */
	public Resource getLinkedResource() {
		return linkedResource;
	}
	
	/**
	 * Adds a Resource to the collection of resources that are linked to the resource.
	 * This method creates a two way relationship.
	 * 
	 * @param resource the Resource to be added
	 */
	public void addLinkedResource(Resource resource) {
		
		if ( resource!=null ) {
			if ( linkedResources.add(resource) )
				resource.setLinkedResource(this);	// inform Resource that is is linked to this resource
		}
	}
	
	/**
	 * 
	 * @return the collection of resources that are linked to the resource
	 */
	public Collection<Resource> getLinkedResources() {
		return linkedResources;
	}
	
	/**
	 * Returns the transitive closure of all resources to which this resource is linked.
	 * 
	 * i.e. the Set of resources that are either directly or indirectly linked to this
	 * Resource via chains of linked resources (in both directions along the chain).
	 * 
	 * The returned set always includes this resource.
	 * 
	 * @return the Collection of ALL resources to which this resource is linked
	 */
	public Collection<Resource> getAllLinkedResources() {
		
		Set<Resource> allLinkedResources = new HashSet<Resource>();
		
		allLinkedResources.add(this);	// add this to set to prevent recursive link problems.
		
		// Use helper method to get full set of ALL resources linked to this resource. 
		getAllLinkedResources2(allLinkedResources);
		
		return allLinkedResources;
	}


	/**
	 * 
	 * @return true if the resource links to itself via the {@link #linkedResource} relationship chain.
	 */
	private boolean checkForInfiniteLink() {
		
		Resource nextLinkedResource = linkedResource;
		
		while ( nextLinkedResource != null ) {
			
			if ( nextLinkedResource == this )
				return true;
			
			nextLinkedResource = nextLinkedResource.getLinkedResource();
		}
		
		return false;		
	}
	
	
	/**
	 * Returns the Runnable that has the highest priority of the two given.
	 * Isr priorities are always evaluated to be higher than Task priorities.
	 * 
	 * If either Runnable is null then returns the other Runnable
	 * If both Runnables are null then returns null
	 * 
	 * @param r1
	 * @param r2
	 * @return Runnable with highest priority (null, if r1 == r2 == null)
	 */
	private static Runnable maxPriorityRunnable(Runnable r1, Runnable r2) {
		
		if (r1 == null ) {
			return r2;
		}
	
		if (r2 == null ) {
			return r1;
		}
		
		if (r1 instanceof Isr && r2 instanceof Task) {
			return r1;
		}
		
		if (r1 instanceof Task && r2 instanceof Isr) {
			return r2;
		}		
	
		if (r1.getPriority() > r2.getPriority()) {
			return r1;
		}
		
		return r2;
	}	
	
	
	/**
	 * Returns the highest priority {@link Runnable} that accesses this resource directly,
	 * i.e. not via any resources linked to this resource.
	 * 
	 * @return the highest priority Runnable that accesses the resource directly (may be null) 
	 * @see #getHighestAccessor()
	 */
	public Runnable getLocalHighestAccessor() {
		
		Runnable highestAccessor = null;
		
		Iterator<Runnable> iter = runnables.iterator();
		
		while (iter.hasNext()) {
			Runnable next = iter.next();
			
			highestAccessor = maxPriorityRunnable(highestAccessor, next);
		}
		
		return highestAccessor;
	}
	
	/**
	 * Returns the highest priority {@link Runnable} that accesses this resource either directly
	 * or indirectly via a linked Resource chain.
	 * 
	 * @return the highest priority Runnable that accesses the resource (may be null)
	 * @see #getLocalHighestAccessor()
	 */
	public Runnable getHighestAccessor() {
		
		Runnable highestAccessor = null;
		
		// Iterate over collection of all resources to which this resource is linked
		// (including the resource itself) and find the highest accessor.
		Iterator<Resource> iter = getAllLinkedResources().iterator();
		
		while (iter.hasNext()) {
			Resource next = iter.next();
			
			// Get local accessor of the next resource
			Runnable nextAccessor = next.getLocalHighestAccessor();
			
			// Check if the current Accessor has a higher priority the nextAccessor
			highestAccessor = maxPriorityRunnable(highestAccessor, nextAccessor);
		}
		
		return highestAccessor;
	}
	
	
	/**
	 * 
	 * @return true if the resource is an INTERNAL resource, else false
	 */
	public boolean isInternal() {
		return  ResourceKind.INTERNAL_LITERAL.equals(resourceProperty);
	}	
	
	/**
	 * 
	 * @return true if the resource is an STANDARD resource, else false
	 */
	public boolean isStandard() {
		return  ResourceKind.STANDARD_LITERAL.equals(resourceProperty);
	}
	
	/**
	 * 
	 * @return true if the resource is an LINKED resource, else false
	 */
	public boolean isLinked() {
		return  ResourceKind.LINKED_LITERAL.equals(resourceProperty);
	}	
	
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems, deepCheck);
		
		// Do check of this element

		//[1] If the resourceProperty value equals LINKED then a linkedResource must be identified.
		if ( isLinked() && linkedResource == null ) {
			problems.add(new Problem(Problem.ERROR, "Resource '"+getName()+"' is a linked resource, but has no target resource specified"));
		}

		//[2] If the resourceProperty value does not equal LINKED then a linkedResource must not be identified.
		if ( isLinked() == false && linkedResource != null ) {
			problems.add(new Problem(Problem.INFORMATION, "Resource '"+getName()+"' is not linked resource, but has a target resource specified"));
		}		

		//[3] Any identified linkedResource must have a resourceProperty value that equals LINKED or STANDARD. 
		if ( linkedResource != null ) {
			
			if ( linkedResource.isStandard() == false && linkedResource.isLinked() == false ) {
				problems.add(new Problem(Problem.ERROR, "Resource '"+getName()+"' may not be linked to an internal resource"));
			}
		}
	
		// [4] A resource may not have a linkedResource that is directly or indirectly linked to itself. [warning]
		if ( linkedResource != null ) {
			if ( linkedResource == this ) {
				problems.add(new Problem(Problem.WARNING, "Resource '"+getName()+"' links directly to itself"));
			}
			if ( checkForInfiniteLink() ) {
				problems.add(new Problem(Problem.WARNING, "Resource '"+getName()+"' links indirectly to itself"));
			}
		}

		// [5] A resource should be accessed by at least one Runnable.
		if ( runnables.isEmpty() ) {
			problems.add(new Problem(Problem.INFORMATION, "Resource '"+getName()+"' is never accessed by a Task/ISR"));
		}
	}

		
	/**
	 * Constructor.
	 * 
	 * @param cpu
	 * @param name
	 */
	public Resource(Cpu cpu,String name) {
		super(cpu,name);
	}	
	
}
