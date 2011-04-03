/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-03-09 06:48:38 +0000 (Fri, 09 Mar 2007) $
 * $LastChangedRevision: 366 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/hooks/serviceid.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * Contains instantiations of service ID variables (should they be needed).
 */
 
#include <osint.h>

/* $Req: artf1230 $ */
union os_param os_param1;									/* Up to three parameters in an API call */
union os_param os_param2;
union os_param os_param3;

/* $Req: artf1229 $ */
OSServiceIdType os_serviceid;							/* Global to indicate which API call failed */
