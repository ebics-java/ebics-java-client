import { useQuasar } from 'quasar';

/**
 * Bank Connections composition API for bank connection list operations with backend REST API
 * @returns
 *  bankConnections synchronized list of bank connections
 *  loadBankConnections function to trigger refreshing of bank connections
 *  deleteBankConnection function to delete bank connection
 */
export default function useDialogs() {
  const q = useQuasar();

  const confirmDialog = (title: string, message: string): Promise<boolean> => {
    return new Promise<boolean>((resolve, reject) => {
      q.dialog({
        title: title,
        message: message,
        cancel: true,
        persistent: true,
      })
        .onOk(() => {
          resolve(true);
        })
        .onCancel(() => {
          reject('Password entry canceled');
        })
        .onDismiss(() => {
          reject('Password entry dismissed');
        });
    });
  };

  return { confirmDialog };
}
