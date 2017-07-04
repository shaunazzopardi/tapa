package example;

import java.util.*;
public class Example {

	public Object q;
	static Object p;
	
	public void hello(){}
	
	public static void stuff(Object o){
		Object oo = o;
		
		Example ex = new Example();
		ex.q = o;
		
		oo.toString();
		ex.hello();
		
//		Example ex = new Example();
//		ex.q = o;
//		
//		ex.q.toString();
//		Set<Object> set = new HashSet<Object>();
//		set.add(new Object());
//		set.add(o);
//		
//		Iterator<Object> iterate = set.iterator();
//		
//		Object oo = o;
//		while(iterate.hasNext()){
//			oo = iterate.next();
//		}
//		oo.toString();
//		Object[] oarr = new Object[2];
//		oarr[0] = o;
//		oarr[1] = new Object();
		
//		while(o.getClass().equals(Object.class)){
//			oo = new Object();
//			if(o.getClass().equals(Object.class)){
//				oo = o;
//			}
//		}
//		
//		oo.toString();
	}
	
	public static void main(String[] args){
		Example ex = new Example();
		ex.q = object();

		Object o = new Object();
		Object o1 = new Object();
		
		while(args.length == 0){
			o = new Object();
		}
//		else{
//			o = o1;
//			p = new Object();
//		}
		
		System.out.println(o);
		stuff(o1);
	}
	
	public static Object object(){
		return new Object();
	}
}
