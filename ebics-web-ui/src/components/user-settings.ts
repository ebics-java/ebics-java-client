import { ref, onMounted } from 'vue';
import { Bank, UserSettings } from 'components/models';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';

const userSettings = ref<UserSettings>();

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
