import apiClient from './apiClient';

/**
 * Preia TOȚI userii (doar admin)
 * @returns {Promise<any[]>}
 */
const getAllUsers = async () => {
    try {
        const response = await apiClient.get('/users');
        return response.data;
    } catch (error) {
        console.error('Eroare la preluarea userilor:', error.response?.data || error.message);
        throw error;
    }
};

/**
 * Creează un user nou (doar admin)
 * @param {object} userData
 * @returns {Promise<any>}
 */
const createUser = async (userData) => {
    try {
        const response = await apiClient.post('/users', userData);
        return response.data;
    } catch (error) {
        console.error('Eroare la crearea userului:', error.response?.data || error.message);
        throw error;
    }
};

/**
 * Actualizează un user (doar admin)
 * @param {string} id
 * @param {object} updates
 * @returns {Promise<any>}
 */
const updateUser = async (id, updates) => {
    try {
        const response = await apiClient.put(`/users/${id}`, updates);
        return response.data;
    } catch (error) {
        console.error('Eroare la actualizarea userului:', error.response?.data || error.message);
        throw error;
    }
};

/**
 * Șterge un user (doar admin)
 * @param {string} id
 * @returns {Promise<void>}
 */
const deleteUser = async (id) => {
    try {
        await apiClient.delete(`/users/${id}`);
    } catch (error) {
        console.error('Eroare la ștergerea userului:', error.response?.data || error.message);
        throw error;
    }
};

export const userService = {
    getAllUsers,
    createUser,
    updateUser,
    deleteUser,
};