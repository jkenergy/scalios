/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-12-21 02:35:27 +0000 (Fri, 21 Dec 2007) $
 * $LastChangedRevision: 509 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/builds/test1/main.c $
 * 
 * Minimal "hello world" test. Does nothing: no tasks, no ISRs, no hooks, etc. Test fails if StartOS returns.
 * The function "idle" overrides the default one in the library; the OS calls idle() repeatedly while running.
 */

#include <osapp.h>
#include <framework.h>

int main() {
	
	StartOS(OSDEFAULTAPPMODE);
	
	test_failed("Shouldn't have reached here");
	
	return 0;
}

/* This is an override of the idle task in the kernel.
 */
void os_idle(void)
{
	test_passed();
}
