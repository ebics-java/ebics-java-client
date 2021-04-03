package org.ebics.client.order;

import java.util.Date;
import java.util.Map;

public abstract class AbstractEbicsDownloadOrder extends EbicsOrder {

    /**
     * Download order
     * @param adminOrderType the EBICS admin order type
     * @param startDate start date (for historical downloads only)
     * @param endDate end date (for historical downloads only)
     */
    public AbstractEbicsDownloadOrder(EbicsAdminOrderType adminOrderType, Date startDate, Date endDate, Map<String, String> params) {
        super(adminOrderType, params);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    private Date startDate;
    private Date endDate;
}
