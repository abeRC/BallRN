#shorthands
_CLASSPATH="dependencies/jME3.3.2-stable/lib/*:dependencies/algs4.jar:."
_JAVAC="javac -classpath "$_CLASSPATH""
_JAVA="java -classpath "$_CLASSPATH""

$_JAVAC HigherDimensionPandemic.java && $_JAVA HigherDimensionPandemic