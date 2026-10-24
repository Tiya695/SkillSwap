/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['"Plus Jakarta Sans"', 'system-ui', 'sans-serif'],
      },
      colors: {
        // Light lavender and white, like the reference page
        brand: {
          50: '#F8F6FF',
          100: '#EFEBFF',
          200: '#E0D9FF',
          300: '#CBBFFF',
          400: '#A794FF',
          DEFAULT: '#6247E8',
          600: '#5237D3',
          700: '#432BB0',
        },
        ink: { DEFAULT: '#15122E', soft: '#2B2750' },
        canvas: '#FAF9FF',
        surface: '#F3F0FF',
        line: '#E6E1FA',
        muted: '#6E6A8A',
        status: {
          pending: '#B7791F',
          accepted: '#0369A1',
          completed: '#15803D',
          rejected: '#B91C1C',
          cancelled: '#6B7280',
        },
      },
      borderRadius: {
        card: '22px',
      },
      boxShadow: {
        soft: '0 1px 2px rgba(60,40,140,0.05), 0 10px 28px rgba(80,60,180,0.08)',
        float: '0 18px 50px rgba(70,50,170,0.18)',
        glow: '0 10px 26px -8px rgba(98,71,232,0.55)',
      },
      letterSpacing: {
        tightest: '-0.035em',
      },
      keyframes: {
        'fade-up': {
          '0%': { opacity: '0', transform: 'translateY(14px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0) translateZ(var(--z, 0px))' },
          '50%': { transform: 'translateY(-12px) translateZ(var(--z, 0px))' },
        },
        drift: {
          '0%, 100%': { transform: 'translate3d(0,0,0) scale(1)' },
          '50%': { transform: 'translate3d(16px,-14px,0) scale(1.06)' },
        },
      },
      animation: {
        'fade-up': 'fade-up .55s ease-out both',
        float: 'float 6s ease-in-out infinite',
        drift: 'drift 9s ease-in-out infinite',
      },
    },
  },
  plugins: [],
}
