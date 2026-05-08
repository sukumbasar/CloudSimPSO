import org.cloudbus.cloudsim.*;
import java.util.*;

public class Scheduler_PSO {

    private static final int MAX_ITERATIONS = 200;
    private static final double W_MAX = 0.9;
    private static final double W_MIN = 0.4;
    private static final double C1 = 1.5;
    private static final double C2 = 1.5;
    private static final int HYBRID_PARTICLES = 10;
    private static final double ALPHA = 0.7;
    private static final double BETA = 0.3;
    private static final int MAX_AGE = 15;
    private static final double CAUCHY_PROB = 0.1;
    private static final int NEIGHBORHOOD_SIZE = 3; // Lbest komşuluk boyutu
    private static final Random random = new Random(42);

    public static double lastFitness = 0.0;
    public static int lastRejuvenations = 0;

    public static List<Cloudlet> applyPSO(List<Cloudlet> cloudletList, List<Vm> vmList) {
        int taskCount = cloudletList.size();
        int vmCount = vmList.size();
        int PARTICLE_COUNT = Math.max(50, taskCount / 10);

        int[][] particles = new int[PARTICLE_COUNT][taskCount];
        double[][] velocities = new double[PARTICLE_COUNT][taskCount];
        int[][] pBest = new int[PARTICLE_COUNT][taskCount];
        double[] pBestFitness = new double[PARTICLE_COUNT];
        int[][] lBest = new int[PARTICLE_COUNT][taskCount]; // Her parçacığın komşuluk en iyisi
        int[] gBest = new int[taskCount];
        double gBestFitness = Double.MAX_VALUE;
        int[] age = new int[PARTICLE_COUNT];

        // 1. İlk 5 parçacık Max-Min varyasyonlarıyla başlat
        for (int i = 0; i < 5; i++) {
            particles[i] = generateMaxMinVariation(cloudletList, vmList, i);
            for (int j = 0; j < taskCount; j++)
                velocities[i][j] = (random.nextDouble() - 0.5) * 0.1;
            pBest[i] = particles[i].clone();
            pBestFitness[i] = calculateFitness(particles[i], cloudletList, vmList);
            age[i] = 0;
            if (pBestFitness[i] < gBestFitness) {
                gBestFitness = pBestFitness[i];
                gBest = particles[i].clone();
            }
        }

        // 2. Sonraki 5 parçacık Min-Min ile başlat
        for (int i = 5; i < HYBRID_PARTICLES; i++) {
            particles[i] = generateMinMinSolution(cloudletList, vmList);
            for (int j = 0; j < taskCount; j++)
                velocities[i][j] = (random.nextDouble() - 0.5) * 0.1;
            pBest[i] = particles[i].clone();
            pBestFitness[i] = calculateFitness(particles[i], cloudletList, vmList);
            age[i] = 0;
            if (pBestFitness[i] < gBestFitness) {
                gBestFitness = pBestFitness[i];
                gBest = particles[i].clone();
            }
        }

        // 3. Geri kalan parçacıkları OBL ile başlat
        for (int i = HYBRID_PARTICLES; i < PARTICLE_COUNT; i++) {
            for (int j = 0; j < taskCount; j++) {
                particles[i][j] = random.nextInt(vmCount);
                velocities[i][j] = (random.nextDouble() - 0.5) * 0.2;
            }
            int[] opposite = generateOpposite(particles[i], vmCount);
            double origFitness = calculateFitness(particles[i], cloudletList, vmList);
            double oppFitness  = calculateFitness(opposite, cloudletList, vmList);
            if (oppFitness < origFitness) particles[i] = opposite;
            pBest[i] = particles[i].clone();
            pBestFitness[i] = calculateFitness(particles[i], cloudletList, vmList);
            age[i] = 0;
            if (pBestFitness[i] < gBestFitness) {
                gBestFitness = pBestFitness[i];
                gBest = particles[i].clone();
            }
        }

        // Lbest başlangıç değerlerini gBest ile başlat
        for (int i = 0; i < PARTICLE_COUNT; i++)
            lBest[i] = gBest.clone();

        // 4. İterasyon döngüsü
        int noImprovementCount = 0;
        double previousBest = gBestFitness;
        int rejuvenationCount = 0;

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {

            // Dinamik W
            double w = W_MAX - (W_MAX - W_MIN) * iter / MAX_ITERATIONS;

            // Adaptive W
            double diversity = calculateDiversity(particles, PARTICLE_COUNT, taskCount, vmCount);
            if (diversity < 0.1) {
                w = Math.min(W_MAX, w * 1.1);
            } else if (diversity > 0.5) {
                w = Math.max(W_MIN, w * 0.9);
            }

            // Lbest güncelle — her parçacık için komşuluk en iyisini bul
            updateLBest(lBest, pBest, pBestFitness, PARTICLE_COUNT, NEIGHBORHOOD_SIZE);

            for (int i = 0; i < PARTICLE_COUNT; i++) {

                // Aging kontrolü
                if (age[i] >= MAX_AGE) {
                    for (int j = 0; j < taskCount; j++) {
                        particles[i][j] = random.nextInt(vmCount);
                        velocities[i][j] = (random.nextDouble() - 0.5) * 0.2;
                    }
                    int[] opposite = generateOpposite(particles[i], vmCount);
                    double oFit = calculateFitness(particles[i], cloudletList, vmList);
                    double oppF = calculateFitness(opposite, cloudletList, vmList);
                    if (oppF < oFit) particles[i] = opposite;
                    pBest[i] = particles[i].clone();
                    pBestFitness[i] = calculateFitness(particles[i], cloudletList, vmList);
                    age[i] = 0;
                    rejuvenationCount++;
                    continue;
                }

                for (int j = 0; j < taskCount; j++) {
                    double r1 = random.nextDouble();
                    double r2 = random.nextDouble();

                    // Lbest topolojisi: gBest yerine lBest[i] kullan
                    velocities[i][j] = w * velocities[i][j]
                            + C1 * r1 * (pBest[i][j] - particles[i][j])
                            + C2 * r2 * (lBest[i][j] - particles[i][j]);

                    double vMax = vmCount * 0.5;
                    velocities[i][j] = Math.max(-vMax, Math.min(vMax, velocities[i][j]));

                    int newPos = (int) Math.round(particles[i][j] + velocities[i][j]);
                    particles[i][j] = Math.max(0, Math.min(vmCount - 1, newPos));
                }

                // Cauchy Mutasyon
                if (random.nextDouble() < CAUCHY_PROB) {
                    int mutIdx = random.nextInt(taskCount);
                    double cauchy = Math.tan(Math.PI * (random.nextDouble() - 0.5));
                    int newPos = (int) Math.round(particles[i][mutIdx] + cauchy);
                    particles[i][mutIdx] = Math.max(0, Math.min(vmCount - 1, newPos));
                }

                double fitness = calculateFitness(particles[i], cloudletList, vmList);

                if (fitness < pBestFitness[i]) {
                    pBest[i] = particles[i].clone();
                    pBestFitness[i] = fitness;
                    age[i] = 0;
                } else {
                    age[i]++;
                }

                if (fitness < gBestFitness) {
                    gBestFitness = fitness;
                    gBest = particles[i].clone();
                }
            }

            // Early Stopping
            if (gBestFitness < previousBest) {
                previousBest = gBestFitness;
                noImprovementCount = 0;
            } else {
                noImprovementCount++;
            }
            if (noImprovementCount >= 20) {
                System.out.printf("  Early stopping: %d. iterasyonda durdu%n", iter);
                break;
            }
        }

        lastFitness = gBestFitness;
        lastRejuvenations = rejuvenationCount;

        for (int j = 0; j < taskCount; j++)
            cloudletList.get(j).setVmId(vmList.get(gBest[j]).getId());

        System.out.printf("  Full PSO (Aging+OBL+Cauchy+AdaptiveW+Lbest) - Makespan: %.2f | Yenilenen: %d%n",
            gBestFitness, rejuvenationCount);
        return cloudletList;
    }

