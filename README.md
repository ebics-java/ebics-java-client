EBICS Java Client
=====

Fork of the Java open source ebics client project - https://sourceforge.net/p/ebics/

How to get started:

https://github.com/uwemaurer/ebics-java-client/wiki/EBICS-Client-HowTo

Main differences with this fork:

- Support for French, German and Swiss banks
- Command line client to do the setup, initialization and to download files from the bank
- Use of maven for compilation instead of ant + Makefile + .sh scripts

Running from command line (without Maven) 

java -cp target/ebics-1.1-SNAPSHOT.jar:target/lib/* EbicsClient -xe2 -i src/test/resources/pain001/pain001.xml

Issues: 
* Fat jar build seems to be not possible with standard maven plugins due to bouncy castle signed jar.