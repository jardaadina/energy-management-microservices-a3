// src/services/authService.js
import apiClient from './apiClient';

/**
 * @typedef {object} User
 * @property {string} id
 * @property {string} username
 * @property {string} name
 * @property {'ADMIN' | 'CLIENT'} role
 */

/**
 * Loghează utilizatorul și stochează token-ul
 * @param {string} username
 * @param {string} password
 * @returns {Promise<User | null>} Obiectul User
 */
const login = async (username, password) => {
    try {
        const response = await apiClient.post('/auth/login', {
            username,
            password,
        });

        // Backend-ul returnează: { token, type, username, role, userId }
        if (response.data && response.data.token) {
            localStorage.setItem('jwtToken', response.data.token);

            // Construim obiectul User din răspuns
            return {
                id: response.data.userId.toString(),
                username: response.data.username,
                name: response.data.username, // Folosim username ca name
                role: response.data.role // "ADMIN" sau "CLIENT"
            };
        }
        return null;
    } catch (error) {
        console.error('Eroare la login:', error.response?.data || error.message);
        throw error; // Aruncăm eroarea ca să o prindă componenta
    }
};

/**
 * Deloghează utilizatorul
 */
const logout = () => {
    localStorage.removeItem('jwtToken');
    window.location.href = '/'; // Reîncarcă și trimite la login
};

/**
 * Verifică token-ul din localStorage la încărcarea aplicației
 * @returns {Promise<User | null>} Obiectul User dacă token-ul e valid
 */
const validateSession = async () => {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        return null;
    }

    try {
        // Apelăm /auth/validate
        const response = await apiClient.get('/auth/validate');

        // Răspunsul conține { valid: true, userId, username, authorities: ["ROLE_..."] }
        if (response.data && response.data.valid) {
            const role = (response.data.authorities[0] || 'ROLE_CLIENT')
                .replace('ROLE_', ''); // Rezultat: "ADMIN" or "CLIENT"

            // Reconstruim obiectul User
            return {
                id: response.data.userId,
                username: response.data.username,
                name: response.data.username, // Folosim username ca name, pt că /validate nu-l returnează
                role: role, // "ADMIN" sau "CLIENT"
            };
        }
        return null;
    } catch (error) {
        console.error('Validare sesiune eșuată:', error);
        localStorage.removeItem('jwtToken'); // Curățăm token-ul invalid
        return null;
    }
};

export const authService = {
    login,
    logout,
    validateSession,
};