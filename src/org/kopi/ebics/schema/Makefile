##
## Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
##
## This program is free software; you can redistribute it and/or modify
## it under the terms of the GNU General Public License version 2
## as published by the Free Software Foundation.
##
## This program is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
## GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with this program; if not, write to the Free Software
## Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
##
## $Id$
##

TOPDIR =	../../../../..

PACKAGE =	org/kopi/ebics/schema

## ----------------------------------------------------------------------
## TOOLS

## ----------------------------------------------------------------------
## FILES

H000DOCUM  = 	EbicsHEVRequest EbicsHEVResponse

H000TYPES  = 	HEVResponseDataType OrderTBaseType ReturnCodeType 	\
		HostIDType ProtocolVersionType SystemReturnCodeType	\
		HEVRequestDataType ReportTextType VersionNumberType

S001DOCUM  =	EBICSSignatureData OrderSignatureData 			\
		SignaturePubKeyInfo SignaturePubKeyOrderData 		\
		UserSignatureData

S001TYPES  =    OrderSignatureDataType OrderSignatureType PartnerIDType	\
		PubKeyInfoType PubKeyValueType SignaturePubKeyInfoType 	\
		SignaturePubKeyOrderDataType SignatureVersionType 	\
		TimestampType UserIDType UserSignatureDataSigBookType

H003DOCUM  = 	AuthSignature BankSignatureData 			\
		EbicsKeyManagementResponse EbicsNoPubKeyDigestsRequest	\
		EBICSOrderData EbicsRequest EbicsResponse 		\
		EBICSSignatureData EbicsUnsecuredRequest 		\
		EbicsUnsignedRequest FDLOrderParams FULOrderParams	\
		GenericOrderParams HAAResponseOrderData 		\
		HCARequestOrderData HCSRequestOrderData			\
		HIARequestOrderData HKDResponseOrderData 		\
		HPBResponseOrderData HPDResponseOrderData		\
		HSARequestOrderData HTDResponseOrderData HVDOrderParams \
		HVDResponseOrderData HVEOrderParams HVSOrderParams 	\
		HVSRequestOrderData HVTOrderParams HVTResponseOrderData \
		HVUOrderParams HVUResponseOrderData 			\
		HVZResponseOrderData HVZOrderParams OrderParams 	\
		Parameter StandardOrderParams

H003TYPES  = 	AccountDescriptionType AccountHolderRoleType 		\
		AccountHolderType AccountIDType AccountNumberRoleType 	\
		AccountNumberType AccountType AddressInfoType 		\
		AmountType AmountValueType AttributedAccountType 	\
		AuthenticationPubKeyInfoType AuthenticationVersionType	\
		AuthOrderInfoType AuthorisationLevelListType 		\
		AuthorisationLevelType BankCodePrefixType 		\
		BankCodeRoleType BankCodeType BankInfoType		\
		BankSignatureDataSigBookType CountryCodeType 		\
		CryptoVersionType CurrencyBaseType CurrencyCode 	\
		DataDigestType DataEncryptionInfoType 			\
		DataTransferRequestType DataTransferResponseType 	\
		DateType DigestAlgorithmType DigestType			\
		EmptyMutableHeaderType EncryptionPubKeyInfoType 	\
		EncryptionVersionType FDLOrderParamsType FileFormatType \
		FULOrderParamsType GenericOrderParamsType 		\
		HAAResponseOrderDataType HCARequestOrderDataType 	\
		HCSRequestOrderDataType HIARequestOrderDataType		\
		HKDResponseOrderDataType HostIDType 			\
		HPBResponseOrderDataType HPDAccessParamsType 		\
		HPDProtocolParamsType HPDResponseOrderDataType		\
		HPDVersionType HSARequestOrderDataType 			\
		HTDReponseOrderDataType HVDOrderParamsType 		\
		HVDResponseOrderDataType HVEOrderParamsType		\
		HVSOrderParamsType HVSRequestOrderDataType 		\
		HVTAccountInfoType HVTOrderFlagsType HVTOrderInfoType 	\
		HVTOrderParamsType HVTResponseOrderDataType 		\
		HVUOrderDetailsType HVUOrderParamsType 			\
		HVUOriginatorInfoType HVUResponseOrderDataType		\
		HVUSigningInfoType HVZOrderDetailsType 			\
		HVZOrderParamsType HVZResponseOrderDataType 		\
		InstituteIDType KeyMgmntResponseMutableHeaderType 	\
		LanguageType MutableHeaderType NameType 		\
		NationalAccountNumberType NationalBankCodeType 		\
		NonceType NoPubKeyDigestsReqOrderDetailsType 		\
		NoPubKeyDigestsRequestStaticHeaderType 			\
		NumOrderInfosType NumSegmentsType 			\
		OrderAttributeBaseType OrderAttributeType OrderDataType \
		OrderDescriptionType OrderDetailsType OrderFormatType 	\
		OrderIDType OrderTBaseType OrderTListType OrderTType 	\
		PartnerIDType PartnerInfoType 				\
		PreValidationAccountAuthType PreValidationRequestType 	\
		ProductElementType ProductType ProtocolRevisionType 	\
		ProtocolVersionType PubKeyDigestType PubKeyInfoType 	\
		PubKeyValueType ReceiptCodeType ReportTextType 		\
		ResponseMutableHeaderType ResponseStaticHeaderType 	\
		ReturnCodeType SecurityMediumType SegmentNumberType 	\
		SignatureDataType SignatureType SignatureVersionType 	\
		SignerInfoType StandardOrderParamsType 			\
		StaticHeaderBaseType StaticHeaderOrderDetailsType 	\
		StaticHeaderType SymmetricKeyType TimestampType 	\
		TransactionIDType TransactionPhaseType 			\
		TransferReceiptRequestType TransferReceiptResponseType 	\
		TransferType UnsecuredReqOrderDetailsType 		\
		UnsecuredRequestStaticHeaderType 			\
		UnsignedReqOrderDetailsType 				\
		UnsignedRequestStaticHeaderType UserIDType UserInfoType \
		UserPermissionType UserStatusType

