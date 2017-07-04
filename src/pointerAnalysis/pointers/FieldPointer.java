package pointerAnalysis.pointers;

import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import pointerAnalysis.objects.AbstractObject;
import pointerAnalysis.pointers.FieldPointer;
import pointerAnalysis.pointers.Pointer;
import soot.Type;
import soot.jimple.FieldRef;
import soot.jimple.internal.JInstanceFieldRef;

public class FieldPointer extends Pointer{
	
	Set<AbstractObject> parents;
	FieldRef fieldref;
	
	public FieldPointer(Set<AbstractObject> parents, FieldRef fieldref){
		this.parents = parents;
		
		this.fieldref = fieldref;
	}
	
	public String toString(){
		if(parents != null) return parents.toString() + "." + fieldref.getFieldRef().name();
		else return "{}." + fieldref.getFieldRef().name();
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder(15, 37);
		hcb.append(fieldref);
		if(parents != null) hcb.append(parents);
		return hcb.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FieldPointer){
			if((this.parents == null && ((FieldPointer) obj).parents == null)
				|| this.parents.equals(((FieldPointer) obj).parents)){
					return this.fieldref.equals(((FieldPointer) obj).fieldref);
				}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	public Type getType(){
		return this.fieldref.getType();
	}
}