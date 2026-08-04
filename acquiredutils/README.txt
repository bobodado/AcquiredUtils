ACQUIREDUTILS - READY TO BUILD
================================

1. EXTRACT THIS ZIP anywhere.

2. OPEN CMD in the "acquiredutils" folder.

3. SET JAVA_HOME (if not already set):
   set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot

4. BUILD:
   gradlew.bat build

5. YOUR MOD IS AT:
   build\libs\acquiredutils-1.0.0.jar

The build.gradle globally excludes fabric-content-registries-v0 to fix
the Loom 1.8 + Mojang mappings crash (even from YACL/ModMenu transitive deps).
