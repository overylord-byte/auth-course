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
                configure: function (proxy) {
                    proxy.on("proxyReq", function (proxyReq, req) {
                        console.log("[vite proxy request]", {
                            method: req.method,
                            url: req.url,
                            target: "".concat(proxyReq.protocol, "//").concat(proxyReq.host).concat(proxyReq.path),
                        });
                    });
                    proxy.on("proxyRes", function (proxyRes, req) {
                        console.log("[vite proxy response]", {
                            method: req.method,
                            url: req.url,
                            statusCode: proxyRes.statusCode,
                            statusMessage: proxyRes.statusMessage,
                        });
                    });
                    proxy.on("error", function (error, req) {
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
