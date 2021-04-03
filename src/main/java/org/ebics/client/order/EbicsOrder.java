package org.ebics.client.order;

import java.util.HashMap;
import java.util.Map;

public class EbicsOrder {
    public EbicsOrder(EbicsAdminOrderType adminOrderType) {
        this.adminOrderType = adminOrderType;
        this.params = new HashMap<>(0);
    }

    public EbicsOrder(EbicsAdminOrderType adminOrderType, Map params) {
        this.adminOrderType = adminOrderType;
        this.params = params;
    }

    public EbicsAdminOrderType getAdminOrderType() {
        return adminOrderType;
    }
    public Map<String, String> getParams() { return params; }

    private EbicsAdminOrderType adminOrderType;
    private Map<String, String> params;
}
