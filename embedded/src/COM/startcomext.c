/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-03-20 20:27:32 +0000 (Tue, 20 Mar 2007) $
 * $LastChangedRevision: 389 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/startcomext.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * Default StartCOMExtension() implementation in the library in case the user does not provide one $Req: artf1314 $
 */

#include <comint.h>

StatusType StartCOMExtension(void)
{
	return E_OK;
}
