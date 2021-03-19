package org.ebics.client.order;

import java.util.Map;

public class EbicsUploadOrder extends EbicsOrder {

    /**
     * Initialize H003, H004 upload order
     * @param orderType the order type
     * @param signatureFlag the signature flag (true = OZHNN, false = DZHNN)
     */
    public EbicsUploadOrder(EbicsOrderType orderType, boolean signatureFlag, Map<String, String> params) {
        super(orderType, params);
        this.signatureFlag = signatureFlag;
    }

    /**
     * Initialize H005 upload order
     * @param ebicsService the ECBIS service for BTU
     * @param signatureFlag the ES flag (whether the signature is provided)
     * @param fileName the optional filename of uploaded file
     */
    public EbicsUploadOrder(EbicsService ebicsService, boolean signatureFlag, String fileName, Map<String, String> params) {
        super(EbicsOrderType.BTU, params);
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
