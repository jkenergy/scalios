$Id: README.txt 19 2006-02-23 23:26:23Z kentindell $

--------------
2006-02-23 21:15:11Z kentindell

This project contains the embedded RTOS code written in C. The main line of development is in

	/jke/rtos/trunk/embedded

Branches of the embedded project are in:

	/jke/rtos/branches/a.b.c/embedded

(where a.b.c is replaced by the product release version)

The Eclipse IDE will place all files in a sub-hierarchy under .../embedded.

The parallel project to .../embedded is .../host (written in Java). The versioning in
branches applies a product version across both Eclipse projects.
--------------
2006-02-23 22:43:10 kentindell

The dsPIC subfolder contains target-specific header files (including compiler-specific ones)
The project has an include path defined to pull in header files from this directory. Further
ports will define further target folders with the target-specific headers. Each build will
then define a different include directory to bring in the right target-specific headers.

