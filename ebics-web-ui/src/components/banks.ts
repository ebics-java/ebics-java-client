import { ref, onMounted } from 'vue';
import { Bank } from 'components/models';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';
import useDialogs from './dialogs';

/**
 * Bank Connections composition API for bank connection list operations with backend REST API
 * @returns
 *  banks synchronized list of banks
 *  loadBanks function to trigger refreshing of banks
 *  deleteBank function to delete bank 
 */
export default function useBanksAPI() {
  const { apiErrorHandler } = useBaseAPI();
  const { confirmDialog } = useDialogs();

  const banks = ref<Bank[]>();

  const loadBanks = async (): Promise<void> => {
    try {
      const response = await api.get<Bank[]>('banks');
      banks.value = response.data;
    } catch (error) {
      apiErrorHandler('Loading of banks failed', error);
    }
  };

  const deleteBank = async (bankId: number, bankName: string, askForConfimation = true): Promise<void> => {
    try {
      const canDelete = askForConfimation ? await confirmDialog('Confirm deletion', `Do you want to realy delete bank : '${bankName}'`) : true
      if (canDelete) {
        await api.delete<Bank>(`banks/${bankId}`);
        await loadBanks();
      }
    } catch (error) {
      apiErrorHandler('Deleting of bank failed', error);
    }
  };

  onMounted(loadBanks);

  return { banks, loadBanks, deleteBank };
}
