/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/buffer.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>



/* Block copy data, scratch duff's device for block copy, may use for certain platforms. */
void com_block_copy(uint8 *src, uint8 *dest, uint16 length)
{
	COMLengthType count = length;
	assert(count > 0);
	
	switch (count & 0x0007U)
	{
		case 0:	do {*dest++ = *src++;
		case 7:  	*dest++ = *src++;
		case 6:  	*dest++ = *src++;
		case 5:  	*dest++ = *src++;
		case 4:  	*dest++ = *src++;
		case 3:  	*dest++ = *src++;
		case 2:  	*dest++ = *src++;
		case 1:  	*dest++ = *src++;
				} while ((count -= 8) > 0);
	}
}
