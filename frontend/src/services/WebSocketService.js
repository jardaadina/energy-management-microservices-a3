import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = 'http://localhost/ws';

class WebSocketService {
    constructor() {
        this.client = null;
        this.connected = false;
        this.subscriptions = [];
        this.userId = null;
        this.adminStore = {
            messages: new Map(),
            activeUsers: new Map(),
            selectedUserId: null
        };
    }



    connect(userId, onMessageReceived, onAlertReceived) {
        if (this.connected && this.userId === userId) {
            console.log('WebSocket already connected for user:', userId);
            return;
        }

        if (this.connected && this.userId !== userId) {
            this.disconnect();
        }

        this.userId = userId;
        console.log('=== CONNECTING WEBSOCKET FOR USER:', userId, '===');

        const socket = new SockJS(WS_URL);

        this.client = new Client({
            webSocketFactory: () => socket,
            debug: (str) => console.log('STOMP:', str),
            reconnectDelay: 5000,
        });

        this.client.onConnect = () => {
            console.log('=== WebSocket Connected! ===');
            this.connected = true;

            const chatPath = `/topic/user/${userId}/messages`;
            console.log(`Subscribing to: ${chatPath}`);

            this.subscriptions.push(
                this.client.subscribe(chatPath, (message) => {
                    try {
                        const parsed = JSON.parse(message.body);
                        console.log('MSG RECEIVED:', parsed);
                        if (onMessageReceived) onMessageReceived(parsed);
                    } catch (e) {
                        console.error('Error parsing chat msg:', e);
                    }
                })
            );

            const alertPath = `/topic/user/${userId}/alerts`;
            console.log(`Subscribing to: ${alertPath}`);

            this.subscriptions.push(
                this.client.subscribe(alertPath, (message) => {
                    try {
                        const parsed = JSON.parse(message.body);
                        console.log('ALERT RECEIVED:', parsed);
                        if (onAlertReceived) onAlertReceived(parsed);
                    } catch (e) {
                        console.error('Error parsing alert:', e);
                    }
                })
            );

            this.sendJoinNotification(userId);
        };

        this.client.onStompError = (frame) => {
            console.error('STOMP error:', frame);
            this.connected = false;
        };

        this.client.activate();
    }

    sendMessage(senderId, recipientId, content, role = 'USER') {
        if (!this.connected || !this.client) {
            console.error('Cannot send: WebSocket disconnected');
            return false;
        }

        const message = {
            messageId: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
            senderId,
            recipientId: recipientId || 'support-bot',
            content,
            senderRole: role,
            timestamp: new Date().toISOString(),
            type: 'CHAT'
        };

        this.client.publish({
            destination: '/app/chat.send',
            body: JSON.stringify(message)
        });
        return true;
    }

    sendJoinNotification(userId) {
        if (!this.connected) return;
        this.client.publish({
            destination: '/app/chat.join',
            body: JSON.stringify({
                senderId: userId,
                content: `User ${userId} joined`,
                type: 'JOIN'
            })
        });
    }

    disconnect() {
        if (this.client) {
            this.subscriptions.forEach(sub => sub.unsubscribe());
            this.subscriptions = [];
            this.client.deactivate();
            this.connected = false;
            this.userId = null;
        }
    }
}

export default new WebSocketService();