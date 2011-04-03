/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-04-04 00:49:40 +0100 (Wed, 04 Apr 2007) $
 * $LastChangedRevision: 409 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://192.168.2.249/svn/repos/ertcs/rtos/trunk/embedded/src/OS/osappcore.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		User
 * 
 * This file defines the internal kernel data structures and defines the OS API calls.
 * 
 * These structures are visible here because they are used to compile the data
 * produced by the build tool.
 */

#ifndef _OSAPPCORE_H_
#define _OSAPPCORE_H_

typedef os_stackword *os_stackp;		/* Stack pointer type */

#include "counters/osappcounter.h"
#include "events/osappevent.h"
#include "hooks/osapphook.h"
#include "kernel/osappkernel.h"
#include "resources/osappresource.h"
#include "schedtables/osappschedtable.h"
#include "tasks/osapptask.h"

/* Link check variable declarations */
extern const unat os_eslnk;
extern const unat os_sslnk;
extern const unat os_smlnk;
extern const unat os_nosmlnk;

#endif /* _OSAPPCORE_H_ */
