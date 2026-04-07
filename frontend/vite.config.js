import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { resolve } from "path";
import { copyFileSync } from "fs";

export default defineConfig({
  plugins: [
    react(),
    {
      name: "copy-redirects",
      closeBundle() {
        copyFileSync(
          resolve(__dirname, "public/_redirects"),
          resolve(__dirname, "dist/_redirects")
        );
      },
    },
  ],
  server: {
    proxy: {
      "/api": "http://165.232.136.214:8080",
    },
  },
});