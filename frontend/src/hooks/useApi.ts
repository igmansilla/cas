import { useState, useCallback } from 'react';
import type { ApiResponse } from '../types/api';
import { ApiError } from '../services/api';

interface UseApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

interface UseApiReturn<T, TArgs extends readonly unknown[] = unknown[]> extends UseApiState<T> {
  execute: (...args: TArgs) => Promise<ApiResponse<T> | null>;
  reset: () => void;
}

/**
 * Hook personalizado para manejar llamadas a la API con estado
 * @param apiFunction - Función de la API a ejecutar
 * @returns Objeto con data, loading, error, execute y reset
 */
export function useApi<T, TArgs extends readonly unknown[] = unknown[]>(
  apiFunction: (...args: TArgs) => Promise<ApiResponse<T>>
): UseApiReturn<T, TArgs> {
  const [state, setState] = useState<UseApiState<T>>({
    data: null,
    loading: false,
    error: null,
  });  const execute = useCallback(
    async (...args: TArgs): Promise<ApiResponse<T> | null> => {
      setState(prev => ({ ...prev, loading: true, error: null }));
      
      try {
        const response = await apiFunction(...args);
        setState({
          data: response.data || null,
          loading: false,
          error: null,
        });
        return response;
      } catch (error) {
        const errorMessage = error instanceof ApiError 
          ? error.message 
          : 'An unexpected error occurred';
        
        setState({
          data: null,
          loading: false,
          error: errorMessage,
        });
        
        console.error('API Error:', error);
        return null;
      }
    },
    [apiFunction]
  );

  const reset = useCallback(() => {
    setState({
      data: null,
      loading: false,
      error: null,
    });
  }, []);

  return {
    ...state,
    execute,
    reset,
  };
}

/**
 * Hook especializado para manejar listas con paginación
 */
export function useApiList<T>(
  apiFunction: () => Promise<ApiResponse<T[]>>
) {
  const [items, setItems] = useState<T[]>([]);
  const [pagination, setPagination] = useState({
    page: 1,
    size: 10,
    total: 0,
    totalPages: 0,
  });

  const baseApi = useApi(apiFunction);
  const execute = useCallback(
    async () => {
      const response = await baseApi.execute();
      
      if (response?.success && response.data) {
        setItems(Array.isArray(response.data) ? response.data : []);
        if (response.pagination) {
          setPagination(response.pagination);
        }
      }
      
      return response;
    },
    [baseApi]
  );

  const reset = useCallback(() => {
    baseApi.reset();
    setItems([]);
    setPagination({
      page: 1,
      size: 10,
      total: 0,
      totalPages: 0,
    });
  }, [baseApi]);

  return {
    ...baseApi,
    items,
    pagination,
    execute,
    reset,
  };
}

export default useApi;
