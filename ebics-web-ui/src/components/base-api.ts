import { EbicsApiError } from 'components/models';
import { useQuasar } from 'quasar';
import { AxiosError } from 'axios';

function isAxiosError<T>(error: unknown): error is AxiosError<T> {
  return (error as AxiosError).isAxiosError !== undefined;
}

export default function useBaseAPI() {
  const q = useQuasar();

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
   * @param apiErrorCallback optional callback overload handling of business error if needed
   */
  const apiErrorHandler = (
    msg: string,
    error: unknown,
    apiErrorCallback: undefined | ((errorMessage: string) => void) = undefined
  ): void => {
    console.log(JSON.stringify(error));
    if (isAxiosError<EbicsApiError>(error)) {
      if (error.response !== null) {
        const ebicsApiError = error.response?.data as EbicsApiError;
        if (apiErrorCallback) apiErrorCallback(ebicsApiError.message);
        let message = ebicsApiError.message;
        if (ebicsApiError.description && !ebicsApiError.description.includes(message))
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

  return {
    apiOkHandler,
    apiErrorHandler,
  };
}
