EBICS Web Client
=====

Core API is fork of the Java open source EBICS client project - https://github.com/uwemaurer/ebics-java-client/
Thanks a lot for all previous contribution.

How to get started:

https://github.com/honza-toegel/ebics-java-client/wiki/EBICS-Client-HowTo

Main differences with this fork:

- Web UI (SPA based on vue3), instead of console client
- REST API exposing EBICS operations
- Support of following EBICS versions 
  - EBICS 2.4 (H003) 
  - EBICS 2.5 (H004)
  - EBICS 3.0 (H005)
- Added API for HTD & HEV
- Support of bcprov-jdk15on
- Some core API refactored to kotlin in order to increase readability, encapsulation and immutability and consistence

Ideas for roadmap:

- More business friendly Web UI for user which doesn't know EBICS
- Stronger authentication than HTTP-BASIC  
- Console client
- To replace xmlbeans with JAXB