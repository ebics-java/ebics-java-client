import { ref, onMounted, computed } from 'vue';
import { Bank, EbicsVersionSettings } from 'components/models';
import { api } from 'boot/axios';
import { AxiosResponse } from 'axios';
import useBaseAPI from './base-api';

/**
 * Bank Connections composition API for bank connection list operations with backend REST API
 * @returns
 *  bank data synchronized with REST backend
 *  loadBank function to trigger refreshing of bank data
 *  createOrUpdateBank function to trigger saving of the bank data
 */
export default function useBankAPI(bankId: number | undefined) {
  const { apiErrorHandler, apiOkHandler } = useBaseAPI();

  const bank = ref<Bank>({
    id: bankId,
    name: '',
    bankURL: 'https://',
    hostId: '',
  } as Bank);
  const versionSettings = ref<EbicsVersionSettings[]>([]);

  const loadBank = async (): Promise<void> => {
    try {
      if (bank.value.id != undefined) {
        const response = await api.get<Bank>(`banks/${bank.value.id}`);
        bank.value = response.data;
      }
    } catch (error) {
      apiErrorHandler('Loading of bank failed', error);
    }
  };

  const createOrUpdateBank = async ():Promise<boolean> => {
    if (bank.value.id === undefined) {
      try {
        const response = await api.post<Bank, AxiosResponse<number>>(
          'banks',
          bank.value
        );
        bank.value.id = response.data; //Store id of the bank
        console.log(`Bank created id=${bank.value?.id}`);
        apiOkHandler('Bank connection created');
        return true;
      } catch (error) {
        apiErrorHandler('Bank connection creation failed', error);
        return false;
      }
    } else {
      try {
        await api.put<Bank>(`banks/${bank.value.id}`, bank.value);
        apiOkHandler('Bank connection updated');
        return true;
      } catch (error) {
        apiErrorHandler('Bank connection update failed', error);
        return false;
      }
    }
  };

  const loadVersionsSettings = async (forceOnline = false): Promise<void> => {
    try {
      if (bank.value?.hostId && bank.value?.bankURL) {
        console.log('Versions live: ' + bank.value?.httpClientConfigurationName);
        const response = await api.get<EbicsVersionSettings[]>(
          `banks/supportedVersions?bankId=${bank.value?.id ?? ''}&hostId=${
            bank.value?.hostId
          }&httpClientConfigurationName=${
            bank.value?.httpClientConfigurationName
          }&bankURL=${bank.value?.bankURL}&mode=${
            forceOnline ? 'ForcedOnline' : 'Offline'
          }`
        );
        versionSettings.value = response.data;
      }
    } catch (error) {
      apiErrorHandler('Loading of supported EBICS versions failed', error);
    }
  };

  const saveVersionsSettings = async (): Promise<boolean> => {
    try {
      console.log(`Save versions for bank id: ${bank.value?.id}`);
      if (bank.value.id != undefined && versionSettings.value?.length) {
        for (const versionSett of versionSettings.value) {
          await api.put<EbicsVersionSettings[]>(
            `banks/${bank.value.id}/supportedVersions/${versionSett.version}`,
            versionSett
          );
          console.log(`Save version ${JSON.stringify(versionSett)}`);
        }
      }
      return true;
    } catch (error) {
      apiErrorHandler('Saving of supported EBICS versions failed', error);
      return false;
    }
  };

  const allowedVersionsCount = computed((): number => {
    return versionSettings.value.filter((v) => v.isAllowedForUse).length;
  });

  onMounted(async () => {
    await loadBank();
    await loadVersionsSettings();
  });

  return {
    bank,
    loadBank,
    createOrUpdateBank,
    versionSettings,
    allowedVersionsCount,
    loadVersionsSettings,
    saveVersionsSettings,
  };
}
