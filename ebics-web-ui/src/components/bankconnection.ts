import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { BankConnection, Partner, Bank, UserPartnerBank } from 'components/models';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';

/**
 * User composition API in order to keep user object in sync with backend REST API
 * @param userId id of the given user in case of editing, undefined in case of the new user
 * @returns 
 *  user - reactive user data
 *  userPartnerBank - computed userPartnerBank data used for creating/updating
 *  refreshUserData - function to be called in order to refresh user data from REST API
 *  createOrUpdateUserData - function used for storing user data using API
 */
export default function useBankConnectionAPI(userId: number | undefined) {
  const router = useRouter()
  const { apiErrorHandler, apiOkHandler } = useBaseAPI();

  const bankConnection = ref<BankConnection>({
    name: '',
    userId: '',
    partner: {
      partnerId: '',
      bank: {
        id: 0,
        name: '',
      } as Bank,
    } as Partner,
    ebicsVersion: 'H005',
    userStatus: 'CREATED',
    guestAccess: false,
    usePassword: false,
    useCertificate: true,
  } as BankConnection);

  const refreshUserData = async () => {
    if (userId !== undefined) {
      try {
        const response = await api.get<BankConnection>(`bankconnections/${userId}`)
        bankConnection.value = response.data;
      } catch(error) {
        apiErrorHandler('Loading of user failed', error)
      }
    }
  };

  /**
   * Converting input User entity from GET request to UserPartnerBank in order for storing/updating of user data.
   */
  const userPartnerBank = computed<UserPartnerBank>(() => {
    return {
      ebicsVersion: bankConnection.value.ebicsVersion,
      userId: bankConnection.value.userId,
      name: bankConnection.value.name,
      dn: bankConnection.value.dn,
      partnerId: bankConnection.value.partner.partnerId,
      bankId: bankConnection.value.partner.bank.id,
      guestAccess: bankConnection.value.guestAccess,
      usePassword: bankConnection.value.usePassword,
      useCertificate: bankConnection.value.useCertificate,
    } as UserPartnerBank;
  });

  const createOrUpdateUserData = async() => {
    if (userId === undefined) {
      try {
        await api.post<UserPartnerBank>('bankconnections', userPartnerBank.value)
        router.go(-1);
        apiOkHandler('Bank connection created')
      } catch(error) {
        apiErrorHandler('Bank connection creation failed', error)
      }
    } else {
      try {
        await api.put<UserPartnerBank>(`bankconnections/${userId}`, userPartnerBank.value)
        router.go(-1);
        apiOkHandler('Bank connection updated')
      } catch(error) {
        apiErrorHandler('Bank connection update failed', error)
      }
    }  
  };

  //User data is refreshed by mounting
  onMounted(refreshUserData);

  return { bankConnection, userPartnerBank, refreshUserData, createOrUpdateUserData };
}