    // Lbest: Her parçacık için komşuluk içindeki en iyi pbest'i bul
    private static void updateLBest(int[][] lBest, int[][] pBest, double[] pBestFitness,
                                     int particleCount, int neighborhoodSize) {
        for (int i = 0; i < particleCount; i++) {
            int bestNeighbor = i;
            double bestFitness = pBestFitness[i];
            for (int k = 1; k <= neighborhoodSize; k++) {
                int left  = (i - k + particleCount) % particleCount;
                int right = (i + k) % particleCount;
                if (pBestFitness[left] < bestFitness) {
                    bestFitness = pBestFitness[left];
                    bestNeighbor = left;
                }
                if (pBestFitness[right] < bestFitness) {
                    bestFitness = pBestFitness[right];
                    bestNeighbor = right;
                }
            }
            lBest[i] = pBest[bestNeighbor].clone();
        }
    }

    // Adaptive W için sürü çeşitliliği hesapla
    private static double calculateDiversity(int[][] particles, int particleCount,
                                              int taskCount, int vmCount) {
        double[] mean = new double[taskCount];
        for (int i = 0; i < particleCount; i++)
            for (int j = 0; j < taskCount; j++)
                mean[j] += particles[i][j];
        for (int j = 0; j < taskCount; j++)
            mean[j] /= particleCount;

        double diversity = 0;
        for (int i = 0; i < particleCount; i++) {
            double dist = 0;
            for (int j = 0; j < taskCount; j++)
                dist += Math.abs(particles[i][j] - mean[j]);
            diversity += dist / taskCount;
        }
        diversity /= (particleCount * (vmCount - 1));
        return Math.min(1.0, diversity);
    }

