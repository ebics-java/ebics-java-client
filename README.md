EBICS Java Client
=====

Fork of the Java open source EBICS client project - https://github.com/uwemaurer/ebics-java-client/

How to get started:

https://github.com/honza-toegel/ebics-java-client/wiki/EBICS-Client-HowTo

Main differences with this fork:

- Support of following versions 
  - EBICS 2.4 (H003) 
  - EBICS 2.5 (H004)
  - EBICS 3.0 (H005 - in testing)
- Separated EBICS API from console client
- Support of bcprov-jdk15on
Working branch:
- Decoupled EBICS API from console client 
- Added EBICS REST client (spring boot & hibernate persistence for EBICS informations)
- Some core API refactored to kotlin in order to increase readablity, encapsulation and immutability and consistence

Issues: 
* Fat jar build seems to be not possible with standard maven plugins due to bouncy castle signed jar.
