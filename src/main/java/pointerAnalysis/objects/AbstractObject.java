package pointerAnalysis.objects;

import pointerAnalysis.objects.AbstractObject;
import pointerAnalysis.objects.NewObject;
import pointerAnalysis.pointers.Pointer;
import pointerAnalysis.objects.UnresolvedPointer;
import soot.Type;
import soot.Value;
import soot.jimple.NewExpr;

public abstract class AbstractObject{
public abstract int hashCode();
	
	public abstract boolean equals(Object obj);
	
	@Override
	public abstract String toString();
	
	public static AbstractObject pointer(Value value){
		if(value instanceof NewExpr){
			return new NewObject((NewExpr) value);
		}
		else if(value instanceof Pointer){
			return new UnresolvedPointer((Pointer) value);
		}
		else return null;
	}
	
	public abstract Type getType();

}

