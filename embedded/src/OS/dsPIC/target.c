/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-02-12 13:22:44 +0000 (Mon, 12 Feb 2007) $
 * $LastChangedRevision: 361 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/target.c $
 * 
 * Target CPU:			dsPIC30F
 * Target compiler:		Standard ANSI C
 * Visibility:			Internal
 */

#include <osint.h>

ISRType os_isrcb_h;		/* Temporary RAM variable Used by ZLIH and FLIH for nesting race resolution */
