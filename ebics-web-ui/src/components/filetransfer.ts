import {
  User,
  EbicsApiError,
  UploadRequest,
} from 'components/models';
import { useQuasar } from 'quasar';
import { api } from 'boot/axios';
import { isAxiosError } from 'components/utils';
import usePasswordAPI from './password';

export default function useFileTransferAPI() {
  const q = useQuasar();

  const { promptCertPassword, resetCertPassword } = usePasswordAPI();

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
   * @param user the related user 
   * @param msg context message, for example 'user A initialization'
   * @param error REST API call error
   */
  const apiErrorHandler = (user: User, msg: string, error: unknown): void => {
    console.log(JSON.stringify(error));
    if (isAxiosError<EbicsApiError>(error)) {
      if (error.response !== null) {
        const ebicsApiError = error.response?.data as EbicsApiError;
        if (ebicsApiError.message.includes('wrong password')) {
          //In case of error 'wrong password' we have to reset temporary stored password in order to ask for new one
          resetCertPassword(user);
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

 
    /**
   * Executest EBICS Admin Ordertype requests like INI, HIA, HPB, SPR,..
   * @param adminOrderType
   * @param pass
   * @returns
   */
     const ebicsUploadRequest = async (
       user: User,
       uploadRequest: UploadRequest,
       uploadFile: Blob,
    ): Promise<void> => {
      try {
        uploadRequest.password  = await promptCertPassword(user, false);
        console.log(JSON.stringify(uploadRequest));
        const formData = new FormData();
        formData.append('uploadRequest', new Blob([JSON.stringify(uploadRequest)], {type:'application/json'}));
        formData.append('uploadFile', uploadFile);
        await api.post<UploadRequest>(
          `/bankconnections/${user.id}/${user.ebicsVersion}/upload`,
          formData, {headers: {'Content-Type': 'multipart/form-data'}}
        );
        apiOkHandler(
          `Upload executed successfully for user name: ${user.name}`
        );
      } catch (error) {
        apiErrorHandler(user, 'Upload Request failed: ', error);
      }
    };

  return {
    ebicsUploadRequest,
  };
}
