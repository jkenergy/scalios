/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-01-09 07:27:49 +0000 (Wed, 09 Jan 2008) $
 * $LastChangedRevision: 528 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/resources/osappresource.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		User
 */

#ifndef RESOURCE_H_
#define RESOURCE_H_

/* Arbitrary number of resources supported */
/* $Req: artf1091 $ */
struct os_rescb {							/* Static (ROM) part of the resource control block */
	struct os_rescb_dyn *dyn;				/* Accessor for dynamic part of resource */
	os_pri ceil;							/* Highest base priority of any task that may lock this resource */
};


/* @TODO really should move this into osgenresource.h because application code only needs to
 * see the structures that are referred to by handles.
 */
 
/* Resource control block */
struct os_rescb_dyn {						/* Dynamic (RAM) part of the resource control block */
	os_pri prev;							/* Priority of task just before it locked this resource */
	ResourceType prevlocked;			/* Reference to the resource previously locked before this one, 0 if resource not currently locked */
										/* points to this resource (i.e. itself) if no previously locked resource */
};

/* Used for ID checks */
#define os_first_res							(&(os_resources[0]))

extern const struct os_rescb * const os_last_res;
extern const struct os_rescb const os_resources[];	/* Resource control block instances */

#endif /*RESOURCE_H_*/
