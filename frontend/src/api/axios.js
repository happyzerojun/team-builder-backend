import axios from "axios";

const BASE_URL = "http://localhost:8080"; // 추후 변경

export const createPost = async (data) => {
  const response = await axios.post(`${BASE_URL}/posts`, data);
  return response.data;
};