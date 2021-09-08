package org.ebics.client.xml.h004;

import org.ebics.client.order.AuthorisationLevel;
import org.ebics.client.order.EbicsAdminOrderType;
import org.ebics.client.order.h004.TransferType;
import org.ebics.schema.h004.AuthOrderInfoType;
import org.ebics.schema.h004.UserPermissionType;
/**
 * This conversion between xml bean generated enums & kotlin enums must be made unfortunatelly in java because of maven compilation bug
 * ex bug: Cannot access class 'org.ebics.schema.h005.ContainerStringType.Enum'. Check your module classpath for missing or conflicting dependencies
 */
public class EnumUtil {
    public static AuthorisationLevel toAuthLevel(UserPermissionType alt) {
        if (alt == null || alt.getAuthorisationLevel() == null) return null;
        else return AuthorisationLevel.valueOf(alt.getAuthorisationLevel().toString());
    }

    public static TransferType toTransferType(AuthOrderInfoType oi) {
        if (oi == null || oi.getTransferType() == null) return null;
        else return TransferType.valueOf(oi.getTransferType().toString());
    }

    public static EbicsAdminOrderType recognizeAdminOrderType(AuthOrderInfoType orderInfo) {
        try {
            if (orderInfo == null || orderInfo.getOrderType() == null) return null;
            return EbicsAdminOrderType.valueOf(orderInfo.getOrderType());
        } catch (Exception ex) {
            if (orderInfo.getTransferType() == org.ebics.schema.h004.TransferType.UPLOAD)
                return EbicsAdminOrderType.UPL;
            else if (orderInfo.getTransferType() == org.ebics.schema.h004.TransferType.DOWNLOAD)
                return EbicsAdminOrderType.DNL;
            else
                return null;
        }
    }
}
