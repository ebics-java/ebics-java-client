/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

package org.ebics.client.xml.h004;

import org.ebics.client.api.EbicsSession;
import org.ebics.client.exception.EbicsException;
import org.ebics.client.order.EbicsAdminOrderType;
import org.ebics.schema.h004.EbicsRequestDocument.EbicsRequest;
import org.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body;
import org.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Header;
import org.ebics.schema.h004.*;
import org.ebics.schema.h004.FDLOrderParamsType.DateRange;
import org.ebics.schema.h004.ParameterDocument.Parameter;
import org.ebics.schema.h004.ParameterDocument.Parameter.Value;
import org.ebics.schema.h004.StaticHeaderOrderDetailsType.OrderType;
import org.ebics.schema.h004.StaticHeaderType.BankPubKeyDigests;
import org.ebics.schema.h004.StaticHeaderType.BankPubKeyDigests.Authentication;
import org.ebics.schema.h004.StaticHeaderType.BankPubKeyDigests.Encryption;
import org.ebics.schema.h004.StaticHeaderType.Product;

import java.util.Calendar;
import java.util.Date;


/**
 * The <code>DInitializationRequestElement</code> is the common initialization
 * for all ebics downloads.
 *
 * @author Hachani
 */
public class DownloadInitializationRequestElement extends InitializationRequestElement {

    /**
     * Constructs a new <code>DInitializationRequestElement</code> for downloads initializations.
     *
     * @param session        the current ebics session
     * @param adminOrderType the download order type (FDL, HTD, HPD)
     * @param startRange     the start range download
     * @param endRange       the end range download
     * @throws EbicsException
     */
    public DownloadInitializationRequestElement(EbicsSession session,
                                                EbicsAdminOrderType adminOrderType, String orderType,
                                                Date startRange,
                                                Date endRange)
            throws EbicsException {
        super(session, adminOrderType);
        this.orderType = orderType;
        this.startRange = startRange;
        this.endRange = endRange;
    }

    @Override
    public void buildInitialization() throws EbicsException {
        EbicsRequest request;
        Header header;
        Body body;
        MutableHeaderType mutable;
        StaticHeaderType xstatic;
        Product product;
        BankPubKeyDigests bankPubKeyDigests;
        Authentication authentication;
        Encryption encryption;
        OrderType orderType;
        StaticHeaderOrderDetailsType orderDetails;

        mutable = EbicsXmlFactory.createMutableHeaderType("Initialisation", null);
        product = EbicsXmlFactory.createProduct(session.getProduct());
        authentication = EbicsXmlFactory.createAuthentication(session.getConfiguration().getAuthenticationVersion(),
                "http://www.w3.org/2001/04/xmlenc#sha256",
                decodeHex(session.getBankCert().getX002Digest()));
        encryption = EbicsXmlFactory.createEncryption(session.getConfiguration().getEncryptionVersion(),
                "http://www.w3.org/2001/04/xmlenc#sha256",
                decodeHex(session.getBankCert().getE002Digest()));
        bankPubKeyDigests = EbicsXmlFactory.createBankPubKeyDigests(authentication, encryption);
        orderType = EbicsXmlFactory.createOrderType(this.orderType != null ? this.orderType : type.toString());
        if (type.equals(EbicsAdminOrderType.FDL)) {
            FDLOrderParamsType fDLOrderParamsType;
            FileFormatType fileFormat;

            fileFormat = EbicsXmlFactory.createFileFormatType(session.getConfiguration().getLocale().getCountry().toUpperCase(),
                    session.getSessionParam("FORMAT"));
            fDLOrderParamsType = EbicsXmlFactory.createFDLOrderParamsType(fileFormat);

            if (startRange != null && endRange != null) {
                DateRange range;
                range = EbicsXmlFactory.createDateRange(startRange, endRange);
                fDLOrderParamsType.setDateRange(range);
            }

            if (Boolean.getBoolean(session.getSessionParam("TEST"))) {
                Parameter parameter;
                Value value;
                value = EbicsXmlFactory.createValue("String", "TRUE");
                parameter = EbicsXmlFactory.createParameter("TEST", value);
                fDLOrderParamsType.setParameterArray(new Parameter[]{parameter});
            }
            orderDetails = EbicsXmlFactory.createStaticHeaderOrderDetailsType(OrderAttributeType.DZHNN,
                    orderType,
                    fDLOrderParamsType);
        } else {
            StandardOrderParamsType standardOrderParamsType;
            standardOrderParamsType = EbicsXmlFactory.createStandardOrderParamsType();
            orderDetails = EbicsXmlFactory.createStaticHeaderOrderDetailsType(OrderAttributeType.DZHNN,
                    orderType,
                    standardOrderParamsType);
        }
        xstatic = EbicsXmlFactory.createStaticHeaderType(session.getBankID(),
                nonce,
                session.getUser().getPartner().getPartnerId(),
                product,
                session.getUser().getSecurityMedium(),
                session.getUser().getUserId(),
                Calendar.getInstance(),
                orderDetails,
                bankPubKeyDigests);
        header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
        body = EbicsXmlFactory.createEbicsRequestBody();
        request = EbicsXmlFactory.createEbicsRequest(header, body);
        document = EbicsXmlFactory.createEbicsRequestDocument(request);
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------
    private final String orderType;
    private Date startRange;
    private Date endRange;
    private static final long serialVersionUID = 3776072549761880272L;
}
