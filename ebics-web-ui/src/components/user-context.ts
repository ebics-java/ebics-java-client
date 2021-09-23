import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { UserContext, AuthenticationType } from 'components/models';
import { useQuasar } from 'quasar';
import { api } from 'boot/axios';
import { AxiosBasicCredentials } from 'axios';

//Read authentication type from quasar env variables defined in config.
function getAuthTypeFromEnv():AuthenticationType {
  switch(process.env.AUTH_TYPE) {
    case 'SSO':
      return AuthenticationType.SSO;
    case 'HTTP_BASIC':
      return AuthenticationType.HTTP_BASIC;
    default:
      return AuthenticationType.SSO;
  }
}

function getAuthSSOBasicTypeFromEnv():boolean {
  return process.env.AUTH_TYPE_SSO_OVER_BASIC ? true : false;
}

const authenticationType = ref<AuthenticationType>(getAuthTypeFromEnv());
const refreshUserContextByMounth = true;
const ssoDevOverBasic = ref(getAuthSSOBasicTypeFromEnv()); 

//Reactive http basic credentials object available in browser session
//Initally no default password & username to force login, can be changed for dev purposes
const basicCredentials = ref<AxiosBasicCredentials>({
  username: '',
  password: '',
});

//Reactive authorization context updated from backend (in case of undefined no login call was done yet.)
const userContext = ref<UserContext | undefined>(undefined);

/**
 * Logged User composition API in order to get authorization&authetication context
 * @returns
 *  userContext - user authorization context, name and roles
 *  refreshUserContextData - function to refresh user context (login)
 */
export default function useUserContextAPI() {
  const q = useQuasar();
  const router = useRouter();

  const resetUserContextData = async () => {
    if (authenticationType.value == AuthenticationType.HTTP_BASIC) {
      basicCredentials.value = { username: '', password: '' };
      userContext.value = undefined;
      await router.push({ path: 'login' });
      q.notify({
        color: 'positive',
        position: 'bottom-right',
        message: 'Authentication context reseted',
        icon: 'report_problem',
      });
    } else if (authenticationType.value == AuthenticationType.SSO) {
      await refreshUserContextData();
    }
  };

  const hasCredentials = (): boolean => {
    if (authenticationType.value == AuthenticationType.HTTP_BASIC)
      return (
        basicCredentials.value.username !== '' &&
        basicCredentials.value.password !== ''
      );
    else if (authenticationType.value == AuthenticationType.SSO) {
      return true;
    }
    return false;
  };

  const refreshUserContextData = async (): Promise<void> => {
    if (
      authenticationType.value == AuthenticationType.HTTP_BASIC &&
      !hasCredentials()
    ) {
      //In case we use http basic, and we dont have yet credential lets redirect to login
      await router.push({ path: 'login' });
    } else {
      //Simulate SSO via HTTP basic with given credentials
      if (
        ssoDevOverBasic.value &&
        authenticationType.value == AuthenticationType.SSO
      ) {
        console.warn(
          'Setting default HTTP basic credentials for dev mode only'
        );
        api.defaults.auth = { username: 'admin', password: 'pass' };
      }

      console.log(
        'Basic HTTP credentials:' + JSON.stringify(api.defaults.auth)
      );
      //We have credential, we do login API call to get principal and roles from backend
      try {
        const response = await api.get<UserContext>('user');
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
    }
  };

  //Check if the user has specific role
  const hasRole = (userRoleName: string): boolean => {
    return (
      userContext.value !== undefined &&
      userContext.value.roles.some((roleName) =>
        roleName.includes(userRoleName)
      )
    );
  };

  const onCredentialsChanged = () => {
    if (authenticationType.value == AuthenticationType.HTTP_BASIC) {
      api.defaults.auth = basicCredentials.value;
    }
  };

  //To keep axios default basic auth credential for all axios call in sync with reactive basicCredentials
  watch(basicCredentials.value, onCredentialsChanged);

  if (refreshUserContextByMounth) {
    //User data is refreshed by mounting if required
    onMounted(refreshUserContextData);
  }

  return {
    authenticationType,
    ssoDevOverBasic,
    userContext,
    basicCredentials,
    hasRole,
    resetUserContextData,
    refreshUserContextData,
  };
}
