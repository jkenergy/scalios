/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal/ipcint.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		User
 */

/* Standard internal messaging COM drivers. Included by internal code only.
 */
#ifndef IPCINT_H_
#define IPCINT_H_

#define IPC_NOTIFY(dest)			((dest)->notify_action ? (com_notify((dest)->notify_data, (dest)->notify_action)) : E_OK)
#define IPC_NOTIFY_HIGH(dest)		((dest)->high_notify_action ? (com_notify((dest)->high_notify_data, (dest)->high_notify_action)) : E_OK)
#define IPC_NOTIFY_LOW(dest)		((dest)->low_notify_action ? (com_notify((dest)->low_notify_data, (dest)->low_notify_action)) : E_OK)

/* COM internal driver data guard macros */
#ifdef COM_EXTENDED_STATUS
#define IPC_LOCK_BUFFER(r)			{ if (GetResource(r) != E_OK) return E_COM_SYS_ACCESS; }
#else
#define IPC_LOCK_BUFFER(r)			{ GetResource(r); }
#endif
#define IPC_UNLOCK_BUFFER(r)		{ ReleaseResource(r); }

#endif /*IPCINT_H_*/
