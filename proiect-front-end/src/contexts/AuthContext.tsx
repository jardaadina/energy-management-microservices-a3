import React, { createContext, useContext, useState, ReactNode } from 'react';

export type User = {
  id: string;
  username: string;
  password: string;
  role: 'admin' | 'user';
  name: string;
};

export type Device = {
  id: string;
  name: string;
  maxConsumption: number;
};

export type DeviceAssignment = {
  userId: string;
  deviceId: string;
};

type AuthContextType = {
  currentUser: User | null;
  users: User[];
  devices: Device[];
  assignments: DeviceAssignment[];
  login: (username: string, password: string) => boolean;
  logout: () => void;
  createUser: (user: Omit<User, 'id'>) => void;
  updateUser: (id: string, user: Partial<User>) => void;
  deleteUser: (id: string) => void;
  createDevice: (device: Omit<Device, 'id'>) => void;
  updateDevice: (id: string, device: Partial<Device>) => void;
  deleteDevice: (id: string) => void;
  assignDevice: (userId: string, deviceId: string) => void;
  unassignDevice: (userId: string, deviceId: string) => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }: { children?: ReactNode }) => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [users, setUsers] = useState<User[]>([
    {
      id: '1',
      username: 'admin',
      password: 'admin123',
      role: 'admin',
      name: 'Administrator',
    },
    {
      id: '2',
      username: 'user',
      password: 'user123',
      role: 'user',
      name: 'John Doe',
    },
  ]);

  console.log({users})

  const [devices, setDevices] = useState<Device[]>([
    { id: '1', name: 'Air Conditioner', maxConsumption: 3500 },
    { id: '2', name: 'Refrigerator', maxConsumption: 150 },
    { id: '3', name: 'Washing Machine', maxConsumption: 2000 },
  ]);

  const [assignments, setAssignments] = useState<DeviceAssignment[]>([
    { userId: '2', deviceId: '1' },
    { userId: '2', deviceId: '2' },
  ]);

  const login = (username: string, password: string): boolean => {
    const user = users.find(
      (u) => u.username === username && u.password === password
    );
    if (user) {
      setCurrentUser(user);
      return true;
    }
    return false;
  };

  const logout = () => {
    setCurrentUser(null);
  };

  const createUser = (user: Omit<User, 'id'>) => {
    const newUser = { ...user, id: Date.now().toString() };
    setUsers([...users, newUser]);
  };

  const updateUser = (id: string, updatedUser: Partial<User>) => {
    setUsers(users.map((u) => (u.id === id ? { ...u, ...updatedUser } : u)));
  };

  const deleteUser = (id: string) => {
    setUsers(users.filter((u) => u.id !== id));
    setAssignments(assignments.filter((a) => a.userId !== id));
  };

  const createDevice = (device: Omit<Device, 'id'>) => {
    const newDevice = { ...device, id: Date.now().toString() };
    setDevices([...devices, newDevice]);
  };

  const updateDevice = (id: string, updatedDevice: Partial<Device>) => {
    setDevices(
      devices.map((d) => (d.id === id ? { ...d, ...updatedDevice } : d))
    );
  };

  const deleteDevice = (id: string) => {
    setDevices(devices.filter((d) => d.id !== id));
    setAssignments(assignments.filter((a) => a.deviceId !== id));
  };

  const assignDevice = (userId: string, deviceId: string) => {
    if (!assignments.find((a) => a.userId === userId && a.deviceId === deviceId)) {
      setAssignments([...assignments, { userId, deviceId }]);
    }
  };

  const unassignDevice = (userId: string, deviceId: string) => {
    setAssignments(
      assignments.filter((a) => !(a.userId === userId && a.deviceId === deviceId))
    );
  };

  return (
    <AuthContext.Provider
      value={{
        currentUser,
        users,
        devices,
        assignments,
        login,
        logout,
        createUser,
        updateUser,
        deleteUser,
        createDevice,
        updateDevice,
        deleteDevice,
        assignDevice,
        unassignDevice,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
