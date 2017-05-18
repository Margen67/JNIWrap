package cs.ArrayUtils;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public final class ArrayOps {

	public static final int reduce(final int[] arr, int identity, final IntBinaryOperator acc) {
		int result = identity;
		for(int value : arr)
			result = acc.applyAsInt(result, value);
		return result;
	}
	
	public static final int[] map(final int[] arr, final IntUnaryOperator op) {
		int[] result = arr.clone();
		for(int i = 0; i < arr.length; ++i) 
			result[i] = op.applyAsInt(arr[i]);
		return result;
	}
	public static <R, A1, A2> ArrayList<R> construct(BiFunction<A1, A2, R> func, Object... args) {
		if(args.length % 2 != 0) //odd number of arguments
			return null;
		//Object[] result = new Object[args.length / 2];
		ArrayList<R> result = new ArrayList<>();
		for(int i = 0; i < args.length / 2; ++i) {
			Object arg1 = args[i*2];
			Object arg2 = args[(i*2)+1];
			//result[i] = ;
			result.add(func.apply((A1)arg1, (A2)arg2));
		}
		return result;
	}
	public static final <T> T reduce(T identity, T[] arr, 
			BinaryOperator<T> op) {
		T result = identity;
		for(T x : arr)
			result = op.apply(result, x);
		return result;
	}
	public static final <TR, TO> TR reduce(TR identity, TO[] arr, 
			BiFunction<TR, TO, TR> op) {
		TR result = identity;
		for(TO x : arr)
			result = op.apply(result, x);
		return result;
	}
	public static final int reduceString(int identity, String arr, 
			IntBinaryOperator op) {
		int result = identity;
		for(int i = 0; i < arr.length(); ++i)
			result = op.applyAsInt(result, (int)arr.charAt(i));
		return result;
	}
	
	public static final <T> void forEach(T[] arr, Consumer<T> op) {
		if(arr == null)
			return;
		for(T element : arr)
			op.accept(element);
	}
	public static final void forEach(int[] arr, IntConsumer op) {
		if(arr == null)
			return;
		for(int element : arr)
			op.accept(element);
	}
	
	public static final <T1, T2> ArrayList<T1> map(T2[] input, final Function<T2, T1> op) {
		ArrayList<T1> result = new ArrayList<T1>(input.length);
		for(T2 val : input)
			result.add(op.apply(val));
		return result;
	}
	
	public static final <T2> int[] mapToInt(T2[] input, ToIntFunction<T2> op) {
		int[] result = new int[input.length];
		for(int i = 0; i < input.length; ++i)
			result[i] = op.applyAsInt(input[i]);
		return result;
	}
	
	public static final int indexOfMatch(int[] input, IntPredicate op) {
		for(int i = 0; i < input.length; ++i)
			if(op.test(input[i]))
				return i;
		return input.length;
	}
	public static final <T> int indexOfMatch(T[] input, Predicate<T> op) {
		for(int i = 0; i < input.length; ++i)
			if(op.test(input[i]))
				return i;
		return input.length;
	}
}
