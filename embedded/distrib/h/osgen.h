/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-01-15 14:45:16 +0000 (Mon, 15 Jan 2007) $
 * $LastChangedRevision: 334 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://192.168.2.249/svn/repos/ertcs/rtos/trunk/embedded/src/OS/osgen.h $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Generator level
 */
 
#ifndef OSGEN_H_
#define OSGEN_H_

#define OS_GEN_BUILD

/* Include library-level api headers */
#include <oslib.h>

/* Include public generation headers */
#include <osappcompiler.h>
#include <osapptarget.h>
#include <osappcore.h>

/* Macro to allow tasks and ISRs to be declared; entry function for task 'a' is named 'taskentry_a' */
#define TASK_PROTO(a)	void NEAR(os_taskentry_##a(void))
#define ISR_PROTO(a)	void NEAR(os_isrentry_##a(void))

#endif /*OSGEN_H_*/
