import { ref, onMounted, computed } from 'vue';
import { Bank, EbicsVersionSettings } from 'components/models';
import { api } from 'boot/axios';
import { AxiosResponse } from 'axios';
import useBaseAPI from './base-api';
import { useRouter } from 'vue-router';

/**
 * Bank Connections composition API for bank connection list operations with backend REST API
 * @returns
 *  bank data synchronized with REST backend
 *  loadBank function to trigger refreshing of bank data
 *  createOrUpdateBank function to trigger saving of the bank data
 */
export default function useBankAPI(bankId: number | undefined) {
  const { apiErrorHandler, apiOkHandler } = useBaseAPI();
  const router = useRouter();

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

  const createOrUpdateBank = async () => {
    if (bank.value.id === undefined) {
      try {
        const response = await api.post<Bank, AxiosResponse<number>>(
          'banks',
          bank.value
        );
        bank.value.id = response.data; //Store id of the bank
        router.go(-1);
        apiOkHandler('Bank connection created');
      } catch (error) {
        apiErrorHandler('Bank connection creation failed', error);
      }
    } else {
      try {
        await api.put<Bank>(`banks/${bank.value.id}`, bank.value);
        router.go(-1);
        apiOkHandler('Bank connection updated');
      } catch (error) {
        apiErrorHandler('Bank connection update failed', error);
      }
    }
  };

  const loadVersionsSettings = async (forceOnline = false): Promise<void> => {
    try {
      if (bank.value.id != undefined) {
        const response = await api.get<EbicsVersionSettings[]>(
          `banks/${bank.value.id}/supportedVersions?mode=${forceOnline ? 'ForcedOnline' : 'Offline'}`
        );
        versionSettings.value = response.data;
      } else if (bank.value?.bankURL?.length && bank.value?.hostId?.length) {
        const response = await api.get<EbicsVersionSettings[]>(
          `banks/supportedVersions?hostId=${bank.value?.hostId}&bankURL=${bank.value?.bankURL}`
        );
        versionSettings.value = response.data;
      }
    } catch (error) {
      apiErrorHandler('Loading of supported EBICS versions failed', error);
    }
  };

  const saveVersionsSettings = async (): Promise<void> => {
    try {
      if (bank.value.id != undefined && versionSettings.value?.length) {
        for (const versionSett of versionSettings.value) {
          await api.put<EbicsVersionSettings[]>(
            `banks/${bank.value.id}/supportedVersions/${versionSett.version}`,
            versionSett
          );
        }
      }
    } catch (error) {
      apiErrorHandler('Loading of supported EBICS versions failed', error);
    }
  };

  const allowedVersionsCount = computed(():number => {
    return versionSettings.value.filter(v => v.isAllowed).length;
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
