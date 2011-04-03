/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-10-20 10:36:51 +0100 (Sat, 20 Oct 2007) $
 * $LastChangedRevision: 479 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/schedtables/osintschedtable.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#ifndef ISCHEDTABLE_H_
#define ISCHEDTABLE_H_

/* os_first_schedtab and os_last_schedtab might be 0 if no schedule tables so check for non-zero handle first */
#define VALID_SCHEDTAB(s)			((s) && (s) >= os_first_schedtab && (s) <= os_last_schedtab)

#endif /*ISCHEDTABLE_H_*/
