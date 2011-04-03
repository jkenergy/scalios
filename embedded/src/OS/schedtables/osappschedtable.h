/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-24 06:32:28 +0000 (Mon, 24 Mar 2008) $
 * $LastChangedRevision: 692 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/schedtables/osappschedtable.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		External
 */

#ifndef SCHEDTABLE_H_
#define SCHEDTABLE_H_

/* Function is a handler to process a schedule table $Req: artf1032 $
 * 
 * This is similar to the three other expiry handlers for alarms:
 * 
 *   os_expiry_activatetask(const struct os_expirycb *)
 *   os_expiry_setevent(const struct os_expirycb *)
 *   os_expiry_callback(const struct os_expirycb *)
 *   os_expiry_incrementcounter(const struct os_expirycb *)
 * 
 * but is implemented in the schedule table subsystem.
 */
void os_expiry_schedtab(const struct os_expirycb *);

/* Indicates the alarm used to drive this schedule table. Callback from alarm is used to process
 * the schedule table. */
struct os_schedtabcb {
	AlarmType alarm;							/* The alarm to use to request events; alarm can be driven by a hardware counter $Req: artf1047 $ or software counter $Req: artf1037 $ */
	struct os_schedtabcb_dyn *dyn;
	const struct os_xpoint *first_xpoint;		/* The first expiry point in the table */
	unat periodic;								/* True if the schedule table is periodic $Req: artf1036 $ */
};

struct os_schedtabcb_dyn {
	unat first_run;								/* Is this the first run of the table since being started? ("Don't care" when not running) */
	unat nexted;								/* Has this been the subject of a NextScheduleTable() call? */
	const struct os_xpoint *current_xpoint;		/* Current expiry point */
	ScheduleTableType next_tab;					/* Next schedule table to set running when this one reaches end of table (0 if none) */
};

/* An expiry point has a set of elements associated with it. This is either a task for activation or
 * an event to be set for a task. The expiry processing is handled in the same way as for alarms.
 * Uses same handlers as alarm processing? */
struct os_xpoint_element {
	void (*process)(const struct os_expirycb *);	/* Function programmed to process the expiry point */
	const struct os_expirycb *action;				/* Data for processing the expiry point */
};

/* Schedule Table Expiry Point. Contains details of the actions to undertake at the expiry point. */
struct os_xpoint {
	const struct os_xpoint *next_xpoint;		/* Next expiry point in the table; 0 = no more in the table */
	TickType delta;								/* Number of ticks to next expiry point in the table */
	const struct os_xpoint_element *actions;	/* Array of actions */
	unat num_actions;							/* Number of actions */
};

/* Used for ID checks */
extern const struct os_schedtabcb * const os_first_schedtab;	/* 0 if no schedule tables */
extern const struct os_schedtabcb * const os_last_schedtab;		/* 0 if no schedule tables */

extern const struct os_schedtabcb const os_schedtables[];
extern const unat os_num_schedtables;

#endif /*SCHEDTABLE_H_*/
