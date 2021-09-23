EBICS Java Client
=====

This library allows to interact with banks using the EBICS (Electronic Banking Internet Communication Standard)

You can use the `EbicsClient` as command line tool or use it from your Java application.

How to get started:

https://github.com/uwemaurer/ebics-java-client/wiki/EBICS-Client-HowTo

You can build it directly from the source with maven or use the releases from [JitPack](https://jitpack.io/#uwemaurer/ebics-java-client/).

Gradle:
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}

dependencies {
    implementation 'com.github.uwemaurer:ebics-java-client:97867ac56e'
}
```
Maven
```
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>

<dependency>
    <groupId>com.github.uwemaurer</groupId>
    <artifactId>ebics-java-client</artifactId>
    <version>97867ac56e</version>
</dependency>
```
 

This project is based on https://sourceforge.net/p/ebics/

Main differences with this fork:

- Support for French, German and Swiss banks
- Command line client to do the setup, initialization and to download files from the bank
- Use of maven for compilation instead of ant + Makefile + .sh scripts
