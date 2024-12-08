import React from 'react';
import { CheckCircle2, CloudOff } from 'lucide-react';

interface SyncStatusProps {
  lastSynced: Date | null;
  error: Error | null;
}

export function SyncStatus({ lastSynced, error }: SyncStatusProps) {
  if (error) {
    return (
      <div className="flex items-center gap-2 text-primary">
        <CloudOff className="w-4 h-4" />
        <span className="text-sm">Error al guardar cambios</span>
      </div>
    );
  }

  if (!lastSynced) {
    return null;
  }

  return (
    <div className="flex items-center gap-2 text-gray-500">
      <CheckCircle2 className="w-4 h-4 text-secondary" />
      <span className="text-sm">
        Guardado {lastSynced.toLocaleTimeString()}
      </span>
    </div>
  );
}