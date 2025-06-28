import React from 'react';

export function Logo() {
  return (
    <div className="relative w-10 h-10">
      <div className="absolute inset-0 rounded-full bg-primary flex items-center justify-center">
        <svg
          viewBox="0 0 100 100"
          className="w-8 h-8 text-secondary"
          fill="currentColor"
        >
          <circle cx="50" cy="50" r="45" fill="currentColor"/>
          <path
            d="M50 15 L80 75 L50 60 L20 75 Z"
            fill="#C41E3A"
            className="transform origin-center"
          />
        </svg>
      </div>
    </div>
  );
}