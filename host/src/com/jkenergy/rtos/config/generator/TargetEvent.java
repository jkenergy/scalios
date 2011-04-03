package com.jkenergy.rtos.config.generator;


/*
 * $LastChangedDate: 2008-02-25 21:38:01 +0000 (Mon, 25 Feb 2008) $
 * $LastChangedRevision: 623 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetEvent.java $
 * 
 */

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.jkenergy.rtos.config.osmodel.Event;


/**
 * Intermediate target element used to store information on Events to be generated.
 * 
 * @author Mark Dixon
 *
 */

public class TargetEvent extends TargetElement {

	/**
	 * The model bit Mask of this event (null if auto-calculated)
	 */
	private BigInteger modelMask = null;

	/**
	 * isAutoMask flag that specifies whether the bit mask is to be automatically calculated
	 */
	private boolean isAutoMask=false;	

	/**
	 * The target bit Mask of this event 
	 */
	private BigInteger targetMask=BigInteger.ZERO;	
	
	/**
	 * Set of TargetTask instances that are set within this event
	 */
	private Collection<TargetTask> targetTasks = new HashSet<TargetTask>();	

	/**
	 * A set of events that do not have tasks in common with this event
	 * and therefore can share same mask bit.
	 */
	private Set<TargetEvent> friends = new HashSet<TargetEvent>();
	
	/**
	 * A set of events that do have tasks in common with this event
	 * and therefore can not share same mask bit (avoids)
	 */
	private Set<TargetEvent> avoids = new HashSet<TargetEvent>();
	
	/**
	 * A set of friend events where all of the members are friends and do not need to avoid each other.
	 */
	private Set<TargetEvent> club = new HashSet<TargetEvent>();
	
	/**
	 * Adds an event to the set of friends of this event
	 * @param event the TargetEvent to add
	 */
	protected void addFriend(TargetEvent event) {
		friends.add(event);
	}
	
	/**
	 * Adds an event to the set of avoids of this event
	 * @param event the TargetEvent to add
	 */
	protected void addAvoid(TargetEvent event) {
		avoids.add(event);
	}

	/**
	 * Adds an event to the set of events in the same club as this event
	 * @param event the TargetEvent to add
	 */
	protected void addToClub(TargetEvent event) {
		club.add(event);
	}
	
	/**
	 * @return Returns the targetMask.
	 */
	protected BigInteger getTargetMask() {
		return targetMask;
	}
	
	/**
	 * @param targetMask The targetMask to set.
	 */
	protected void setTargetMask(BigInteger targetMask) {
		this.targetMask = targetMask;
	}

	/**
	 * 
	 * @return the position of the highest bit set in the target mask (0 = no bits set, 1 = bit0 set, 16 = bit15 set, etc.)
	 */
	public int getHighestSetMaskBit() {
		
		BigInteger tmpMask = targetMask;
		int highestSetBitPos = 0;
		
		while ( tmpMask.equals(BigInteger.ZERO) == false ) {
			// more bits in the mask
			highestSetBitPos++;
			tmpMask = tmpMask.shiftRight(1);
		}
		
		return highestSetBitPos;
	}

	
	/**
	 * @return Returns the modelMask.
	 */
	protected BigInteger getModelMask() {
		return modelMask;
	}

	/**
	 * @return Returns the isAutoMask.
	 */
	protected boolean isAutoMask() {
		return isAutoMask;
	}

	/**
	 * @return Returns the targetTasks.
	 */
	protected Collection<TargetTask> getTargetTasks() {
		return targetTasks;
	}

	/**
	 * Associates the given TargetTask with the event.
	 * @param task the TargetTask to be associated with the event
	 */
	protected void addTargetTask(TargetTask task) {
		targetTasks.add(task);
	}	
	
	
	/**
	 * Sets up the internal associations to referenced elements
	 *
	 */
	@Override
	protected void initialiseModelAssociations() {
		
		super.initialiseModelAssociations();
		
		Event event = getEvent();
		
		if (event != null) {
			
			targetTasks = getAllTargetElements(event.getTasks());		
		}
	}	
	
	/**
	 * @return Returns the OS Model Event on which the TargetEvent is based (if any)
	 */
	public Event getEvent() {
		
		assert  getOsModelElement() == null || getOsModelElement() instanceof Event;
		
		return (Event)getOsModelElement();
	}	
	
	/**
	 * Standard Constructor that creates a TargetEvent that does not represent a OSModelElement.
	 * @param cpu the TargetCpu that owns the element
	 * @param name the name of the element
	 */
	protected TargetEvent(TargetCpu cpu, String name) {
		super(cpu, name);
	}

	/**
	 * Copy contructor that creates a TargetEvent that represents a OSModelElement.
	 * 
	 * @param cpu the TargetCpu that owns the element
	 * @param osModelElement the osModelElement which this element is to represent.
	 */
	protected TargetEvent(TargetCpu cpu, Event osModelElement) {
		
		super(cpu, osModelElement);
		
		// copy required info. from the given OSModelElement

		this.isAutoMask = osModelElement.isAutoMask();
		
		if (this.isAutoMask == false) {
			this.modelMask = osModelElement.getMask();
		}
	}

	/**
	 * @return Returns the avoids.
	 */
	protected Set<TargetEvent> getAvoids() {
		return avoids;
	}

	/**
	 * @return Returns the club.
	 */
	protected Set<TargetEvent> getClub() {
		return club;
	}

	/**
	 * @return Returns the friends.
	 */
	protected Set<TargetEvent> getFriends() {
		return friends;
	}	
}
