package org.ebics.client.order;

import java.util.Date;
import java.util.Map;

public class EbicsDownloadOrder extends EbicsOrder {

    /**
     * H003, H004 DE Download orders
     * @param orderType the EBICS order type
     * @param startDate start date (for historical downloads only)
     * @param endDate end date (for historical downloads only)
     */
    public EbicsDownloadOrder(String orderType, Date startDate, Date endDate, Map<String, String> params) {
        super(EbicsAdminOrderType.DNL, params);
        this.ebicsOrderType = orderType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * H003, H004 FR Download orders
     * @param startDate start date (for historical downloads only)
     * @param endDate end date (for historical downloads only)
     */
    public EbicsDownloadOrder(Date startDate, Date endDate, Map<String, String> params) {
        super(EbicsAdminOrderType.FDL, params);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * H005 Download order
     * @param ebicsService the ECBIS service for BTU
     * @param startDate start date (for historical downloads only)
     * @param endDate end date (for historical downloads only)
     */
    public EbicsDownloadOrder(EbicsService ebicsService, Date startDate, Date endDate, Map<String, String> params) {
        super(EbicsAdminOrderType.BTD, params);
        this.ebicsService = ebicsService;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public EbicsService getOrderService() {
        return ebicsService;
    }

    public String getEbicsOrderType() {
        return ebicsOrderType;
    }

    private String ebicsOrderType;
    private EbicsService ebicsService;
    private Date startDate;
    private Date endDate;
}
