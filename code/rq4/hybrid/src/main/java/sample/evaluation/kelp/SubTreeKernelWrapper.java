package sample.evaluation.kelp;
import it.uniroma2.sag.kelp.data.representation.tree.TreeRepresentation;
import it.uniroma2.sag.kelp.kernel.tree.SubTreeKernel;

public class SubTreeKernelWrapper implements KernelWrapper {
    private final SubTreeKernel kernel;

    public SubTreeKernelWrapper(float LAMBDA, String representationIdentifier) {
        this.kernel = new SubTreeKernel(LAMBDA, representationIdentifier);
    }

    @Override
    public float kernelComputation(TreeRepresentation t1, TreeRepresentation t2) {
        return kernel.kernelComputation(t1, t2); //kelp method
    }
}

