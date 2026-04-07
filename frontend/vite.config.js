import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import fs from "fs";
import path from "path";

export default defineConfig({
  plugins: [
    react(),
    {
      name: "copy-redirects",
      writeBundle() {
        fs.copyFileSync(
          path.resolve(__dirname, "public/_redirects"),
          path.resolve(__dirname, "dist/_redirects")
        );
        console.log("_redirects copied to dist/");
      },
    },
  ],
  server: {
    proxy: {
      "/api": "http://165.232.136.214:8080",
    },
  },
});