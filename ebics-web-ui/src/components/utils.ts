import { AxiosError } from 'axios';

export function isAxiosError<T>(error: unknown): error is AxiosError<T> {
  return (error as AxiosError).isAxiosError !== undefined;
}

export class CustomMap<K, V> extends Map<K, V> {
  constructor() {
    super();
  }

  /**
   * @returns the value for given @param key from map
   * If no such element for given @param key exist, 
   * new element with @param defaultValue is stored to map
   */
  getOrAdd(key: K, defaultValue: V):V {
    const value = super.get(key);
    if (value)
        return value;
    else {
        super.set(key, defaultValue);
        return defaultValue;
    }
  }
}

/**
 * Execute all input @param promisses 
 * @param parallelExecution if true then are executed paralled, otherwise serial
 */
export async function promiseAllSettled<T>(promisses: Promise<T>[], parallelExecution = true) {
  if (parallelExecution) {
    return Promise.allSettled(promisses)
  } else {
    for (const promisse of promisses) {
      await promisse
    }
  }
}
