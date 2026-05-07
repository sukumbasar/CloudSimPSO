import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;
import java.util.*;

public class SimulationEngine {

    private ResultLogger logger;
    int[] taskCounts = {100, 500, 1000};
    int[] vmCounts = {10, 25, 50};

    public SimulationEngine() {
        this.logger = new ResultLogger();
    }

    public void runAllScenarios() {
        System.out.println("=== CloudSim Simülasyonu Başlıyor ===\n");

        for (int taskCount : taskCounts) {
            for (int vmCount : vmCounts) {
                System.out.println("\n--- Senaryo: " + taskCount + " Task, " + vmCount + " VM ---");
                runScenario(taskCount, vmCount, "FCFS");
                runScenario(taskCount, vmCount, "RR");
                runScenario(taskCount, vmCount, "SJF");
                runScenario(taskCount, vmCount, "MINMIN");
                runScenario(taskCount, vmCount, "MAXMIN");
                runScenario(taskCount, vmCount, "PSO");
            }
        }

        logger.saveToCSV();
        System.out.println("\n=== Simülasyon Tamamlandı ===");
    }

    private void runScenario(int taskCount, int vmCount, String algorithm) {
        try {
            CloudSim.init(1, Calendar.getInstance(), false);

            DatacenterBroker broker = new DatacenterBroker("Broker");
            int brokerId = broker.getId();

            createDatacenter("Datacenter");

            List<Vm> vmList = DataGenerator.createVMs(brokerId, vmCount);
            List<Cloudlet> cloudletList = DataGenerator.createCloudlets(brokerId, taskCount);

            cloudletList = Switchboard.applyAlgorithm(algorithm, cloudletList, vmList);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            List<Cloudlet> finishedCloudlets = broker.getCloudletReceivedList();

            double makespan = calculateMakespan(finishedCloudlets);
            double avgUtilization = calculateUtilization(finishedCloudlets, vmList, makespan);

            logger.addRecord(algorithm, taskCount, vmCount, makespan, avgUtilization);

        } catch (Exception e) {
            System.err.println("Hata: " + e.getMessage());
        }
    }

    private double calculateMakespan(List<Cloudlet> cloudlets) {
        double max = 0;
        for (Cloudlet c : cloudlets) {
            if (c.getFinishTime() > max) {
                max = c.getFinishTime();
            }
        }
        return max;
    }

    private double calculateUtilization(List<Cloudlet> cloudlets, List<Vm> vmList, double makespan) {
        if (makespan <= 0) return 0;

        Map<Integer, Double> vmActiveTime = new HashMap<>();
        for (Vm vm : vmList) {
            vmActiveTime.put(vm.getId(), 0.0);
        }

        for (Cloudlet c : cloudlets) {
            int vmId = c.getVmId();
            double execTime = c.getActualCPUTime();
            vmActiveTime.put(vmId, vmActiveTime.getOrDefault(vmId, 0.0) + execTime);
        }

        double totalUtil = 0;
        for (double activeTime : vmActiveTime.values()) {
            double util = Math.min(1.0, activeTime / makespan);
            totalUtil += util;
        }

        return (totalUtil / vmList.size()) * 100.0;
    }

    private Datacenter createDatacenter(String name) throws Exception {
        List<Host> hostList = new ArrayList<>();

        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(500000)));

        hostList.add(new Host(0,
                new RamProvisionerSimple(1000000),
                new BwProvisionerSimple(10000000),
                1000000,
                peList,
                new VmSchedulerTimeShared(peList)));

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86", "Linux", "Xen", hostList, 10.0,
                3.0, 0.05, 0.1, 0.1);

        return new Datacenter(name, characteristics,
                new VmAllocationPolicySimple(hostList),
                new LinkedList<>(), 0);
    }

    public ResultLogger getLogger() {
        return logger;
    }
}