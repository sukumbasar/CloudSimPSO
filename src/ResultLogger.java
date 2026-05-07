import java.io.*;
import java.util.*;

public class ResultLogger {
    
    private static final String CSV_FILE = "results.csv";
    private List<String[]> records = new ArrayList<>();
    
    public ResultLogger() {
        records.add(new String[]{"Algorithm", "TaskCount", "VMCount", "Makespan", "AvgUtilization", "PSO_Internal_Makespan", "Rejuvenations"});
    }
    
    public void addRecord(String algorithm, int taskCount, int vmCount, 
                          double makespan, double avgUtilization) {
        String internalMakespan = algorithm.equals("PSO") ? 
            String.format("%.2f", Scheduler_PSO.lastFitness) : "N/A";
        String rejuvenations = algorithm.equals("PSO") ? 
            String.valueOf(Scheduler_PSO.lastRejuvenations) : "N/A";

        records.add(new String[]{
            algorithm,
            String.valueOf(taskCount),
            String.valueOf(vmCount),
            String.format("%.2f", makespan),
            String.format("%.2f", avgUtilization),
            internalMakespan,
            rejuvenations
        });
        
        System.out.printf("%-10s | Tasks: %4d | VMs: %2d | Makespan: %8.2f | Utilization: %6.2f%% | PSO Internal: %s%n",
            algorithm, taskCount, vmCount, makespan, avgUtilization, internalMakespan);
    }
    
    public void saveToCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_FILE))) {
            for (String[] record : records) {
                pw.println(String.join(",", record));
            }
            System.out.println("\nSonuçlar kaydedildi: " + CSV_FILE);
        } catch (IOException e) {
            System.err.println("CSV kaydetme hatası: " + e.getMessage());
        }
    }
    
    public List<String[]> getRecords() {
        return records;
    }
}