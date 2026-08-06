import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { ConfigProvider } from "antd";
import trTR from "antd/locale/tr_TR";

import "./index.css";
import "antd/dist/reset.css";

import { AuthProvider } from "./context/AuthContext";
import AppRouter from "./routes/AppRouter";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ConfigProvider locale={trTR}>
      <AuthProvider>
        <AppRouter />
      </AuthProvider>
    </ConfigProvider>
  </StrictMode>
);