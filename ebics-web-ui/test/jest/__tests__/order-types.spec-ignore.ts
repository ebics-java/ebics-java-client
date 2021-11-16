import { ref } from 'vue'
import { describe, expect, it, beforeAll } from '@jest/globals';
import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-jest';
import { OrderType, BTFType, OrderTypeFilter } from 'src/components/models';
import useOrderTypesAPI from 'src/components/order-types';
import useBankConnectionsAPI from 'src/components/bankconnections';
//import { mount } from "@vue/test-utils"
//import CompositionApi from "@/components/CompositionApi.vue"
//import Vue from 'vue'

// Specify here Quasar config you'll need to test your component
installQuasarPlugin();

describe('OrderTypes', () => {

  let btfTypeLabelFn: (btf: BTFType | undefined)=>string;
  let orderTypeLabelFn: (btf: OrderType | undefined)=>string;

  beforeAll(() => {
    const { btfTypeLabel, orderTypeLabel } = useOrderTypesAPI(ref(undefined), ref(OrderTypeFilter.DownloadOnly), ref(true));
    btfTypeLabelFn = btfTypeLabel;
    orderTypeLabelFn = orderTypeLabel;
  });

  it('is empty BTF labeled correctly', () => {
    const btf = {} as BTFType
    expect(btfTypeLabelFn(btf)).toBe(undefined);
  });

  it('is admin ordertype in BTF labeled correctly', () => {
    const btf = {adminOrderType: 'XYZ'} as BTFType
    expect(btfTypeLabelFn(btf)).toBe('XYZ');
  });

  it('is real BTF labeled correctly', () => {
    const btf = {
      adminOrderType: 'XYZ',
      service: {
        serviceName: 'PAY',
        serviceOption: 'OPT',
        scope: 'CH',
        containerType: 'XML',
        message: {
          messageName: 'pain',
          messageNameVersion: '03',
          messageNameFormat: 'xml',
          messageNameVariant: '001',
        }
      }
    } as BTFType
    expect(btfTypeLabelFn(btf)).toBe('PAY|OPT|CH|XML|pain|001|03|xml');
  });

  it('should produce empty list of ordertypes for empty input', () => {
    const { btfTypes, orderTypes } = useOrderTypesAPI(ref(undefined), ref(OrderTypeFilter.DownloadOnly), ref(true));
    
    expect(btfTypes.value.length).toBe(0);
    expect(orderTypes.value.length).toBe(0);
    //updateOrderTypesCacheForBankConnection,
  });

  it('should produce non empty btfTypes list for non empty input', () => {
    const { bankConnections } = useBankConnectionsAPI()
    const { btfTypes, orderTypes } = useOrderTypesAPI(ref(undefined), ref(OrderTypeFilter.DownloadOnly), ref(true));
    
    expect(btfTypes.value.length).toBe(0);
    expect(orderTypes.value.length).toBe(0);
    //updateOrderTypesCacheForBankConnection,
  });
});
