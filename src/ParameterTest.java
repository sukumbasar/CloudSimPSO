import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;
import java.util.*;
import java.io.*;

public class ParameterTest {

    static int[] swarmSizes = {30, 50, 100};
    static int[] iterationCounts = {100, 500, 1000};

    public static void main(String[] args) throws Exception {
        StringBuilder csv = new StringBuilder();
        csv.append("SwarmSize,Iterations,Makespan\n");

        for (int swarm : swarmSizes) {
            for (int iters : iterationCounts) {
                double makespan = runTest(100, 10, swarm, iters);
                csv.append(swarm).append(",")
                   .append(iters).append(",")
                   .append(String.format("%.2f", makespan)).append("\n");
            }
        }

        PrintWriter pw = new PrintWriter("parameter_matrix.csv");
        pw.print(csv.toString());
        pw.close();
    }

    private static double runTest(int taskCount, int vmCount,
                                   int particleCount, int maxIterations) throws Exception {
        CloudSim.init(1, Calendar.getInstance(), false);
        DatacenterBroker broker = new DatacenterBroker("Broker");
        int brokerId = broker.getId();

        createDatacenter("DC");

        List<Vm> vmList = DataGenerator.createVMs(brokerId, vmCount);
        List<Cloudlet> cloudletList = DataGenerator.createCloudlets(brokerId, taskCount);

        cloudletList = runPSOWithParams(cloudletList, vmList, particleCount, maxIterations);

        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);

        CloudSim.startSimulation();
        CloudSim.stopSimulation();

        List<Cloudlet> finished = broker.getCloudletReceivedList();
        double makespan = 0;
        for (Cloudlet c : finished) {
            if (c.getFinishTime() > makespan) makespan = c.getFinishTime();
        }
        return makespan;
    }

    private static List<Cloudlet> runPSOWithParams(List<Cloudlet> cloudletList,
                                                    List<Vm> vmList,
                                                    int particleCount,
                                                    int maxIterations) {
        int taskCount = cloudletList.size();
        int vmCount = vmList.size();
        Random random = new Random(42);

        double W_MAX = 0.9, W_MIN = 0.4, C1 = 1.5, C2 = 1.5;

        int[][] particles = new int[particleCount][taskCount];
        double[][] velocities = new double[particleCount][taskCount];
        int[][] pBest = new int[particleCount][taskCount];
        double[] pBestFitness = new double[particleCount];
        int[] gBest = new int[taskCount];
        double gBestFitness = Double.MAX_VALUE;

        int hybridCount = Math.min(5, particleCount);
        for (int i = 0; i < hybridCount; i++) {
            particles[i] = generateMaxMin(cloudletList, vmList, i, random);
            for (int j = 0; j < taskCount; j++)
                velocities[i][j] = (random.nextDouble() - 0.5) * 0.1;
            pBest[i] = particles[i].clone();
            pBestFitness[i] = fitness(particles[i], cloudletList, vmList);
            if (pBestFitness[i] < gBestFitness) {
                gBestFitness = pBestFitness[i];
                gBest = particles[i].clone();
            }
        }

        for (int i = hybridCount; i < particleCount; i++) {
            for (int j = 0; j < taskCount; j++) {
                particles[i][j] = random.nextInt(vmCount);
                velocities[i][j] = (random.nextDouble() - 0.5) * 0.2;
            }
            pBest[i] = particles[i].clone();
            pBestFitness[i] = fitness(particles[i], cloudletList, vmList);
            if (pBestFitness[i] < gBestFitness) {
                gBestFitness = pBestFitness[i];
                gBest = particles[i].clone();
            }
        }

        for (int iter = 0; iter < maxIterations; iter++) {
            double w = W_MAX - (W_MAX - W_MIN) * iter / maxIterations;
            for (int i = 0; i < particleCount; i++) {
                for (int j = 0; j < taskCount; j++) {
                    double r1 = random.nextDouble(), r2 = random.nextDouble();
                    velocities[i][j] = w * velocities[i][j]
                        + C1 * r1 * (pBest[i][j] - particles[i][j])
                        + C2 * r2 * (gBest[j] - particles[i][j]);
                    double vMax = vmCount * 0.5;
                    velocities[i][j] = Math.max(-vMax, Math.min(vMax, velocities[i][j]));
                    int newPos = (int) Math.round(particles[i][j] + velocities[i][j]);
                    particles[i][j] = Math.max(0, Math.min(vmCount - 1, newPos));
                }
                double f = fitness(particles[i], cloudletList, vmList);
                if (f < pBestFitness[i]) { pBest[i] = particles[i].clone(); pBestFitness[i] = f; }
                if (f < gBestFitness) { gBestFitness = f; gBest = particles[i].clone(); }
            }
        }

        for (int j = 0; j < taskCount; j++)
            cloudletList.get(j).setVmId(vmList.get(gBest[j]).getId());
        return cloudletList;
    }

    private static int[] generateMaxMin(List<Cloudlet> cloudletList, List<Vm> vmList,
                                         int variationId, Random random) {
        int taskCount = cloudletList.size();
        int vmCount = vmList.size();
        int[] solution = new int[taskCount];
        double[] vmLoad = new double[vmCount];

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) indices.add(i);
        indices.sort((a, b) -> Long.compare(
            cloudletList.get(b).getCloudletLength(),
            cloudletList.get(a).getCloudletLength()));

        for (int idx : indices) {
            int bestVm = 0;
            double minFinish = Double.MAX_VALUE;
            for (int j = 0; j < vmCount; j++) {
                double finish = vmLoad[j] +
                    (double) cloudletList.get(idx).getCloudletLength() / vmList.get(j).getMips();
                if (finish < minFinish) { minFinish = finish; bestVm = j; }
            }
            if (variationId > 0 && random.nextDouble() < 0.1 * variationId)
                bestVm = random.nextInt(vmCount);
            solution[idx] = bestVm;
            vmLoad[bestVm] += (double) cloudletList.get(idx).getCloudletLength()
                              / vmList.get(bestVm).getMips();
        }
        return solution;
    }

    private static double fitness(int[] pos, List<Cloudlet> cloudletList, List<Vm> vmList) {
        double[] vmTime = new double[vmList.size()];
        for (int j = 0; j < cloudletList.size(); j++) {
            vmTime[pos[j]] += (double) cloudletList.get(j).getCloudletLength()
                               / vmList.get(pos[j]).getMips();
        }
        double max = 0;
        for (double t : vmTime) if (t > max) max = t;
        return max;
    }

    private static Datacenter createDatacenter(String name) throws Exception {
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(500000)));
        List<Host> hostList = new ArrayList<>();
        hostList.add(new Host(0,
            new RamProvisionerSimple(1000000),
            new BwProvisionerSimple(10000000),
            1000000, peList,
            new VmSchedulerTimeShared(peList)));
        DatacenterCharacteristics dc = new DatacenterCharacteristics(
            "x86", "Linux", "Xen", hostList, 10.0, 3.0, 0.05, 0.1, 0.1);
        return new Datacenter(name, dc,
            new VmAllocationPolicySimple(hostList), new LinkedList<>(), 0);
    }
}