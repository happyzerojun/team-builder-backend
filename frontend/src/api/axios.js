import axios from "axios";

const BASE_URL = "http://52.78.71.42:8080";

export const createPost = async (data) => {
  const response = await axios.post(`${BASE_URL}/posts`, data);
  return response.data;
};