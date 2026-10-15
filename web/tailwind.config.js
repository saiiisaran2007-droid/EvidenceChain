/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        navy: {
          950: '#0b0f19', // Root app background
          900: '#0f172a', // Sidebar and primary cards
          850: '#131c31', // Header and elevated panels
          800: '#1e293b', // Borders and dividers
          700: '#334155', // Hover states
          600: '#475569',
        },
        cyan: {
          brand: '#06b6d4',
          glow: 'rgba(6, 182, 212, 0.15)',
          border: 'rgba(6, 182, 212, 0.3)',
        },
        slate: {
          950: '#020617',
          900: '#0f172a',
          800: '#1e293b',
          700: '#334155',
          400: '#94a3b8',
          300: '#cbd5e1',
          100: '#f1f5f9',
          50: '#f8fafc',
        }
      },
      fontFamily: {
        mono: ['ui-monospace', 'SFMono-Regular', 'Menlo', 'Monaco', 'Consolas', '"Liberation Mono"', '"Courier New"', 'monospace'],
        sans: ['"Inter"', 'system-ui', '-apple-system', 'BlinkMacSystemFont', '"Segoe UI"', 'Roboto', 'sans-serif'],
      },
      boxShadow: {
        'cyan-glow': '0 0 15px -3px rgba(6, 182, 212, 0.25), 0 0 6px -2px rgba(6, 182, 212, 0.2)',
        'blue-glow': '0 0 15px -3px rgba(59, 130, 246, 0.25)',
        'emerald-glow': '0 0 15px -3px rgba(16, 185, 129, 0.25)',
        'red-glow': '0 0 15px -3px rgba(239, 68, 68, 0.25)',
      }
    },
  },
  plugins: [],
}
