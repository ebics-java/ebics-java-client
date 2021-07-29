import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { User, Partner, Bank, UserPartnerBank } from 'components/models';
import { useQuasar } from 'quasar';
import { api } from 'boot/axios';

/**
 * User composition API in order to keep user object in sync with backend REST API
 * @param userId id of the given user in case of editing, undefined in case of the new user
 * @returns 
 *  user - reactive user data
 *  userPartnerBank - computed userPartnerBank data used for creating/updating
 *  refreshUserData - function to be called in order to refresh user data from REST API
 *  createOrUpdateUserData - function used for storing user data using API
 */
export default function useUserAPI(userId: number | undefined) {
  const q = useQuasar();
  const router = useRouter()

  const user = ref<User>({
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
  } as User);

  const refreshUserData = () => {
    if (userId !== undefined) {
      api
        .get<User>(`/users/${userId}`)
        .then((response) => {
          console.log('User data refreshed')
          user.value = response.data;
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
  };

  /**
   * Converting input User entity from GET request to UserPartnerBank in order for storing/updating of user data.
   */
  const userPartnerBank = computed<UserPartnerBank>(() => {
    return {
      ebicsVersion: user.value.ebicsVersion,
      userId: user.value.userId,
      name: user.value.name,
      dn: user.value.dn,
      partnerId: user.value.partner.partnerId,
      bankId: user.value.partner.bank.id,
      useCertificate: user.value.useCertificate,
      usePassword: user.value.usePassword,
    } as UserPartnerBank;
  });

  const createOrUpdateUserData = () => {
    if (userId === undefined) {
      api
        .post<UserPartnerBank>('/users', userPartnerBank.value)
        .then(() => {
          q.notify({
            color: 'green-4',
            textColor: 'white',
            icon: 'cloud_done',
            message: 'User created successfully',
          });
          router.go(-1);
        })
        .catch((error: Error) => {
          q.notify({
            color: 'negative',
            position: 'bottom-right',
            message: `Creating of user failed: ${error.message}`,
            icon: 'report_problem',
          });
        });
    } else {
      api
        .put<UserPartnerBank>(`/users/${userId}`, userPartnerBank.value)
        .then(() => {
          q.notify({
            color: 'green-4',
            textColor: 'white',
            icon: 'cloud_done',
            message: 'User updated sucessfully',
          });
          router.go(-1);
        })
        .catch((error: Error) => {
          q.notify({
            color: 'negative',
            position: 'bottom-right',
            message: `User update failed: ${error.message}`,
            icon: 'report_problem',
          });
        });
    }
  };

  //User data is refreshed by mounting
  onMounted(refreshUserData);

  return { user, userPartnerBank, refreshUserData, createOrUpdateUserData };
}
