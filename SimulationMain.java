import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SimulationMain {

    static class Task {
        int sensorId;
        long timestamp;
        String taskType;
        double data;
        int priority;

        public Task(int sensorId, String taskType, double data, int priority) {
            this.sensorId = sensorId;
            this.taskType = taskType;
            this.data = data;
            this.priority = priority;
            this.timestamp = System.currentTimeMillis();
        }

        public String toJson() {
            return String.format("{\"sensor_id\": %d, \"timestamp\": %d, \"task_type\": \"%s\", \"data\": %.2f, \"priority\": %d}",
                    sensorId, timestamp, taskType, data, priority);
        }
    }

    static class Sensor {
        private int sensorId;
        private Random random = new Random();

        public Sensor(int sensorId) {
            this.sensorId = sensorId;
        }

        public Task generateTask() {
            String[] taskTypes = {"power_usage", "temperature", "humidity"};
            String taskType = taskTypes[random.nextInt(taskTypes.length)];
            double data = 50 + random.nextDouble() * 150;
            int priority = random.nextInt(5) + 1;
            return new Task(sensorId, taskType, data, priority);
        }
    }

    static class EdgeNode {
        private String nodeId;

        public EdgeNode(String nodeId) {
            this.nodeId = nodeId;
        }

        public String processTask(Task task, boolean cloudOnly) {
            if (!cloudOnly && task.priority <= 2) {
                return String.format("{\"sensor_id\": %d, \"timestamp\": %d, \"action\": \"Processed %s: %.2f at Edge\"}",
                        task.sensorId, task.timestamp, task.taskType, task.data);
            } else {
                return String.format("{\"offload\": true, \"data\": %s}", task.toJson());
            }
        }
    }

    static class CloudServer {
        public String processTask(String raw) {
            int id = extractInt(raw, "\"sensor_id\": ");
            long ts = extractLong(raw, "\"timestamp\": ");
            String type = extractString(raw, "\"task_type\": \"");
            double val = extractDouble(raw, "\"data\": ");
            return String.format("{\"sensor_id\": %d, \"timestamp\": %d, \"action\": \"Processed %s: %.2f at Cloud\"}",
                    id, ts, type, val);
        }

        public void exportResults(List<String> rows, String filename) {
            try (FileWriter writer = new FileWriter(filename)) {
                writer.write("Scenario,Latency(ms),Result\n");
                for (String row : rows) {
                    writer.write(row + "\n");
                }
                System.out.println("📁 CSV exported: " + filename);
            } catch (IOException e) {
                System.err.println("CSV export failed.");
            }
        }

        public void drawLatencyChart(List<Double> latencies, String scenario) {
            System.out.println("\n📊 Latency Chart (" + scenario + "):");
            double max = latencies.stream().mapToDouble(v -> v).max().orElse(1);
            for (int i = 0; i < latencies.size(); i++) {
                int bar = (int) (latencies.get(i) / max * 50);
                System.out.printf("Task %2d: %s %.2f ms\n", i + 1, "*".repeat(bar), latencies.get(i));
            }
        }

        private static int extractInt(String str, String key) {
            try {
                int start = str.indexOf(key) + key.length();
                int end = str.indexOf(",", start);
                if (end == -1) end = str.indexOf("}", start);
                return Integer.parseInt(str.substring(start, end).trim());
            } catch (Exception e) {
                return -1;
            }
        }

        private static long extractLong(String str, String key) {
            try {
                int start = str.indexOf(key) + key.length();
                int end = str.indexOf(",", start);
                if (end == -1) end = str.indexOf("}", start);
                return Long.parseLong(str.substring(start, end).trim());
            } catch (Exception e) {
                return -1;
            }
        }

        private static double extractDouble(String str, String key) {
            try {
                int start = str.indexOf(key) + key.length();
                int end = str.indexOf(",", start);
                if (end == -1) end = str.indexOf("}", start);
                return Double.parseDouble(str.substring(start, end).trim());
            } catch (Exception e) {
                return 0;
            }
        }

        private static String extractString(String str, String key) {
            try {
                int start = str.indexOf(key) + key.length();
                int end = str.indexOf("\"", start);
                return str.substring(start, end);
            } catch (Exception e) {
                return "";
            }
        }
    }

    public static void main(String[] args) {
        Sensor sensor = new Sensor(1);
        EdgeNode edge = new EdgeNode("EdgeNode1");
        CloudServer cloud = new CloudServer();

        List<Double> edgeLatencies = new ArrayList<>();
        List<Double> cloudLatencies = new ArrayList<>();
        List<String> csvRows = new ArrayList<>();

        System.out.println("=== Edge Processing Scenario ===");
        int total = 10, success = 0, offloaded = 0;

        for (int i = 0; i < total; i++) {
            Task task = sensor.generateTask();
            long start = System.nanoTime();
            String result = edge.processTask(task, false);
            long end = System.nanoTime();
            double latency = (end - start) / 1_000_000.0;
            edgeLatencies.add(latency);
            if (result.contains("offload")) {
                result = cloud.processTask(result);
                offloaded++;
            } else {
                success++;
            }
            csvRows.add("Edge," + String.format("%.2f", latency) + "," + result.replace(",", ";"));
        }

        printResults("Edge", edgeLatencies, total, success, offloaded);

        System.out.println("\n=== Cloud-Only Processing Scenario ===");
        total = 10; success = 0; offloaded = 0;
        for (int i = 0; i < total; i++) {
            Task task = sensor.generateTask();
            long start = System.nanoTime();
            String result = edge.processTask(task, true);
            long end = System.nanoTime();
            double latency = (end - start) / 1_000_000.0;
            cloudLatencies.add(latency);
            if (result.contains("offload")) {
                result = cloud.processTask(result);
                offloaded++;
            }
            success++;
            csvRows.add("Cloud," + String.format("%.2f", latency) + "," + result.replace(",", ";"));
        }

        printResults("Cloud", cloudLatencies, total, success, offloaded);

        cloud.drawLatencyChart(edgeLatencies, "Edge");
        cloud.drawLatencyChart(cloudLatencies, "Cloud");
        cloud.exportResults(csvRows, "simulation_results.csv");
    }

    static void printResults(String label, List<Double> latencies, int total, int success, int offload) {
        double avg = latencies.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        System.out.printf("\n%-25s | %s\n", label + " Scenario Results", "-".repeat(40));
        System.out.printf("Total Tasks         : %d\n", total);
        System.out.printf("Successful Tasks    : %d\n", success);
        System.out.printf("Offloaded to Cloud  : %d\n", offload);
        System.out.printf("Avg Task Latency    : %.2f ms\n", avg);
        System.out.printf("Data Reduction      : %.2f %%\n", ((total - offload) * 100.0 / total));
    }
}
