import { User, UploadRequest, UploadResponse } from 'components/models';
import { api } from 'boot/axios';
import usePasswordAPI from './password-api';
import { AxiosResponse } from 'axios';

export default function useFileTransferAPI() {
  const { pwdApiOkHandler, pwdApiErrorHandler, promptCertPassword } =
    usePasswordAPI();

  /**
   * Executest EBICS Admin Ordertype requests like INI, HIA, HPB, SPR,..
   * @param adminOrderType
   * @param pass
   * @returns
   */
  const ebicsUploadRequest = async (
    user: User,
    uploadRequest: UploadRequest,
    uploadFile: Blob
  ): Promise<UploadResponse | undefined> => {
    try {
      uploadRequest.password = await promptCertPassword(user, false);
      console.log(JSON.stringify(uploadRequest));
      const formData = new FormData();
      formData.append(
        'uploadRequest',
        new Blob([JSON.stringify(uploadRequest)], { type: 'application/json' })
      );
      formData.append('uploadFile', uploadFile);
      const response = await api.post<UploadRequest, AxiosResponse<UploadResponse>>(
        `bankconnections/${user.id}/${user.ebicsVersion}/upload`,
        formData,
        { headers: { 'Content-Type': 'multipart/form-data' } }
      );
      console.log('Upload response: ' + JSON.stringify(response))
      pwdApiOkHandler(
        `File uploaded successfully for bank connection: ${user.name}, order number: ${response.data.orderNumber}`
      );
      return response.data;
    } catch (error) {
      pwdApiErrorHandler(user, 'File upload failed: ', error);
    }
  };

  return {
    ebicsUploadRequest,
  };
}
