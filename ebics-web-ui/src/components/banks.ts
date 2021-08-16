import { ref, onMounted } from 'vue';
import { Bank } from 'components/models';
import { useQuasar } from 'quasar';
import { api } from 'boot/axios';

/**
 * Bank composition API in order to keep banks list updated from backend REST API
 * @returns 
 *  banks - reactive banks list
 *  refreshBanksData - function to be called in order to refresh banks list from REST API
 */
export default function useBanksAPI() { 
    const q = useQuasar();
    const banks = ref<Bank[]>([]);

    //Function to refresh banks data
    const refreshBanksData = () => {
        api
          .get<Bank[]>('banks')
          .then((response) => {
            banks.value = response.data;
          })
          .catch((error: Error) => {
            q.notify({
              color: 'negative',
              position: 'bottom-right',
              message: `Loading failed: ${error.message}`,
              icon: 'report_problem',
            });
          });
      }

    //Banks data is refreshed by mounting
    onMounted(refreshBanksData);

    return {banks, refreshBanksData };
}