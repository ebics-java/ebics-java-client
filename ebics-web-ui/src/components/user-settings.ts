import { ref, onMounted } from 'vue';
import { Bank, UserSettings } from 'components/models';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';

const userSettings = ref<UserSettings>({
  uploadOnDrop: true,
  testerSettings: true,
  adjustmentOptions: {
    applyAuthomatically: true,
    pain001: {
      msgId: true,
      pmtInfId: false,
      instrId: true,
      endToEndId: true,
      uetr: true,
      nbOfTrxsCalc: true,
      ctrlSumCalc: true,
      reqdExctnDt: true,
      creDtTm: true,
      idPrefix: 't51246',
    },
    mt101: {
      uetr: true,
      f20: true,
      f21: false,
    },
  },
});

/**
 * 
 * @returns user settings related API
 */
export default function useUserSettingsAPI() {
  const { apiErrorHandler } = useBaseAPI();

  const loadUserSettings = async (): Promise<void> => {
    try {
      const response = await api.get<UserSettings>('user/settings');
      userSettings.value = response.data;
    } catch (error) {
      apiErrorHandler('Loading of user settings failed', error);
    }
  };

  const saveUserSettings = async (): Promise<void> => {
    try {
      await api.put<Bank>('user/settings', userSettings);
    } catch (error) {
      apiErrorHandler('Deleting of bank failed', error);
    }
  };

  onMounted(loadUserSettings);

  return { userSettings, loadUserSettings, saveUserSettings };
}
