import apiClient from './apiClient';

/**
 * Send a chat message to customer support (REST fallback)
 */
const sendChatMessage = async (userId, message) => {
    try {
        const response = await apiClient.post('/support/chat', {
            userId,
            message,
            timestamp: new Date().toISOString()
        });
        return response.data;
    } catch (error) {
        console.error('Error sending chat message:', error.response?.data || error.message);
        throw error;
    }
};

/**
 * Get chatbot statistics (admin only)
 */
const getChatbotStats = async () => {
    try {
        const response = await apiClient.get('/support/stats');
        return response.data;
    } catch (error) {
        console.error('Error fetching chatbot stats:', error);
        throw error;
    }
};

/**
 * Get all chatbot rules (admin only)
 */
const getChatbotRules = async () => {
    try {
        const response = await apiClient.get('/support/rules');
        return response.data;
    } catch (error) {
        console.error('Error fetching chatbot rules:', error);
        throw error;
    }
};

/**
 * Admin sends message to user
 */
const adminSendMessage = async (adminId, userId, message) => {
    try {
        const response = await apiClient.post('/support/admin/send', {
            adminId,
            userId,
            message,
            timestamp: new Date().toISOString()
        });
        return response.data;
    } catch (error) {
        console.error('Error sending admin message:', error);
        throw error;
    }
};

export const chatService = {
    sendChatMessage,
    getChatbotStats,
    getChatbotRules,
    adminSendMessage
};