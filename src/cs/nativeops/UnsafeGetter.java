package cs.nativeops;

import java.lang.reflect.Field;

import sun.misc.Unsafe;
public final class UnsafeGetter {

	public static Unsafe getUnsafe() {
	    try {
	            Field f = Unsafe.class.getDeclaredField("theUnsafe");
	            f.setAccessible(true);
	            return (Unsafe)f.get(null);
	    } catch (Exception e) {
	    	System.out.println("Couldn't get Unsafe!");
	    }
	    return null;
	}
	
	public static final Unsafe unsafe = getUnsafe();
	

}