    // OBL: Karşıt pozisyon üret
    private static int[] generateOpposite(int[] position, int vmCount) {
        int[] opposite = new int[position.length];
        for (int j = 0; j < position.length; j++)
            opposite[j] = (vmCount - 1) - position[j];
        return opposite;
    }

    private static int[] generateMaxMinVariation(List<Cloudlet> cloudletList,
                                                   List<Vm> vmList, int variationId) {
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
                if (finish < minFinish) {
                    minFinish = finish;
                    bestVm = j;
                }
            }
            if (variationId > 0 && random.nextDouble() < 0.1 * variationId)
                bestVm = random.nextInt(vmCount);
            solution[idx] = bestVm;
            vmLoad[bestVm] += (double) cloudletList.get(idx).getCloudletLength()
                              / vmList.get(bestVm).getMips();
        }
        return solution;
    }

    private static int[] generateMinMinSolution(List<Cloudlet> cloudletList, List<Vm> vmList) {
        int taskCount = cloudletList.size();
        int vmCount = vmList.size();
        int[] solution = new int[taskCount];
        double[] vmLoad = new double[vmCount];

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) indices.add(i);
        indices.sort((a, b) -> Long.compare(
            cloudletList.get(a).getCloudletLength(),
            cloudletList.get(b).getCloudletLength()));

        for (int idx : indices) {
            int bestVm = 0;
            double minFinish = Double.MAX_VALUE;
            for (int j = 0; j < vmCount; j++) {
                double finish = vmLoad[j] +
                    (double) cloudletList.get(idx).getCloudletLength() / vmList.get(j).getMips();
                if (finish < minFinish) {
                    minFinish = finish;
                    bestVm = j;
                }
            }
            solution[idx] = bestVm;
            vmLoad[bestVm] += (double) cloudletList.get(idx).getCloudletLength()
                              / vmList.get(bestVm).getMips();
        }
        return solution;
    }

    private static double calculateFitness(int[] position,
                                            List<Cloudlet> cloudletList,
                                            List<Vm> vmList) {
        double[] vmFinishTime = new double[vmList.size()];
        for (int j = 0; j < cloudletList.size(); j++) {
            int vmIndex = position[j];
            double execTime = (double) cloudletList.get(j).getCloudletLength()
                              / vmList.get(vmIndex).getMips();
            vmFinishTime[vmIndex] += execTime;
        }

        double makespan = 0;
        for (double t : vmFinishTime)
            if (t > makespan) makespan = t;

        int activeVMs = 0;
        for (double t : vmFinishTime)
            if (t > 0) activeVMs++;
        double utilization = makespan > 0 ? (double) activeVMs / vmList.size() : 0;

        return ALPHA * makespan + BETA * (1.0 - utilization) * makespan;
    }
}
