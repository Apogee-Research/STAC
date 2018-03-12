To modify the application, edit the source code (located in ../source)
and then use the following command to rebuild the application jar:

  ./gradlew build

After this successfully completes, there will be a jar file located in
the build/libs directory.  This jar file can be copied to replace the
one in the challenge program library using the command:

  cp build/libs/*.jar ../challenge_program/lib

