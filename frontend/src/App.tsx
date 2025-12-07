import { useState, useEffect } from 'react';
import { Button } from './components/ui/button';
import { Input } from './components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from './components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './components/ui/table';
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from './components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './components/ui/select';
import { Badge } from './components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './components/ui/tabs';
import { Toaster } from './components/ui/sonner';
import { LogOut, Plus, Pencil, Trash2, Users, Cpu, Link2, Zap, Lock } from 'lucide-react';
import { toast } from 'sonner';
import EnergyChart from './components/EnergyChart';
import ChatWidget from './components/ChatWidget';

// @ts-ignore
import { authService } from './services/authService.js';
// @ts-ignore
import { deviceService } from './services/deviceService.js';
// @ts-ignore
import { userService } from './services/userService.js';
import AdminChatPanel from "./components/AdminChatPanel";
import { MessageCircle } from 'lucide-react';

export type User = {
    id: string;
    username: string;
    name: string;
    email: string;
    age: number;
    address: string;
    role: 'ADMIN' | 'CLIENT';
};

export type Device = {
    id: string;
    name: string;
    maxConsumption: number;
};

export type Assignment = {
    id: string;
    userId: string;
    deviceId: string;
};

export type CreateUserRequest = Omit<User, 'id' | 'role'> & {
    password: string;
    role: string;
};

export type CreateDeviceRequest = Omit<Device, 'id'>;


export default function App() {
    const [currentUser, setCurrentUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const checkSession = async () => {
            try {
                const user = await authService.validateSession();
                if (user) {
                    setCurrentUser(user);
                }
            } catch (error) {
                console.error('Session check failed', error);
            }
            setLoading(false);
        };
        checkSession();
    }, []);

    const handleLogout = () => {
        authService.logout();
        setCurrentUser(null);
    };

    if (loading) {
        return <div className="flex h-screen items-center justify-center">Loading...</div>;
    }

    if (!currentUser) {
        return <LoginPage onLogin={setCurrentUser} />;
    }

    if (currentUser.role.toLowerCase() === 'admin') {
        return <AdminPage user={currentUser} onLogout={handleLogout} />;
    }

    return <UserPage user={currentUser} onLogout={handleLogout} />;
}

function LoginPage({ onLogin }: { onLogin: (user: User) => void }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!username || !password) {
            toast.error('Please enter username and password');
            return;
        }

        setLoading(true);
        try {
            const user = await authService.login(username, password);

            if (user) {
                onLogin(user);
                toast.success('Login successful');
            } else {
                toast.error('Invalid credentials');
            }
        } catch (error) {
            toast.error('Login failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
                <Card className="w-full max-w-md">
                    <CardHeader className="space-y-1">
                        <div className="flex items-center justify-center mb-4">
                            <div className="w-12 h-12 bg-indigo-600 rounded-full flex items-center justify-center">
                                <Lock className="w-6 h-6 text-white" />
                            </div>
                        </div>
                        <CardTitle className="text-center">Device Management</CardTitle>
                        <p className="text-center text-sm text-muted-foreground">Sign in to manage devices</p>
                    </CardHeader>
                    <CardContent>
                        <form onSubmit={handleSubmit} className="space-y-4">
                            <Input
                                placeholder="Username"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                disabled={loading}
                            />
                            <Input
                                type="password"
                                placeholder="Password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                disabled={loading}
                            />
                            <Button type="submit" className="w-full" disabled={loading}>
                                {loading ? 'Signing in...' : 'Sign In'}
                            </Button>
                        </form>
                    </CardContent>
                </Card>
            </div>
            <Toaster />
        </>
    );
}

