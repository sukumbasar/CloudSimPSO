import org.cloudbus.cloudsim.*;
import java.util.*;

public class Switchboard {

    public static List<Cloudlet> applyAlgorithm(String algorithm, 
                                                 List<Cloudlet> cloudletList, 
                                                 List<Vm> vmList) {
        switch (algorithm) {
            case "FCFS":
                return applyFCFS(cloudletList, vmList);
            case "RR":
                return applyRR(cloudletList, vmList);
            case "SJF":
                return applySJF(cloudletList, vmList);
            case "MINMIN":
                return applyMinMin(cloudletList, vmList);
            case "MAXMIN":
                return applyMaxMin(cloudletList, vmList);
            case "PSO":
                return Scheduler_PSO.applyPSO(cloudletList, vmList);
            default:
                return applyFCFS(cloudletList, vmList);
        }
    }

    // FCFS: Sırayla ata
    private static List<Cloudlet> applyFCFS(List<Cloudlet> cloudletList, List<Vm> vmList) {
        for (int i = 0; i < cloudletList.size(); i++) {
            int vmId = i % vmList.size();
            cloudletList.get(i).setVmId(vmList.get(vmId).getId());
        }
        return cloudletList;
    }

    // RR: Döngüsel sırayla ata
    private static List<Cloudlet> applyRR(List<Cloudlet> cloudletList, List<Vm> vmList) {
        for (int i = 0; i < cloudletList.size(); i++) {
            int vmId = i % vmList.size();
            cloudletList.get(i).setVmId(vmList.get(vmId).getId());
        }
        return cloudletList;
    }

    // SJF: Küçük taskları önce sırala, sonra sırayla ata
    private static List<Cloudlet> applySJF(List<Cloudlet> cloudletList, List<Vm> vmList) {
        cloudletList.sort(Comparator.comparingLong(Cloudlet::getCloudletLength));
        for (int i = 0; i < cloudletList.size(); i++) {
            int vmId = i % vmList.size();
            cloudletList.get(i).setVmId(vmList.get(vmId).getId());
        }
        return cloudletList;
    }

    // MINMIN: En küçük taskı en hızlı bitirecek VM'e ata
    private static List<Cloudlet> applyMinMin(List<Cloudlet> cloudletList, List<Vm> vmList) {
        double[] vmLoad = new double[vmList.size()];

        List<Cloudlet> sorted = new ArrayList<>(cloudletList);
        sorted.sort((a, b) -> Long.compare(a.getCloudletLength(), b.getCloudletLength()));

        for (Cloudlet cloudlet : sorted) {
            int bestVm = 0;
            double minFinishTime = Double.MAX_VALUE;

            for (int j = 0; j < vmList.size(); j++) {
                double finishTime = vmLoad[j] +
                    (double) cloudlet.getCloudletLength() / vmList.get(j).getMips();
                if (finishTime < minFinishTime) {
                    minFinishTime = finishTime;
                    bestVm = j;
                }
            }

            cloudlet.setVmId(vmList.get(bestVm).getId());
            vmLoad[bestVm] += (double) cloudlet.getCloudletLength() / vmList.get(bestVm).getMips();
        }

        return cloudletList;
    }

    // MAXMIN: En büyük taskı en hızlı VM'e ata
    private static List<Cloudlet> applyMaxMin(List<Cloudlet> cloudletList, List<Vm> vmList) {
        double[] vmLoad = new double[vmList.size()];

        List<Cloudlet> sorted = new ArrayList<>(cloudletList);
        sorted.sort((a, b) -> Long.compare(b.getCloudletLength(), a.getCloudletLength()));

        for (Cloudlet cloudlet : sorted) {
            int bestVm = 0;
            double minFinishTime = Double.MAX_VALUE;

            for (int j = 0; j < vmList.size(); j++) {
                double finishTime = vmLoad[j] + 
                    (double) cloudlet.getCloudletLength() / vmList.get(j).getMips();
                if (finishTime < minFinishTime) {
                    minFinishTime = finishTime;
                    bestVm = j;
                }
            }

            cloudlet.setVmId(vmList.get(bestVm).getId());
            vmLoad[bestVm] += (double) cloudlet.getCloudletLength() / vmList.get(bestVm).getMips();
        }

        return cloudletList;
    }
}
