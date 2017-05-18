package cs.jniwrap.utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.ToIntFunction;
import java.util.stream.*;
import cs.ArrayUtils.*;
public class Bitcompress {
	
	enum BitOper {
		none,
		bitrev,
		byterev,
		bytebitrev,
		bitbyterev
	};
	
	private BitOper oper = BitOper.none;
	private int initialShift = 0;
	private Bitcompress next;
	private int countOccurences(int[] values, final int mask) {
		return ArrayOps.reduce(values, 0, (x, y) -> (y& mask )== mask ? x + 1 : x);
	}
	public Bitcompress(int[] values) {
		int[] favouredStream = null;
		int totalPopcount = ArrayOps.reduce(values, 0, (x, y) -> x + Integer.bitCount(y));
		double averageBits = (double)totalPopcount / values.length;
		{
			
			final int[] flippedBits = ArrayOps.map(values, i -> Integer.reverse(i));
			final int[] flippedBytes = ArrayOps.map(values, i -> Integer.reverseBytes(i));
			
			final class BestChoice {
				public int[] stream;
				public int lowestOf;
				public BitOper oper;
				public BestChoice(int[] stream, BitOper oper) {
					this.stream = stream;
					this.lowestOf = Integer.numberOfTrailingZeros( ArrayOps.reduce(stream, 0, (x, y) -> x | y) );
					this.oper = oper;
				}
			}
			final BestChoice worstChoice = new BestChoice(new int[] {1}, BitOper.none);
			
			final ArrayList<BestChoice> _choices = ArrayOps.construct(BestChoice::new, 
						values, BitOper.none, 
						flippedBits, BitOper.bitrev,
						flippedBytes, BitOper.byterev,
						ArrayOps.map(flippedBytes, i -> Integer.reverse(i)), BitOper.bytebitrev,
						ArrayOps.map(flippedBits, i -> Integer.reverseBytes(i)), BitOper.bitbyterev
					);
			final BestChoice[] __choices = _choices.toArray(new BestChoice[_choices.size()]);
			
			
			final BestChoice choice = ArrayOps.reduce(worstChoice, __choices, (x, y) -> x.lowestOf > y.lowestOf ? x : y);
			
			favouredStream = choice.stream;
			oper = choice.oper;
			initialShift = choice.lowestOf;
		}
		
		int[] shifted = Arrays.stream(favouredStream).map(i -> i >>> initialShift).toArray();
		/*
		 * find the bits that all of the values have
		 */
		int common = Arrays.stream(shifted).reduce(~0, (x, y) -> x & y);
		int union = Arrays.stream(shifted).reduce(0, (x, y) -> x | y);
		final int lowerBound = Arrays.stream(shifted).reduce(Integer.MAX_VALUE, (x, y) -> Integer.compareUnsigned(x, y) < 0 ? x : y);
		int[] subtracted = Arrays.stream(shifted).map(i -> (int)(Integer.toUnsignedLong(i) - lowerBound)).toArray();
		if(lowerBound != 0)
			next = new Bitcompress(subtracted);
		
		System.out.printf("Common: 0x%X, Union: 0x%X\n", common, union);
	}

}
