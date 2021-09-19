# REST API Configuration

The configuration of WAR file is externalized to have easier control over deployed application in production.
The external configuration define: 
* spring properties using config.properties (or config.yaml)
* logging settings 

##Configuration home directory

The config.properties & logback.xml is expected on path
$EWC_CONFIG_HOME/config.properties (or config.yaml)
$EWC_CONFIG_HOME/logback.xml

## HTTPS Certificate

In order to support HTTPS the appropriate certificate sign by verified cert authority must be configured.
For development purposes only can be used self-signed certificate

### How to create self-signed certificate for localhost (dev only)

Create private & public key:

```openssl req -x509 -new -nodes -days 720 -keyout selfsigned.key -out selfsigned.crt -config openssl.cnf```

Create PKCS12 format of keystore out of it:

```openssl pkcs12 -export -in selfsigned.crt  -inkey selfsigned.key  -out selfsigned.pfx -name ebics-rest-api```
