import java.io.*;
import java.util.*;

public class ResultLogger {
    
    private static final String CSV_FILE = "results.csv";
    private List<String[]> records = new ArrayList<>();
    
    public ResultLogger() {
        // Başlık satırı
        records.add(new String[]{"Algorithm", "TaskCount", "VMCount", "Makespan", "AvgUtilization"});
    }
    
    public void addRecord(String algorithm, int taskCount, int vmCount, 
                          double makespan, double avgUtilization) {
        records.add(new String[]{
            algorithm,
            String.valueOf(taskCount),
            String.valueOf(vmCount),
            String.format("%.2f", makespan),
            String.format("%.2f", avgUtilization)
        });
        
        // Terminale de yazdır
        System.out.printf("%-10s | Tasks: %4d | VMs: %2d | Makespan: %8.2f | Utilization: %6.2f%%%n",
            algorithm, taskCount, vmCount, makespan, avgUtilization);
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