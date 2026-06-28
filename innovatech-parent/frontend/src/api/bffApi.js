import axios from "axios";

const gatewayBaseURL =
  import.meta.env.VITE_GATEWAY_API_URL || "http://localhost:8090/api/v1";

const api = axios.create({
  baseURL: `${gatewayBaseURL}/bff`,
});

export const authApi = axios.create({
  baseURL: `${gatewayBaseURL}/auth`,
});

api.interceptors.request.use(
  (config) => {
    const token =
      localStorage.getItem("innovatech_token") ||
      localStorage.getItem("accessToken");

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("innovatech_token");
      localStorage.removeItem("innovatech_username");
      localStorage.removeItem("innovatech_roles");

      localStorage.removeItem("accessToken");
      localStorage.removeItem("tokenType");
      localStorage.removeItem("username");
      localStorage.removeItem("roles");

      window.location.href = "/login";
    }

    return Promise.reject(error);
  }
);

export default api;