DSIGDOCUM  = 	CanonicalizationMethod DigestMethod DigestValue 	\
		DSAKeyValue KeyInfo KeyName KeyValue Manifest MgmtData 	\
		Object PGPData Reference RetrievalMethod RSAKeyValue 	\
		Signature SignatureMethod SignatureProperties 		\
		SignatureProperty SignatureValue SignedInfo SPKIData	\
		Transform Transforms X509Data

DSIGTYPES  = 	CanonicalizationMethodType CryptoBinary 		\
		DigestMethodType DigestValueType DSAKeyValueType	\
		HMACOutputLengthType KeyInfoType KeyValueType 		\
		ManifestType ObjectType PGPDataType ReferenceType	\
		RetrievalMethodType RSAKeyValueType SignatureMethodType \
		SignaturePropertiesType SignaturePropertyType 		\
		SignatureType SignatureValueType SignedInfoType 	\
		SPKIDataType TransformsType TransformType X509DataType 	\
		X509IssuerSerialType

JAVAFILES =	$(H000DOCUM:%=h000/%Document)				\
		$(H000DOCUM:%=h000/impl/%DocumentImpl)			\
		$(H000TYPES:%=h000/%) $(H000TYPES:%=h000/impl/%Impl) 	\
		$(H003DOCUM:%=h003/%Document)				\
		$(H003DOCUM:%=h003/impl/%DocumentImpl)			\
		$(H003TYPES:%=h003/%) $(H003TYPES:%=h003/impl/%Impl) 	\
		$(S001DOCUM:%=s001/%Document)				\
		$(S001DOCUM:%=s001/impl/%DocumentImpl)			\
		$(S001TYPES:%=s001/%) $(S001TYPES:%=s001/impl/%Impl) 	\
		$(DSIGDOCUM:%=xmldsig/%Document)			\
		$(DSIGDOCUM:%=xmldsig/impl/%DocumentImpl)		\
		$(DSIGTYPES:%=xmldsig/%) 				\
		$(DSIGTYPES:%=xmldsig/impl/%Impl)

XSDFILES  =	$(TOPDIR)/schema/ebics_hev				\
		$(TOPDIR)/schema/ebics 					\
		$(TOPDIR)/schema/ebics_keymgmt_request			\
		$(TOPDIR)/schema/ebics_keymgmt_response			\
		$(TOPDIR)/schema/ebics_orders				\
		$(TOPDIR)/schema/ebics_request				\
		$(TOPDIR)/schema/ebics_response				\
		$(TOPDIR)/schema/ebics_signature			\
		$(TOPDIR)/schema/ebics_types				\
		$(TOPDIR)/schema/xmldsig-core-schema			\

XSDCONFIG =	$(TOPDIR)/schema/config

FTOCLEAN  =  	scomp.stamp

DTOCLEAN  =	h000 s001 h003 xmldsig


## ----------------------------------------------------------------------
## GOALS

build:		scomp.stamp

default:	build

clean:		dirsclean filesclean

## ----------------------------------------------------------------------
## GLOBAL DEFINITIONS

include $(TOPDIR)/Make.Defs
