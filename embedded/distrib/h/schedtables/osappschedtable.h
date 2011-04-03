/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-03-23 02:56:13 +0000 (Fri, 23 Mar 2007) $
 * $LastChangedRevision: 401 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://192.168.2.249/svn/repos/ertcs/rtos/trunk/embedded/src/OS/schedtables/osappschedtable.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		External
 */

#ifndef SCHEDTABLE_H_
#define SCHEDTABLE_H_

/* Indicates the alarm used to drive this schedule table. Callback from alarm is used to process
 * the schedule table. */
struct os_schedtabcb {
	AlarmType alarm;							/* The alarm to use to request events; alarm can be driven by a hardware counter $Req: artf1047 $ or software counter $Req: artf1037 $ */
	void (*expired)(ScheduleTableType);			/* The handler that processes this schedule table */
	struct os_schedtabcb_dyn *dyn;
	const struct os_xpoint *first_xpoint;		/* The first expiry point in the table */
	unat periodic;								/* True if the schedule table is periodic $Req: artf1036 $ */
};

struct os_schedtabcb_dyn {
	unat nexted;								/* Has this been the subject of a NextScheduleTable() call? */
	const struct os_xpoint *current_xpoint;		/* Current expiry point */
	ScheduleTableType next_tab;					/* Next schedule table to set running when this one reaches end of table (0 if none) */
};

/* An expiry point has a set of elements associated with it. This is either a task for activation or
 * an event to be set for a task. The expiry processing is handled in the same way as for alarms.
 * Uses same handlers as alarm processing? */
struct os_xpoint_element {
	void (*process)(const union os_expirycb *);	/* Function programmed to process the expiry point */
	const union os_expirycb action;				/* Data for processing the expiry point */
};

/* Schedule Table Expiry Point. Contains details of the action to undertake at the expiry point. */
struct os_xpoint {
	const struct os_xpoint *next_xpoint;		/* Next expiry point in the table; 0 = no more in the table */
	TickType delta;								/* Number of ticks to next expiry point in the table */
	const struct os_xpoint_element *actions;	/* Array of actions */
	unat num_actions;							/* Number of actions */
};

/* Used for ID checks */
extern const struct os_schedtabcb *os_first_schedtab;	/* 0 if no schedule tables */
extern const struct os_schedtabcb *os_last_schedtab;	/* 0 if no schedule tables */

extern const struct os_schedtabcb os_schedtables[];
extern const unat os_num_schedtables;

#endif /*SCHEDTABLE_H_*/
