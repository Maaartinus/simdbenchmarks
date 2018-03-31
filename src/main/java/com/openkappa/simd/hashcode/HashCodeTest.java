package com.openkappa.simd.hashcode;

import java.util.Arrays;
import java.util.Random;

public class HashCodeTest {
	public static void main(String[] args) {
		Random random = new Random(0);
		for (int i=0; i<500; ++i) {
			int a[] = new int[i];
			for (int j = 0; j < a.length; ++j) {
				a[j] = random.nextInt();
			}
			int expected = Arrays.hashCode(a);
			int actual4 = HashCode.shiny4HashCode(a);
			if (expected != actual4) throw new AssertionError();
			int actual8 = HashCode.shiny8HashCode(a);
			if (expected != actual8) throw new AssertionError();
		}
	}

}
