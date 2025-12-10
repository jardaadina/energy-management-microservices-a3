import apiClient from './apiClient';

const getAllUsers = async () => {
    try {
        const response = await apiClient.get('/users');
        return response.data;
    } catch (error) {
        console.error('Eroare la preluarea userilor:', error.response?.data || error.message);
        throw error;
    }
};

const createUser = async (userData) => {
    try {
        const response = await apiClient.post('/users', userData);
        return response.data;
    } catch (error) {
        console.error('Eroare la crearea userului:', error.response?.data || error.message);
        throw error;
    }
};


const updateUser = async (id, updates) => {
    try {
        const response = await apiClient.put(`/users/${id}`, updates);
        return response.data;
    } catch (error) {
        console.error('Eroare la actualizarea userului:', error.response?.data || error.message);
        throw error;
    }
};

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