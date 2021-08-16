import { computed, Ref, ref } from 'vue';
import {
  User,
  UserIniWizzStep,
  UserPassword,
  AdminOrderType,
  EbicsApiError,
} from 'components/models';
import { useQuasar } from 'quasar';
import { api } from 'boot/axios';
import { AxiosError } from 'axios';
import usePasswordAPI from './password';

function isAxiosError<T>(error: unknown): error is AxiosError<T> {
  return (error as AxiosError).isAxiosError !== undefined;
}

export default function useUserInitAPI(
  user: Ref<User>
) {
  const q = useQuasar();
  const { resetCertPassword } = usePasswordAPI();

  const apiOkHandler = (msg: string): void => {
    q.notify({
      color: 'positive',
      position: 'bottom-right',
      message: msg,
      icon: 'gpp_good',
    });
  };

  /**
   * REST API Error Handler
   * - Log the whole error in console
   * - Notify user with some readable error message
   * @param msg context message, for example 'user A initialization'
   * @param error REST API call error
   */
  const apiErrorHandler = (msg: string, error: unknown): void => {
    console.log(JSON.stringify(error));
    if (isAxiosError<EbicsApiError>(error)) {
      if (error.response !== null) {
        const ebicsApiError = error.response?.data as EbicsApiError;
        if (ebicsApiError.message.includes('wrong password')) {
          //In case of error 'wrong password' we have to reset temporary stored password in order to ask for new one
          resetCertPassword(user.value)
        }
        let message = ebicsApiError.message;
        if (!ebicsApiError.description.includes(message))
          message = `message: ${message} description: ${ebicsApiError.description}`;
        q.notify({
          color: 'negative',
          position: 'bottom-right',
          message: `${msg} '${message}'`,
          closeBtn: true,
          icon: 'report_problem',
          timeout: 10000,
        });
      } else if (error.request) {
        q.notify({
          color: 'negative',
          position: 'bottom-right',
          message: `${msg} '${JSON.stringify(error.request)}'`,
          icon: 'report_problem',
        });
      } else {
        q.notify({
          color: 'negative',
          position: 'bottom-right',
          message: `${msg} '${JSON.stringify(error.message)}'`,
          icon: 'report_problem',
        });
      }
    } else {
      q.notify({
        color: 'negative',
        position: 'bottom-right',
        message: `${msg} '${JSON.stringify(error)}'`,
        icon: 'report_problem',
      });
    }
  };

  //TBD: needs to be somehow persisted to user state
  const userStatusLetterPrinted = ref<boolean>(false);

  //TBD: needs to be somehow persisted to user state
  const userStatusBankKeysVerified = ref<boolean>(false);

  /*
   * This computed property reflects step which is calculated from actual userStatus,
   * It is needed in order to know which steps are actually finished
   */
  const actualWizardStep = computed<UserIniWizzStep>({
    get() {
      switch (user.value.userStatus) {
        case 'CREATED':
          return UserIniWizzStep.CreateUserKeys;
        case 'NEW':
        case 'LOCKED':
        case 'PARTLY_INITIALIZED_INI':
        case 'PARTLY_INITIALIZED_HIA':
          return UserIniWizzStep.UploadUserKeys;
        case 'INITIALIZED':
          return userStatusLetterPrinted.value
            ? UserIniWizzStep.DownloadBankKeys
            : UserIniWizzStep.PrintUserLetters;
        case 'READY':
          return userStatusBankKeysVerified.value
            ? UserIniWizzStep.Finish
            : UserIniWizzStep.VerifyBankKeys;
      }
      return UserIniWizzStep.CreateUserKeys;
    },
    set(value) {
      switch (value) {
        case UserIniWizzStep.DownloadBankKeys:
          if (user.value.userStatus == 'INITIALIZED') {
            userStatusLetterPrinted.value = true;
          }
        case UserIniWizzStep.Finish:
          if (user.value.userStatus == 'READY') {
            userStatusBankKeysVerified.value = true;
          }
      }
    },
  });

  /**
   * Returns true if the @param step is already done in compare with actual status of user
   * @param step wizart step to be tested if is done
   * @returns
   */
  const isStepDone = (step: UserIniWizzStep): boolean => {
    return step < actualWizardStep.value;
  };

  const createUserKeysRequest = async (pass: string): Promise<void> => {
    try {
      await api.post<UserPassword>(`bankconnections/${user.value.id}/certificates`, {
        password: pass,
      });
      apiOkHandler(
        `Certificates created successfully for user name: ${user.value.name} dn: ${user.value.dn}`
      );
    } catch (error) {
      apiErrorHandler('Create certificates failed: ', error);
    }
  };

  const resetUserStatusRequest = async (): Promise<void> => {
    try {
      await api.post<UserPassword>(`bankconnections/${user.value.id}/resetStatus`);
      apiOkHandler(
        `Initialization status resetted successfully for user name: ${user.value.name}`
      );
    } catch (error) {
      apiErrorHandler('Initialization status reset failed: ', error);
    }
  };

  /**
   * Executest EBICS Admin Ordertype requests like INI, HIA, HPB, SPR,..
   * @param adminOrderType
   * @param pass
   * @returns
   */
  const ebicsAdminTypeRequest = async (
    adminOrderType: AdminOrderType,
    pass: string
  ): Promise<void> => {
    try {
      await api.post<UserPassword>(
        `bankconnections/${user.value.id}/${user.value.ebicsVersion}/send${adminOrderType}`,
        { password: pass }
      );
      apiOkHandler(
        `${adminOrderType} executed successfully for user name: ${user.value.name}`
      );
    } catch (error) {
      apiErrorHandler(`${adminOrderType} failed: `, error);
    }
  };

  return {
    actualWizardStep,
    isStepDone,
    createUserKeysRequest,
    ebicsAdminTypeRequest,
    resetUserStatusRequest,
  };
}
