import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router'
import { UserContext } from 'components/models';
import { useQuasar } from 'quasar';
import { api } from 'boot/axios';
import { AxiosBasicCredentials } from 'axios';

/**
 * Logged User composition API in order to get authorization&authetication context
 * @returns 
 *  userContext - user authorization context, name and roles
 *  refreshUserContextData - function to refresh user context (login)
 */
export default function useUserContextAPI() {
  const q = useQuasar();
  const router = useRouter();

  //Reactive credentials object
  const basicCredentials = ref<AxiosBasicCredentials>(api.defaults.auth as AxiosBasicCredentials)

  //Reactive authorization context updated from backend, in case of undefined no login call was done yet.
  const userContext = ref<UserContext | undefined>(undefined);

  const resetUserContextData = async() => {
    q.notify({
      color: 'positive',
      position: 'bottom-right',
      message: 'Authentication context reseted',
      icon: 'report_problem',
    });
    userContext.value = undefined;
    await router.push({path: 'login'})
  };

  const refreshUserContextData = async(): Promise<void>  => {
    try {
      const response = await api.get<UserContext>('/user');  
      q.notify({
        color: 'positive',
        position: 'bottom-right',
        message: 'Authentication successfull',
        icon: 'report_problem',
      });
      console.log(JSON.stringify(response.data));
      userContext.value = response.data;
      userContext.value.time = new Date().toISOString();
    } catch (error) {
      userContext.value = undefined;
      q.notify({
        color: 'negative',
        position: 'bottom-right',
        message: `Authentication failed: ${JSON.stringify(error)}`,
        icon: 'report_problem',
      });
      throw error;
    }
  };

  //Check if the user has specific role
  const hasRole = (userRoleName: string): boolean => {
    return userContext.value !== undefined && userContext.value.roles.some(roleName => roleName.includes(userRoleName));
  };

  const onCredentialsChanged = () => {
    api.defaults.auth = basicCredentials.value
  };

  //To keep axios default basic auth credential for all axios call in sync with reactive basicCredentials
  watch(basicCredentials, onCredentialsChanged)

  //User data is refreshed by mounting
  onMounted(refreshUserContextData);

  return { userContext, basicCredentials, hasRole, resetUserContextData, refreshUserContextData };
}
