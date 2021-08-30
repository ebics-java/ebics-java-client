export interface Bank {
  id: number;
  bankURL: string;
  name: string;
  hostId: string;
}

export interface UserPartnerBank {
  ebicsVersion: string;
  userId: string;
  name: string;
  dn: string;
  partnerId: string;
  bankId: number;
  useCertificate: boolean;
  usePassword: boolean;
}

export interface UserWizz {
  id: number;
  ebicsVersion: string;
  dn: string;
  userStatus: string;
  usePassword: boolean;
}

export interface EbicsApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  description: string;
}

export interface User extends UserWizz {
  id: number;
  ebicsVersion: string;
  userId: string;
  name: string;
  dn: string;
  userStatus: string;
  partner: Partner;
  keyStore: string;
  useCertificate: boolean;
  usePassword: boolean;
  securityMedium: string;
}

export interface UserContext {
  name: string;
  roles: Array<string>;
  time: string;
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

export interface UploadRequest {
  password: string;
  params: Map<string, string>;
}

export interface UploadResponse {
  orderNumber: string;
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
  XML,
  SWIFT,
  TEXT,
  BINARY,
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
}

export interface UserSettings {
  uploadOnDrop: boolean;
  testerSettings: boolean;
  adjustmentOptions: {
    applyAuthomatically: boolean;
    pain001: AutoAdjustmentsPain00x;
    mt101: AutoAdjustmentsSwift;
  },
}