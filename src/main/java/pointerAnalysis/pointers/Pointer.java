package pointerAnalysis.pointers;

import java.util.HashSet;
import java.util.Set;

import pointerAnalysis.Assignments;
import pointerAnalysis.objects.AbstractObject;
import pointerAnalysis.objects.UnresolvedPointer;
import pointerAnalysis.pointers.ArrayRefPointer;
import pointerAnalysis.pointers.FieldPointer;
import pointerAnalysis.pointers.LocalPointer;
import pointerAnalysis.pointers.ParameterPointer;
import pointerAnalysis.pointers.Pointer;
import soot.Local;
import soot.Type;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.FieldRef;
import soot.jimple.ParameterRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.JInstanceFieldRef;

public abstract class Pointer{
	
	public abstract int hashCode();
	
	public abstract boolean equals(Object obj);
	
	@Override
	public abstract String toString();

	public abstract Type getType();
}
