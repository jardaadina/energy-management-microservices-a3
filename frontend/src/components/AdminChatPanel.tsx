import { useState, useEffect, useRef } from 'react';
import { MessageCircle, Send, User, Shield } from 'lucide-react';
import { toast } from 'sonner';
// @ts-ignore
import webSocketService from '../services/WebSocketService';

type Message = {
    senderId: string;
    recipientId: string;
    content: string;
    timestamp: string;
    senderRole: 'ADMIN' | 'USER' | 'SYSTEM';
    messageId?: string;
};

type ActiveUser = {
    userId: string;
    lastMessage: string;
    unreadCount: number;
    timestamp: string;
};

type AdminChatPanelProps = {
    adminId: string;
    adminName: string;
};

export default function AdminChatPanel({ adminId, adminName }: AdminChatPanelProps) {
    const [activeUsers, setActiveUsers] = useState<Map<string, ActiveUser>>(() => {
        return webSocketService.adminStore?.activeUsers || new Map();
    });

    const [messages, setMessages] = useState<Map<string, Message[]>>(() => {
        return webSocketService.adminStore?.messages || new Map();
    });

    const [selectedUserId, setSelectedUserId] = useState<string | null>(() => {
        return webSocketService.adminStore?.selectedUserId || null;
    });

    const [newMessage, setNewMessage] = useState('');
    const [isConnected, setIsConnected] = useState(false);
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const processedRefs = useRef(new Set<string>());

    const theme = {
        bg: '#ffffff',
        border: '#e2e8f0',
        sidebarBg: '#f8fafc',
        primary: '#f97316',
        primaryHover: '#ea580c',
        myMsgBg: '#fff7ed',
        myMsgBorder: '#fdba74',
        myMsgText: '#9a3412',
        userMsgBg: '#eff6ff',
        userMsgBorder: '#bfdbfe',
        userMsgText: '#1e3a8a',
    };

    useEffect(() => {
        webSocketService.adminStore = { activeUsers, messages, selectedUserId };
    }, [activeUsers, messages, selectedUserId]);

    useEffect(() => {
        console.log('🔧 AdminChatPanel: Mounting...');

        const onMessageReceived = (message: any) => {
            const userId = message.senderRole === 'USER' ? message.senderId : message.recipientId;

            if (message.senderRole === 'ADMIN' && message.senderId === adminId) return;

            const msgTime = new Date(message.timestamp).getTime();
            const msgKey = message.messageId || `${userId}-${message.content}-${Math.floor(msgTime / 1000)}`;

            if (processedRefs.current.has(msgKey)) {
                return;
            }
            processedRefs.current.add(msgKey);

            setMessages(prev => {
                const newMap = new Map(prev);
                const list = newMap.get(userId) || [];
                newMap.set(userId, [...list, {
                    senderId: message.senderId,
                    recipientId: message.recipientId,
                    content: message.content,
                    timestamp: message.timestamp,
                    senderRole: message.senderRole || 'USER',
                    messageId: message.messageId
                }]);
                return newMap;
            });

            setActiveUsers(prev => {
                const newUsers = new Map(prev);
                const existing = newUsers.get(userId);

                if (message.senderRole === 'USER') {
                    newUsers.set(userId, {
                        userId,
                        lastMessage: message.content,
                        unreadCount: selectedUserId === userId ? 0 : (existing?.unreadCount || 0) + 1,
                        timestamp: message.timestamp
                    });

                    if (selectedUserId !== userId) {
                        toast.info(`New message from User ${userId}`);
                    }
                } else if (existing) {
                    newUsers.set(userId, {
                        ...existing,
                        lastMessage: `You: ${message.content}`,
                        timestamp: message.timestamp
                    });
                }
                return newUsers;
            });
        };

        if (webSocketService.connected) webSocketService.disconnect();

        webSocketService.connect(
            'admin',
            onMessageReceived,
            (alert: any) => toast.error(`⚠️ High Consumption: User ${alert.userId}`)
        );
        setIsConnected(true);

        return () => {
        };
    }, [adminId]);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages, selectedUserId]);

    const handleSendMessage = (e?: React.FormEvent) => {
        if (e) e.preventDefault();
        if (!newMessage.trim() || !selectedUserId) return;

        const timestamp = new Date().toISOString();
        const msgId = `local-${Date.now()}`;

        const msgKey = `${selectedUserId}-${newMessage}-${Math.floor(new Date(timestamp).getTime() / 1000)}`;
        processedRefs.current.add(msgKey);

        const message: Message = {
            senderId: adminId,
            recipientId: selectedUserId,
            content: newMessage,
            timestamp: timestamp,
            senderRole: 'ADMIN',
            messageId: msgId
        };

        setMessages(prev => {
            const newMap = new Map(prev);
            const list = newMap.get(selectedUserId) || [];
            newMap.set(selectedUserId, [...list, message]);
            return newMap;
        });

        webSocketService.sendMessage(adminId, selectedUserId, newMessage, 'ADMIN');
        setNewMessage('');
    };

    const handleSelectUser = (userId: string) => {
        setSelectedUserId(userId);
        setActiveUsers(prev => {
            const newMap = new Map(prev);
            const user = newMap.get(userId);
            if (user) {
                user.unreadCount = 0;
                newMap.set(userId, user);
            }
            return newMap;
        });
    };

    const sortedUsers = Array.from(activeUsers.values())
        .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

    const currentMessages = selectedUserId ? (messages.get(selectedUserId) || []) : [];

    return (
        <div style={{
            display: 'flex',
            height: '600px',
            width: '100%',
            backgroundColor: 'white',
            borderRadius: '12px',
            border: `1px solid ${theme.border}`,
            boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
            overflow: 'hidden',
            fontFamily: 'system-ui, sans-serif'
        }}>

            <div style={{
                width: '280px',
                backgroundColor: theme.sidebarBg,
                borderRight: `1px solid ${theme.border}`,
                display: 'flex', flexDirection: 'column'
            }}>
                <div style={{ padding: '20px', borderBottom: `1px solid ${theme.border}`, backgroundColor: 'white' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#334155', fontWeight: 'bold' }}>
                        <Shield size={20} color={theme.primary} />
                        <span>Admin Panel</span>
                    </div>
                    <div style={{ fontSize: '12px', color: isConnected ? '#16a34a' : '#dc2626', marginTop: '4px', fontWeight: '500' }}>
                        {isConnected ? '● Online' : '○ Connecting...'}
                    </div>
                </div>

                <div style={{ flex: 1, overflowY: 'auto', padding: '12px' }}>
                    {sortedUsers.length === 0 ? (
                        <div style={{ textAlign: 'center', padding: '40px 10px', color: '#94a3b8' }}>
                            <MessageCircle size={32} style={{ margin: '0 auto 10px', opacity: 0.3 }} />
                            <p style={{ fontSize: '13px' }}>No active chats</p>
                        </div>
                    ) : (
                        sortedUsers.map(user => (
                            <div
                                key={user.userId}
                                onClick={() => handleSelectUser(user.userId)}
                                style={{
                                    padding: '12px', marginBottom: '8px', borderRadius: '8px', cursor: 'pointer',
                                    backgroundColor: selectedUserId === user.userId ? 'white' : 'transparent',
                                    boxShadow: selectedUserId === user.userId ? '0 2px 4px rgba(0,0,0,0.05)' : 'none',
                                    border: selectedUserId === user.userId ? `1px solid ${theme.primary}` : '1px solid transparent',
                                    transition: 'all 0.2s'
                                }}
                            >
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                                    <span style={{ fontWeight: '600', fontSize: '14px', color: '#334155' }}>User {user.userId}</span>
                                    {user.unreadCount > 0 && (
                                        <span style={{ backgroundColor: '#ef4444', color: 'white', fontSize: '10px', padding: '2px 6px', borderRadius: '10px', fontWeight: 'bold' }}>
                                            {user.unreadCount}
                                        </span>
                                    )}
                                </div>
                                <div style={{ fontSize: '12px', color: '#64748b', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                    {user.lastMessage}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', backgroundColor: 'white' }}>
                {selectedUserId ? (
                    <>
                        <div style={{
                            padding: '16px 24px', borderBottom: `1px solid ${theme.border}`,
                            display: 'flex', alignItems: 'center', gap: '12px', flexShrink: 0
                        }}>
                            <div style={{ width: '36px', height: '36px', borderRadius: '50%', backgroundColor: '#f1f5f9', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                <User size={18} color="#64748b" />
                            </div>
                            <div>
                                <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 'bold', color: '#1e293b' }}>User {selectedUserId}</h3>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '12px', color: '#16a34a' }}>
                                    <div style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: '#16a34a' }} />
                                    Active
                                </div>
                            </div>
                        </div>

                        <div style={{
                            flex: 1,
                            overflowY: 'auto',
                            padding: '24px',
                            display: 'flex', flexDirection: 'column', gap: '12px',
                            backgroundColor: '#fafafa',
                            minHeight: 0
                        }}>
                            {currentMessages.map((msg, idx) => {
                                const isAdmin = msg.senderRole === 'ADMIN';

                                return (
                                    <div key={idx} style={{
                                        alignSelf: isAdmin ? 'flex-end' : 'flex-start',
                                        maxWidth: '75%',
                                        display: 'flex', flexDirection: 'column',
                                        alignItems: isAdmin ? 'flex-end' : 'flex-start'
                                    }}>
                                        <div style={{
                                            padding: '10px 16px',
                                            borderRadius: '12px',
                                            borderTopRightRadius: isAdmin ? '2px' : '12px',
                                            borderTopLeftRadius: isAdmin ? '12px' : '2px',
                                            backgroundColor: isAdmin ? theme.myMsgBg : theme.userMsgBg,
                                            border: `1px solid ${isAdmin ? theme.myMsgBorder : theme.userMsgBorder}`,
                                            color: isAdmin ? theme.myMsgText : theme.userMsgText,
                                            fontSize: '14px', lineHeight: '1.5',
                                            boxShadow: '0 1px 2px rgba(0,0,0,0.03)',
                                            wordBreak: 'break-word', whiteSpace: 'pre-wrap'
                                        }}>
                                            {msg.content}
                                        </div>
                                        <span style={{ fontSize: '10px', color: '#94a3b8', marginTop: '4px', padding: '0 2px' }}>
                                            {isAdmin ? 'You' : 'Client'} • {new Date(msg.timestamp).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                                        </span>
                                    </div>
                                );
                            })}
                            <div ref={messagesEndRef} />
                        </div>

                        <div style={{ padding: '16px', borderTop: `1px solid ${theme.border}`, backgroundColor: 'white', flexShrink: 0 }}>
                            <form onSubmit={handleSendMessage} style={{ display: 'flex', gap: '10px' }}>
                                <input
                                    type="text"
                                    placeholder="Type a reply..."
                                    value={newMessage}
                                    onChange={(e) => setNewMessage(e.target.value)}
                                    style={{
                                        flex: 1, padding: '10px 16px', borderRadius: '20px',
                                        border: `1px solid ${theme.border}`, backgroundColor: '#f8fafc',
                                        fontSize: '14px', outline: 'none'
                                    }}
                                />
                                <button
                                    type="submit"
                                    disabled={!newMessage.trim()}
                                    style={{
                                        width: '42px', height: '42px', borderRadius: '50%',
                                        backgroundColor: newMessage.trim() ? theme.primary : '#cbd5e1',
                                        color: 'white', border: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center',
                                        cursor: newMessage.trim() ? 'pointer' : 'default', transition: 'all 0.2s'
                                    }}
                                >
                                    <Send size={18} />
                                </button>
                            </form>
                        </div>
                    </>
                ) : (
                    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: '#94a3b8' }}>
                        <div style={{ padding: '20px', backgroundColor: '#f1f5f9', borderRadius: '50%', marginBottom: '16px' }}>
                            <MessageCircle size={32} color="#cbd5e1" />
                        </div>
                        <p>Select a user to start chatting</p>
                    </div>
                )}
            </div>
        </div>
    );
}