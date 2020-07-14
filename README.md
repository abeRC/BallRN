# BallRN
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
 ` --help            Print this information. 
  --chart           Draw a chart using StdDraw. 
  --space           Use a space texture for the cube. 
  --social-distancing     Restrict movement by 3/4. 
  --max-social-distancing   Restrict movement by 7/8. 
  --dump-walls         Dump wall collision information to stdout.`