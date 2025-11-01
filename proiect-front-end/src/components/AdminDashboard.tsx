import { useAuth } from '../contexts/AuthContext';
import { Button } from './ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { UserManagement } from './UserManagement';
import { DeviceManagement } from './DeviceManagement';
import { DeviceAssignment } from './DeviceAssignment';
import { LogOut, Users, Cpu, Link2 } from 'lucide-react';

export const AdminDashboard = () => {
  const { logout, currentUser } = useAuth();

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
      <header className="bg-white border-b">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-indigo-600">Device Management System</h1>
              <p className="text-sm text-muted-foreground">
                Welcome, {currentUser?.name}
              </p>
            </div>
            <Button variant="outline" onClick={logout}>
              <LogOut className="mr-2 h-4 w-4" />
              Logout
            </Button>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8">
        <Tabs defaultValue="users" className="space-y-6">
          <TabsList className="grid w-full max-w-md grid-cols-3">
            <TabsTrigger value="users">
              <Users className="mr-2 h-4 w-4" />
              Users
            </TabsTrigger>
            <TabsTrigger value="devices">
              <Cpu className="mr-2 h-4 w-4" />
              Devices
            </TabsTrigger>
            <TabsTrigger value="assignments">
              <Link2 className="mr-2 h-4 w-4" />
              Assignments
            </TabsTrigger>
          </TabsList>

          <TabsContent value="users">
            <UserManagement />
          </TabsContent>

          <TabsContent value="devices">
            <DeviceManagement />
          </TabsContent>

          <TabsContent value="assignments">
            <DeviceAssignment />
          </TabsContent>
        </Tabs>
      </main>
    </div>
  );
};
