package cs.tests;

import java.util.Random;

public class VectorizationTest {
	public float dot(final float a[], final float b[]) {
        float sum = 0;
        for (int i = 0; i < 50; i++) {
            sum += a[i]*b[i];
        }
        return sum;
    }
	
	public float[] randomFloatArray(int size) {
		float[] result = new float[size];
		Random r = new Random();
		for(int i = 0; i < size; ++i)
			result[i] = r.nextFloat();
		return result;
	}
	
	public float test() {
		return dot(randomFloatArray(64), randomFloatArray(64));
	}
	public VectorizationTest() {
		
	}
}
