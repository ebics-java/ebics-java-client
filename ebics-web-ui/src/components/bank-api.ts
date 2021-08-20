import { ref, onMounted } from 'vue';
import { Bank } from 'components/models';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';

/**
 * Bank composition API in order to keep banks list updated from backend REST API
 * @returns 
 *  banks - reactive banks list
 *  refreshBanksData - function to be called in order to refresh banks list from REST API
 */
export default function useBanksAPI() { 
    const banks = ref<Bank[]>([]);
    const {apiOkHandler, apiErrorHandler} = useBaseAPI();
 
    //Function to refresh banks data
    const refreshBanksData = async () => {
      try {
        const response = await api.get<Bank[]>('banks')
        banks.value = response.data
      } catch(error) {
        apiErrorHandler('Loading banks', error)
      }  
    }

    //Banks data is refreshed by mounting
    onMounted(refreshBanksData);

    return {banks, refreshBanksData };
}