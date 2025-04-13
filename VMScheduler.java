import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import java.util.*;
public class VMScheduler {
    public static void main(String[] args) {
        System.out.println("🟢 Starting VM Scheduling + Load Balancing + Fault Tolerance Simulation...");
        int numUsers = 1;
        Calendar calendar = Calendar.getInstance();
        boolean traceFlag = false;
        CloudSim.init(numUsers, calendar, traceFlag);
        Datacenter datacenter0 = createDatacenter("Datacenter_0");
        DatacenterBroker broker = null;
        try {
            broker = new CustomBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        int brokerId = broker.getId();
        // VMs
        List<Vm> vmList = new ArrayList<>();
        vmList.add(new Vm(0, brokerId, 1000, 1, 1024, 1000, 10000, "TimeShared", new CloudletSchedulerTimeShared()));
        vmList.add(new Vm(1, brokerId, 1000, 1, 1024, 1000, 10000, "TimeShared", new CloudletSchedulerTimeShared()));
        broker.submitVmList(vmList);
        // Cloudlets
        List<Cloudlet> cloudletList = new ArrayList<>();
        UtilizationModel utilizationModel = new UtilizationModelFull();
        for (int i = 0; i < 4; i++) {
            Cloudlet cloudlet = new Cloudlet(i, 40000, 1, 300, 300, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudletList.add(cloudlet);
        }
        broker.submitCloudletList(cloudletList);
        // Start simulation
        CloudSim.startSimulation();
        List<Cloudlet> resultList = broker.getCloudletReceivedList();
        CloudSim.stopSimulation();
        printCloudletList(resultList);
        System.out.println("✅ Simulation finished!");
    }
    private static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(1000)));
        peList.add(new Pe(1, new PeProvisionerSimple(1000)));
        int hostId = 0;
        int ram = 2048;
        long storage = 1000000;
        int bw = 10000;
        hostList.add(new Host(hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)
        ));
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw
        );
        try {
            return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<Storage>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private static void printCloudletList(List<Cloudlet> list) {
        String indent = "    ";
        System.out.println("\n========== CLOUDLET EXECUTION RESULTS ==========");
        System.out.println("CloudletID" + indent + "STATUS" + indent +
                "DataCenterID" + indent + "VMID" + indent + "Time" + indent +
                "Start Time" + indent + "Finish Time");
        for (Cloudlet cloudlet : list) {
            System.out.print(cloudlet.getCloudletId() + indent);
            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                System.out.print("SUCCESS");
                System.out.println(indent + cloudlet.getResourceId() + indent + cloudlet.getVmId() +
                        indent + cloudlet.getActualCPUTime() + indent +
                        cloudlet.getExecStartTime() + indent + cloudlet.getFinishTime());
            }
        }
    }
}
