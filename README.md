# HigherDimensionPandemic
Simulation of a pandemic in higher dimensions

## jMonkeyEngine

* Installation: https://wiki.jmonkeyengine.org/docs/documentation.html#install
  * Recommended: run `download-dependencies.sh` to automatically download and uncompress the dependencies. After that, always run javac/java with `-classpath "dependencies/jME3.3.2-stable/lib/*:dependencies/algs4.jar:."`
    ...Or just point your favorite IDE to the appropriate JAR and java source files.
  * Note: Windows users should use a semicolon (`;`) instead of a colon (`:`).

* Tutorials: https://wiki.jmonkeyengine.org/docs/jme3.html#tutorials-for-beginners

## Execution and usage

Simulate an N-dimensional pandemic with CollisionSystemRN.java, 
and display it with jMonkeyEngine. 

  `java HigherDimensionalPandemic DIM PNUM`

where DIM is the number of dimensions and PNUM is the number of random 
particles to create. 

### Accepted flags:  
 ```
  --help          			  Print this information.
  --fullscreen                             Display in fullscreen (if settings are hidden).
  --chart          			  Draw a chart using StdDraw. 
  --space           	   		  Use a space texture for the cube. 
  --textured-balls         	          Use a lagoon texture for the balls.
  --social-distancing [DOUBLE]             Restrict movement of this fraction of particles. 
  --dump-walls         		 	  Dump wall collision information to stdout.
  --dump-events                            Dump PQ event information to stdout.
 ```

### NOTE: 

If `ParticleN.DEFAULTRADIUS` is set to large-ish values, the program will go crazy. The (set of) fix(es) needed is pretty involved.

## Other licenses
This program uses [jMonkeyEngine](https://wiki.jmonkeyengine.org/docs/documentation.html), which is released under the [BSD license](https://wiki.jmonkeyengine.org/docs/bsd_license.html).

This program also uses Sedgewick & Wayne's [algs4.jar](https://algs4.cs.princeton.edu/code/) library, which is released under the [GNU GPLv3](http://www.gnu.org/copyleft/gpl.html). You should've already received a copy of it.

## Created
as part of the final CCM0128 (Computação II) assignment in the Molecular Sciences Course (CCM-USP) by:
* Aécio Beltrão Rodrigues Castro
* Lia Noguchi Simões
* Tiago Estevam Corrêa
