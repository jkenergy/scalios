/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-12-20 19:20:44 +0000 (Thu, 20 Dec 2007) $
 * $LastChangedRevision: 506 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/testing/assert.c $
 * 
 * $CodeReview: kentindell, 2006-10-16 $
 * $CodeReviewItem: update comments for the extended task dispatch to reflect restorecx being a flag not a function pointer $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Application
 * 
 * Default assertion failure function included in the OS library. Can be overriden with specific assertion failure handler.
 */
void os_assertionfailure(char *where)
{
	volatile int x;
	for(;;) {
		x++;				/* Place breakpoint here to detect assertion failures */
	}
}
