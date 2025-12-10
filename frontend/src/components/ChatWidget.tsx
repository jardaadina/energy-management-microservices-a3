import { useState, useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Card } from './ui/card';
import { Avatar, AvatarFallback } from './ui/avatar';
import { MessageCircle, X, Send, Bot, Minimize2, User, ShieldCheck } from 'lucide-react';
import { toast } from 'sonner';
// @ts-ignore
import webSocketService from '../services/WebSocketService';

type Message = {
    senderId: string;
    content: string;
    timestamp: string;
    senderRole: 'ADMIN' | 'USER' | 'SYSTEM';
};

type ChatWidgetProps = {
    userId: string;
    userName: string;
    role: string;
};

export default function ChatWidget({ userId, userName, role }: ChatWidgetProps) {
    // Ascundem widget-ul pentru Admin
    if (role && role.toUpperCase() === 'ADMIN') {
        return null;
    }

    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState<Message[]>([]);
    const [newMessage, setNewMessage] = useState('');
    const [isConnected, setIsConnected] = useState(false);
    const [mounted, setMounted] = useState(false);
    const scrollRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        setMounted(true);
        return () => setMounted(false);
    }, []);

    useEffect(() => {
        if (mounted && !isConnected && userId) {
            console.log("Initializing WebSocket connection...");
            webSocketService.connect(
                userId,
                (message: any) => {
                    setMessages((prev) => [...prev, {
                        senderId: message.senderId,
                        content: message.content,
                        timestamp: message.timestamp,
                        senderRole: message.senderRole || 'SYSTEM'
                    }]);
                },
                (alert: any) => {
                    console.log(" ALERT RECEIVED IN COMPONENT:", alert);
                    toast.error(` Overconsumption Alert: ${alert.message} `, {
                        duration: 5000, });
                }
            );
            setIsConnected(true);
        }
    }, [mounted, userId, isConnected]);

    useEffect(() => {
        if (scrollRef.current) {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [messages, isOpen]);

    const handleSendMessage = () => {
        if (!newMessage.trim()) return;
        const msg: Message = {
            senderId: userId,
            content: newMessage,
            timestamp: new Date().toISOString(),
            senderRole: 'USER'
        };
        setMessages((prev) => [...prev, msg]);
        webSocketService.sendMessage(userId, 'support-bot', newMessage, 'USER');
        setNewMessage('');
    };

    if (!mounted) return null;

    const colors = {
        primary: '#4f46e5',
        primaryDark: '#4338ca',
        botBg: '#f3f4f6',
        botText: '#1f2937',
        adminBg: '#fff7ed',
        adminBorder: '#fed7aa',
        adminText: '#9a3412',
        userBg: '#4f46e5',
        userText: '#ffffff'
    };

    return createPortal(
        <div style={{ position: 'fixed', bottom: '24px', right: '24px', zIndex: 99999, fontFamily: 'sans-serif' }}>
            {!isOpen ? (
                <button
                    onClick={() => setIsOpen(true)}
                    style={{
                        width: '60px', height: '60px', borderRadius: '50%',
                        backgroundColor: colors.primary, color: 'white',
                        border: 'none', cursor: 'pointer',
                        boxShadow: '0 4px 14px rgba(0,0,0,0.25)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        transition: 'transform 0.2s'
                    }}
                    onMouseOver={(e) => e.currentTarget.style.transform = 'scale(1.1)'}
                    onMouseOut={(e) => e.currentTarget.style.transform = 'scale(1)'}
                >
                    <MessageCircle size={28} />
                    {isConnected && <span style={{ position: 'absolute', top: 0, right: 0, width: '14px', height: '14px', backgroundColor: '#22c55e', border: '2px solid white', borderRadius: '50%' }}></span>}
                </button>
            ) : (
                <Card style={{
                    width: '360px', height: '550px', maxHeight: '80vh',
                    display: 'flex', flexDirection: 'column',
                    backgroundColor: 'white', borderRadius: '16px',
                    boxShadow: '0 10px 40px rgba(0,0,0,0.2)', border: '1px solid #e5e7eb',
                    overflow: 'hidden'
                }}>
                    <div style={{
                        background: `linear-gradient(135deg, ${colors.primary}, ${colors.primaryDark})`,
                        padding: '16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                        color: 'white', flexShrink: 0
                    }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                            <div style={{ position: 'relative' }}>
                                <div style={{ background: 'rgba(255,255,255,0.2)', padding: '6px', borderRadius: '50%' }}>
                                    <Bot size={20} color="white" />
                                </div>
                                <span style={{ position: 'absolute', bottom: -2, right: -2, width: '10px', height: '10px', backgroundColor: isConnected ? '#4ade80' : '#f87171', borderRadius: '50%', border: '2px solid #4338ca' }}></span>
                            </div>
                            <div>
                                <h3 style={{ margin: 0, fontSize: '15px', fontWeight: '600' }}>Energy Support</h3>
                                <p style={{ margin: 0, fontSize: '11px', opacity: 0.8 }}>{isConnected ? 'Online' : 'Connecting...'}</p>
                            </div>
                        </div>
                        <Button variant="ghost" size="icon" onClick={() => setIsOpen(false)} style={{ color: 'white', borderRadius: '50%' }}>
                            <Minimize2 size={18} />
                        </Button>
                    </div>

                    <div
                        ref={scrollRef}
                        style={{
                            flex: 1,
                            overflowY: 'auto',
                            padding: '16px',
                            backgroundColor: 'white',
                            minHeight: 0
                        }}
                    >
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>

                            <div style={{ alignSelf: 'flex-start', maxWidth: '85%' }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px', marginLeft: '4px' }}>
                                    <Bot size={12} color="#6b7280" />
                                    <span style={{ fontSize: '10px', color: '#6b7280', fontWeight: 'bold' }}>AI Assistant</span>
                                </div>
                                <div style={{
                                    padding: '12px 16px', borderRadius: '18px', borderTopLeftRadius: '2px',
                                    backgroundColor: colors.botBg, color: colors.botText, fontSize: '13px', lineHeight: '1.4'
                                }}>
                                    Salut <strong>{userName}</strong>! 👋<br />Sunt asistentul tău virtual.
                                </div>
                            </div>

                            {messages.map((msg, index) => {
                                const isMe = msg.senderId === userId && msg.senderRole !== 'ADMIN';
                                const isBot = msg.senderId === 'support-bot';
                                const isAdmin = !isMe && !isBot && (msg.senderRole === 'ADMIN' || msg.senderId === 'admin');

                                return (
                                    <div key={index} style={{
                                        alignSelf: isMe ? 'flex-end' : 'flex-start',
                                        maxWidth: '85%', display: 'flex', flexDirection: 'column',
                                        alignItems: isMe ? 'flex-end' : 'flex-start'
                                    }}>
                                        {!isMe && (
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px', marginLeft: '4px' }}>
                                                {isAdmin ? <User size={12} color={colors.adminText} /> : <Bot size={12} color="#6b7280" />}
                                                <span style={{
                                                    fontSize: '10px', fontWeight: 'bold',
                                                    color: isAdmin ? colors.adminText : '#6b7280'
                                                }}>
                                                    {isAdmin ? 'Administrator' : 'AI Assistant'}
                                                </span>
                                            </div>
                                        )}

                                        <div style={{
                                            padding: '12px 16px', borderRadius: '18px',
                                            fontSize: '13px', lineHeight: '1.4',
                                            whiteSpace: 'pre-wrap', wordBreak: 'break-word',
                                            borderTopRightRadius: isMe ? '2px' : '18px',
                                            borderTopLeftRadius: isMe ? '18px' : '2px',
                                            backgroundColor: isMe ? colors.userBg : (isAdmin ? colors.adminBg : colors.botBg),
                                            color: isMe ? colors.userText : (isAdmin ? colors.adminText : colors.botText),
                                            border: isAdmin ? `1px solid ${colors.adminBorder}` : 'none'
                                        }}>
                                            {msg.content}
                                        </div>
                                        <span style={{ fontSize: '10px', color: '#9ca3af', marginTop: '4px', marginRight: '4px' }}>
                                            {msg.timestamp ? new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                                        </span>
                                    </div>
                                );
                            })}
                        </div>
                    </div>

                    <div style={{
                        padding: '16px',
                        borderTop: '1px solid #e5e7eb',
                        backgroundColor: 'white',
                        flexShrink: 0
                    }}>
                        <form
                            onSubmit={(e) => { e.preventDefault(); handleSendMessage(); }}
                            style={{ display: 'flex', alignItems: 'center', gap: '10px' }}
                        >
                            <Input
                                placeholder="Scrie un mesaj..."
                                value={newMessage}
                                onChange={(e) => setNewMessage(e.target.value)}
                                style={{
                                    borderRadius: '24px', backgroundColor: '#f9fafb', border: '1px solid #e5e7eb',
                                    padding: '0 16px', height: '42px', flex: 1
                                }}
                            />
                            <Button
                                type="submit"
                                size="icon"
                                disabled={!newMessage.trim()}
                                style={{
                                    borderRadius: '50%', width: '42px', height: '42px',
                                    backgroundColor: newMessage.trim() ? colors.primary : '#e5e7eb',
                                    color: 'white', border: 'none', cursor: newMessage.trim() ? 'pointer' : 'default'
                                }}
                            >
                                <Send size={18} style={{ marginLeft: '2px' }} />
                            </Button>
                        </form>
                    </div>
                </Card>
            )}
        </div>,
        document.body
    );
}