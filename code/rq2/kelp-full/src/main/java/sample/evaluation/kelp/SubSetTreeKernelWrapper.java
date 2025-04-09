package sample.evaluation.kelp;
import it.uniroma2.sag.kelp.data.representation.tree.TreeRepresentation;
import it.uniroma2.sag.kelp.kernel.tree.SubSetTreeKernel;

public class SubSetTreeKernelWrapper implements KernelWrapper {
    private final SubSetTreeKernel kernel;

    public SubSetTreeKernelWrapper(float LAMBDA, String representationIdentifier) {
        this.kernel = new SubSetTreeKernel(LAMBDA, representationIdentifier);
    }

    @Override
    public float kernelComputation(TreeRepresentation t1, TreeRepresentation t2) {
        return kernel.kernelComputation(t1, t2);
    }
}

