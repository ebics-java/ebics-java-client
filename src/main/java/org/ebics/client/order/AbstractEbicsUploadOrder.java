package org.ebics.client.order;

import java.util.Map;

public abstract class AbstractEbicsUploadOrder extends EbicsOrder {

    /**
     * Initialize EBICS upload order
     * @param adminOrderType the EBICS admin order type
     * @param signatureFlag the signature flag (true = OZHNN, false = DZHNN)
     * @param params the additional key-value parameters for upload
     */
    public AbstractEbicsUploadOrder(EbicsAdminOrderType adminOrderType, boolean signatureFlag, Map<String, String> params) {
        super(adminOrderType, params);
        this.signatureFlag = signatureFlag;
    }

    public boolean isSignatureFlag() {
        return signatureFlag;
    }

    private boolean signatureFlag;
}
