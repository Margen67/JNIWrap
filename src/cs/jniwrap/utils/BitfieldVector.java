package cs.jniwrap.utils;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public final class BitfieldVector extends AbstractList<Integer> {
	private int mask;
	private byte nbits;
	private int array[];
	private int index = 0;
	private int growthSize;
	private static final int makeMask(int nbits) {
		int result = 0;
		for(int i = 0; i < nbits; ++i)
			result |= 1 << i;
		
		return result;
	}
	
	private float calcRatio() {
		return ((float)Integer.SIZE) / nbits;
	}
	
	public BitfieldVector(int nbits, int expectedSize, int growthSize) throws IllegalArgumentException {
		if(nbits >= 32 || nbits > 1)
			throw new IllegalArgumentException("Bitfield must be between 0 and 32 bits wide.");
		if(expectedSize <= 0)
			throw new IllegalArgumentException("BitfieldVector's expected size must be greater than 0.");
		if(growthSize <= 0)
			throw new IllegalArgumentException("BitfieldVector's array growth size must be greater than 0.");
		this.growthSize = growthSize;
		this.nbits = (byte)nbits;
		mask = makeMask(nbits);
		float ratio = calcRatio();
		int actualNeeded = (int)(expectedSize/nbits) + 1;
		array = new int[actualNeeded];
		
	}
	public BitfieldVector(int nbits, int expectedSize) {
		this(nbits, expectedSize, 128);
	}
	public BitfieldVector(int nbits) {
		this(nbits, 128);
		
	}
	private int indexFor(int i) {
		return (int)(i / Math.floor(calcRatio()));
	}
	private int bitIndexOf(int i) {
		return i % (int)calcRatio();
	}
	

	@Override
	public boolean add(Integer arg0) {
		if(arg0 > mask)
			return false;
		if(index >= array.length) 
			array = Arrays.copyOf(array, array.length + growthSize);
		
		int valIndex = indexFor(index);
		int bitIndex = bitIndexOf(index);
		int shift = Integer.SIZE - nbits - (nbits * bitIndex);
		
		int valueShifted = arg0.intValue() << shift;
		
		array[valIndex] |= valueShifted;
		index++;
		return true;
	}
	
	public Integer get(int index) {
		int valIndex = indexFor(index);
		int bitIndex = bitIndexOf(index);
		int shift = Integer.SIZE - nbits - (nbits * bitIndex);
		return (array[valIndex] >>> shift) & mask;
	}
	@Override
	public void clear() {
		for(int i = 0; i < array.length; ++i)
			array[i] = 0;
		index = 0;
	}

	@Override
	public int size() {
		return index;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(array);
		result = prime * result + growthSize;
		result = prime * result + index;
		result = prime * result + mask;
		result = prime * result + nbits;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BitfieldVector other = (BitfieldVector) obj;
		if (!Arrays.equals(array, other.array))
			return false;
		if (growthSize != other.growthSize)
			return false;
		if (index != other.index)
			return false;
		if (mask != other.mask)
			return false;
		if (nbits != other.nbits)
			return false;
		return true;
	}

}
