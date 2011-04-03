/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-10-20 10:36:51 +0100 (Sat, 20 Oct 2007) $
 * $LastChangedRevision: 479 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/osint.h $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#ifndef _IOS_H_
#define _IOS_H_

#define OS_INTERNAL_BUILD

/* Include library-level api headers */
#include <oslib.h>

/* Include library build environment */
#include <osappcompiler.h>
#include <osapptarget.h>
#include <osappcore.h>

/* Include internal headers */
#include <osintcompiler.h>
#include <osinttarget.h>

#include "counters/osintcounter.h"
#include "devices/osintdevices.h"
#include "events/osintevent.h"
#include "hooks/osinthook.h"
#include "kernel/osintkernel.h"
#include "resources/osintresource.h"
#include "schedtables/osintschedtable.h"
#include "tasks/osinttask.h"
#include "trace/osinttrace.h"

#endif /* _IOS_H_ */
