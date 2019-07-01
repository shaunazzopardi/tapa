package pointerAnalysis.pointers;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import pointerAnalysis.pointers.FieldPointer;
import pointerAnalysis.pointers.Pointer;
import soot.Type;
import soot.jimple.FieldRef;

public class StaticFieldPointer extends Pointer{
	
	FieldRef fieldref;
	
	public StaticFieldPointer(FieldRef fieldref){
		this.fieldref = fieldref;
	}
	
	public String toString(){
		return "{}." + fieldref.getFieldRef().name();
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder(15, 37);
		hcb.append(fieldref);
		return hcb.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FieldPointer){
			return this.fieldref.equals(((FieldPointer) obj).fieldref);
		}
		else{
			return false;
		}
	}
	
	public Type getType(){
		return this.fieldref.getType();
	}
}