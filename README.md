EBICS Java Client
=====

This library allows to interact with banks using the EBICS (Electronic Banking Internet Communication Standard)

You can use the `EbicsClient` as command line tool or use it from your Java application.

Features:

- EBICS 3.0
- Support for French, German and Swiss banks
- Command line client to do the setup, initialization and to download files from the bank
- Tested extensively with [ZKB](https://zkb.ch)

How to get started:

https://github.com/ebics-java/ebics-java-client/wiki/EBICS-Client-HowTo

Parameterized launcher (without `ebics.txt`):

```
export EBICS_PASSWORD='changeit'
export EBICS_USER_ID='USER123'
export EBICS_PARTNER_ID='PARTNER123'
export EBICS_HOST_ID='HOST123'
export EBICS_BANK_URL='https://bank.example/ebics'

mvn exec:java \
  -Dexec.mainClass=org.kopi.ebics.client.ParameterizedEbicsClientLauncher \
  -Dexec.args="--create --ini --hia --hpb"
```

This mode is useful for containerized or ephemeral environments where `ebics.txt` should not be persisted.

The library is published to [Maven Central](https://central.sonatype.com/artifact/io.github.ebics-java/ebics-java-client).

Maven:
```
<dependency>
    <groupId>io.github.ebics-java</groupId>
    <artifactId>ebics-java-client</artifactId>
    <version>2.2.0</version>
</dependency>
```

Gradle:
```
dependencies {
    implementation 'io.github.ebics-java:ebics-java-client:2.2.0'
}
```

You can also build it directly from the source with Maven (`./mvnw clean install`).
See [RELEASING.md](RELEASING.md) for how releases are published to Maven Central.

### Snapshot / unreleased builds via JitPack

To pull in a specific commit or an unreleased version (any tag, branch, or commit hash) before it
reaches Maven Central, use [JitPack](https://jitpack.io/#ebics-java/ebics-java-client):

Maven:
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
    <version>master-SNAPSHOT</version> <!-- or a tag, branch, or commit hash -->
</dependency>
```

Gradle:
```
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.ebics-java:ebics-java-client:master-SNAPSHOT'
}
```

Note that JitPack builds keep the `com.github.ebics-java` group id (derived from the GitHub repo),
whereas released artifacts on Maven Central use `io.github.ebics-java`.

