/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 *
 * $LastChangedDate: 2008-03-29 02:11:24 +0000 (Sat, 29 Mar 2008) $
 * $LastChangedRevision: 698 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/osappcounter.h $
 *
 * Target CPU:          Generic
 * Target compiler:     Standard ANSI C
 * Visibility:          User
 */
 
#ifndef COUNTER_H_
#define COUNTER_H_

/* Software counter driver function prototypes and macro to detect if a given counter is a software one */
void os_cdevicedrv_soft_stop(DeviceId);
void os_cdevicedrv_soft_start(DeviceId);
void os_cdevicedrv_soft_disable_ints(DeviceId);
void os_cdevicedrv_soft_enable_ints(DeviceId, TickType, TickType);
TickType os_cdevicedrv_soft_now(DeviceId dev);

struct os_expirycb;                                                     /* Anonymous struct; can only assign pointers. This is cast by expiry processing functions to appropriate type (see structs below) */
 
/* Structs to store the data required for possible actions upon alarm expiry; Ptrs to these are stored and passed as os_expirycb */
 
struct os_expiry_eventcb {
      EventMaskType event;                                 				/* Mask of event set when alarm expires */ 
      TaskType task;                                              		/* Task to which event is sent $Req: artf1210 $ */
};

struct os_expiry_taskcb {
      TaskType task;                                                    /* Task activated $Req: artf1210 $ */
};

struct os_expiry_callbackcb {
      os_callbackf callback;                                      		/* Alarm callback function to be called when alarm expires $Req: artf1210 $ */
};

struct os_expiry_countercb {
      CounterType counter;                                        		/* Counter that is incremented */
};
 
struct os_expiry_schedtabcb {
      ScheduleTableType schedtable;                         			/* Schedule table that is driven by this alarm */
};
 
/* Arbitrary number of alarms supported; $Req: artf1093 $ */
struct os_alarmcb {                                                     /* Static (ROM) part of the alarm control block */
      struct {                                                          /* Accessor to the appropriate dynamic (RAM) part of the control block */
            struct os_alarmcb_multi_dyn *m;                       		/* 0 if alarm is a singleton */
            struct os_alarmcb_common_dyn *c;
      } dyn;
      void (*process)(const struct os_expirycb *);    					/* Function programmed to process the alarm action */
      CounterType counter;                                        		/* Counter to which the alarm is bound $Req: artf1209 $ */
      const struct os_expirycb *action;                     			/* ptr to expiry action data passed to the function that processes the alarm expiry */
};

/*
 * Structures that specify a software counter device.
 * This is a concrete implementation of the os_devicecb anonymous struct
 */  
struct os_devicecb_soft_dyn {                                    		/* Software counter control block; stored in RAM */
      TickType count;
      TickType match;
      TickType enabled;
};
 
struct os_devicecb_soft {
      struct os_devicecb_soft_dyn *dyn;
      TickType maxallowedvalue;
};
 
/*
 * Structure that specifies driver functions for each counter Driver.
 * This is a concrete implementation of the os_drivercb anonymous struct
 */ 
struct os_counter_drivercb {
      void (*stop)(DeviceId);                        		/* Stop the underlying hardware counter from running */
      void (*start)(DeviceId);                       		/* Start the underlying hardware counter running; set count to zero if possible (see 11.3 p42 OS spec. 2.2.3) */
      void (*disable_ints)(DeviceId);                		/* Stop the counter from interrupting (clear down any pending interrupts; leave counter running) */
      void (*enable_ints)(DeviceId, TickType, TickType);	/* Start the counter interrupting rel after a given time 'now' (defined to be in the past); clear down any prior pending interrupt */
      TickType (*now)(DeviceId);							/* Time 'now' for the given counter hardware */
}; 
 
/* Counter control block */
struct os_countercb {
      struct os_countercb_dyn *dyn;													/* Dynamic part of counter control block */
      AlarmBaseType alarmbase;                                          			/* Details of the TickType range, etc. */
      AlarmType singleton_alarm;                                        			/* Singleton alarm (0 if counter supports multiple alarms) */
      void (*expired)(CounterType);  		                              			/* Appropriate function for handling expiry: singleton or multi; returns alarm that expired */
      void (*setrelalarm)(AlarmType, CounterType, os_longtick, TickType, TickType);	/* Handler to set the alarm with a relative time */
      void (*cancel)(AlarmType, CounterType);										/* Function programmed to cancel the alarm */
      DeviceId device;                                								/* Hardware-specific device control block pointer; this is cast to appropriate concrete type by driver functions */
	  const struct os_counter_drivercb *driver;                        				/* Driver to handle counter device */      
};

struct os_alarmcb_multi_dyn {                   /* Dynamic (RAM) stored part of the alarm control block for many alarms on a single counter $Req: artf1104 $*/
      /* "next" and "prev" are "don't care" when "running" is zero */
      AlarmType         next;                   /* Next alarm in the time-ordered chain on the same counter */
      AlarmType         prev;                   /* Previous alarm in the time-ordered chain on the same counter; value is "don't care" when item is at the head) */
};

struct os_alarmcb_common_dyn {                 	/* Dynamic (RAM) stored part of the alarm control block for a singleton alarm */
      os_longtick 		due;                    /* Time this alarm is due to expire */
      TickType          cycle;                  /* Period for cyclic alarms, otherwise 0 for single shot alarms */
      unat        		running;                /* Flag that determines whether alarm is running */
};

struct os_countercb_dyn {           		    /* Dynamic (RAM) stored part of the counter control block $Req: artf1104 $ */
      AlarmType 		head;                   /* Alarm at the head of the event list; 0 = empty list (always 0 for singleton counters) */
      TickType 			last_now;               /* Last value returned by the now() device driver; used to synthesise longer time range */
      uint32 			long_tick;				/* Top part of tick; see os_now() */
};

/* Used for ID checks */
extern const struct os_countercb * const os_first_counter;		/* 0 if no counters */
extern const struct os_countercb * const os_last_counter;		/* 0 if no counters */

extern const struct os_countercb const os_counters[];       	/* Counter control block instances */
extern const unat os_num_counters;

/* Used for ID checks */
extern const struct os_alarmcb * const os_first_alarm;       	/* 0 if no alarms */
extern const struct os_alarmcb * const os_last_alarm;        	/* 0 if no alarms */

extern const struct os_alarmcb const os_alarms[];     			/* Alarm control block instances */
extern const unat os_num_alarms;

/* Handlers for alarm expiry, accessed via function pointers */
void os_expiry_activatetask(const struct os_expirycb *);
void os_expiry_setevent(const struct os_expirycb *);
void os_expiry_callback(const struct os_expirycb *);
void os_expiry_incrementcounter(const struct os_expirycb *);

/* Function for incrementing counter; shared between API call and increment counter handler */
void os_increment_counter(CounterType c);

/* Handlers for ExpireCounter, accessed via function pointers */
void os_counter_expired_multi(CounterType c);
void os_counter_expired_single(CounterType c);

/* Handlers for SetRelAlarm, accessed via function pointers */
void os_setrelalarm_multi(AlarmType a, CounterType c, os_longtick long_now, TickType short_now, TickType rel);
void os_setrelalarm_single(AlarmType a, CounterType c, os_longtick long_now, TickType short_now, TickType rel);

/* Handlers for CancelAlarm, access via function pointers */
void os_cancel_alarm_multi(AlarmType a, CounterType c);
void os_cancel_alarm_single(AlarmType a, CounterType c);

/* Worker functions to deal with modulo tick time */
os_longtick os_now(CounterType c, TickType short_now);

#endif /*COUNTER_H_*/