function AdminPage({ user, onLogout }: { user: User; onLogout: () => void }) {
    return (
        <>
            <header className="flex h-16 items-center justify-between border-b bg-background px-6">
                <h1 className="text-xl font-semibold">Admin Dashboard</h1>
                <div className="flex items-center gap-4">
                    <span>Welcome, {user.name}</span>
                    <Button variant="outline" size="icon" onClick={onLogout}>
                        <LogOut className="h-4 w-4" />
                    </Button>
                </div>
            </header>
            <main className="p-6">
                <Tabs defaultValue="users">
                    <TabsList>
                        <TabsTrigger value="users">
                            <Users className="mr-2 h-4 w-4" /> Users
                        </TabsTrigger>
                        <TabsTrigger value="devices">
                            <Cpu className="mr-2 h-4 w-4" /> Devices
                        </TabsTrigger>
                        <TabsTrigger value="assignments">
                            <Link2 className="mr-2 h-4 w-4" /> Assignments
                        </TabsTrigger>
                        <TabsTrigger value="chat">
                            <MessageCircle className="mr-2 h-4 w-4" /> Customer Support
                        </TabsTrigger>
                    </TabsList>
                    <TabsContent value="users">
                        <UserManagementTab />
                    </TabsContent>
                    <TabsContent value="devices">
                        <DeviceManagementTab />
                    </TabsContent>
                    <TabsContent value="assignments">
                        <AssignmentManagementTab />
                    </TabsContent>
                    <TabsContent value="chat">
                        <AdminChatPanel adminId={user.id} adminName={user.name} />
                    </TabsContent>
                </Tabs>
            </main>
            <Toaster />
        </>
    );
}

