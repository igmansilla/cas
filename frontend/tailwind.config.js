/** @type {import('tailwindcss').Config} */
import { casColors } from './src/themeColors'; // Adjusted for ES module

export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#C41E3A', // Deep red from the logo
          dark: '#A01830',
          light: '#D63B54',
        },
        secondary: {
          DEFAULT: '#FF6B1A', // Orange from the logo
          dark: '#E55E15',
          light: '#FF8142',
        },
        'cas-orange': casColors.orange,
        'cas-red': casColors.red,
        'cas-black': casColors.black,
        'cas-white': casColors.white,
      },
    },
  },
  plugins: [],
};