package com.jkenergy.rtos.config.serializers;

/*
 * $LastChangedDate: 2008-01-26 22:59:46 +0000 (Sat, 26 Jan 2008) $
 * $LastChangedRevision: 588 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/serializers/PreProcessorException.java $
 * 
 */

import java.io.IOException;

/**
 * Exception indicating a value is out of range
 * 
 * @author Mark Dixon
 *
 */

@SuppressWarnings("serial")
public class PreProcessorException extends IOException {

	protected PreProcessorException(String msg) {
		super(msg);
	}
}