function UserManagementTab() {
    const [users, setUsers] = useState<User[]>([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingUser, setEditingUser] = useState<User | null>(null);

    const [formData, setFormData] = useState({
        name: '',
        username: '',
        email: '',
        age: 0,
        address: '',
        password: '',
        role: 'CLIENT'
    });

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        try {
            const data = await userService.getAllUsers();
            setUsers(data);
        } catch (error) {
            toast.error('Failed to load users.');
        }
    };

    const openModal = (user: User | null = null) => {
        setEditingUser(user);
        setFormData({
            name: user?.name || '',
            username: user?.username || '',
            email: user?.email || '',
            age: user?.age || 0,
            address: user?.address || '',
            password: '',
            role: user?.role || 'CLIENT',
        });
        setIsModalOpen(true);
    };

    const handleSave = async () => {
        try {
            if (editingUser) {
                await userService.updateUser(editingUser.id, {
                    name: formData.name,
                    username: formData.username,
                    email: formData.email,
                    age: Number(formData.age),
                    address: formData.address,
                    role: formData.role,
                });
                toast.success('User updated successfully.');
            } else {
                await userService.createUser({
                    name: formData.name,
                    username: formData.username,
                    email: formData.email,
                    age: Number(formData.age),
                    address: formData.address,
                    password: formData.password,
                    role: formData.role.toUpperCase(),
                });
                toast.success('User created successfully.');
            }
            setIsModalOpen(false);
            loadUsers();
        } catch (error) {
            toast.error('Failed to save user.');
        }
    };

    const handleDelete = async (id: string) => {
        if (window.confirm('Are you sure you want to delete this user?')) {
            try {
                await userService.deleteUser(id);
                toast.success('User deleted.');
                loadUsers();
            } catch (error) {
                toast.error('Failed to delete user.');
            }
        }
    };

    return (
        <Card>
            <CardHeader>
                <div className="flex justify-between">
                    <CardTitle>User Management</CardTitle>
                    <Button size="sm" onClick={() => openModal()}>
                        <Plus className="mr-2 h-4 w-4" /> Add User
                    </Button>
                </div>
            </CardHeader>
            <CardContent>
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Name</TableHead>
                            <TableHead>Username</TableHead>
                            <TableHead>Email</TableHead>
                            <TableHead>Role</TableHead>
                            <TableHead>Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {users.map((user) => (
                            <TableRow key={user.id}>
                                <TableCell>{user.name}</TableCell>
                                <TableCell>{user.username}</TableCell>
                                <TableCell>{user.email}</TableCell>
                                <TableCell>
                                    <Badge variant={user.role.toLowerCase() === 'admin' ? 'default' : 'secondary'}>{user.role}</Badge>
                                </TableCell>
                                <TableCell className="space-x-2">
                                    <Button variant="outline" size="icon" onClick={() => openModal(user)}>
                                        <Pencil className="h-4 w-4" />
                                    </Button>
                                    <Button variant="destructive" size="icon" onClick={() => handleDelete(user.id)}>
                                        <Trash2 className="h-4 w-4" />
                                    </Button>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </CardContent>

            <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>{editingUser ? 'Edit User' : 'Add New User'}</DialogTitle>
                    </DialogHeader>
                    <div className="space-y-4 py-4">
                        <Input
                            placeholder="Name"
                            value={formData.name}
                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                        />
                        <Input
                            placeholder="Username"
                            value={formData.username}
                            onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                        />
                        <Input
                            type="email"
                            placeholder="Email"
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        />
                        <Input
                            type="number"
                            placeholder="Age"
                            value={formData.age}
                            onChange={(e) => setFormData({ ...formData, age: Number(e.target.value) || 0 })}
                        />
                        <Input
                            placeholder="Address"
                            value={formData.address}
                            onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                        />
                        <Input
                            type="password"
                            placeholder={editingUser ? 'Password (leave blank to keep unchanged)' : 'Password'}
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                            disabled={!!editingUser}
                        />
                        <Select value={formData.role} onValueChange={(value) => setFormData({ ...formData, role: value })}>
                            <SelectTrigger>
                                <SelectValue placeholder="Select role" />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="CLIENT">Client</SelectItem>
                                <SelectItem value="ADMIN">Admin</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                    <DialogFooter>
                        <Button variant="ghost" onClick={() => setIsModalOpen(false)}>
                            Cancel
                        </Button>
                        <Button onClick={handleSave}>Save</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </Card>
    );
}

function DeviceManagementTab() {
    const [devices, setDevices] = useState<Device[]>([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingDevice, setEditingDevice] = useState<Device | null>(null);

    const [formData, setFormData] = useState({ name: '', maxConsumption: 0 });

    useEffect(() => {
        loadDevices();
    }, []);

    const loadDevices = async () => {
        try {
            const data = await deviceService.getAllDevices();
            setDevices(data);
        } catch (error) {
            toast.error('Failed to load devices.');
        }
    };

    const openModal = (device: Device | null = null) => {
        setEditingDevice(device);
        setFormData({
            name: device?.name || '',
            maxConsumption: device?.maxConsumption || 0,
        });
        setIsModalOpen(true);
    };

    const handleSave = async () => {
        try {
            const deviceData = {
                name: formData.name,
                maxConsumption: Number(formData.maxConsumption),
            };

            if (editingDevice) {
                await deviceService.updateDevice(editingDevice.id, deviceData);
                toast.success('Device updated successfully.');
            } else {
                await deviceService.createDevice(deviceData as CreateDeviceRequest);
                toast.success('Device created successfully.');
            }
            setIsModalOpen(false);
            loadDevices();
        } catch (error) {
            toast.error('Failed to save device.');
        }
    };

    const handleDelete = async (id: string) => {
        if (window.confirm('Are you sure you want to delete this device?')) {
            try {
                await deviceService.deleteDevice(id);
                toast.success('Device deleted.');
                loadDevices();
            } catch (error) {
                toast.error('Failed to delete device.');
            }
        }
    };

    return (
        <Card>
            <CardHeader>
                <div className="flex justify-between">
                    <CardTitle>Device Management</CardTitle>
                    <Button size="sm" onClick={() => openModal()}>
                        <Plus className="mr-2 h-4 w-4" /> Add Device
                    </Button>
                </div>
            </CardHeader>
            <CardContent>
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Name</TableHead>
                            <TableHead>Max Consumption (W)</TableHead>
                            <TableHead>Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {devices.map((device) => (
                            <TableRow key={device.id}>
                                <TableCell>{device.name}</TableCell>
                                <TableCell>{device.maxConsumption}</TableCell>
                                <TableCell className="space-x-2">
                                    <Button variant="outline" size="icon" onClick={() => openModal(device)}>
                                        <Pencil className="h-4 w-4" />
                                    </Button>
                                    <Button variant="destructive" size="icon" onClick={() => handleDelete(device.id)}>
                                        <Trash2 className="h-4 w-4" />
                                    </Button>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </CardContent>

            <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>{editingDevice ? 'Edit Device' : 'Add New Device'}</DialogTitle>
                    </DialogHeader>
                    <div className="space-y-4 py-4">
                        <Input
                            placeholder="Name (e.g., Air Conditioner)"
                            value={formData.name}
                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                        />
                        <Input
                            type="number"
                            placeholder="Max Consumption (W)"
                            value={formData.maxConsumption}
                            onChange={(e) => setFormData({ ...formData, maxConsumption: Number(e.target.value) })}
                        />
                    </div>
                    <DialogFooter>
                        <Button variant="ghost" onClick={() => setIsModalOpen(false)}>
                            Cancel
                        </Button>
                        <Button onClick={handleSave}>Save</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </Card>
    );
}

function AssignmentManagementTab() {
    const [userIdInput, setUserIdInput] = useState('');
    const [deviceIdInput, setDeviceIdInput] = useState('');

    const [lookupUserId, setLookupUserId] = useState('');
    const [searchedUserId, setSearchedUserId] = useState('');
    const [lookedUpDevices, setLookedUpDevices] = useState<Device[]>([]);
    const [isLoadingLookup, setIsLoadingLookup] = useState(false);

    const handleAssign = async () => {
        if (!userIdInput || !deviceIdInput) {
            toast.warning('Please enter a User ID and a Device ID.');
            return;
        }
        try {
            await deviceService.assignDevice(userIdInput, deviceIdInput);
            toast.success('Device assigned.');

            if (searchedUserId === userIdInput) {
                handleSearch(searchedUserId);
            }

            setUserIdInput('');
            setDeviceIdInput('');
        } catch (error) {
            toast.error('Failed to assign device. Check IDs.');
        }
    };

    const handleSearch = async (idToSearch: string) => {
        if (!idToSearch) {
            toast.warning('Please enter a User ID to search.');
            return;
        }
        setIsLoadingLookup(true);
        setSearchedUserId(idToSearch);
        try {
            const devices = await deviceService.getDevicesForUser(idToSearch);
            setLookedUpDevices(devices);
            if (devices.length === 0) {
                toast.info('No devices found for this user.');
            }
        } catch (error) {
            toast.error('Failed to look up user devices.');
            setLookedUpDevices([]);
        }
        setIsLoadingLookup(false);
    };

    const handleUnassign = async (deviceId: string) => {
        if (!searchedUserId) return;

        if (window.confirm(`Are you sure you want to unassign device ${deviceId} from user ${searchedUserId}?`)) {
            try {
                await deviceService.unassignDevice(searchedUserId, deviceId);
                toast.success('Device unassigned.');
                handleSearch(searchedUserId);
            } catch (error) {
                toast.error('Failed to unassign device.');
            }
        }
    };

    return (
        <Card>
            <CardHeader>
                <CardTitle>Device Assignments</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="mb-6 space-y-4 rounded-md border p-4">
                    <h3 className="font-semibold">Assign New Device</h3>
                    <div className="flex gap-4">
                        <Input
                            placeholder="Enter User ID (ex: 5)"
                            value={userIdInput}
                            onChange={(e) => setUserIdInput(e.target.value)}
                        />
                        <Input
                            placeholder="Enter Device ID (ex: 3)"
                            value={deviceIdInput}
                            onChange={(e) => setDeviceIdInput(e.target.value)}
                        />
                        <Button onClick={handleAssign}>Assign</Button>
                    </div>
                </div>

                {/* Partea de "User Lookup" & "Unassign" (Corectată) */}
                <div className="mt-8 space-y-4 rounded-md border p-4">
                    <h3 className="font-semibold">View & Unassign User Devices</h3>
                    <div className="flex gap-4">
                        <Input
                            placeholder="Enter User ID to view devices"
                            value={lookupUserId}
                            onChange={(e) => setLookupUserId(e.target.value)}
                        />
                        <Button onClick={() => handleSearch(lookupUserId)}>Search</Button>
                    </div>

                    {searchedUserId && (
                        <div className="mt-6">
                            <h4 className="font-medium">Devices for User ID: {searchedUserId}</h4>
                            {isLoadingLookup ? (
                                <p>Loading...</p>
                            ) : (
                                <Table className="mt-2">
                                    <TableHeader>
                                        <TableRow>
                                            <TableHead>Device Name</TableHead>
                                            <TableHead>Max Consumption (W)</TableHead>
                                            <TableHead>Actions</TableHead>
                                        </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                        {lookedUpDevices.length === 0 ? (
                                            <TableRow>
                                                <TableCell colSpan={3} className="text-center">
                                                    No devices assigned to this user.
                                                </TableCell>
                                            </TableRow>
                                        ) : (
                                            lookedUpDevices.map((device) => (
                                                <TableRow key={device.id}>
                                                    <TableCell>{device.name}</TableCell>
                                                    <TableCell>{device.maxConsumption}</TableCell>
                                                    <TableCell>
                                                        <Button
                                                            variant="destructive"
                                                            size="sm"
                                                            onClick={() => handleUnassign(device.id)}
                                                        >
                                                            Unassign
                                                        </Button>
                                                    </TableCell>
                                                </TableRow>
                                            ))
                                        )}
                                    </TableBody>
                                </Table>
                            )}
                        </div>
                    )}
                </div>
            </CardContent>
        </Card>
    );
}

function UserPage({ user, onLogout }: { user: User; onLogout: () => void }) {
    const [devices, setDevices] = useState<Device[]>([]);
    const [loading, setLoading] = useState(true);
    const [selectedDeviceId, setSelectedDeviceId] = useState<string | null>(null);

    useEffect(() => {
        loadDevices();
    }, []);

    const loadDevices = async () => {
        setLoading(true);
        try {
            const data = await deviceService.getMyDevices();
            setDevices(data);
            // Select first device by default if available
            if (data.length > 0 && !selectedDeviceId) {
                setSelectedDeviceId(data[0].id);
            }
        } catch (error) {
            console.error('Failed to load devices', error);
            toast.error('Failed to load your devices.');
        } finally {
            setLoading(false);
        }
    };

    const totalConsumption = devices.reduce((sum, d) => sum + d.maxConsumption, 0);

    return (
        <>
            <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
                <header className="bg-white border-b">
                    <div className="container mx-auto px-4 py-4 flex justify-between items-center">
                        <div>
                            <h1 className="text-xl font-bold text-indigo-600">My Energy Monitor</h1>
                            <p className="text-sm text-muted-foreground">Welcome, {user.name}</p>
                        </div>
                        <Button variant="outline" onClick={onLogout}>
                            <LogOut className="mr-2 h-4 w-4" />
                            Logout
                        </Button>
                    </div>
                </header>

                <main className="container mx-auto px-4 py-8 max-w-6xl">
                    <div className="grid gap-4 md:grid-cols-2 mb-6">
                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-sm font-medium">Total Devices</CardTitle>
                                <Cpu className="h-4 w-4 text-muted-foreground" />
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-bold text-indigo-600">{devices.length}</div>
                                <p className="text-xs text-muted-foreground">Assigned to you</p>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-sm font-medium">Total Consumption</CardTitle>
                                <Zap className="h-4 w-4 text-muted-foreground" />
                            </CardHeader>
                            <CardContent>
                                <div className="text-2xl font-bold text-indigo-600">{totalConsumption} W</div>
                                <p className="text-xs text-muted-foreground">Max power consumption</p>
                            </CardContent>
                        </Card>
                    </div>

                    <Card>
                        <CardHeader>
                            <CardTitle>My Devices</CardTitle>
                        </CardHeader>
                        <CardContent>
                            {loading ? (
                                <div className="text-center py-8 text-muted-foreground">Loading...</div>
                            ) : devices.length === 0 ? (
                                <div className="text-center py-8 text-muted-foreground">
                                    <Cpu className="h-12 w-12 mx-auto mb-4 opacity-50" />
                                    <p>No devices assigned</p>
                                </div>
                            ) : (
                                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                                    {devices.map((device) => (
                                        <Card
                                            key={device.id}
                                            className={`cursor-pointer transition-all ${
                                                selectedDeviceId === device.id
                                                    ? 'ring-2 ring-indigo-600 shadow-lg'
                                                    : 'hover:shadow-md'
                                            }`}
                                            onClick={() => setSelectedDeviceId(device.id)}
                                        >
                                            <CardHeader>
                                                <div className="flex items-center gap-3">
                                                    <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                                                        selectedDeviceId === device.id
                                                            ? 'bg-indigo-600'
                                                            : 'bg-indigo-100'
                                                    }`}>
                                                        <Cpu className={`h-5 w-5 ${
                                                            selectedDeviceId === device.id
                                                                ? 'text-white'
                                                                : 'text-indigo-600'
                                                        }`} />
                                                    </div>
                                                    <CardTitle className="text-base">{device.name}</CardTitle>
                                                </div>
                                            </CardHeader>
                                            <CardContent>
                                                <Badge variant="outline">
                                                    <Zap className="h-3 w-3 mr-1" />
                                                    {device.maxConsumption} W
                                                </Badge>
                                            </CardContent>
                                        </Card>
                                    ))}
                                </div>
                            )}
                        </CardContent>
                    </Card>

                    {/* Energy Chart - Shows only if a device is selected */}
                    {selectedDeviceId && (
                        <EnergyChart deviceId={selectedDeviceId} />
                    )}
                </main>
            </div>
            <ChatWidget userId={user.id} userName={user.name} role={user.role} />
            <Toaster />
        </>
    );
}