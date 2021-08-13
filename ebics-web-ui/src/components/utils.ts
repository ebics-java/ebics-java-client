import { AxiosError } from 'axios';

export function isAxiosError<T>(error: unknown): error is AxiosError<T> {
    return (error as AxiosError).isAxiosError !== undefined;
}