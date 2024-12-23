
This is the command line I want to assemble: 

/usr/lib/jvm/java-17/bin/java
-ea
-Dfile.encoding=UTF-8
-Dstdout.encoding=UTF-8
-Dstderr.encoding=UTF-8
-classpath 

/workspaces/socbn258/phkurrle/ws-phkurrle-EXE-2160-benchmarking-frameworks-backup/vobs/zenith/workspace/stex/libs/test_only/jmh-core.jar
/home/phkurrle/.m2/repository/org/openjdk/jmh/jmh-core/1.37/jmh-core-1.37-sources.jar
/workspaces/socbn258/phkurrle/ws-phkurrle-EXE-2160-benchmarking-frameworks-backup/vobs/zenith/workspace/stex/libs/test_only/jmh-generator-annprocess.jar
/home/phkurrle/.m2/repository/org/openjdk/jmh/jmh-generator-annprocess/1.37/jmh-generator-annprocess-1.37-sources.jar
/home/phkurrle/.m2/repository/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar
/home/phkurrle/.m2/repository/net/sf/jopt-simple/jopt-simple/5.0.4/jopt-simple-5.0.4.jar

-XX:+ShowCodeDetailsInExceptionMessages org.eclipse.jdt.internal.junit.runner.RemoteTestRunner
-version 3
-port 34757
-testLoaderClass org.eclipse.jdt.internal.junit5.runner.JUnit5TestLoader
-loaderpluginname org.eclipse.jdt.junit5.runtime
-classNames xoc.stex.jmh.CollectionsBenchmark


from intellij

C:\Users\philipp.kurrle\.jdks\openjdk-20.0.1\bin\java.exe "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2023.1.2\lib\idea_rt.jar=59419:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2023.1.2\bin" -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath C:\Users\philipp.kurrle\IdeaProjects\untitled2\target\classes;C:\Users\philipp.kurrle\.m2\repository\org\openjdk\jmh\jmh-core\1.37\jmh-core-1.37.jar;C:\Users\philipp.kurrle\.m2\repository\net\sf\jopt-simple\jopt-simple\5.0.4\jopt-simple-5.0.4.jar;C:\Users\philipp.kurrle\.m2\repository\org\apache\commons\commons-math3\3.6.1\commons-math3-3.6.1.jar org.openjdk.jmh.Main org.example.MyBenchmark.testMethod$