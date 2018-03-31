package com.openkappa.simd.hashcode;

import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.openkappa.simd.DataUtil.createIntArray;

@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
public class HashCode {
	private static final int P0 = 1;
	private static final int P1 = 31 * P0;
	private static final int P2 = 31 * P1;
	private static final int P3 = 31 * P2;
	private static final int P4 = 31 * P3;
	private static final int P5 = 31 * P4;
	private static final int P6 = 31 * P5;
	private static final int P7 = 31 * P6;
	private static final int P8 = 31 * P7;

	static int shiny8HashCode(int[] data) {
		int a0 = 0;
		int a1 = 0;
		int a2 = 0;
		int a3 = 0;
		int a4 = 0;
		int a5 = 0;
		int a6 = 0;
		int a7 = 1;
		int i = 0;
		for (; i + 7 < data.length; i += 8) {
			a0 = P8 * a0 + data[i+0];
			a1 = P8 * a1 + data[i+1];
			a2 = P8 * a2 + data[i+2];
			a3 = P8 * a3 + data[i+3];
			a4 = P8 * a4 + data[i+4];
			a5 = P8 * a5 + data[i+5];
			a6 = P8 * a6 + data[i+6];
			a7 = P8 * a7 + data[i+7];
		}
		int sum = (P7 * a0 + P6 * a1 + P5 * a2 + P4 * a3) + (P3 * a4 + P2 * a5 + P1 * a6 + P0 * a7);
		if (i + 3 < data.length) {
			sum = P4 * sum + P3 * data[i+0] + P2 * data[i+1] + P1 * data[i+2] + P0 * data[i+3];
			i += 4;
		}
		if (i + 1 < data.length) {
			sum = P2 * sum + P1 * data[i+0] + P0 * data[i+1];
			i += 2;
		}
		if (i < data.length) {
			sum = P1 * sum + P0 * data[i];
		}
		return sum;
	}

	static int shiny4HashCode(int[] data) {
		int a0 = 0;
		int a1 = 0;
		int a2 = 0;
		int a3 = 1;
		int i = 0;
		for (; i + 3 < data.length; i += 4) {
			a0 = P4 * a0 + data[i+0];
			a1 = P4 * a1 + data[i+1];
			a2 = P4 * a2 + data[i+2];
			a3 = P4 * a3 + data[i+3];
		}
		int sum = (P3 * a0 + P2 * a1 + P1 * a2 + P0 * a3);
		if (i + 1 < data.length) {
			sum = P2 * sum + P1 * data[i+0] + P0 * data[i+1];
			i += 2;
		}
		if (i < data.length) {
			sum = P1 * sum + P0 * data[i];
		}
		return sum;
	}

    @Param({
            "256",
            "1024",
            "8192"
    })
    private int size;


    public static void main(String[] args) {
        HashCode hashCode = new HashCode();
        hashCode.size = 256;
        hashCode.init();
        System.out.println(hashCode.BuiltIn());
        System.out.println(hashCode.StrengthReduction());
    }


    private int[] data;

    private int[] coefficients;
    private int seed;

    private FixedLengthHashCode hashCode;

    @Setup(Level.Trial)
    public void init() {
        data = createIntArray(size);
        this.coefficients = new int[size];
        coefficients[size - 1] = 1;
        for (int i = size - 2; i >= 0; --i) {
            coefficients[i] = 31 * coefficients[i + 1];
        }
        seed = 31 * coefficients[0];
        this.hashCode = new FixedLengthHashCode(size);
    }

    @Benchmark
    public int Unrolled() {
        if (data == null)
            return 0;

        int result = 1;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 31 * 31 * 31 * 31 * 31 * 31 * 31 * 31 * result
                   + 31 * 31 * 31 * 31 * 31 * 31 * 31 * data[i]
                   + 31 * 31 * 31 * 31 * 31 * 31 * data[i + 1]
                   + 31 * 31 * 31 * 31 * 31 * data[i + 2]
                   + 31 * 31 * 31 * 31 * data[i + 3]
                   + 31 * 31 * 31 * data[i + 4]
                   + 31 * 31 * data[i + 5]
                   + 31 * data[i + 6]
                   + data[i + 7]
                    ;
        }
        for (; i < data.length; i++) {
            result = 31 * result + data[i];
        }
        return result;
    }

    @Benchmark
    public int StrengthReduction() {
        int result = 1;
        for (int i = 0; i < data.length; ++i) {
            result = (result << 5) - result + data[i];
        }
        return result;
    }

    @Benchmark
    public int BuiltIn() {
        return Arrays.hashCode(data);
    }

    @Benchmark
    public int FixedLength() {
        return hashCode.hashCode(data);
    }

    @Benchmark
    public int Vectorised() {
        int result = seed;
        for (int i = 0; i < data.length && i < coefficients.length; ++i) {
            result += coefficients[i] * data[i];
        }
        return result;
    }


    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int Shiny8HashCode() {
        return data == null ? 0 : shiny8HashCode(data);
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public int Shiny4HashCode() {
        return data == null ? 0 : shiny8HashCode(data);
    }


}
