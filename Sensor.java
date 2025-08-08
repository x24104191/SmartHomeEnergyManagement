package smarthome;

import java.util.Random;

// Simulates a smart home sensor generating tasks
public class Sensor {
    private int sensorId;
    private Random random;

    // Constructor
    public Sensor(int sensorId) {
        this.sensorId = sensorId;
        this.random = new Random();
    }

    // Generate task data
    public String generateTask() {
        String[] taskTypes = {"power_usage", "temperature", "humidity"};
        String taskType = taskTypes[random.nextInt(taskTypes.length)];
        double data = 50 + random.nextDouble() * 150; // e.g., power in watts
        int priority = random.nextInt(5) + 1; // 1=high, 5=low
        long timestamp = System.currentTimeMillis();

        // Create JSON-like task string
        String task = String.format("{\"sensor_id\": %d, \"timestamp\": %d, \"task_type\": \"%s\", \"data\": %.2f, \"priority\": %d}",
                sensorId, timestamp, taskType, data, priority);
        System.out.println("Sensor " + sensorId + " generated task: " + task);
        return task;
    }

    // Simulate sending task to edge node with 5G latency
    public String sendToEdge(String edgeNodeId) {
        String task = generateTask();
        try {
            Thread.sleep(1); // Simulate 5G latency (1ms)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Sensor " + sensorId + " sending to " + edgeNodeId + ": " + task);
        return task;
    }
}