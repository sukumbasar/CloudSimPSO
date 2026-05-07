import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import java.util.Calendar;

public class HelloCloudSim {
    public static void main(String[] args) throws Exception {
        CloudSim.init(1, Calendar.getInstance(), false);
        System.out.println("CloudSim başarıyla çalışıyor!");
    }
}