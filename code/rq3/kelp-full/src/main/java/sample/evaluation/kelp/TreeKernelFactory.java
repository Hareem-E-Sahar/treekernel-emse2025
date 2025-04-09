package sample.evaluation.kelp;
import it.uniroma2.sag.kelp.kernel.tree.PartialTreeKernel;
import it.uniroma2.sag.kelp.kernel.tree.SubTreeKernel;
import it.uniroma2.sag.kelp.kernel.tree.SubSetTreeKernel;

public class TreeKernelFactory {
    public static KernelWrapper createKernel(String kernelType, float LAMBDA, String representationIdentifier) {
        switch (kernelType) {
            case "STK":
                return new SubTreeKernelWrapper(LAMBDA, representationIdentifier); 
            case "PTK":
                return new PartialTreeKernelWrapper(LAMBDA, representationIdentifier);
            case "SSTK":
                return new SubSetTreeKernelWrapper(LAMBDA, representationIdentifier);
            default:
                throw new IllegalArgumentException("Invalid kernel type: " + kernelType);
        }
    }
}
