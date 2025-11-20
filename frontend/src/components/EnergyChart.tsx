import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    BarElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';
import { Line, Bar } from 'react-chartjs-2';
import { toast } from 'sonner';
import { Activity, Calendar, TrendingUp } from 'lucide-react';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    BarElement,
    Title,
    Tooltip,
    Legend
);

type HourlyConsumption = {
    id: number;
    deviceId: number;
    timestamp: string;
    totalConsumption: number;
};

type EnergyChartProps = {
    deviceId: string;
};

export default function EnergyChart({ deviceId }: EnergyChartProps) {
    const [chartData, setChartData] = useState<any>(null);
    const [selectedDate, setSelectedDate] = useState(
        new Date().toISOString().split('T')[0]
    );
    const [chartType, setChartType] = useState<'line' | 'bar'>('line');
    const [loading, setLoading] = useState(false);
    const [totalDailyConsumption, setTotalDailyConsumption] = useState(0);

    useEffect(() => {
        fetchData();
    }, [selectedDate, deviceId]);

    const fetchData = async () => {
        setLoading(true);

        try {
            const token = localStorage.getItem('jwtToken');
            const response = await fetch(
                `http://localhost/monitoring/device/${deviceId}/consumption?date=${selectedDate}`,
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (!response.ok) {
                throw new Error('Failed to fetch energy data');
            }

            const data: HourlyConsumption[] = await response.json();

            if (data.length === 0) {
                setChartData(null);
                setTotalDailyConsumption(0);
                toast.info('No data available for this date');
                return;
            }

            const labels = data.map(item => {
                const date = new Date(item.timestamp);
                const hour = date.getHours();
                return `${hour.toString().padStart(2, '0')}:00`;
            });

            const values = data.map(item => item.totalConsumption);
            const total = values.reduce((sum, val) => sum + val, 0);
            setTotalDailyConsumption(Number(total.toFixed(2)));

            setChartData({
                labels,
                datasets: [
                    {
                        label: 'Energy Consumption (kWh)',
                        data: values,
                        borderColor: 'rgb(99, 102, 241)',
                        backgroundColor: 'rgba(99, 102, 241, 0.6)',
                        borderWidth: 3,
                        fill: true,
                        tension: 0.4,
                        pointRadius: 6,
                        pointHoverRadius: 8,
                        pointBackgroundColor: 'rgb(99, 102, 241)',
                        pointBorderColor: '#fff',
                        pointBorderWidth: 2,
                    }
                ]
            });
        } catch (error) {
            console.error('Error fetching energy data:', error);
            toast.error('Failed to load energy consumption data');
            setChartData(null);
        } finally {
            setLoading(false);
        }
    };

    const options: any = {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
            mode: 'index',
            intersect: false,
        },
        plugins: {
            legend: {
                display: false,
            },
            title: {
                display: false,
            },
            tooltip: {
                backgroundColor: 'rgba(0, 0, 0, 0.8)',
                padding: 16,
                titleFont: {
                    size: 16,
                    weight: 'bold',
                },
                bodyFont: {
                    size: 14,
                },
                callbacks: {
                    label: function(context: any) {
                        const value = context.parsed?.y ?? 0;
                        return `Energy: ${value.toFixed(2)} kWh`;
                    }
                }
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                grid: {
                    color: 'rgba(0, 0, 0, 0.08)',
                    lineWidth: 1,
                },
                ticks: {
                    font: {
                        size: 14,
                    },
                    callback: function(value: any) {
                        return `${Number(value).toFixed(1)} kWh`;
                    },
                    padding: 10,
                },
                title: {
                    display: true,
                    text: 'Consumption (kWh)',
                    font: {
                        size: 14,
                        weight: 'bold' as const,
                    },
                    padding: 16,
                }
            },
            x: {
                grid: {
                    display: false,
                },
                ticks: {
                    font: {
                        size: 13,
                    },
                    padding: 8,
                },
                title: {
                    display: true,
                    text: 'Hour of Day',
                    font: {
                        size: 14,
                        weight: 'bold' as const,
                    },
                    padding: 12,
                }
            }
        }
    };

    return (
        <Card className="mt-6">
            <CardHeader className="pb-4">
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        <Activity className="h-5 w-5 text-indigo-600" />
                        <CardTitle className="text-lg">Energy Consumption Chart</CardTitle>
                    </div>
                    {totalDailyConsumption > 0 && (
                        <div className="flex items-center gap-2 text-sm">
                            <TrendingUp className="h-5 w-5 text-indigo-600" />
                            <span className="font-bold text-indigo-600 text-lg">
                                {totalDailyConsumption} kWh
                            </span>
                            <span className="text-muted-foreground">daily total</span>
                        </div>
                    )}
                </div>
            </CardHeader>
            <CardContent className="pb-8">
                <div className="flex flex-wrap gap-4 mb-8 items-end">
                    <div className="flex-1 min-w-[200px]">
                        <label className="text-sm font-medium mb-2 flex items-center gap-2">
                            <Calendar className="h-4 w-4" />
                            Select Date
                        </label>
                        <Input
                            type="date"
                            value={selectedDate}
                            onChange={(e) => setSelectedDate(e.target.value)}
                            max={new Date().toISOString().split('T')[0]}
                            className="h-11 text-base"
                        />
                    </div>

                    <div className="flex-1 min-w-[150px]">
                        <label className="text-sm font-medium mb-2 block">Chart Type</label>
                        <Select value={chartType} onValueChange={(val) => setChartType(val as 'line' | 'bar')}>
                            <SelectTrigger className="h-11 text-base">
                                <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="line">Line Chart</SelectItem>
                                <SelectItem value="bar">Bar Chart</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>

                    <Button onClick={fetchData} disabled={loading} className="h-11 px-6">
                        {loading ? 'Loading...' : 'Refresh Data'}
                    </Button>
                </div>

                <div className="w-full border rounded-lg p-4 bg-white" style={{ height: '600px' }}>
                    {loading ? (
                        <div className="flex items-center justify-center h-full">
                            <div className="text-muted-foreground text-lg">Loading chart data...</div>
                        </div>
                    ) : !chartData ? (
                        <div className="flex flex-col items-center justify-center h-full text-muted-foreground">
                            <Activity className="h-16 w-16 mb-4 opacity-50" />
                            <p className="text-lg font-medium">No consumption data available for this date</p>
                            <p className="text-sm mt-2">Try selecting a different date or wait for data to be collected</p>
                        </div>
                    ) : (
                        <div style={{ height: '100%', width: '100%' }}>
                            {chartType === 'line' ? (
                                <Line options={options} data={chartData} />
                            ) : (
                                <Bar options={options} data={chartData} />
                            )}
                        </div>
                    )}
                </div>
            </CardContent>
        </Card>
    );
}