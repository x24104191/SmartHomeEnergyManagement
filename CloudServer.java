package smarthome;

import org.json.JSONObject;
import java.util.List;

// Aggregates and analyzes tasks at cloud
public class CloudServer {
    // Aggregate task data
    public String aggregateTasks(List<String> taskList) {
        int totalTasks = taskList.size();
        double totalPower = 0;
        int powerTaskCount = 0;

        for (String task : taskList) {
            JSONObject data = new JSONObject(task);
            if (data.has("data")) {
                JSONObject taskData = data.getJSONObject("data");
                if (taskData.getString("task_type").equals("power_usage")) {
                    totalPower += taskData.getDouble("data");
                    powerTaskCount++;
                }
            }
        }

        double avgPower = powerTaskCount > 0 ? totalPower / powerTaskCount : 0;
        String summary = String.format("{\"total_tasks\": %d, \"avg_power_usage\": %.2f}", totalTasks, avgPower);
        System.out.println("CloudServer analytics: " + summary);
        return summary;
    }
}