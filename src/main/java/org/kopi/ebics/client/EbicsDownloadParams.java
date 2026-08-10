package org.kopi.ebics.client;

import java.util.Date;

/**
 * Service parameters for an EBICS 3.0 (H005) BTD download order.
 *
 * <p>With a {@code serviceName} set, the request is sent as {@code AdminOrderType=BTD} with a
 * {@code BTDOrderParams/Service} block. With {@code serviceName} left {@code null}, only the
 * optional date range is applied and the legacy EBICS 2.x order type is kept, so existing
 * callers keep their behaviour.
 */
public record EbicsDownloadParams(
    String serviceName,
    String scope,
    String option,
    String messageName,
    String messageVersion,
    String containerType,
    Date startDate,
    Date endDate) {

    /** Date-range-only parameters for the legacy (non-BTD) download path. */
    public static EbicsDownloadParams dateRangeOnly(Date startDate, Date endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }
        return new EbicsDownloadParams(null, null, null, null, null, null, startDate, endDate);
    }

    /** Whether these parameters describe an EBICS 3.0 BTD business transaction format order. */
    public boolean isBtd() {
        return serviceName != null;
    }
}
