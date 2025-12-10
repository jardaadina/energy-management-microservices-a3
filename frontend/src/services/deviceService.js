import apiClient from './apiClient.js';

const getMyDevices = async () => {
    try {
        const response = await apiClient.get('/user-devices/my-devices');
        return response.data;
    } catch (error) {
        console.error('Eroare la preluarea "my-devices":', error.response?.data || error.message);
        throw error;
    }
};

const getAllDevices = async () => {
    try {
        const response = await apiClient.get('/devices');
        return response.data;
    } catch (error) {
        console.error('Eroare la preluarea dispozitivelor:', error.response?.data || error.message);
        throw error;
    }
};

const createDevice = async (deviceData) => {
    const response = await apiClient.post('/devices', deviceData);
    return response.data;
};

const updateDevice = async (id, updates) => {
    const response = await apiClient.put(`/devices/${id}`, updates);
    return response.data;
};


const deleteDevice = async (id) => {
    await apiClient.delete(`/devices/${id}`);
};

const getDevicesForUser = async (userId) => {
    try {
        // Asigură-te că ID-ul este trimis ca număr în URL
        const response = await apiClient.get(`/user-devices/user/${Number(userId)}`);
        return response.data;
    } catch (error) {
        console.error('Eroare la getDevicesForUser:', error.response?.data || error.message);
        throw error;
    }
};


const assignDevice = async (userId, deviceId) => {
    try {
        await apiClient.post('/user-devices/assign', {
            userId: Number(userId),
            deviceId: Number(deviceId)
        });
    } catch (error) {
        console.error('Eroare la assignDevice:', error.response?.data || error.message);
        throw error;
    }
};


const unassignDevice = async (userId, deviceId) => {
    try {
        // Metoda delete din axios trimite body-ul în câmpul 'data'
        await apiClient.delete('/user-devices/unassign', {
            data: {
                userId: Number(userId),
                deviceId: Number(deviceId)
            }
        });
    } catch (error) {
        console.error('Eroare la unassignDevice:', error.response?.data || error.message);
        throw error;
    }
};

export const deviceService = {
    getMyDevices,
    getAllDevices,
    createDevice,
    updateDevice,
    deleteDevice,
    assignDevice,
    unassignDevice,
    getDevicesForUser,
};