/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-03-23 02:56:13 +0000 (Fri, 23 Mar 2007) $
 * $LastChangedRevision: 401 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://192.168.2.249/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/osappcounter.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		User
 */
 
#ifndef COUNTER_H_
#define COUNTER_H_

/* A union describing one of four possible actions upon expiry; also used in the schedule table code */
union os_expirycb {
	struct {
		EventMaskType event;						/* Mask of event set when alarm expires */	
		TaskType task;								/* Task to which event is sent $Req: artf1210 $ */
	} event;
	TaskType task;									/* Task activated $Req: artf1210 $ */
	os_callbackf callback;							/* Alarm callback function to be called when alarm expires $Req: artf1210 $ */
	CounterType counter;							/* Counter that is incremented */
	ScheduleTableType schedtable;					/* Schedule table that is driven by this alarm */
};

/* Arbitrary number of alarms supported; $Req: artf1093 $ */
struct os_alarmcb {									/* Static (ROM) part of the alarm control block */
	struct {										/* Accessor to the appropriate dynamic (RAM) part of the control block */
		struct os_alarmcb_multi_dyn *m;				/* 0 if alarm is a singleton */
		struct os_alarmcb_common_dyn *c;
	} dyn;
	void (*process)(const union os_expirycb *);		/* Function programmed to process the alarm action */
	CounterType counter;							/* Counter to which the alarm is bound $Req: artf1209 $ */
	union os_expirycb action;
};

struct os_cdevicecb;								/* Anonymous struct; can only assign pointers */

struct os_cdevice_softcb_dyn {						/* Software counter control block; stored in RAM */
	TickType count;
	TickType enabled;
};

/* Counter control block */
struct os_countercb {
	struct os_countercb_multi_dyn *dyn_m;							/* $Req: artf1104 $ 0 if counter is a singleton */
	AlarmBaseType alarmbase;								/* Details of the TickType range, etc. */
	AlarmType singleton_alarm;										/* Singleton alarm (0 if counter supports multiple alarms) */
	AlarmType (*expired)(CounterType);								/* Appropriate function for handling expiry: singleton or multi; returns alarm that expired */
	void (*setrelalarm)(CounterType, AlarmType, os_longtick, TickType);			/* Handler to set the alarm with a relative time */
	void (*cancel)(AlarmType, CounterType);							/* Function programmed to cancel the alarm */
	struct os_cdevicecb *device;									/* Hardware-specific device control block pointer; null if the counter is software */
	void (*stop)(struct os_cdevicecb *);							/* Stop the underlying hardware counter from running */
	void (*start)(struct os_cdevicecb *);							/* Start the underlying hardware counter running; set count to zero if possible (see 11.3 p42 OS spec. 2.2.3) */
	void (*disable_ints)(struct os_cdevicecb *);					/* Stop the counter from interrupting (clear down any pending interrupts; leave counter running) */
	void (*enable_ints)(struct os_cdevicecb *, TickType, TickType);		/* Start the counter interrupting rel after a given time 'now' (defined to be in the past); clear down any prior pending interrupt */
	TickType (*now)(struct os_cdevicecb *);							/* Time 'now' for the given counter hardware */
};

struct os_alarmcb_multi_dyn {				/* Dynamic (RAM) stored part of the alarm control block for many alarms on a single counter $Req: artf1104 $*/
	/* "next" and "prev" are "don't care" when "running" is zero */
	AlarmType		next;				/* Next alarm in the time-ordered chain on the same counter */
	AlarmType		prev;				/* Previous alarm in the time-ordered chain on the same counter; value is "don't care" when item is at the head) */
};

struct os_alarmcb_common_dyn {				/* Dynamic (RAM) stored part of the alarm control block for a singleton alarm */
	os_longtick	due;					/* Time this alarm is due to expire */
	TickType		cycle;				/* Period for cyclic alarms, otherwise 0 for single shot alarms */
	unat		running;				/* Flag that determines whether alarm is running */
};

struct os_countercb_multi_dyn {			/* Dynamic (RAM) stored part of the multi-alarm counter control block $Req: artf1104 $ */
	AlarmType head;						/* Alarm at the head of the event list */
	TickType last_now;					/* Last value returned by the now() device driver; used to synthesise longer time range */
	TickType top_now;					/* Top word of synthesized 'now' */
};

/* Used for ID checks */
#define os_first_counter 		(&(os_counters[0]))		/* Always at least one valid counter ("System Counter") */
extern const struct os_countercb *os_last_counter;

extern const struct os_countercb os_counters[];		/* Counter control block instances */
extern const unat os_num_counters;

/* Used for ID checks */
extern const struct os_alarmcb *os_first_alarm;		/* 0 if no alarms */
extern const struct os_alarmcb *os_last_alarm;		/* 0 if no alarms */

extern const struct os_alarmcb os_alarms[];	/* Alarm control block instances */
extern const unat os_num_alarms;

/* Counter time function */
os_longtick os_counter_now(CounterType c, TickType short_now);

/* Handlers for alarm expiry, accessed via function pointers */
void os_expiry_activatetask(union os_expirycb *);
void os_expiry_setevent(union os_expirycb *);
void os_expiry_callback(union os_expirycb *);
void os_expiry_incrementcounter(union os_expirycb *);

/* Function for incrementing counter; shared between API call and increment counter handler */
void os_increment_counter(CounterType c);

/* Handlers for ExpireCounter, accessed via function pointers */
AlarmType os_counter_expired_multi(CounterType c);
AlarmType os_counter_expired_single(CounterType c);

/* Handlers for SetRelAlarm, accessed via function pointers */
void os_setrelalarm_multi(AlarmType a, CounterType c, os_longtick long_now, TickType rel);
void os_setrelalarm_single(AlarmType a, CounterType c, os_longtick long_now, TickType rel);

/* Handlers for CancelAlarm, access via function pointers */
void os_cancel_alarm_multi(AlarmType a, CounterType c);
void os_cancel_alarm_single(AlarmType a, CounterType c);

#endif /*COUNTER_H_*/
