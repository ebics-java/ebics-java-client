#EBICS Web Client

Core EBICS API is fork of the Java open source EBICS client project - https://github.com/uwemaurer/ebics-java-client/
Thanks a lot for all contribution.

###Demo application
Check the following demo to get idea about functionality, here is the latest version of app deployed.

https://ebics-web-client.herokuapp.com/

###Wiki pages
- [Installation manual](https://github.com/honza-toegel/ebics-java-client/wiki/Installation-Manual)
- [Getting started](https://github.com/honza-toegel/ebics-java-client/wiki/Getting-Started)

Main differences with this fork:

- Web UI (SPA based on vue3), instead of console client
- REST API exposing EBICS operations
- Support of following EBICS versions 
  - EBICS 2.5 (H004)
  - EBICS 3.0 (H005)
- Added API for HTD & HEV
- Support of bcprov-jdk15on
- Some core API refactored to kotlin in order to increase readability, encapsulation and immutability and consistence

Ideas for roadmap:

- Display hash of X509 Bank Certificates
- Better unit test coverage
- More business friendly Web UI for users which doesn't know anything about EBICS (like classical eBanking UI)
- Implementing UserDetailService to maintain user data  
- Download file indexing in order to find relevant data (like payment status by id, account statement by transaction,..) 
- Replace xmlbeans with JAXB