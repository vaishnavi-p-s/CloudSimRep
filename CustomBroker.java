import org.cloudbus.cloudsim.*;
import java.util.List;
public class CustomBroker extends DatacenterBroker {
    public CustomBroker(String name) throws Exception {
        super(name);
    }
    @Override
    protected void submitCloudlets() {
        int vmIndex = 0;
        for (Cloudlet cloudlet : getCloudletList()) {
            Vm selectedVm;
            // Simulate failure in VM 1
            if (vmIndex == 1) {
                System.out.println("⚠️ Simulated failure in VM 1 — reassigning to VM 0");
                selectedVm = getVmList().get(0); // fallback to VM 0
            } else {
                selectedVm = getVmList().get(vmIndex);
            }
            System.out.println("➡️ Assigning Cloudlet " + cloudlet.getCloudletId() + " to VM " + selectedVm.getId());
            bindCloudletToVm(cloudlet.getCloudletId(), selectedVm.getId());
            vmIndex = (vmIndex + 1) % getVmList().size();
        }
        super.submitCloudlets();
    }}