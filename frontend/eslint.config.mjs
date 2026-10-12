import { defineConfig, globalIgnores } from "eslint/config"
import nextVitals from "eslint-config-next/core-web-vitals"

export default defineConfig([
  ...nextVitals,

  {
    rules: {
      // JSX text doesn't need HTML entity escaping for ordinary apostrophes.
      "react/no-unescaped-entities": "off",

      // We intentionally use effects to synchronize application state.
      "react-hooks/set-state-in-effect": "off",

      // blocked it lol.
      "react-hooks/purity": "off",

      // Native <img> is acceptable where next/image isn't appropriate.
      "@next/next/no-img-element": "off",
    },
  },

  globalIgnores([
    ".next/**",
    "out/**",
    "build/**",
    "next-env.d.ts",
  ]),
])