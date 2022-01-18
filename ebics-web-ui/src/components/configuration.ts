import { ref, onMounted, computed } from 'vue';
import { api } from 'boot/axios';
import useBaseAPI from './base-api';

export interface HttpClientGlobalConfiguration {
  connectionPoolMaxTotal: number;
  connectionPoolDefaultMaxPerRoute: number;
  configurations: Map<string, HttpClientConfiguration>;
}

export interface HttpClientConfiguration {
  /**
   * The configuration display name
   */
  displayName: string;
  /**
   * The SSL trusted store, used for establishing connections if needed (usually for no public EBICS servers only).
   */
  sslTrustedStoreFile?: string;
  sslTrustedStoreFilePassword?: string;

  /**
   * HTTP Proxy, with optional technical user/password
   */
  httpProxyHost?: string;
  httpProxyPort?: number;
  httpProxyUser?: string;
  httpProxyPassword?: string;

  /**
   * Timeouts in milli-seconds (or -1)
   * Default suggested value 300.000 = 300s
   */
  socketTimeoutMilliseconds: number;
  connectionTimeoutMilliseconds: number;
}

export interface NamedHttpClientConfiguration {
  /**
   * The configuration name (used for APIs)
   */
  name: string;

  /**
   * The configuration display name
   */
  displayName: string;
}

export default function useConfigurationAPI() {
  const { apiErrorHandler } = useBaseAPI();

  const configuration = ref<HttpClientGlobalConfiguration>();

  const loadConfigurations = async (): Promise<void> => {
    try {
      const response = await api.get<HttpClientGlobalConfiguration>(
        'configuration/httpclient'
      );
      configuration.value = response.data;
    } catch (error) {
      apiErrorHandler('Loading of http configuration failed', error);
    }
  };

  onMounted(loadConfigurations);

  const configurationNames = computed<NamedHttpClientConfiguration[]>(() => {
    const resultArray: NamedHttpClientConfiguration[] = [];
    if (configuration.value) {
      return Object.entries(configuration.value?.configurations).map( conf => {
        return {name: conf[0], displayName: (conf[1] as HttpClientConfiguration).displayName} as NamedHttpClientConfiguration
      });
    }
    return resultArray;
  });

  return {
    configuration, configurationNames,
  };
}
