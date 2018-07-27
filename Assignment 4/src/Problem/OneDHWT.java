package Problem;

import java.util.ArrayList;

public class OneDHWT {

	public static void orderedFastHaarWaveletTransformForNumIters(double[] sample, int num_iters) {
		//Check number if iterations is power of 2 or not
		if (!(sample.length > 0) && ((sample.length & (sample.length - 1)) == 0)) {
			return;
		}

		//Count number of sweeps
		final int powerOf2 = (int) (Math.log(sample.length) / Math.log(2.0));

		//Calculate a and c coefficients for 'num_iters' number of iterations
		ArrayList<Double> a = new ArrayList<>();
		ArrayList<Double> c = new ArrayList<>();
		for (int i = 1; i <= num_iters; i++) {
			a.clear();
			c.clear();
			//Calculate late index of a coefficient in sample signal
			int last_index = ((int) Math.pow(2.0, (double) (powerOf2 - i + 1))) - 1;
			for (int j = 0; j < last_index; j += 2) {
				double a1 = (sample[j] + sample[j + 1]) / 2.0;
				double c1 = (sample[j] - sample[j + 1]) / 2.0;
				a.add(a1);
				c.add(c1);
			}

			//Update sample signal with new values after sweep
			int size = (int) Math.pow(2.0, (double) (powerOf2 - i));
			for (int k = 0; k < size; k++) {
				sample[k] = a.get(k);
				sample[k + size] = c.get(k);
			}
		}
	}

	public static void inPlaceFastHaarWaveletTransformForNumIters(double[] sample, int num_iters) {
		//Sample length
		int len = sample.length;

		//Check if num_iters is power of 2 or not
		if (!(len > 0) && ((len & (len - 1)) == 0)) {
			return;
		}

		//Increment Index
		int index = 1;

		//Gap size
		int num_avg_element = 2;

		//Calculate in-place HWT
		double a = 0;
		double c = 0;
		//Loop for each iteration
		for (int i = 1; i <= num_iters; i++) {
			len = len / 2;
			//Loop till first half of the sample
			for (int j = 0; j < len; j++) {
				//Calculate 'a' coefficient
				a = (sample[num_avg_element * j] + sample[num_avg_element * j + index]) / 2.0;
				//Calculate 'c' coefficient
				c = (sample[num_avg_element * j] - sample[num_avg_element * j + index]) / 2.0;
				//Assign 'a' coefficient to original sample
				sample[num_avg_element * j] = a;
				//Assign 'c' coefficient to original sample
				sample[num_avg_element * j + index] = c;
			}
			index = num_avg_element;
			num_avg_element *= 2;
		}
	}
	
	public static void orderedFastInverseHaarWaveletTransformForNumIters(double[] signal, int numIters) {
        // Sample length
        int len = signal.length;
        // Check if num_iters is power of 2 or not
        if (!(len > 0) && ((len & (len - 1)) == 0)) {
            return;
        }
        // Number of iteration should be less than power of 2
        int power0f2 = (int) (Math.log(len) / Math.log(2.0));
        if (numIters > power0f2) {
            return;
        }
        double[] changedSignal;
        int separation;
        // Calculate separation between signal values
        // Separation increases by power of 2 after each iteration]
        separation = (int) (Math.pow(2.0, power0f2 - numIters));
        // Loop for the number of iteration
        for (int i = 1; i <= numIters; i++) {
            changedSignal = new double[2 * separation];
            // Calculate sweeps
            for (int j = 0; j < separation; j++) {
                changedSignal[2 * j] = signal[j] + signal[separation + j];
                changedSignal[2 * j + 1] = signal[j] - signal[separation + j];
            }
            // Update original signal with changes signal values
            for(int k = 0; k < 2 * separation; k++) {
                signal[k] = changedSignal[k];
            }
            // Double separation for next iteration
            separation *= 2;
        }
    }

    public static void inPlaceFastInverseHaarWaveletTransformForNumIters(double[] sample, int num_iters) {
        int len = sample.length;
        // Check if num_iters is power of 2 or not
        if (!(len > 0) && ((len & (len - 1)) == 0)) {
            return;
        }
        // Number of iteration should be less than power of 2
        int power0f2 = (int) (Math.log(len) / Math.log(2.0));
        if (num_iters > power0f2) {
            return;
        }
        int a, b, c;
        // Loop till power of 2 of original signal length
        for (int i = power0f2 - num_iters + 1; i <= power0f2; i++) {
            //Check for inner power of 2
            a = (int) (Math.pow(2.0, power0f2 - i));
            b = 2 * a;
            c = (int) (Math.pow(2.0, i - 1));
            // Jump to inner loop
            for (int j = 0; j < c; j++) {
                double x = sample[b * j] + sample[b * j + a];
                double y = sample[b * j] - sample[b * j + a];
                // In-place change the signals.
                sample[b * j] = x;
                sample[b * j + a] = y;
            }
        }
    }

	//Utility for printing sample
	public static void displaySample(double[] sample) {
		System.out.print("Sample: ");
		for (int i = 0; i < sample.length; i++) {
			System.out.print(sample[i] + " ");
		}
		System.out.println();
	}
}