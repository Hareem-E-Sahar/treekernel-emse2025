package sample.evaluation.kelp;
import it.uniroma2.sag.kelp.data.representation.tree.TreeRepresentation;
import it.uniroma2.sag.kelp.kernel.tree.PartialTreeKernel;

public class PartialTreeKernelWrapper implements KernelWrapper {
    private final PartialTreeKernel kernel;

    public PartialTreeKernelWrapper(float LAMBDA, String representationIdentifier) {
    	float MU = 0.4f;
    	float terminalFactor = 1f;
        this.kernel = new PartialTreeKernel(LAMBDA, MU, terminalFactor, representationIdentifier);
    }

    @Override
    public float kernelComputation(TreeRepresentation t1, TreeRepresentation t2) {
        return kernel.kernelComputation(t1, t2);
    }
}

