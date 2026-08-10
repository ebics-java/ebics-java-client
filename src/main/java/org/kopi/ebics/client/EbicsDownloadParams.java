package org.kopi.ebics.client;

import java.time.LocalDate;

/**
 * Service parameters for an EBICS 3.0 (H005) BTD download order.
 *
 * <p>With a {@code serviceName} set, the request is sent as {@code AdminOrderType=BTD} with a
 * {@code BTDOrderParams/Service} block. With {@code serviceName} left {@code null}, only the
 * optional date range is applied and the legacy EBICS 2.x order type is kept, so existing
 * callers keep their behaviour.
 *
 * <p>The report period is a pair of calendar days ({@link LocalDate}), not instants: EBICS sends
 * it as {@code xs:date}, and a timezone in that position only creates off-by-one-day bugs.
 *
 * <p>The constructor rejects a partial or reversed range. Both would otherwise travel silently:
 * a half range is dropped when the request is built, and a reversed one is schema-valid and comes
 * back as "no data available", which is indistinguishable from a period that really was empty.
 * This is the single place every caller passes through, so the check lives here rather than in
 * each caller.
 */
public record EbicsDownloadParams(
    String serviceName,
    String scope,
    String option,
    String messageName,
    String messageVersion,
    String containerType,
    LocalDate startDate,
    LocalDate endDate) {

    public EbicsDownloadParams {
        if ((startDate == null) != (endDate == null)) {
            throw new IllegalArgumentException(
                "startDate and endDate must be given together (--start/--end); a single one"
                    + " would be dropped from the bank request");
        }
        if (startDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                "endDate must not be before startDate, got " + startDate + " to " + endDate);
        }
    }

    /** Date-range-only parameters for the legacy (non-BTD) download path. */
    public static EbicsDownloadParams dateRangeOnly(LocalDate startDate, LocalDate endDate) {
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
