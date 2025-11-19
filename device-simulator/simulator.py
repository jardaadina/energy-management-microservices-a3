import pika
import json
import random
import time
from datetime import datetime
import sys

class DeviceSimulator:
    def __init__(self, config_file='config.json'):
        # Load configuration
        with open(config_file, 'r') as f:
            self.config = json.load(f)

        self.device_id = self.config['device_id']
        self.rabbitmq_host = self.config['rabbitmq']['host']
        self.rabbitmq_port = self.config['rabbitmq']['port']
        self.rabbitmq_user = self.config['rabbitmq']['username']
        self.rabbitmq_pass = self.config['rabbitmq']['password']
        self.exchange = self.config['rabbitmq']['exchange']
        self.routing_key = self.config['rabbitmq']['routing_key']

        # Simulation parameters
        self.base_load = self.config['simulation']['base_load']
        self.peak_hours = self.config['simulation']['peak_hours']
        self.night_hours = self.config['simulation']['night_hours']
        self.interval_seconds = self.config['simulation']['interval_seconds']

        # Initialize RabbitMQ connection
        self.connection = None
        self.channel = None
        self.connect_to_rabbitmq()

    def connect_to_rabbitmq(self):
        """Establish connection to RabbitMQ"""
        try:
            credentials = pika.PlainCredentials(self.rabbitmq_user, self.rabbitmq_pass)
            parameters = pika.ConnectionParameters(
                host=self.rabbitmq_host,
                port=self.rabbitmq_port,
                credentials=credentials,
                heartbeat=600,
                blocked_connection_timeout=300
            )
            self.connection = pika.BlockingConnection(parameters)
            self.channel = self.connection.channel()
            print(f"✅ Connected to RabbitMQ at {self.rabbitmq_host}:{self.rabbitmq_port}")
        except Exception as e:
            print(f"❌ Failed to connect to RabbitMQ: {e}")
            sys.exit(1)

    def calculate_consumption(self):
        """
        Calculate energy consumption based on time of day.
        Returns consumption in kWh for a 10-minute interval.
        """
        current_hour = datetime.now().hour

        # Base consumption
        consumption = self.base_load

        # Peak hours (higher consumption)
        if current_hour in self.peak_hours:
            consumption += random.uniform(0.2, 0.5)

        # Night hours (lower consumption)
        elif current_hour in self.night_hours:
            consumption -= random.uniform(0.1, 0.3)

        # Add random variation
        consumption += random.uniform(-0.1, 0.1)

        # Ensure consumption is positive
        return max(0.05, round(consumption, 2))

    def send_measurement(self):
        """Generate and send a measurement to RabbitMQ"""
        timestamp = datetime.now().strftime('%Y-%m-%dT%H:%M:%S')
        measurement_value = self.calculate_consumption()

        message = {
            "timestamp": timestamp,
            "deviceId": self.device_id,
            "measurementValue": measurement_value
        }

        try:
            self.channel.basic_publish(
                exchange=self.exchange,
                routing_key=self.routing_key,
                body=json.dumps(message),
                properties=pika.BasicProperties(
                    delivery_mode=2,  # Persistent message
                    content_type='application/json'
                )
            )
            print(f"📤 Sent: Device {self.device_id} | {timestamp} | {measurement_value} kWh")
        except Exception as e:
            print(f"❌ Failed to send message: {e}")
            self.connect_to_rabbitmq()  # Reconnect on failure

    def run(self):
        """Run the simulator continuously"""
        print(f"🚀 Starting Device Simulator for Device ID: {self.device_id}")
        print(f"⏱️  Sending measurements every {self.interval_seconds} seconds")
        print("Press Ctrl+C to stop\n")

        try:
            while True:
                self.send_measurement()
                time.sleep(self.interval_seconds)
        except KeyboardInterrupt:
            print("\n⛔ Simulator stopped by user")
        finally:
            if self.connection and not self.connection.is_closed:
                self.connection.close()
                print("✅ Connection closed")

if __name__ == "__main__":
    simulator = DeviceSimulator()
    simulator.run()