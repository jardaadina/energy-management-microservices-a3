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

        if (response.data && response.data.token) {
            localStorage.setItem('jwtToken', response.data.token);

            return {
                id: response.data.userId.toString(),
                username: response.data.username,
                name: response.data.username,
                role: response.data.role
            };
        }
        return null;
    } catch (error) {
        console.error('Eroare la login:', error.response?.data || error.message);
        throw error;
    }
};

/**
 * Deloghează utilizatorul
 */
const logout = () => {
    localStorage.removeItem('jwtToken');
    window.location.href = '/';
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

        const response = await apiClient.get('/auth/validate');

        if (response.data && response.data.valid) {
            const role = (response.data.authorities[0] || 'ROLE_CLIENT')
                .replace('ROLE_', '');


            return {
                id: response.data.userId,
                username: response.data.username,
                name: response.data.username,
                role: role,
            };
        }
        return null;
    } catch (error) {
        console.error('Validare sesiune eșuată:', error);
        localStorage.removeItem('jwtToken');
        return null;
    }
};

export const authService = {
    login,
    logout,
    validateSession,
};