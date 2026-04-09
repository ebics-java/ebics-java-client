/*
public record EbicsParams(String orderId, OrderParams orderParams) {

    public record OrderParams(String serviceName, String scope, String option, String messageName,
                              String messageVersion, boolean signatureFlag) {
    }
}
*/

package org.kopi.ebics.client;
import java.util.Objects;

public class EbicsParams {

    private final String orderId;
    private final OrderParams orderParams;

    public EbicsParams(String orderId, OrderParams orderParams) {
        this.orderId = orderId;
        this.orderParams = orderParams;
    }

    public String getOrderId() {
        return orderId;
    }

    public OrderParams getOrderParams() {
        return orderParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EbicsParams)) return false;
        EbicsParams that = (EbicsParams) o;
        return Objects.equals(orderId, that.orderId) &&
               Objects.equals(orderParams, that.orderParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, orderParams);
    }

    @Override
    public String toString() {
        return "EbicsParams{" +
                "orderId='" + orderId + '\'' +
                ", orderParams=" + orderParams +
                '}';
    }

    public static class OrderParams {

        private final String serviceName;
        private final String scope;
        private final String option;
        private final String messageName;
        private final String messageVersion;
        private final boolean signatureFlag;

        public OrderParams(String serviceName, String scope, String option,
                           String messageName, String messageVersion, boolean signatureFlag) {
            this.serviceName = serviceName;
            this.scope = scope;
            this.option = option;
            this.messageName = messageName;
            this.messageVersion = messageVersion;
            this.signatureFlag = signatureFlag;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getScope() {
            return scope;
        }

        public String getOption() {
            return option;
        }

        public String getMessageName() {
            return messageName;
        }

        public String getMessageVersion() {
            return messageVersion;
        }

        public boolean isSignatureFlag() {
            return signatureFlag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OrderParams)) return false;
            OrderParams that = (OrderParams) o;
            return signatureFlag == that.signatureFlag &&
                   Objects.equals(serviceName, that.serviceName) &&
                   Objects.equals(scope, that.scope) &&
                   Objects.equals(option, that.option) &&
                   Objects.equals(messageName, that.messageName) &&
                   Objects.equals(messageVersion, that.messageVersion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceName, scope, option, messageName, messageVersion, signatureFlag);
        }

        @Override
        public String toString() {
            return "OrderParams{" +
                    "serviceName='" + serviceName + '\'' +
                    ", scope='" + scope + '\'' +
                    ", option='" + option + '\'' +
                    ", messageName='" + messageName + '\'' +
                    ", messageVersion='" + messageVersion + '\'' +
                    ", signatureFlag=" + signatureFlag +
                    '}';
        }
    }
}
