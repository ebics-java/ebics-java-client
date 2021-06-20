export interface Bank {
  id: number;
  bankURL: string;
  name: string;
  hostId: string;
}

export interface UserInfo {
  ebicsVersion: string;
  userId: string;
  name: string;
  dn: string;
  userStatus: string; 
}

export interface User extends UserInfo {
  id: number;
  ebicsVersion: string;
  userId: string;
  name: string;
  dn: string;
  userStatus: string;
  partner: Partner;
  keyStore: string;
  securityMedium: string;
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

export interface Todo {
  id: number;
  content: string;
}

export interface Meta {
  totalCount: number;
}
