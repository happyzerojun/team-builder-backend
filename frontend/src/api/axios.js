import axios from "axios";

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export const createPost = async (data) => {
  const response = await axios.post(`${BASE_URL}/posts`, data);
  return response.data;
};