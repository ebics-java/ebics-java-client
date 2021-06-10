export interface Bank {
  id: number;
  bankURL: string;
  name: string;
  hostId: string;
}

export interface User {
  id: number;
  ebicsVersion: string;
  userId: string;
  name: string;
  dn: string;
  userStatus: string;
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
