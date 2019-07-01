package pointerAnalysis;

import pointerAnalysis.objects.AbstractObject;
import pointerAnalysis.pointers.Pointer;
import soot.Value;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public abstract class PointerAnalysis<T> extends ForwardBranchedFlowAnalysis<T> {
    public PointerAnalysis(UnitGraph graph) {
        super(graph);
    }

    public abstract boolean mayAlias(Pointer p, Stmt s, Pointer q, Stmt r);

    public abstract boolean mustAlias(Pointer p, Stmt s, Pointer q, Stmt r);
}
