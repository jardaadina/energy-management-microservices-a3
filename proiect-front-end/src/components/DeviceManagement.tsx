import { useState } from 'react';
import { useAuth, Device } from '../contexts/AuthContext';
import { Button } from './ui/button';
import { Input } from './ui/input';
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
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from './ui/alert-dialog';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import { toast } from 'sonner@2.0.3';

export const DeviceManagement = () => {
  const { devices, createDevice, updateDevice, deleteDevice } = useAuth();
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingDevice, setEditingDevice] = useState<Device | null>(null);
  
  const [formData, setFormData] = useState({
    name: '',
    maxConsumption: '',
  });

  const resetForm = () => {
    setFormData({ name: '', maxConsumption: '' });
  };

  const handleCreate = () => {
    if (!formData.name || !formData.maxConsumption) {
      toast.error('Please fill in all fields');
      return;
    }
    const consumption = parseFloat(formData.maxConsumption);
    if (isNaN(consumption) || consumption <= 0) {
      toast.error('Please enter a valid max consumption value');
      return;
    }
    createDevice({ name: formData.name, maxConsumption: consumption });
    toast.success('Device created successfully');
    setIsCreateOpen(false);
    resetForm();
  };

  const handleUpdate = () => {
    if (!editingDevice) return;
    if (!formData.name || !formData.maxConsumption) {
      toast.error('Please fill in all fields');
      return;
    }
    const consumption = parseFloat(formData.maxConsumption);
    if (isNaN(consumption) || consumption <= 0) {
      toast.error('Please enter a valid max consumption value');
      return;
    }
    updateDevice(editingDevice.id, {
      name: formData.name,
      maxConsumption: consumption,
    });
    toast.success('Device updated successfully');
    setEditingDevice(null);
    resetForm();
  };

  const handleDelete = (id: string) => {
    deleteDevice(id);
    toast.success('Device deleted successfully');
  };

  const openEditDialog = (device: Device) => {
    setEditingDevice(device);
    setFormData({
      name: device.name,
      maxConsumption: device.maxConsumption.toString(),
    });
  };

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0">
        <CardTitle>Device Management</CardTitle>
        <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
          <DialogTrigger asChild>
            <Button onClick={resetForm}>
              <Plus className="mr-2 h-4 w-4" />
              Add Device
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Create New Device</DialogTitle>
              <DialogDescription>
                Add a new device to the system
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="create-device-name">Device Name</Label>
                <Input
                  id="create-device-name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="e.g., Air Conditioner"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="create-consumption">Max Consumption (W)</Label>
                <Input
                  id="create-consumption"
                  type="number"
                  value={formData.maxConsumption}
                  onChange={(e) => setFormData({ ...formData, maxConsumption: e.target.value })}
                  placeholder="e.g., 3500"
                />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setIsCreateOpen(false)}>
                Cancel
              </Button>
              <Button onClick={handleCreate}>Create Device</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Device Name</TableHead>
              <TableHead>Max Consumption (W)</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {devices.map((device) => (
              <TableRow key={device.id}>
                <TableCell>{device.name}</TableCell>
                <TableCell>{device.maxConsumption} W</TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-2">
                    <Dialog
                      open={editingDevice?.id === device.id}
                      onOpenChange={(open) => {
                        if (!open) {
                          setEditingDevice(null);
                          resetForm();
                        }
                      }}
                    >
                      <DialogTrigger asChild>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => openEditDialog(device)}
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                      </DialogTrigger>
                      <DialogContent>
                        <DialogHeader>
                          <DialogTitle>Edit Device</DialogTitle>
                          <DialogDescription>
                            Update device information
                          </DialogDescription>
                        </DialogHeader>
                        <div className="space-y-4 py-4">
                          <div className="space-y-2">
                            <Label htmlFor="edit-device-name">Device Name</Label>
                            <Input
                              id="edit-device-name"
                              value={formData.name}
                              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                            />
                          </div>
                          <div className="space-y-2">
                            <Label htmlFor="edit-consumption">Max Consumption (W)</Label>
                            <Input
                              id="edit-consumption"
                              type="number"
                              value={formData.maxConsumption}
                              onChange={(e) => setFormData({ ...formData, maxConsumption: e.target.value })}
                            />
                          </div>
                        </div>
                        <DialogFooter>
                          <Button variant="outline" onClick={() => setEditingDevice(null)}>
                            Cancel
                          </Button>
                          <Button onClick={handleUpdate}>Update Device</Button>
                        </DialogFooter>
                      </DialogContent>
                    </Dialog>
                    <AlertDialog>
                      <AlertDialogTrigger asChild>
                        <Button variant="ghost" size="sm">
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </AlertDialogTrigger>
                      <AlertDialogContent>
                        <AlertDialogHeader>
                          <AlertDialogTitle>Delete Device</AlertDialogTitle>
                          <AlertDialogDescription>
                            Are you sure you want to delete {device.name}? This will remove all assignments and cannot be undone.
                          </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                          <AlertDialogCancel>Cancel</AlertDialogCancel>
                          <AlertDialogAction onClick={() => handleDelete(device.id)}>
                            Delete
                          </AlertDialogAction>
                        </AlertDialogFooter>
                      </AlertDialogContent>
                    </AlertDialog>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
};
