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

  function joinToString(labelParts: (string | undefined)[], delimiter: string, undefinedPartReplacement = '') {
    return labelParts.map(label => {
      if (undefinedPartReplacement.length == 0)
        return label
      else
        return s(label, undefinedPartReplacement);
    }).filter(label => label).join(delimiter);
  }

  /**
   * @returns string representation of @param bts
   */
  const btfServiceLabel = (bts: BtfService): string => {
    const btm = bts.message;

    const messageLabel = joinToString([
      btm.messageName,
      btm.messageNameVariant,
      btm.messageNameVersion,
    ], '.', '_');

    return joinToString([
      bts.serviceName,
      bts.serviceOption,
      bts.scope,
      bts.containerType,
      messageLabel,
      btm.messageNameFormat,
    ], '|', '-');
  };

  return {
    btfTypeLabel,
    orderTypeLabel,
    btfServiceLabel,
  };
}
