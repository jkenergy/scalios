/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-01-10 08:52:06 +0000 (Thu, 10 Jan 2008) $
 * $LastChangedRevision: 547 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/hooks/osinthook.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */
 
#ifndef IHOOK_H_
#define IHOOK_H_


/* The error hook scheme requires that the API provide access to details of what the
 * last API call was, what the parameters to the call were, etc. This is done
 * by the ERRORHOOK_n macros, where n indicates the number of parameters to the call.
 * 
 * The serviceid global variable has assigned to it the system call (passed via
 * the ERRORHOOK_n macro parameter). The parameters are assigned to a union (for
 * type compatibility) via the same macro. The standard OSEK API calls are macros
 * to pull the data back out. The ERRORHOOK_n macros are called from the functions
 * implementing each OSEK API call.
 * 
 * The os_call_error_hook() function actually makes the call to the error hook function.
 * 
 * The ERRORHOOK_n macros are invoked only when it is clear that the error hook needs
 * to be called (not E_OK, error hook configured to be called, not already in an error hook)
 * 
 * $Req: artf1228 $
 * $Req: artf1229 $
 * $Req: artf1230 $
 * $Req: artf1115 $
 * $Req: artf1114 $
 */
#define OS_ERRORHOOK_0(api, rc) 			{	os_serviceid = OSServiceId_##api; os_call_error_hook(rc); }

#define OS_ERRORHOOK_1(api, rc, p1)			{											\
												os_param1.api##_##p1 = p1;				\
												OS_ERRORHOOK_0(api, rc);				\
											}
									     

#define OS_ERRORHOOK_2(api, rc, p1, p2)		{											\
												os_param1.api##_##p1 = p1;				\
												os_param2.api##_##p2 = p2;				\
												OS_ERRORHOOK_0(api, rc);				\
											}									     

#define OS_ERRORHOOK_3(api, rc, p1, p2, p3)	{											\
												os_param1.api##_##p1 = p1;				\
												os_param2.api##_##p2 = p2;				\
												os_param3.api##_##p3 = p3;				\
												OS_ERRORHOOK_0(api, rc);				\
											}									     


#endif /*IHOOK_H_*/
