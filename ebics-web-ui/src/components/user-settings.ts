import { ref, onMounted } from 'vue';
import { UserSettings } from 'components/models';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';

const userSettings = ref<UserSettings>({
  uploadOnDrop: true,
  testerSettings: true,
  adjustmentOptions: {
    applyAutomatically: true,
    pain00x: {
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
    swift: {
      uetr: true,
      f20: true,
      f21: false,
      f30: true,
      idPrefix: 't111111',
      randomIds: false,
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
      console.log('User settings loaded: ' + JSON.stringify(userSettings.value))
    } catch (error) {
      apiErrorHandler('Loading of user settings failed', error);
    }
  };

  const saveUserSettings = async (): Promise<void> => {
    try {
      await api.put<UserSettings>('user/settings', userSettings);
    } catch (error) {
      apiErrorHandler('Saving settings failed', error);
    }
  };

  onMounted(loadUserSettings);

  return { userSettings, loadUserSettings, saveUserSettings };
}
