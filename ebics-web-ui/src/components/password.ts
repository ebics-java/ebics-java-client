import { ref, Ref } from 'vue';
import { useQuasar } from 'quasar';
import { User } from 'components/models'

/**
 * Passwords entered on actual session for the given @param user.id
 * @returns 
 *  promptCertPassword getting the certificate password, if not yet entered in this session by user, then asking for it via dialog
 *  resetCertPassword resetting the actual certificate password for given user in this session (used if is wrong password used)
 */
const tempPasswords: Ref<Map<number, string | undefined>> = ref<Map<number, string | undefined>>(new Map());

export default function usePasswordAPI() {
  const q = useQuasar();

  const passwordDialog = (userId: number, createPass: boolean): Promise<string> => {
    return new Promise<string>((resolve, reject) => {
      q.dialog({
        title: createPass ? 'Create new password' : 'Enter password',
        message: createPass
          ? 'Create new password for your user certificate'
          : 'Enter password for your user certificate',
        prompt: {
          model: '',
          type: 'password',
        },
        cancel: true,
        persistent: true,
      })
        .onOk((data: unknown) => {
            const pwd = data as string
            tempPasswords.value.set(userId, pwd)
          console.log(`Entered password: ${pwd}`);
          resolve(pwd);
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
  const promptCertPassword = (user: User, createPass: boolean): Promise<string> => {
    return new Promise<string>((resolve) => {
      if (!user.usePassword) {
        console.log('No pass required');
        resolve(''); //No password required
      } else {
        //Password required, did we stored some already?
        const pwd = tempPasswords.value.get(user.id);
        if (pwd !== undefined) {
          console.log(`Temp pass used ${pwd}`);
          resolve(pwd);
        } else {
          //We will ask user for password
          resolve(passwordDialog(user.id, createPass));
        }
      }
    });
  };

  const resetCertPassword = (user: User): void => {
    tempPasswords.value.set(user.id, undefined)
  }

  return { resetCertPassword, promptCertPassword };
}
