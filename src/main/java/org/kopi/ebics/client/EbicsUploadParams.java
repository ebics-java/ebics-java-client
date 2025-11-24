package org.kopi.ebics.client;

public record EbicsUploadParams(String orderId, OrderParams orderParams) {

    public record OrderParams(String serviceName, String scope, String option, String messageName,
                              String messageVersion, boolean signatureFlag) {
    }
}
