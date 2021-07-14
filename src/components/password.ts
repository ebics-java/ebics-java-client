import { ref, Ref } from 'vue';
import { User } from 'components/models';
import { useQuasar } from 'quasar';

export default function usePasswordAPI(user: Ref<User>) {
  const q = useQuasar();

  /**
   * Password entered on actual session for the @param user
   */
  const tempPassword: Ref<string | undefined> = ref<string | undefined>(
    undefined
  );

  const passwordDialog = (createPass: boolean): Promise<string> => {
    return new Promise<string>((resolve, reject) => {
      q.dialog({
        title: createPass ? 'Create new password' : 'Enter password',
        message: createPass
          ? 'Create new password for your user certificate'
          : 'Enter password for your user certificate',
        prompt: {
          model: '',
          //isValid: val => (val as string).length > 2,
          type: 'password',
        },
        cancel: true,
        persistent: true,
      })
        .onOk((data: unknown) => {
            tempPassword.value = data as string
          console.log(`Entered password: ${tempPassword.value}`);
          resolve(tempPassword.value);
        })
        .onCancel(() => {
          reject('Password entry canceled');
        })
        .onDismiss(() => {
          reject('Password entry dismissed');
        });
    });
  };

  /**
   * Asking for user certificat password, if required
   * createPass=true in order to ask for new password
   * createPass=false in order to ask for existing password
   */
  const promptCertPassword = (createPass: boolean): Promise<string> => {
    return new Promise<string>((resolve) => {
      if (!user.value.usePassword) {
        console.log('No pass required');
        resolve(''); //No password required
      } else {
        //Password required, did we stored some already?
        if (tempPassword.value !== undefined) {
          console.log(`Temp pass used ${tempPassword.value}`);
          resolve(tempPassword.value);
        } else {
          //We will ask user for password
          resolve(passwordDialog(createPass));
        }
      }
    });
  };

  return { tempPassword, promptCertPassword };
}
