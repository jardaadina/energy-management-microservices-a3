import apiClient from './apiClient';

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

const logout = () => {
    localStorage.removeItem('jwtToken');
    window.location.href = '/';
};

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