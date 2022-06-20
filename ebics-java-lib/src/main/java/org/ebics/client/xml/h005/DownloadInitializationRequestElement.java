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

package org.ebics.client.xml.h005;

import org.ebics.client.api.EbicsSession;
import org.ebics.client.exception.EbicsException;
import org.ebics.client.order.EbicsAdminOrderType;
import org.ebics.client.order.h005.EbicsDownloadOrder;
import org.ebics.schema.h005.*;
import org.ebics.schema.h005.EbicsRequestDocument.EbicsRequest;
import org.ebics.schema.h005.EbicsRequestDocument.EbicsRequest.Body;
import org.ebics.schema.h005.EbicsRequestDocument.EbicsRequest.Header;
import org.ebics.schema.h005.ParameterDocument.Parameter;
import org.ebics.schema.h005.ParameterDocument.Parameter.Value;
import org.ebics.schema.h005.StaticHeaderOrderDetailsType.AdminOrderType;
import org.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests;
import org.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests.Authentication;
import org.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests.Encryption;
import org.ebics.schema.h005.StaticHeaderType.Product;

import java.util.Calendar;


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
     * @param session    the current ebics session
     * @param ebicsOrder the detail of download order
     * @throws EbicsException
     */
    public DownloadInitializationRequestElement(EbicsSession session,
                                                EbicsDownloadOrder ebicsOrder)
            throws EbicsException {
        super(session, ebicsOrder);
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
        AdminOrderType adminOrderType;
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
        adminOrderType = EbicsXmlFactory.createAdminOrderType(ebicsOrder.getAdminOrderType().toString());
        if (ebicsOrder.getAdminOrderType().equals(EbicsAdminOrderType.BTD)) {
            BTDParamsType btdParamsType = EbicsXmlFactory.createBTDParamsType((EbicsDownloadOrder)ebicsOrder);

            if (Boolean.getBoolean(session.getSessionParam("TEST"))) {
                Parameter parameter;
                Value value;

                value = EbicsXmlFactory.createValue("String", "TRUE");
                parameter = EbicsXmlFactory.createParameter("TEST", value);
                btdParamsType.setParameterArray(new Parameter[]{parameter});
            }
            orderDetails = EbicsXmlFactory.createStaticHeaderOrderDetailsType(adminOrderType, btdParamsType);
        } else {
            StandardOrderParamsType standardOrderParamsType = EbicsXmlFactory.createStandardOrderParamsType();
            orderDetails = EbicsXmlFactory.createStaticHeaderOrderDetailsType(null,
                    adminOrderType,
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

    private static final long serialVersionUID = 3776072549761880272L;
}
