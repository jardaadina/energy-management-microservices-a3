// src/services/api.ts
import apiClient from './apiClient';

// --- TIPURI (Actualizate să corespundă cu DTO-urile din Backend) ---

export type User = {
    id: string;
    username: string;
    name: string;
    role: 'ADMIN' | 'CLIENT';
};

// Am actualizat tipul Device să corespundă cu DeviceDTO.java
export type Device = {
    id: string;
    description: string; // Am schimbat 'name' în 'description'
    address: string;
    maxConsumption: number;
};

// Acesta este DTO-ul primit de la /user-devices (UserDeviceDTO.java)
export type Assignment = {
    id: string; // ID-ul mapării
    userId: string;
    deviceId: string;
};

// Tipul pentru crearea unui user (fără ID)
export type CreateUserRequest = Omit<User, 'id' | 'role'> & {
    password: string;
    role: string; // Backend-ul se așteaptă la string (ex: "ADMIN", "USER")
};

// Tipul pentru crearea unui device (fără ID)
export type CreateDeviceRequest = Omit<Device, 'id'>;

// Tipul pentru răspunsul de la login (AuthResponse.java)
type AuthResponse = {
    token: string;
    userDTO: User;
};

// Tipul pentru răspunsul de la validare (/auth/validate)
type ValidateResponse = {
    valid: boolean;
    userId: string;
    username: string;
    authorities: string[]; // ex: ["ROLE_ADMIN"]
};

// --- AUTHENTICATION ---

export const login = async (username: string, password: string): Promise<User | null> => {
    try {
        const response = await apiClient.post<AuthResponse>('/auth/login', { username, password });

        if (response.data && response.data.token) {
            localStorage.setItem('jwtToken', response.data.token);
            return response.data.userDTO;
        }
        return null;
    } catch (error) {
        console.error('Login failed:', error);
        throw error; // Aruncăm eroarea pentru ca LoginPage să o prindă
    }
};

export const validateSession = async (): Promise<User | null> => {
    try {
        const response = await apiClient.get<ValidateResponse>('/auth/validate');
        if (response.data && response.data.valid) {
            // Reconstruim obiectul User din datele de validare
            const role = (response.data.authorities[0] || 'ROLE_USER')
                .replace('ROLE_', '')
                .toLowerCase() as 'ADMIN' | 'CLIENT';

            return {
                id: response.data.userId,
                username: response.data.username,
                name: response.data.username, // Numele nu e în răspunsul de validare, folosim username
                role: role,
            };
        }
        return null;
    } catch (error) {
        localStorage.removeItem('jwtToken');
        return null;
    }
};

// --- USERS (Admin) ---

export const getUsers = async (): Promise<User[]> => {
    const response = await apiClient.get<User[]>('/users');
    return response.data;
};

export const createUser = async (userData: CreateUserRequest): Promise<User> => {
    const response = await apiClient.post<User>('/users', userData);
    return response.data;
};

export const updateUser = async (id: string, updates: Partial<User>): Promise<User> => {
    const response = await apiClient.put<User>(`/users/${id}`, updates);
    return response.data;
};

export const deleteUser = async (id: string): Promise<void> => {
    await apiClient.delete(`/users/${id}`);
};

// --- DEVICES (Admin) ---

export const getDevices = async (): Promise<Device[]> => {
    const response = await apiClient.get<Device[]>('/devices');
    return response.data;
};

export const createDevice = async (deviceData: CreateDeviceRequest): Promise<Device> => {
    const response = await apiClient.post<Device>('/devices', deviceData);
    return response.data;
};

export const updateDevice = async (id: string, updates: Partial<Device>): Promise<Device> => {
    const response = await apiClient.put<Device>(`/devices/${id}`, updates);
    return response.data;
};

export const deleteDevice = async (id: string): Promise<void> => {
    await apiClient.delete(`/devices/${id}`);
};

// --- ASSIGNMENTS (Admin) ---

export const getAssignments = async (): Promise<Assignment[]> => {
    const response = await apiClient.get<Assignment[]>('/user-devices');
    return response.data;
};

export const assignDevice = async (userId: string, deviceId: string): Promise<void> => {
    await apiClient.post('/user-devices', { userId, deviceId });
};

export const unassignDevice = async (userId: string, deviceId: string): Promise<void> => {
    // Folosim endpoint-ul tău specific pentru asta
    await apiClient.delete(`/user-devices/user/${userId}/device/${deviceId}`);
};

// --- USER-SPECIFIC (Pentru UserPage) ---

export const getMyDevices = async (): Promise<Device[]> => {
    // Aici folosim endpoint-ul /user-devices/my-devices
    // Acesta returnează direct List<DeviceDTO>, ceea ce e perfect.
    const response = await apiClient.get<Device[]>('/user-devices/my-devices');
    return response.data;
};