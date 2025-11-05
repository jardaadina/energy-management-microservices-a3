// src/services/apiClient.js
import axios from 'axios';

const apiClient = axios.create({
    baseURL: 'http://localhost' // Traefik pe portul 80
});

// Interceptor pentru REQUEST (adaugă token-ul)
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

// Interceptor pentru RESPONSE (gestionează erorile 401/403)
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            // Token invalid, expirat sau permisiuni insuficiente
            console.error('Authentication error:', error.response.status, error.response.data);
            localStorage.removeItem('jwtToken');
            // Reîncărcăm aplicația pentru a trimite utilizatorul la pagina de login
            window.location.href = '/';
        }
        return Promise.reject(error);
    }
);

export default apiClient;