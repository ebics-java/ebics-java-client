package org.ebics.client.order;

import java.util.Map;

public class EbicsUploadOrder extends EbicsOrder {

    /**
     * Initialize DE H003, H004 upload order
     * @param orderType the order type
     * @param signatureFlag the signature flag (true = OZHNN, false = DZHNN)
     */
    public EbicsUploadOrder(String orderType, boolean signatureFlag, Map<String, String> params) {
        super(EbicsAdminOrderType.UPL, params);
        this.orderType = orderType;
        this.signatureFlag = signatureFlag;
    }

    /**
     * Initialize FR H003, H004 upload order
     * @param signatureFlag the signature flag (true = OZHNN, false = DZHNN)
     */
    public EbicsUploadOrder(boolean signatureFlag, Map<String, String> params) {
        super(EbicsAdminOrderType.FUL, params);
        this.signatureFlag = signatureFlag;
    }

    /**
     * Initialize H005 upload order
     * @param ebicsService the ECBIS service for BTU
     * @param signatureFlag the ES flag (whether the signature is provided)
     * @param fileName the optional filename of uploaded file
     */
    public EbicsUploadOrder(EbicsService ebicsService, boolean signatureFlag, String fileName, Map<String, String> params) {
        super(EbicsAdminOrderType.BTU, params);
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
    public String getOrderType() {
        return orderType;
    }

    private String orderType;
    private EbicsService ebicsService;
    private boolean signatureFlag;
    private String fileName;
}
