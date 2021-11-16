import { ref } from 'vue';
import { describe, expect, it, jest } from '@jest/globals';
import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-jest';
import useBankConnectionsAPI from 'src/components/bankconnections';
import axios from 'axios';
import { BankConnection } from 'src/components/models';
import { mountComposition } from 'vue-composition-test-utils';
import { Notify } from 'quasar';
import { nextTick } from 'process';
import { AxiosInstance } from 'axios'
import { api } from 'boot/axios'

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $axios: AxiosInstance;
  }
}

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;
//const api2 = axios.create();
//const mockedApiAxios = api2 as jest.Mocked<AxiosInstance>;

beforeAll(() => {
  mockedAxios.create.mockReturnThis();
});

// Specify here Quasar config you'll need to test your component
installQuasarPlugin({ plugins: { Notify } });

describe('BankConnections', () => {

  it('Axios Static mocked call returning mock result', async() => {
    const resp = {
      data: [
        {
          name: 'test',
          ebicsVersion: 'H005',
          userId: 'uid',
        } as BankConnection,
      ] as BankConnection[],
    };

    //mockedAxios.get.mockImplementation(() => Promise.resolve({ data: resp.data })); //Same as bellow
    mockedAxios.get.mockResolvedValue(resp);

    const result = await axios.get<BankConnection[]>('random/get/url');
    
    expect(result).toBeTruthy();
    expect(result.data).toBeTruthy();
    expect(result.data.length).toBe(1);
    expect(result.data[0].name).toBe('test');
  });

  it('Axios Instance mocked call returning mock result', async() => {
    const resp = {
      data: [
        {
          name: 'test',
          ebicsVersion: 'H005',
          userId: 'uid',
        } as BankConnection,
      ] as BankConnection[],
    };

    //mockedApiAxios.get.mockImplementation(() => Promise.resolve({ data: resp.data })); //Same as bellow
    mockedAxios.get.mockResolvedValue(resp);

    const api = axios.create(); //If defined globally, then is undefined for non known reason
    expect(api).toBeTruthy();
    expect(typeof api.get).toBe('function');

    const result = await api.get<BankConnection[]>('random/get/url');
    
    expect(result).toBeTruthy();
    expect(result.data).toBeTruthy();
    expect(result.data.length).toBe(1);
    expect(result.data[0].name).toBe('test');
  });

  /*it('test', () => {
    const wrapper = mountComposition(useBankConnectionsAPI);
    expect(wrapper).toBeTruthy();
  });*/

  it('should produce NON empty bankConnections list for NON empty input', async () => {
    const resp = {
      data: [
        {
          name: 'test',
          ebicsVersion: 'H005',
          userId: 'uid',
        } as BankConnection,
      ] as BankConnection[],
    };

    mockedAxios.get.mockResolvedValue(resp);

    const api = axios.create(); //If defined globally, then is undefined for non known reason
    
    const wrapper = mountComposition(useBankConnectionsAPI, {
      component: {
        template: 'Bank connections', // {{result.current.bankConnections.length}}',
      },
      global: {
        axios: mockedAxios,
        api: api
      }
    });
    

    //await nextTick(async() => {
      await wrapper.result.current?.loadBankConnections();
    //});
    
    expect(wrapper.result.current?.bankConnections.value).not.toBe(undefined);
    const bankConnectionsList = wrapper.result.current?.bankConnections
      .value as BankConnection[];
    expect(bankConnectionsList.length).toBe(1);
    expect(bankConnectionsList[0].name).toBe('test');
  });
});
