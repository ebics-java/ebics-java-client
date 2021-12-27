import { ref, onMounted } from 'vue';
import { BankConnection, BtfService, EbicsVersion } from 'components/models';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';

export enum TraceType {
  EbicsEnvelope = 'EbicsEnvelope',
  Content = 'Content',
}

export interface OrderTypeDefinition {
  adminOrderType: string,
  ebicsServiceType?: BtfService,
  businessOrderType?: string,
}

export interface TraceEntry {
  id: number,
  messageBody: string,
  user: BankConnection,
  creator: string,
  dateTime: Date, 
  sessionId: string,
  orderNumber?: string,
  ebicsVesion: EbicsVersion,
  upload: boolean,
  traceType: TraceType,
  orderType: OrderTypeDefinition,
}

export default function useTracesAPI() {
  const { apiErrorHandler } = useBaseAPI();

  const traces = ref<TraceEntry[]>();

  const loadTraces = async (): Promise<void> => {
    try {
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
