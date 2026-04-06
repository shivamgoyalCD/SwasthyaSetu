/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#0f172a',
        mist: '#f8fafc',
        tide: '#d9f1f2',
        spruce: '#0f766e',
        ember: '#ea580c'
      },
      boxShadow: {
        panel: '0 24px 60px rgba(15, 23, 42, 0.12)'
      },
      fontFamily: {
        sans: ['"Segoe UI Variable"', '"Avenir Next"', '"Trebuchet MS"', 'sans-serif']
      }
    }
  },
  plugins: []
};
