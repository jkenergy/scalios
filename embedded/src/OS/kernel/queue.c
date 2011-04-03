#ifndef QUEUE_H
#define QUEUE_H

#include <osint.h>

/* Compiled twice: OPTIMIZED_QUEUES defined for optimized faster version, STANDARD_QUEUES otherwise. */

#if !defined(OPTIMIZED_QUEUES) && !defined(STANDARD_QUEUES)
#error Must have OPTIMIZED_QUEUES or STANDARD_QUEUES defined
#endif

/* Queue a task on its associated priority queue and increment its activation count  */
#ifdef OPTIMIZED_QUEUES
void os_queuetask_opt(TaskType t)
#else
void os_queuetask_std(TaskType t)
#endif
{
	assert(KERNEL_LOCKED());

	/* First step is to check it's OK to continue. This is a vital test because the FIFO queues are
	 * exactly large enough to hold the number of activations of the tasks at the priority of the queue.
	 */
	assert(*t->count < t->countlimit);
	
	assert(t != OS_IDLE_TASK);											/* Idle task never gets queued */
	
	(*t->count)++;													/* Activation count incremented $Req: artf1086 $ */

#ifndef OPTIMIZED_QUEUES
	if (t->queue) {													/* Queued or counted? This is an optimisation over the OSEK FIFO spec */
		/* Declare and initialise cache accessors to the FIFO queue */
		os_queueh q = t->queue;
		struct os_queuecb_dyn *qdyn = q->dyn;

		assert(q->last != q->first);								/* Cannot have 1-element queue: for 1-element there will be no queue and counted instead */
		assert(q->uniquetask == 0);

		/* Now queue the task in the FIFO priority queue */
		*qdyn->tail = t;
		if (qdyn->tail == q->last) {
			qdyn->tail = q->first;
		} else {
			qdyn->tail++;
		}
	}
	else {
#endif
		assert(os_priqueue[t->basepri - 1]->uniquetask == t);		/* Asserts that the queue control block corresponding
																	 * to the base priority of the task indicates the task
																	 * is the only one in the queue */
		assert(os_priqueue[t->basepri - 1]->first == 0);
		assert(os_priqueue[t->basepri - 1]->last == 0);
		assert(os_priqueue[t->basepri - 1]->dyn == 0);
#ifndef OPTIMIZED_QUEUES
	}
#endif

	/* Set the bit in the mask to indicate the queue is not empty */
	os_priqueuestatus |= t->basepri_mask;

#ifdef ASSERTIONCHECKS
	if (t->basepri > os_curpri) {		  							/* curpri is the priority of the calling task before we entered the kernel */
		/* If execution reaches here then the newly queued task must be queued in a previously empty
		 * queue because otherwise tasks in that queue would be running and this caller wouldn't be
		 * running and therefore wouldn't have made this API call in the first place.
		 */
		 
		/* Assert the queue has one element used:
		 * 
		 * Either task is unique and count == 1; OR
		 * Either tail is one behind the head; OR
		 * Tail has wrapped and head is at the start
		 */
		assert((t->queue == 0 && *(t->count) == 1) ||
				(t->queue->dyn->tail == t->queue->dyn->head + 1) ||
				(t->queue->dyn->tail == t->queue->first && t->queue->dyn->head == t->queue->last));

		assert(!IN_CAT2_ISR());										/* Can't ever be ready to switch when running an ISR handler */
	}
#endif

	assert(os_nexttask);											/* Never not set */
	
	/* The activated task might now be the highest priority
	 * one; do a quick test and change the cache of the next highest priority task
	 */
	if (t->basepri > os_nexttask->basepri) {
		os_nexttask = t;
	}	
}

#ifdef OPTIMIZED_QUEUES
/* De-queue the current task from its associated priority queue and decrement its activation count  */
void os_dequeuetask_opt(void)
#else
void os_dequeuetask_std(void)
#endif
{	
	unat c;															/* Cache of *(os_curtask->count) */
	os_queueh q = os_curtask->queue;								/* Cache of os_curtask->queue */
	os_primask tmp;
	
	assert(KERNEL_LOCKED());
	
	assert(os_curtask != OS_IDLE_TASK);								/* Idle task never gets dequeued */
	
	assert(*(os_curtask->count) > 0);
	assert(*(os_curtask->count) <= os_curtask->countlimit);
	
	/* Decrement task activation count and remove from front of FIFO priority queue */
	c = --(*(os_curtask->count));									/* $Req: artf1086 $ */

	assert(os_priqueuestatus & os_curtask->basepri_mask);			/* Bit corresponding to task's base priority is set */

#ifndef OPTIMIZED_QUEUES
	if (q) {
		struct os_queuecb_dyn *qdyn = q->dyn;

		assert(q->last != q->first);								/* Cannot have 1-element queue: for 1-element there will be no queue and counted instead */
		if (qdyn->head == q->last) {								/* May need to manually cache these since compiler might not codegen well; @todo */
			qdyn->head = q->first;
		} else {
			qdyn->head++;
		};
		
		/* Update the priority bitmask; head == tail only if full or empty; can't be full because we just took an item out */
		if (qdyn->head == qdyn->tail) {								/* Head == Tail means queue is empty; if queue is now empty then take out the bit in the status mask */
			os_priqueuestatus ^= os_curtask->basepri_mask;
			assert(c == 0);											/* Assert this task cannot be in the queue any more */
		}
	}
	else {
#endif
		/* the priority queue has been optimised away, so no need to manipulate to dequeue */
		if (c == 0) {
			os_priqueuestatus ^= os_curtask->basepri_mask;
		}
#ifndef OPTIMIZED_QUEUES
	}
#endif
	
	/* Find the new highest priority task to run
	 * 
	 * Use FSB to find the highest priority non-empty queue, then go to the queue control block
	 * and find the head of the queue. Look at the slot and return the task handle, which is the
	 * highest priority ready task in the system.
	 * 
	 * If the priority queue has been optimised away (see ActivateTask) then we need to check
	 * to see if we can go straight to the task
	 * 
	 */
	tmp = os_priqueuestatus;
	if(tmp) {
		/* If there is something in the priority queues then find the highest priority task */
		/* FSB() returns 0 .. n (n = 15 or 31 or 63 depending on target-specifics). A return value of 0 corresponds to priority 1, and so on. */
		FSB(tmp);	/* FSB has side-effect to write first-set-bit back into tmp; makes inline asm work better */
		q = os_priqueue[tmp];
#ifdef OPTIMIZED_QUEUES
		os_nexttask = q->uniquetask;
#else
		if (q->uniquetask) {												/* If uniquetask is non-null then it points to the only task in the queue; queue storage optimised away */
			os_nexttask = q->uniquetask;
		}
		else {
			os_nexttask = *(q->dyn->head);
		}
#endif
	}
	else {
		/* If there is nothing queued then the highest priority task is the idle task */
		os_nexttask = OS_IDLE_TASK;
	}
}

#endif /* QUEUE_H */
