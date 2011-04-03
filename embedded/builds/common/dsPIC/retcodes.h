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
		case E_OS_SYS_UNKNOWN_CODE: return "E_OS_SYS_UNKNOWN_CODE";
		case E_OS_SYS_TRAP0: return "E_OS_SYS_TRAP0";
		case E_OS_SYS_OSCFAILURE: return "E_OS_SYS_OSCFAILURE";
		case E_OS_SYS_ADDRERROR: return "E_OS_SYS_ADDRERROR";
		case E_OS_SYS_MATHERROR: return "E_OS_SYS_MATHERROR";
		case E_OS_SYS_TRAP5: return "E_OS_SYS_TRAP5";
		case E_OS_SYS_TRAP6: return "E_OS_SYS_TRAP6";
		case E_OS_SYS_TRAP7: return "E_OS_SYS_TRAP7";
#endif /*RETCODES_H_*/
