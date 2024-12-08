import { useState, useEffect } from 'react';

interface StorageState<T> {
  value: T;
  lastSynced: Date | null;
  error: Error | null;
}

const isStorageAvailable = () => {
  try {
    const test = '__storage_test__';
    localStorage.setItem(test, test);
    localStorage.removeItem(test);
    return true;
  } catch (e) {
    return false;
  }
};

export function useLocalStorage<T>(
  key: string,
  initialValue: T
): [T, (value: T | ((prev: T) => T)) => void, { lastSynced: Date | null; error: Error | null }] {
  const [state, setState] = useState<StorageState<T>>(() => {
    if (!isStorageAvailable()) {
      return {
        value: initialValue,
        lastSynced: null,
        error: new Error('localStorage is not available'),
      };
    }

    try {
      const item = window.localStorage.getItem(key);
      if (!item) {
        return {
          value: initialValue,
          lastSynced: null,
          error: null,
        };
      }

      try {
        const parsedItem = JSON.parse(item);
        return {
          value: parsedItem,
          lastSynced: new Date(),
          error: null,
        };
      } catch (parseError) {
        // If parsing fails, remove the invalid item
        window.localStorage.removeItem(key);
        return {
          value: initialValue,
          lastSynced: null,
          error: new Error('Invalid stored data'),
        };
      }
    } catch (error) {
      return {
        value: initialValue,
        lastSynced: null,
        error: error instanceof Error ? error : new Error(String(error)),
      };
    }
  });

  useEffect(() => {
    if (!isStorageAvailable()) {
      return;
    }

    try {
      const serializedValue = JSON.stringify(state.value);
      window.localStorage.setItem(key, serializedValue);
      setState(prev => ({
        ...prev,
        lastSynced: new Date(),
        error: null,
      }));
    } catch (error) {
      console.error('Error writing to localStorage:', error);
      setState(prev => ({
        ...prev,
        error: error instanceof Error ? error : new Error(String(error)),
      }));
    }
  }, [key, state.value]);

  const setValue = (value: T | ((prev: T) => T)) => {
    setState(prev => ({
      ...prev,
      value: value instanceof Function ? value(prev.value) : value,
    }));
  };

  return [state.value, setValue, { lastSynced: state.lastSynced, error: state.error }];
}