export interface Bank {
  id: number;
  bankURL: string;
  name: string;
  hostId: string;
  keyStore: {
    id: number;
    e002DigestHex: string;
    x002DigestHex: string;
  }
  ebicsVersions: EbicsVersion[];
}

export enum EbicsVersion {
  H002 = 'H002',
  H003 = 'H003',
  H004 = 'H004',
  H005 = 'H005',
  H006 = 'H006',
}

export interface EbicsVersionSettings {
  version: EbicsVersion;
  isSupportedByBank: boolean;
  isSupportedByClient: boolean;
  isAllowed: boolean;
  isDefault: boolean;
}

export interface UserPartnerBank {
  ebicsVersion: EbicsVersion;
  userId: string;
  name: string;
  dn: string;
  partnerId: string;
  bankId: number;
  guestAccess: boolean;
  useCertificate: boolean;
}

export interface EbicsApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  description: string;
}

export interface User {
  id: number;
  ebicsVersion: EbicsVersion;
  userId: string;
  name: string;
  dn: string;
  userStatus: string;
  partner: Partner;
  keyStore: string;
  usePassword: boolean;
  guestAccess: boolean;
  creator: string;
  securityMedium: string;
  useCertificate: boolean;
}

export interface UserContext {
  name: string;
  roles: Array<string>;
  time: string;
  appVersion: string;
  appBuildTimestamp: string;
}

export interface Partner {
  id: number;
  partnerId: string;
  bank: Bank;
}

export interface BtfInt {
  serviceName: string;
  serviceOption?: string;
  scope?: string;
  containerType?: string;
  message: BtfMessageInt;
}

export interface BtfMessageInt {
  messageName: string;
  messageNameVariant?: string;
  messageNameVersion?: string;
  messageNameFormat?: string;
}

export class Btf implements BtfInt {
  constructor(
    serviceName: string,
    serviceOption: string | undefined = undefined,
    scope: string | undefined = undefined,
    containerType: string | undefined = undefined,
    message: BtfMessage
  ) {
    this.serviceName = serviceName;
    this.serviceOption = serviceOption;
    this.scope = scope;
    this.containerType = containerType;
    this.message = message;
  }
  serviceName: string;
  serviceOption?: string;
  scope?: string;
  containerType?: string;
  message: BtfMessage;
  label(): string {
    return `${this.serviceName}|${s(this.serviceOption)}|${s(this.scope)}|${
      s(this.containerType)
    }|${this.message.label()}`;
  }
}

function s(txt: string | undefined): string {
  return txt ? txt : '-'
}

export class BtfMessage implements BtfMessageInt {
  constructor(
    messageName: string,
    messageNameVariant: string | undefined = undefined,
    messageNameVersion: string | undefined = undefined,
    messageNameFormat: string | undefined = undefined,
  ) {
    this.messageName = messageName;
    this.messageNameFormat = messageNameFormat;
    this.messageNameVariant = messageNameVariant;
    this.messageNameVersion = messageNameVersion;
  }
  messageName: string;
  messageNameVariant?: string;
  messageNameVersion?: string;
  messageNameFormat?: string;
  label(): string {
    return `${this.messageName}.${s(this.messageNameVariant)}.${s(this.messageNameVersion)}.${s(this.messageNameFormat)}`;
  }
}

export enum AdminOrderType {
  INI = 'INI',
  HIA = 'HIA',
  HPB = 'HPB',
  SPR = 'SPR',
}

export interface UserPassword {
  password: string;
}

export interface CertRequest extends UserPassword {
  dn: string;
  usePassword: boolean;
  password: string;
}

export interface UploadRequest {
  password: string;
  params: Map<string, string>;
}

export interface UploadResponse {
  orderNumber: string;
  transactionId: string;
}

export interface UploadRequestH004 extends UploadRequest {
  password: string;
  orderType: string;
  attributeType: string;
  params: Map<string, string>;
}

export interface UploadRequestH005 extends UploadRequest {
  password: string;
  orderService: Btf;
  signatureFlag: boolean;
  edsFlag: boolean;
  fileName: string;
  params: Map<string, string>;
}

export interface DownloadRequest {
  password: string;
  adminOrderType: string;
  params: Map<string, string>;
  startDate?: Date;
  endDate?: Date;
}

export interface DownloadRequestH004 extends DownloadRequest {
  password: string;
  adminOrderType: string;
  orderType?: string;
  params: Map<string, string>;
  startDate?: Date;
  endDate?: Date;
}

export interface DownloadRequestH005 extends DownloadRequest {
  password: string;
  adminOrderType: string;
  orderService?: Btf;
  params: Map<string, string>;
  startDate?: Date;
  endDate?: Date;
}


interface Letter {
  letterText: string;
  hash: string;
}

export interface UserLettersResponse {
  signature: Letter;
  encryption: Letter;
  authentication: Letter;
}

export enum UserIniWizzStep {
  CreateUserKeys = 1,
  UploadUserKeys,
  PrintUserLetters,
  DownloadBankKeys,
  VerifyBankKeys,
  Finish,
  Unknown,
}

export enum AuthenticationType {
  SSO = 'Single sign on',
  HTTP_BASIC = 'HTTP Basic (username + password)',
}

export enum FileFormat {
  XML = 'xml',
  SWIFT = 'swift',
  TEXT = 'text',
  BINARY = 'binary',
}

export interface AutoAdjustmentsPain00x {
  msgId: boolean;
  pmtInfId: boolean;
  instrId: boolean;
  endToEndId: boolean;
  uetr: boolean;
  reqdExctnDt: boolean;
  creDtTm: boolean;
  nbOfTrxsCalc: boolean;
  ctrlSumCalc: boolean;
  idPrefix: string;
}

export interface AutoAdjustmentsSwift {
  uetr: boolean;
  f20: boolean;
  f21: boolean;
  f30: boolean;
  idPrefix: string;
  randomIds: boolean;
}

export interface UserSettings {
  uploadOnDrop: boolean;
  testerSettings: boolean;
  adjustmentOptions: {
    applyAutomatically: boolean;
    pain00x: AutoAdjustmentsPain00x;
    swift: AutoAdjustmentsSwift;
  },
}

export enum AuthorisationLevel {
  T, A, B, E,
}

/**
 * H005 order type description
 */
export interface BTFType {
  adminOrderType: string;
  service?: BtfInt;
  description?: string;
  authorizationLevel?: AuthorisationLevel;
  numSigRequired?: BigInteger;
}

/**
 * H003, H004 order type description
 */
export interface OrderType {
  adminOrderType: string;
  orderType: string;
  transferType?: TransferType;
  description?: string;
  authorizationLevel?: AuthorisationLevel;
  numSigRequired?: BigInteger;
}

/**
 * H003, H004, H005 order type descriptions
 */
export interface OrderTypeList {
  btfTypes: BTFType[];
  orderTypes: OrderType[];
}

export enum TransferType {
  Upload, Download,
}

export enum OrderTypeFilter {
  UploadOnly,
  DownloadOnly,
  All,
}