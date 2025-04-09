package sample.evaluation.kelp;
import it.uniroma2.sag.kelp.data.representation.tree.TreeRepresentation;

public interface KernelWrapper {
    float kernelComputation(TreeRepresentation t1, TreeRepresentation t2);
}
