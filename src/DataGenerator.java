import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.*;
import java.util.*;

public class DataGenerator {
    
    private static final long SEED = 42;
    private static final Random random = new Random(SEED);
    
    public static List<Vm> createVMs(int brokerId, int vmCount) {
        List<Vm> vmList = new ArrayList<>();
        int[] mipsValues = {500, 750, 1000, 1250, 1500, 1750, 2000, 2250, 2500};
        
        for (int i = 0; i < vmCount; i++) {
            int mips = mipsValues[random.nextInt(mipsValues.length)];
            int pesNumber = 1;
            int ram = 512;
            long bw = 1000;
            long size = 10000;
            String vmm = "Xen";
            
            Vm vm = new Vm(i, brokerId, mips, pesNumber, ram, bw, size, vmm,
                    new CloudletSchedulerTimeShared());
            vmList.add(vm);
        }
        return vmList;
    }
    
    public static List<Cloudlet> createCloudlets(int brokerId, int cloudletCount) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        UtilizationModel utilizationModel = new UtilizationModelFull();
        
        for (int i = 0; i < cloudletCount; i++) {
            long length = 1000 + (long)(random.nextDouble() * 19000);
            long fileSize = 300;
            long outputSize = 300;
            int pesNumber = 1;
            
            Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize,
                    outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudletList.add(cloudlet);
        }
        return cloudletList;
    }
}