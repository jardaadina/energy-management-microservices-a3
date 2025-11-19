import axios from 'axios';

const apiClient = axios.create({
    baseURL: 'http://localhost'
});

apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('jwtToken');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            console.error('Authentication error:', error.response.status, error.response.data);
            localStorage.removeItem('jwtToken');
            window.location.href = '/';
        }
        return Promise.reject(error);
    }
);

export default apiClient;