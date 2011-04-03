/* Target-specific fragment of C code switch statement to give a string version
 * of a target-specific StatusType code.
 *
 * Copyright (C) 2004, 2005, 2006, 2007, 2008 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-11 23:35:25 +0000 (Tue, 11 Mar 2008) $
 * $LastChangedRevision: 677 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://192.168.1.240/svn/repos/ertcs/rtos/trunk/embedded/builds/common/dsPIC/counterhw.oil $
 */

#ifndef RETCODES_H_
#define RETCODES_H_
		#define E_OS_SYS_UNDEFINED_INSTR		(40U)
		#define E_OS_SYS_SOFTWARE_INT			(41U)
		#define E_OS_SYS_PREFETCH_ABORT			(42U)
		#define E_OS_SYS_DATA_ABORT				(43U)
		#define E_OS_SYS_RESERVED				(44U)
#endif /*RETCODES_H_*/
