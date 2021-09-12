package org.ebics.client.xml.h000;

import org.ebics.client.model.EbicsVersion;
import org.ebics.schema.h000.HEVResponseDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This conversion between xml bean generated enums & kotlin enums must be made unfortunatelly in java because of maven compilation bug
 * ex bug: Cannot access class 'org.ebics.schema.h005.ContainerStringType.Enum'. Check your module classpath for missing or conflicting dependencies
 */
public class EnumUtil {
    @NotNull
    public static List<EbicsVersion> toEbicsVersions(@NotNull HEVResponseDataType response) {
        HEVResponseDataType.VersionNumber [] versions = response.getVersionNumberArray();
        if (versions == null) return Collections.emptyList();
        return Arrays.stream(versions).map(versionNumber -> EbicsVersion.valueOf(versionNumber.getProtocolVersion())).collect(Collectors.toList());
    }
}
