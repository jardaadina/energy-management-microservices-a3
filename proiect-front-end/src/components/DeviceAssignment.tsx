import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Button } from './ui/button';
import { Label } from './ui/label';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from './ui/table';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from './ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from './ui/select';
import { Badge } from './ui/badge';
import { Link2, Unlink } from 'lucide-react';
import { toast } from 'sonner@2.0.3';

export const DeviceAssignment = () => {
  const { users, devices, assignments, assignDevice, unassignDevice } = useAuth();
  const [isAssignOpen, setIsAssignOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState('');
  const [selectedDevice, setSelectedDevice] = useState('');

  const handleAssign = () => {
    if (!selectedUser || !selectedDevice) {
      toast.error('Please select both user and device');
      return;
    }
    assignDevice(selectedUser, selectedDevice);
    toast.success('Device assigned successfully');
    setIsAssignOpen(false);
    setSelectedUser('');
    setSelectedDevice('');
  };

  const handleUnassign = (userId: string, deviceId: string) => {
    unassignDevice(userId, deviceId);
    toast.success('Device unassigned successfully');
  };

  const getAssignmentData = () => {
    return assignments.map((assignment) => {
      const user = users.find((u) => u.id === assignment.userId);
      const device = devices.find((d) => d.id === assignment.deviceId);
      return { assignment, user, device };
    });
  };

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0">
        <CardTitle>Device Assignments</CardTitle>
        <Dialog open={isAssignOpen} onOpenChange={setIsAssignOpen}>
          <DialogTrigger asChild>
            <Button>
              <Link2 className="mr-2 h-4 w-4" />
              Assign Device
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Assign Device to User</DialogTitle>
              <DialogDescription>
                Select a user and device to create an assignment
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="assign-user">User</Label>
                <Select value={selectedUser} onValueChange={setSelectedUser}>
                  <SelectTrigger id="assign-user">
                    <SelectValue placeholder="Select a user" />
                  </SelectTrigger>
                  <SelectContent>
                    {users.filter((u) => u.role === 'user').map((user) => (
                      <SelectItem key={user.id} value={user.id}>
                        {user.name} ({user.username})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="assign-device">Device</Label>
                <Select value={selectedDevice} onValueChange={setSelectedDevice}>
                  <SelectTrigger id="assign-device">
                    <SelectValue placeholder="Select a device" />
                  </SelectTrigger>
                  <SelectContent>
                    {devices.map((device) => (
                      <SelectItem key={device.id} value={device.id}>
                        {device.name} ({device.maxConsumption}W)
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setIsAssignOpen(false)}>
                Cancel
              </Button>
              <Button onClick={handleAssign}>Assign</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>User</TableHead>
              <TableHead>Device</TableHead>
              <TableHead>Max Consumption</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {getAssignmentData().length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} className="text-center text-muted-foreground">
                  No device assignments yet
                </TableCell>
              </TableRow>
            ) : (
              getAssignmentData().map(({ assignment, user, device }) => (
                <TableRow key={`${assignment.userId}-${assignment.deviceId}`}>
                  <TableCell>
                    {user ? (
                      <div>
                        <div>{user.name}</div>
                        <div className="text-sm text-muted-foreground">{user.username}</div>
                      </div>
                    ) : (
                      'Unknown User'
                    )}
                  </TableCell>
                  <TableCell>{device?.name || 'Unknown Device'}</TableCell>
                  <TableCell>
                    <Badge variant="outline">
                      {device?.maxConsumption || 0} W
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() =>
                        handleUnassign(assignment.userId, assignment.deviceId)
                      }
                    >
                      <Unlink className="h-4 w-4" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
};
