package org.ebics.client.xml.h005;

import org.ebics.client.order.AuthorisationLevel;
import org.ebics.client.order.h005.ContainerType;
import org.ebics.schema.h005.ContainerFlagType;
import org.ebics.schema.h005.UserPermissionType;

/**
 * This conversion between xml bean generated enums & kotlin enums must be made unfortunatelly in java because of maven compilation bug
 * ex bug: Cannot access class 'org.ebics.schema.h005.ContainerStringType.Enum'. Check your module classpath for missing or conflicting dependencies
 */
public class EnumUtil {
    public static AuthorisationLevel toAuthLevel(UserPermissionType alt) {
        if (alt == null || alt.getAuthorisationLevel() == null) return null;
        else return AuthorisationLevel.valueOf(alt.getAuthorisationLevel().toString());
    }

    public static ContainerType toContainerType(ContainerFlagType ctf) {
        if (ctf == null || ctf.getContainerType() == null) return null;
        else return ContainerType.valueOf(ctf.getContainerType().toString());
    }
}
