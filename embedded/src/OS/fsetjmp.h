/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-03-23 03:02:44 +0000 (Fri, 23 Mar 2007) $
 * $LastChangedRevision: 402 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/fsetjmp.h $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */
  
#ifndef FSETJMP_H_
#define FSETJMP_H_

/* Use proprietary fast setjmp code if necessary (some platforms have poor implementations of setjmp/longjmp) */
#ifdef USE_STANDARD_SETJMP

#include <setjmp.h>

#define SETJMP			setjmp
#define LONGJMP			longjmp
#define JMP_BUF			jmp_buf

#else

typedef os_fjmp_buf_word os_fjmp_buf[OS_FJMP_BUF_SIZE];

int os_fsetjmp(os_fjmp_buf env);
void os_flongjmp(os_fjmp_buf env, int val);

#define SETJMP(e)		os_fsetjmp(e)
#define LONGJMP(e, v)	os_flongjmp((e), (v))
#define JMP_BUF			os_fjmp_buf

#endif

#endif /*FSETJMP_H_*/
