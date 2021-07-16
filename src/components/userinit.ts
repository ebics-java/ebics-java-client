import { computed, Ref } from 'vue';
import {
  User,
  UserIniWizzStep,
  UserPassword,
  AdminOrderType,
} from 'components/models';
import { useQuasar } from 'quasar';
import { api } from 'boot/axios';
import { AxiosError } from 'axios';

function isAxiosError(error: unknown): error is AxiosError {
  return (error as AxiosError).isAxiosError !== undefined;
}

export default function useUserInitAPI(user: Ref<User>) {
  const q = useQuasar();

  const apiOkHandler = (msg: string): void => {
    q.notify({
      color: 'positive',
      position: 'bottom-right',
      message: msg,
      icon: 'gpp_good',
    });
  };

  const apiErrorHandler = (msg: string, error: unknown): void => {
    if (isAxiosError(error)) {
      if (error.response !== null) {
        q.notify({
          color: 'negative',
          position: 'bottom-right',
          message: `${msg} '${JSON.stringify(error.response?.data)}'`,
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

  /*
   * This computed property reflects step which is calculated from actual userStatus,
   * It is needed in order to know which steps are actually finished
   */
  const actualWizardStep = computed<UserIniWizzStep>(() => {
    switch (user.value.userStatus) {
      case 'CREATED':
        return UserIniWizzStep.CreateUserKeys;
      case 'NEW':
      case 'LOCKED':
      case 'PARTLY_INITIALIZED_INI':
      case 'PARTLY_INITIALIZED_HIA':
        return UserIniWizzStep.UploadUserKeys;
      case 'INITIALIZED':
        return UserIniWizzStep.PrintUserLetters;
      case 'READY':
        return UserIniWizzStep.VerifyBankKeys;
    }
    return UserIniWizzStep.CreateUserKeys;
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
      await api.post<UserPassword>(`/users/${user.value.id}/certificates`, {
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
      await api.post<UserPassword>(`/users/${user.value.id}/resetStatus`);
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
    if (user.value.userStatus != 'PARTLY_INITIALIZED_INI') {
      try {
        await api.post<UserPassword>(
          `/users/${user.value.id}/${user.value.ebicsVersion}/send${adminOrderType}`,
          { password: pass }
        );
        apiOkHandler(
          `${adminOrderType} executed successfully for user name: ${user.value.name}`
        );
      } catch (error) {
        apiErrorHandler(`${adminOrderType} failed: `, error);
      }
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
