import axios from "axios";

const gatewayBaseUrl = import.meta.env.VITE_GATEWAY_API_URL || "http://localhost:8090/api/v1";

const bffApi = axios.create({
  baseURL: `${gatewayBaseUrl}/bff`,
});

export const authApi = axios.create({
  baseURL: `${gatewayBaseUrl}/auth`,
});

bffApi.interceptors.request.use((config) => {
  const token = localStorage.getItem("innovatech_token");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

export default bffApi;
