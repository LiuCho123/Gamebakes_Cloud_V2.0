import { useMsal } from "@azure/msal-react";
import axios from "axios";
import { loginRequest } from "./authConfig.js";

export const useAxios = () => {
    const { instance, accounts } = useMsal();

    const axiosInstance = axios.create({
        baseURL: import.meta.env.VITE_API_URL
    });

    axiosInstance.interceptors.request.use(
        async (config) => {
            if (accounts.length > 0) {
                try {
                    const response = await instance.acquireTokenSilent({
                        ...loginRequest,
                        account: accounts[0]
                    });
                    config.headers.Authorization = `Bearer ${response.accessToken}`;
                } catch (error) {
                    console.error("Error obteniendo el token de MSAL: ", error);
                    instance.loginPopup(loginRequest);
                }
            }
            else {
                const cognitoToken = sessionStorage.getItem('token');
                if (cognitoToken) {
                    config.headers.Authorization = `Bearer ${cognitoToken}`;
                }
            }
            return config;
        },
        (error) => {
            return Promise.reject(error);
        }
    );
    return axiosInstance;
}