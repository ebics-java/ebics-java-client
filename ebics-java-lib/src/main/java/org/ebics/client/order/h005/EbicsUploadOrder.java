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
     * @param requestEDSFlag the EDS flag (only when signatureFlag=true, when VEU=EDS is required (de/ch) or not (fr))
     * @param fileName the optional filename of uploaded file
     */
    public EbicsUploadOrder(EbicsService ebicsService, boolean signatureFlag, boolean requestEDSFlag, String fileName, Map<String, String> params) {
        super(EbicsAdminOrderType.BTU, signatureFlag, params);
        this.ebicsService = ebicsService;
        this.fileName = fileName;
        this.requestEDSFlag = requestEDSFlag;
    }

    public String getFileName() {
        return fileName;
    }
    public EbicsService getOrderService() {
        return ebicsService;
    }
    public boolean isRequestEDSFlag() { return requestEDSFlag; }

    private EbicsService ebicsService;
    private String fileName;
    private boolean requestEDSFlag;
}
