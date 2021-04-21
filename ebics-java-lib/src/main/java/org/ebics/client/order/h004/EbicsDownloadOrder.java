package org.ebics.client.order.h004;

import org.ebics.client.order.AbstractEbicsDownloadOrder;
import org.ebics.client.order.EbicsAdminOrderType;

import java.util.Date;
import java.util.Map;

public class EbicsDownloadOrder extends AbstractEbicsDownloadOrder {

    /**
     * H003, H004 DE Download orders
     * @param orderType the EBICS order type
     * @param startDate start date (for historical downloads only)
     * @param endDate end date (for historical downloads only)
     */
    public EbicsDownloadOrder(String orderType, Date startDate, Date endDate, Map<String, String> params) {
        super(EbicsAdminOrderType.DNL, startDate, endDate, params);
        this.ebicsOrderType = orderType;
    }

    /**
     * H003, H004 FR Download orders
     * @param startDate start date (for historical downloads only)
     * @param endDate end date (for historical downloads only)
     */
    public EbicsDownloadOrder(Date startDate, Date endDate, Map<String, String> params) {
        super(EbicsAdminOrderType.FDL, startDate, endDate, params);
    }

    public String getEbicsOrderType() {
        return ebicsOrderType;
    }

    private String ebicsOrderType;
}
