/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-21 08:30:36 +0100 (Tue, 21 Aug 2007) $
 * $LastChangedRevision: 466 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/comint.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */
 
#ifndef COMINT_H_
#define COMINT_H_

#include <comlib.h>
#include <comappcore.h>

#define COM_TRACE_GET_MESSAGE_STATUS								(151U)
#define COM_TRACE_GET_MESSAGE_STATUS_FINISH							(152U)
#define COM_TRACE_INIT_MESSAGE										(153U)
#define COM_TRACE_INIT_MESSAGE_FINISH								(154U)
#define COM_TRACE_RECEIVE_REGULAR_MESSAGE							(155U)
#define COM_TRACE_RECEIVE_REGULAR_MESSAGE_FINISH					(156U)
#define COM_TRACE_RECEIVE_STREAM_MESSAGE							(157U)
#define COM_TRACE_RECEIVE_STREAM_MESSAGE_FINISH						(158U)
#define COM_TRACE_SEND_REGULAR_MESSAGE								(159U)
#define COM_TRACE_SEND_REGULAR_MESSAGE_FINISH						(160U)
#define COM_TRACE_SEND_STREAM_MESSAGE								(161U)
#define COM_TRACE_SEND_STREAM_MESSAGE_FINISH						(162U)
#define COM_TRACE_SEND_ZERO_MESSAGE									(163U)
#define COM_TRACE_SEND_ZERO_MESSAGE_FINISH							(164U)
#define COM_TRACE_START												(165U)
#define COM_TRACE_SHUTDOWN											(166U)

#ifndef DISABLE_TRACING
#include <osint.h>

#define COM_TRACE_ON()			{ENTER_KERNEL();}
#define COM_TRACE_OFF()			{LEAVE_KERNEL();}
#define COM_TRACE_CODE(c)		{OS_TRACE_CODE(c);}
#define COM_TRACE_HANDLE(h)		{OS_TRACE_HANDLE(h);}

#else

#define COM_TRACE_ON()			{}
#define COM_TRACE_OFF()			{}
#define COM_TRACE_CODE(c)		{c;}
#define COM_TRACE_HANDLE(h)		{h;}

#endif

/* If there is no resource allocated (r == 0) then there is no need
 * to guard access (the offline build process can detect when there
 * is no need for a resource and it can be optimized away). For
 * extended status this optimization doesn't take place since the error
 * checking for access to the messages is necessary.
 */

extern unat com_calldepth;			/* nesting level of COM API calls; used to allow graceful shutdown */
extern unat com_shutdown_pending;	/* shutdown pending flag; used to allow graceful shutdown */


/* @TODO task1041 check concurrent access to COM from error hook: is it OK to make COM calls from the error hook? */
#define ENTER_COM_ERROR_HOOK()					(GetResource(COM_RES_OS), com_COMErrorHook_callable = 0)
#define LEAVE_COM_ERROR_HOOK()					(com_COMErrorHook_callable = 1U, ReleaseResource(COM_RES_OS))
#define COM_ERROR_HOOK_CALLABLE()				(com_COMErrorHook_callable)

#define COM_STARTED()							(com_appmode != 0)


void com_final_shutdown(void);

#define COM_API_ENTER()							(com_calldepth++)
#define COM_API_LEAVE()							{if (--com_calldepth==0 && com_shutdown_pending) com_final_shutdown();}

#ifdef COM_EXTENDED_STATUS
/* Macros to range check MessageIdentifier values */
#define IS_SEND_MESSAGE(msg)					((msg) >= com_send_msgs && (msg) <= com_last_send_msg)
#define IS_RECEIVE_MESSAGE(msg)					((msg) >= com_rcv_msgs && (msg) <= com_last_rcv_msg)


#endif


/* @TODO artf1309 rename the namespace for the OS versions of the above (namespace is pretty polluted and needs to be fixed up) */

#define COM_ERRORHOOK_0(api, rc) 			{ com_serviceid = COMServiceId_##api; COMErrorHook(rc); }	/* $Req: artf1269 $ */

#define COM_ERRORHOOK_1(api, rc, p1)		{										\
												com_param1.api##_##p1 = p1;			\
												COM_ERRORHOOK_0(api, rc);			\
											}
									     
#define COM_ERRORHOOK_2(api, rc, p1, p2)	{										\
												com_param1.api##_##p1 = p1;			\
												com_param2.api##_##p2 = p2;			\
												COM_ERRORHOOK_0(api, rc);			\
											}									     

#define COM_ERRORHOOK_3(api, rc, p1, p2, p3)	{									\
												com_param1.api##_##p1 = p1;			\
												com_param2.api##_##p2 = p2;			\
												com_param3.api##_##p3 = p3;			\
												COM_ERRORHOOK_0(api, rc);			\
											}		

extern ResourceType COM_RES_OS;			/* Resource to lock to OS level, used by COM error hook only */

#endif /*COMINT_H_*/
