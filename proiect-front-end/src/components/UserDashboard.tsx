import { useAuth } from '../contexts/AuthContext';
import { Button } from './ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from './ui/card';
import { Badge } from './ui/badge';
import { LogOut, Cpu, Zap } from 'lucide-react';

export const UserDashboard = () => {
  const { logout, currentUser, devices, assignments } = useAuth();

  const assignedDevices = assignments
    .filter((a) => a.userId === currentUser?.id)
    .map((a) => devices.find((d) => d.id === a.deviceId))
    .filter(Boolean);

  const totalConsumption = assignedDevices.reduce(
    (sum, device) => sum + (device?.maxConsumption || 0),
    0
  );

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
        <div className="max-w-4xl mx-auto space-y-6">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm">Total Devices</CardTitle>
                <Cpu className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-indigo-600">{assignedDevices.length}</div>
                <p className="text-xs text-muted-foreground">
                  Devices assigned to you
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm">Total Consumption</CardTitle>
                <Zap className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-indigo-600">{totalConsumption} W</div>
                <p className="text-xs text-muted-foreground">
                  Maximum power consumption
                </p>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>My Devices</CardTitle>
              <CardDescription>
                Devices assigned to your account
              </CardDescription>
            </CardHeader>
            <CardContent>
              {assignedDevices.length === 0 ? (
                <div className="text-center py-8 text-muted-foreground">
                  <Cpu className="h-12 w-12 mx-auto mb-4 opacity-50" />
                  <p>No devices assigned yet</p>
                </div>
              ) : (
                <div className="grid gap-4 md:grid-cols-2">
                  {assignedDevices.map((device) => (
                    <Card key={device?.id}>
                      <CardHeader>
                        <div className="flex items-start justify-between">
                          <div className="flex items-center gap-3">
                            <div className="w-10 h-10 bg-indigo-100 rounded-lg flex items-center justify-center">
                              <Cpu className="h-5 w-5 text-indigo-600" />
                            </div>
                            <div>
                              <CardTitle className="text-base">
                                {device?.name}
                              </CardTitle>
                            </div>
                          </div>
                        </div>
                      </CardHeader>
                      <CardContent>
                        <div className="flex items-center gap-2">
                          <Badge variant="outline">
                            <Zap className="h-3 w-3 mr-1" />
                            {device?.maxConsumption} W
                          </Badge>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  );
};
