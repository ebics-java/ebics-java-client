EBICS Java Client
=====

Fork of the Java open source EBICS client project - https://github.com/uwemaurer/ebics-java-client/

How to get started:

https://github.com/honza-toegel/ebics-java-client/wiki/EBICS-Client-HowTo

Main differences with this fork:

- Support of EBICS Versions 
  - 2.4 (H003) 
  - 2.5 (H004)
  - 3.0 (H005 - in testing)
- Support of bcprov-jdk15on

Issues: 
* Fat jar build seems to be not possible with standard maven plugins due to bouncy castle signed jar.