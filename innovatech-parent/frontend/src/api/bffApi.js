import axios from "axios";

const bffApi = axios.create({
  baseURL: "http://localhost:8090/api/v1/bff",
});

export default bffApi;