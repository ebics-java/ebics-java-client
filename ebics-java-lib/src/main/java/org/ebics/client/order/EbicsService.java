package org.ebics.client.order;

public class EbicsService {

    public EbicsService(String serviceName, String serviceOption, String scope, String containerType, String messageName, String messageNameVariant, String messageNameVersion, String messageNameFormat) {
        this.serviceName = serviceName;
        this.serviceOption = serviceOption;
        this.scope = scope;
        this.containerType = containerType;
        this.messageName = messageName;
        this.messageNameVariant = messageNameVariant;
        this.messageNameVersion = messageNameVersion;
        this.messageNameFormat = messageNameFormat;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceOption() {
        return serviceOption;
    }

    public String getScope() {
        return scope;
    }

    public String getContainerType() {
        return containerType;
    }

    public String getMessageName() {
        return messageName;
    }

    public String getMessageNameVariant() {
        return messageNameVariant;
    }


    public String getMessageNameVersion() {
        return messageNameVersion;
    }


    public String getMessageNameFormat() {
        return messageNameFormat;
    }


    private final String serviceName;
    private final String serviceOption;
    private final String scope;
    private final String containerType;
    private final String messageName;
    private final String messageNameVariant;
    private final String messageNameVersion;
    private final String messageNameFormat;
}
