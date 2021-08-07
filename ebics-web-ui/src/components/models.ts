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

export interface DeleteConfirmDialog {
  show: boolean;
  id: number;
  name: string;
}

export interface BtfInt {
  serviceName: string;
  serviceOption: string;
  scope: string;
  containerType: string;
  message: BtfMessageInt;
}

export interface BtfMessageInt {
  messageName: string;
  messageNameVariant: string;
  messageNameVersion: string;
  messageNameFormat: string;  
}

export class Btf implements BtfInt {
  constructor(serviceName: string = '' as string, serviceOption: string = '' as string,scope: string = '' as string,containerType: string = '' as string,message: BtfMessage = new BtfMessage()) {
    this.serviceName = serviceName;
    this.serviceOption = serviceOption;
    this.scope = scope;
    this.containerType = containerType;
    this.message = message;
  }
  serviceName: string;
  serviceOption: string;
  scope: string;
  containerType: string;
  message: BtfMessage;
  label(): string {
    return `${this.serviceName}|${this.serviceOption}|${this.scope}|${this.containerType}|${this.message.label()}`;
  }
}

export class BtfMessage implements BtfMessageInt {
  constructor(messageName: string = '' as string, messageNameVariant: string = '' as string,messageNameVersion: string = '' as string, messageNameFormat: string = '' as string) {
    this.messageName = messageName;
    this.messageNameFormat = messageNameFormat;
    this.messageNameVariant = messageNameVariant;
    this.messageNameVersion = messageNameVersion;
  }
  messageName: string;
  messageNameVariant: string;
  messageNameVersion: string;
  messageNameFormat: string;
  label(): string {
    return `${this.messageName}.${this.messageNameVariant}.${this.messageNameVersion}.${this.messageNameFormat}`;
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

export enum UserIniWizzStep {
  CreateUserKeys = 1,
  UploadUserKeys,
  PrintUserLetters,
  DownloadBankKeys,
  VerifyBankKeys,
  Finish,
  Unknown,
}