import { ref, onMounted } from 'vue';
import { BankConnection } from 'components/models';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';

export interface TraceEntry {
  id: number,
  messageBody: string,
  user: BankConnection,
  creator: string,
  dateTime: Date, 
}

export default function useTracesAPI() {
  const { apiErrorHandler } = useBaseAPI();

  const traces = ref<TraceEntry[]>();

  const loadTraces = async (): Promise<void> => {
    try {
      //console.info('api: ' + JSON.stringify(api));
      const response = await api.get<TraceEntry[]>('traces');
      traces.value = response.data;
    } catch (error) {
      apiErrorHandler('Loading of traces failed', error);
    }
  };

  onMounted(loadTraces);

  return {
    traces,
  };
}
