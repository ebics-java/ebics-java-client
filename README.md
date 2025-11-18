EBICS Java Client
=====

This library allows to interact with banks using the EBICS (Electronic Banking Internet Communication Standard)

You can use the `EbicsClient` as command line tool or use it from your Java application.

Features:

- EBICS 2.5 (EBICS 3.0 coming soon)
- Support for French, German and Swiss banks
- Command line client to do the setup, initialization and to download files from the bank
- Tested extensively with [ZKB](https://zkb.ch)

How to get started:

https://github.com/ebics-java/ebics-java-client/wiki/EBICS-Client-HowTo

You can build it directly from the source with maven or use the releases from [JitPack](https://jitpack.io/#ebics-java/ebics-java-client/).

Gradle:
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}

dependencies {
    implementation 'com.github.ebics-java:ebics-java-client:1.3'
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
    <groupId>com.github.ebics-java</groupId>
    <artifactId>ebics-java-client</artifactId>
    <version>1.3</version>
</dependency>
```
 

