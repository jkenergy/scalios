package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/Generator.java $
 * 
 */

import java.io.IOException;


/**
 * Interface that should be implemented by any class wishing to generate code.
 * 
 * @author Mark Dixon
 *
 */
public interface Generator {

	/**
	 * Perform generation from the current osModel, directing generated content to the speicifed root path location.
	 * @param rootPath the root of the generated content (if null uses CWD)
	 * @throws IOException
	 */
	public abstract void generate(String rootPath) throws IOException;
}
