/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
    "../../packages/ui/src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Roboto", "sans-serif"],
      },
      fontSize: {
        "preset-1": [
          "56px",
          {
            lineHeight: "100%",
            letterSpacing: "0px",
            fontWeight: "700",
          },
        ],
        "preset-1-mobile": [
          "88px",
          {
            lineHeight: "100%",
            letterSpacing: "0px",
            fontWeight: "700",
          },
        ],
        "preset-2": [
          "16px",
          {
            lineHeight: "150%",
            letterSpacing: "0px",
          },
        ],
        "preset-3": [
          "12px",
          { lineHeight: "150%", letterSpacing: "0px", fontWeight: "700" },
        ],
      },
      colors: {
        gray: "#949494",
        "blue-800": "#242742",
        "slate-900": "#133041",
        "gray-700": "#36384D",
        red: "#FF6155",
        "red-100": "#FFE7E6",
        gradient4_from: "#FF6A3A",
        gradient4_to: "#FF527B",
      },
      backgroundImage: {
        "gradient-4": "linear-gradient(to right, #FF6A3A, #FF527B)",
      },
      spacing: {
        100: "8px",
        200: "16px",
        300: "24px",
        400: "32px",
        500: "40px",
        600: "48px",
        700: "60px",
        800: "64px",
        900: "72px",
        1300: "104px",
      },
    },
  },
  plugins: [],
};
