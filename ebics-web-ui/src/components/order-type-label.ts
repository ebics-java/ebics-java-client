import {
  BTFType,
  OrderType,
  BtfService,
} from 'components/models';

export default function useOrderTypeLabelAPI(
) {
  /**
   * @returns @param input if its not empty, otherwise returns @param delimiter
   */
  function s(input: string | undefined, delimiter = '-'): string {
    if (input) return input;
    else return delimiter;
  }

  /**
   * @returns string representation of @param orderType
   */
  const orderTypeLabel = (orderType: OrderType | undefined): string => {
    if (orderType) {
      return orderType.orderType;
    } else return '';
  };

  /**
   * @returns string representation of @param btf
   */
  const btfTypeLabel = (btf: BTFType | undefined): string => {
    if (btf) {
      const bts = btf.service;
      const btm = bts?.message;
      if (bts && btm) {
        return btfServiceLabel(bts);
      } else {
        return btf.adminOrderType;
      }
    } else return '';
  };

  /**
   * @returns string representation of @param bts
   */
  const btfServiceLabel = (bts: BtfService): string => {  
    const btm = bts.message;    
    return `${s(bts.serviceName)}|${s(bts.serviceOption)}|${s(
      bts.scope
    )}|${s(bts.containerType)}|${s(btm.messageName)}|${s(
      btm.messageNameVariant
    )}|${s(btm.messageNameVersion)}|${s(btm.messageNameFormat)}`;
  };

  return {
    btfTypeLabel,
    orderTypeLabel,
    btfServiceLabel,
  };
}
