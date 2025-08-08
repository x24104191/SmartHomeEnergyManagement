package smarthome;

import org.json.JSONObject;

// Processes tasks at edge node
public class EdgeNode {
    private String nodeId;

    // Constructor
    public EdgeNode(String nodeId) {
        this.nodeId = nodeId;
    }

    // Process task based on priority
    public String processTask(String rawData) {
        JSONObject task = new JSONObject(rawData);
        int priority = task.getInt("priority");
        String taskType = task.getString("task_type");
        double data = task.getDouble("data");
        int sensorId = task.getInt("sensor_id");
        long timestamp = task.getLong("timestamp");

        // High-priority tasks (1-2) processed locally
        if (priority <= 2) {
            String result = String.format("{\"sensor_id\": %d, \"timestamp\": %d, \"action\": \"Processed %s: %.2f\"}",
                    sensorId, timestamp, taskType, data);
            System.out.println("EdgeNode " + nodeId + " processed: " + result);
            return result;
        } else {
            // Low-priority tasks offloaded to cloud
            String offload = String.format("{\"offload\": true, \"data\": %s}", rawData);
            System.out.println("EdgeNode " + nodeId + " offloading: " + offload);
            return offload;
        }
    }

    // Forward offloaded tasks to cloud
    public String forwardToCloud(String taskData) {
        System.out.println("EdgeNode " + nodeId + " forwarding to cloud: " + taskData);
        return taskData;
    }
}