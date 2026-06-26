import axios from "axios";

const API_BASE_URL =
    import.meta.env.VITE_GATEWAY_API_URL ||
    import.meta.env.VITE_API_URL ||
    "http://localhost:8090/api/v1";

const bffApi = axios.create({
    baseURL: `${API_BASE_URL}/bff`,
    headers: {
        "Content-Type": "application/json",
    },
});

export const authApi = axios.create({
    baseURL: `${API_BASE_URL}/auth`,
    headers: {
        "Content-Type": "application/json",
    },
});

bffApi.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("innovatech_token");

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

authApi.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("innovatech_token");

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

export { bffApi };
export default bffApi;