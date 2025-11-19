import apiClient from './apiClient';


export type User = {
    id: string;
    username: string;
    name: string;
    role: 'ADMIN' | 'CLIENT';
};

export type Device = {
    id: string;
    description: string;
    address: string;
    maxConsumption: number;
};









