import { computed, Ref } from 'vue';
import {
  User,
  UserIniWizzStep,
  UserPassword,
  AdminOrderType,
} from 'components/models';
import { useQuasar } from 'quasar';
import { api } from 'boot/axios';

export default function useUserInitAPI(user: Ref<User>) {
  const q = useQuasar();

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

  const createUserKeysRequest = (pass: string): Promise<void> => {
    return new Promise<void>((resolve, reject) => {
      api
        .post<UserPassword>(`/users/${user.value.id}/certificates`, { password: pass })
        .then(() => {
          q.notify({
            color: 'positive',
            position: 'bottom-right',
            message: `Certificates created successfully for user name: ${user.value.name} dn: ${user.value.dn}`,
            icon: 'gpp_good',
          });
          resolve();
        })
        .catch((error: Error) => {
          reject(`Create certificates failed: ${error.message}`);
        });
    });
  };

  const resetUserStatusRequest = (): Promise<void> => {
    return new Promise<void>((resolve, reject) => {
      api
        .post<UserPassword>(`/users/${user.value.id}/resetStatus`)
        .then(() => {
          q.notify({
            color: 'positive',
            position: 'bottom-right',
            message: `Initialization status resetted successfully for user name: ${user.value.name}`,
            icon: 'gpp_good',
          });
          resolve();
        })
        .catch((error: Error) => {
          reject(`Initialization status reset failed: ${error.message}`);
        });
    });
  };

  /**
   * Executest EBICS Admin Ordertype requests like INI, HIA, HPB, SPR,..
   * @param adminOrderType
   * @param pass
   * @returns
   */
  const ebicsAdminTypeRequest = (
    adminOrderType: AdminOrderType,
    pass: string
  ): Promise<void> => {
    return new Promise<void>((resolve, reject) => {
      if (user.value.userStatus == 'PARTLY_INITIALIZED_INI') resolve();
      else {
        api
          .post<UserPassword>(
            `/users/${user.value.id}/${user.value.ebicsVersion}/send${adminOrderType}`,
            { password: pass }
          )
          .then(() => {
            q.notify({
              color: 'positive',
              position: 'bottom-right',
              message: `${adminOrderType} executed successfully for user name: ${user.value.name}`,
              icon: 'gpp_good',
            });
            resolve();
          })
          .catch((error: Error) => {
            reject(
              new Error(`${adminOrderType} execution failed: ${error.message}`)
            );
          });
      }
    });
  };

  return { actualWizardStep, isStepDone, createUserKeysRequest, ebicsAdminTypeRequest, resetUserStatusRequest };
}
