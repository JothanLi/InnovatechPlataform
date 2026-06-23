import axios from "axios";

const bffApi = axios.create({
  baseURL: import.meta.env.VITE_BFF_API_URL || "http://localhost:8090/api/v1/bff",
});

export default bffApi;