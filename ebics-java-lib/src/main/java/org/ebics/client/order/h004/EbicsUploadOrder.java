package org.ebics.client.order.h004;

import org.ebics.client.order.AbstractEbicsUploadOrder;
import org.ebics.client.order.EbicsAdminOrderType;

import java.util.Map;

public class EbicsUploadOrder extends AbstractEbicsUploadOrder {

    /**
     * Initialize DE H003, H004 upload order
     * @param orderType the order type
     * @param signatureFlag the signature flag (true = OZHNN, false = DZHNN)
     */
    public EbicsUploadOrder(String orderType, boolean signatureFlag, Map<String, String> params) {
        super(EbicsAdminOrderType.UPL, signatureFlag, params);
        this.orderType = orderType;
    }

    /**
     * Initialize FR H003, H004 upload order
     * @param signatureFlag the signature flag (true = OZHNN, false = DZHNN)
     */
    public EbicsUploadOrder(boolean signatureFlag, Map<String, String> params) {
        super(EbicsAdminOrderType.FUL, signatureFlag, params);
    }

    public String getOrderType() {
        return orderType;
    }

    private String orderType;
}
