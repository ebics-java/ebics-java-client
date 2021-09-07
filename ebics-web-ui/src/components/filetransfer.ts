import { User, UploadRequest, UploadResponse, DownloadRequest, UserPassword, BTFType } from 'components/models';
import { api } from 'boot/axios';
import usePasswordAPI from './password-api';
import { AxiosResponse } from 'axios';

export default function useFileTransferAPI() {
  const { pwdApiOkHandler, pwdApiErrorHandler, promptCertPassword } =
    usePasswordAPI();

  /**
   * Executest EBICS upload file request
   * @param adminOrderType
   * @param pass
   * @returns
   */
  const ebicsUploadRequest = async (
    user: User,
    uploadRequest: UploadRequest,
    uploadFile: Blob,
    ebicsVersion: string = user.ebicsVersion,
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
        `bankconnections/${user.id}/${ebicsVersion}/upload`,
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

  /**
   * Executest EBICS download file request
   * @param adminOrderType
   * @param pass
   * @returns
   */
     const ebicsDownloadRequest = async (
      user: User,
      downloadRequest: DownloadRequest,
      ebicsVersion: string = user.ebicsVersion,
    ): Promise<string | undefined> => {
      try {
        downloadRequest.password = await promptCertPassword(user, false);
        console.log(JSON.stringify(downloadRequest));
        const response = await api.post<DownloadRequest, AxiosResponse<string>>(
          `bankconnections/${user.id}/${ebicsVersion}/download`,
          downloadRequest,
        );
        console.log('Download response: ' + JSON.stringify(response))
        pwdApiOkHandler(
          `File downloaded successfully for bank connection: ${user.name}`
        );
        return response.data;
      } catch (error) {
        pwdApiErrorHandler(user, 'File download failed: ', error);
      }
    };

    /**
     * Executest EBICS HTD request in order to get avaialable order types  
     * @param user bank connection for this request
     * @param ebicsVersion ebics version used, if other that from bank connection default
     * @returns
     */
    const ebicsOrderTypes = async (
      user: User,
      ebicsVersion: string = user.ebicsVersion,
    ): Promise<BTFType[]> => {
      try {
        const password = await promptCertPassword(user, false);
        const response = await api.post<UserPassword, AxiosResponse<BTFType[]>>(
          `bankconnections/${user.id}/${ebicsVersion}/orderTypes`,
          {password: password},
        );
        console.log('Download order-types response: ' + JSON.stringify(response))
        pwdApiOkHandler(
          `Order types downloaded successfully for bank connection: ${user.name}`
        );
        return response.data;
      } catch (error) {
        pwdApiErrorHandler(user, 'File download failed: ', error);
        return [];
      }
    };

  return {
    ebicsUploadRequest, ebicsDownloadRequest, ebicsOrderTypes,
  };
}
