package org.ebics.client.order;

import java.util.HashMap;
import java.util.Map;

public class EbicsOrder {
    public EbicsOrder(EbicsOrderType orderType) {
        this.orderType = orderType;
        this.params = new HashMap<>(0);
    }

    public EbicsOrder(EbicsOrderType orderType, Map params) {
        this.orderType = orderType;
        this.params = params;
    }

    public EbicsOrderType getOrderType() {
        return orderType;
    }
    public Map<String, String> getParams() { return params; }

    private EbicsOrderType orderType;
    private Map<String, String> params;
}
