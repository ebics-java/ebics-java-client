package org.ebics.client.order.h005;

import org.ebics.client.order.EbicsAdminOrderType;
import org.ebics.client.order.AbstractEbicsDownloadOrder;
import org.ebics.client.order.EbicsService;

import java.util.Date;
import java.util.Map;

public class EbicsDownloadOrder extends AbstractEbicsDownloadOrder {

    /**
     * H005 Download order
     * @param ebicsService the ECBIS service for BTU
     * @param startDate start date (for historical downloads only)
     * @param endDate end date (for historical downloads only)
     */
    public EbicsDownloadOrder(EbicsService ebicsService, Date startDate, Date endDate, Map<String, String> params) {
        super(EbicsAdminOrderType.BTD, startDate, endDate, params);
        this.ebicsService = ebicsService;
    }

    public EbicsService getOrderService() {
        return ebicsService;
    }

    private EbicsService ebicsService;
}
