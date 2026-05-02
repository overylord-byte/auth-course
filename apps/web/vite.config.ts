import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:3001",
        changeOrigin: true,
        secure: false,
        configure: (proxy) => {
          proxy.on("proxyReq", (proxyReq, req) => {
            console.log("[vite proxy request]", {
              method: req.method,
              url: req.url,
              target: `${proxyReq.protocol}//${proxyReq.host}${proxyReq.path}`,
            });
          });

          proxy.on("proxyRes", (proxyRes, req) => {
            console.log("[vite proxy response]", {
              method: req.method,
              url: req.url,
              statusCode: proxyRes.statusCode,
              statusMessage: proxyRes.statusMessage,
            });
          });

          proxy.on("error", (error, req) => {
            console.error("[vite proxy error]", {
              method: req.method,
              url: req.url,
              message: error.message,
            });
          });
        },
      },
    },
  },
});
