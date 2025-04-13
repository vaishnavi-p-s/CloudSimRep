import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;

            CloudSim.init(numUsers, calendar, traceFlag);

            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            DatacenterBroker broker = new DatacenterBroker("Broker");
            int brokerId = broker.getId();

            List<Vm> vmList = new ArrayList<>();
            int vmId = 0;
            int mips = 1000;
            long size = 10000;
            int ram = 512;
            long bw = 1000;
            int pesNumber = 1;
            String vmm = "Xen";

            Vm vm = new Vm(vmId, brokerId, mips, pesNumber, ram, bw, size, vmm,
                    new CloudletSchedulerTimeShared());
            vmList.add(vm);

            broker.submitVmList(vmList);

            List<Cloudlet> cloudletList = new ArrayList<>();
            int cloudletId = 0;
            long length = 40000;
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();

            Cloudlet cloudlet = new Cloudlet(cloudletId, length, pesNumber,
                    fileSize, outputSize, utilizationModel,
                    utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudletList.add(cloudlet);

            broker.submitCloudletList(cloudletList);

            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();
            printCloudletList(newList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Datacenter createDatacenter(String name) throws Exception {
        List<Host> hostList = new ArrayList<>();

        int hostId = 0;
        int ram = 2048;
        long storage = 1000000;
        int bw = 10000;

        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(1000)));

        hostList.add(new Host(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)
        ));

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double timeZone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, timeZone, cost,
                costPerMem, costPerStorage, costPerBw);

        return new Datacenter(
                name,
                characteristics,
                new VmAllocationPolicySimple(hostList),
                new LinkedList<Storage>(),
                0
        );
    }

    private static void printCloudletList(List<Cloudlet> list) {
        String indent = "    ";
        System.out.println("\n========== CLOUDLET EXECUTION RESULTS ==========");
        System.out.println("CloudletID" + indent + "STATUS" + indent +
                "DataCenterID" + indent + "VMID" + indent + "Time" +
                indent + "Start Time" + indent + "Finish Time");

        for (Cloudlet cloudlet : list) {
            String status = cloudlet.getStatus() == Cloudlet.SUCCESS ? "SUCCESS" : "FAILED";

            System.out.println(cloudlet.getCloudletId() + indent + status + indent +
                    cloudlet.getResourceId() + indent + cloudlet.getVmId() + indent +
                    cloudlet.getActualCPUTime() + indent +
                    cloudlet.getExecStartTime() + indent +
                    cloudlet.getFinishTime());
        }
    }
}
