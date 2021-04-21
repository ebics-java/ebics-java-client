package org.ebics.client.order.h005;

import org.ebics.client.order.EbicsAdminOrderType;
import org.ebics.client.order.EbicsService;
import org.ebics.client.order.AbstractEbicsUploadOrder;

import java.util.Map;

public class EbicsUploadOrder extends AbstractEbicsUploadOrder {

    /**
     * Initialize H005 upload order
     * @param ebicsService the ECBIS service for BTU
     * @param signatureFlag the ES flag (whether the signature is provided)
     * @param fileName the optional filename of uploaded file
     */
    public EbicsUploadOrder(EbicsService ebicsService, boolean signatureFlag, String fileName, Map<String, String> params) {
        super(EbicsAdminOrderType.BTU, signatureFlag, params);
        this.ebicsService = ebicsService;
        this.signatureFlag = signatureFlag;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
    public boolean isSignatureFlag() {
        return signatureFlag;
    }
    public EbicsService getOrderService() {
        return ebicsService;
    }

    private EbicsService ebicsService;
    private boolean signatureFlag;
    private String fileName;
}
