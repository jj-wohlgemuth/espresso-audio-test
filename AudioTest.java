import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.OptionalDouble;

import org.apache.commons.math3.util.DoubleArray;
import org.apache.commons.math3.util.Precision;
import org.apache.commons.math3.util.ResizableDoubleArray;
import org.jtransforms.fft.DoubleFFT_1D;

// You can parse AudioData objects or File objects
public class AudioTest {

    public static TestResult testNoise(AudioData audioDatCalib, AudioData audioDatDut, limitsHzDb limits,
                                       String filterCoeffLocation) throws IOException {

        TestResult TestResultObj = new TestResult(0);

        try {
            // Disregard audio at the beginning of the file
            AudioData adCalibShort = disregardBeginning(limits, audioDatCalib);
            AudioData adDutShort = disregardBeginning(limits, audioDatDut);

            // Read FIR filter coefficients from file
            double[] firCoeffs = readFilter(filterCoeffLocation);

            // Circular convolution with FIR Filter
            double[] timeDomDutCC = firFilter(adDutShort.timeDomDataFirstChannel, firCoeffs);
            double[] timeDomCaliCC = firFilter(adCalibShort.timeDomDataFirstChannel, firCoeffs);

            // wavWriter wavWriter = new wavWriter();
            // productionLineTestDsp.wavWriter.write("CCIR.wav", timeDomDutCC, 16000);

            double temp = 2 * (dB(rms(timeDomDutCC)) - dB(rms(timeDomCaliCC)));
            TestResultObj.dutValue = Precision.round(temp, 1);
            if ((TestResultObj.dutValue > limits.noiseLimitDb)||(TestResultObj.dutValue < -2*(limits.noiseLimitDb))) {
                TestResultObj.isPass = false;
            }

            TestResultObj.comment = "DUT mic self noise minus reference:  " + Double.toString(TestResultObj.dutValue)
                    + " dB";

            return TestResultObj;
        } catch (IOException e) {
            System.err.println(e);
            e.printStackTrace();
            throw e;
        }
    }

    // Tests if the spectral difference between reference and golden sample signal
    // exceeds a tolerance in a certain band

    // method for testing two arrays of double
    public static TestResult testSensitivity(AudioData audioDatCalib, AudioData audioDatDut, limitsHzDb limits) {
        // Disregard audio at the beginning of the file
        AudioData adCalibShort = disregardBeginning(limits, audioDatCalib);
        AudioData adDutShort = disregardBeginning(limits, audioDatDut);

        // shorten longer file
        AudioData zpOut = shortenLongerFile(adCalibShort.timeDomDataFirstChannel, adDutShort.timeDomDataFirstChannel);

        // make frequency vector
        double[] freqVec = makeFreqVec(audioDatDut.samplingRate,
                zpOut.timeDomDataSecondChannel.length / limits.smoothingBwBins);

        // look up indizies corresponding to frequency values
        int[] toleranceIdx = hz2Idx(freqVec, limits.toleranceHz);

        int rangeLen = toleranceIdx[(toleranceIdx.length - 1)] - toleranceIdx[0];

        // initialize return variable
        TestResult TestResultObj = new TestResult(rangeLen);

        // get smoothed Fourier transforms in dB
        double[] freqDomCalibDb = dB(smooth(dft(zpOut.timeDomDataFirstChannel), limits.smoothingBwBins));
        double[] freqDomDutDb = dB(smooth(dft(zpOut.timeDomDataSecondChannel), limits.smoothingBwBins));

        double[] differenceVectorDbAbs = new double[rangeLen];
        String outString = "device failed test in band numbers: ";

        for (int u = 0; u < limits.toleranceHz.length - 1; u++) {
            boolean toggle = true;
            for (int i = toleranceIdx[u]; i <= toleranceIdx[u + 1] - 1; i++) {
                // difference vectors (abs and not abs)
                differenceVectorDbAbs[i - toleranceIdx[0]] = java.lang.Math
                        .abs(freqDomDutDb[i] - freqDomCalibDb[i]);
                TestResultObj.diffVecDB[i - toleranceIdx[0]] = freqDomDutDb[i] - freqDomCalibDb[i];

                // this is the ugliest hack
                double valAbs = differenceVectorDbAbs[i - toleranceIdx[0]];
                double val = TestResultObj.diffVecDB[i - toleranceIdx[0]];

                if ((valAbs == Double.NEGATIVE_INFINITY) || (valAbs == Double.POSITIVE_INFINITY)
                        || Double.isNaN(valAbs)) {
                    differenceVectorDbAbs[i - toleranceIdx[0]] = 0;
                }
                if ((val == Double.NEGATIVE_INFINITY) || (val == Double.POSITIVE_INFINITY) || Double.isNaN(val)) {
                    TestResultObj.diffVecDB[i - toleranceIdx[0]] = 0;
                }

                // The DUT fails the test when the absolute difference is higher than the
                // tolerance
                if (differenceVectorDbAbs[i - toleranceIdx[0]] >= limits.toleranceDb[u]) {
                    TestResultObj.isPass = false;
                    // Generate output comment
                    if (toggle) {
                        outString = outString.concat(Integer.toString(u + 1) + " ");
                        toggle = false;
                    }
                    TestResultObj.comment = (outString);
                }
            }
            if (TestResultObj.isPass) {
                TestResultObj.comment = ("device passed test.");
            }
        }

        // Cut frequency vector for plotting
        System.arraycopy(freqVec, toleranceIdx[0], TestResultObj.freqVec, 0, rangeLen);

        // Make third octave vector for CSV logging
        makeThirdOctaveVec(TestResultObj);

        return TestResultObj;
    }

    // In case you pass wav File Objects instead of audio data objects
    public static TestResult testSensitivity(File wavFileNameCalib, File wavFileNameDut, limitsHzDb limits)
            throws Exception {
        try {
            // read wav to double
            AudioData audioDatCalib = wav2AudioData(wavFileNameCalib);
            AudioData audioDatDut = wav2AudioData(wavFileNameDut);

            TestResult TestResultObj;
            TestResultObj = testSensitivity(audioDatCalib, audioDatDut, limits);
            return TestResultObj;

        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
            throw e;
        }
    }

    // In case you pass wav File Objects instead of audio data objects
    public static TestResult testNoise(File wavFileNameCalib, File wavFileNameDut, limitsHzDb limits,
                                       String filterCoeffLocation) throws Exception {
        try {
            // read wav to double
            AudioData audioDatCalib = wav2AudioData(wavFileNameCalib);
            AudioData audioDatDut = wav2AudioData(wavFileNameDut);

            TestResult TestResultObj;
            TestResultObj = testNoise(audioDatCalib, audioDatDut, limits, filterCoeffLocation);
            return TestResultObj;
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
            throw e;
        }
    }

    private static AudioData disregardBeginning(limitsHzDb limits, AudioData audioDat) {
        // Disregard first samples due to audio problems
        int disregardSamp = (int) (limits.disregardSecs * audioDat.samplingRate);
        int newLen = audioDat.timeDomDataFirstChannel.length - disregardSamp;
        AudioData out = new AudioData(newLen);
        System.arraycopy(audioDat.timeDomDataFirstChannel, disregardSamp, out.timeDomDataFirstChannel, 0, newLen);

        return out;
    }

    // Convert wav file to Audio Data
    private static AudioData wav2AudioData(File fileToBeRead) throws WavFileException, IOException {
        try {
            // Open the wav file
            WavFile wavFile = WavFile.openWavFile(fileToBeRead);

            // Get number of frames and convert to int
            long nFrames = wavFile.getNumFrames();
            int nFramesInt = (int) nFrames;

            // Create buffers with the right length
            double[] timeDomData = new double[nFramesInt];

            // Read Frames
            wavFile.readFrames(timeDomData, nFramesInt);

            // Get sampling rate
            long samplingRate = wavFile.getSampleRate();

            // Make return object
            AudioData audioDataObj;
            audioDataObj = new AudioData(timeDomData.length);
            audioDataObj.samplingRate = (int) samplingRate;
            audioDataObj.timeDomDataFirstChannel = timeDomData;

            // Close Wav File
            wavFile.close();

            return audioDataObj;
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
            throw e;
        }
    }

    private static void makeThirdOctaveVec(TestResult TestResultObj) {

        int length = 16;

        int[] thirdOctaveFreqs = new int[length];
        thirdOctaveFreqs[0] = (int) 250;
        thirdOctaveFreqs[1] = (int) 315;
        thirdOctaveFreqs[2] = (int) 400;
        thirdOctaveFreqs[3] = (int) 500;
        thirdOctaveFreqs[4] = (int) 630;
        thirdOctaveFreqs[5] = (int) 800;
        thirdOctaveFreqs[6] = (int) 1000;
        thirdOctaveFreqs[7] = (int) 1250;
        thirdOctaveFreqs[8] = (int) 1600;
        thirdOctaveFreqs[9] = (int) 2000;
        thirdOctaveFreqs[10] = (int) 2500;
        thirdOctaveFreqs[11] = (int) 3150;
        thirdOctaveFreqs[12] = (int) 4000;
        thirdOctaveFreqs[13] = (int) 5000;
        thirdOctaveFreqs[14] = (int) 6300;
        thirdOctaveFreqs[15] = (int) 8000;

        int[] thrirdOctaveFVecIdx = hz2Idx(TestResultObj.freqVec, thirdOctaveFreqs);

        for (int i = 0; i < length; i++) {
            TestResultObj.diffVecDBThird[i] = Precision.round(TestResultObj.diffVecDB[thrirdOctaveFVecIdx[i]], 1);
        }

    }

    private static double[] firFilter(double[] inputVector, double[] filterCoeffs) {
        int length = filterCoeffs.length;
        double[] impulseResponse = filterCoeffs;
        double[] delayLine = new double[length];
        int count = 0;
        double[] output = new double[inputVector.length];

        for (int u = 0; u < inputVector.length; u++) {
            delayLine[count] = inputVector[u];
            double result = 0.0;
            int index = count;
            for (int i = 0; i < length; i++) {
                result += impulseResponse[i] * delayLine[index--];
                if (index < 0)
                    index = length - 1;
            }
            if (++count >= length)
                count = 0;
            output[u] = result;
        }
        return output;
    }

    private static double[] readFilter(String csvFile) throws IOException {
        String line = "";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // extend output
            DoubleArray out = new ResizableDoubleArray();
            // read values
            while ((line = br.readLine()) != null) {
                out.addElement(Double.parseDouble(line));
            }
            return out.getElements();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static double rms(double[] timeDomData) {
        double sqSum = 0;

        for (int i = 0; i < timeDomData.length; i++) {
            sqSum += timeDomData[i] * timeDomData[i];
        }
        return Math.sqrt(sqSum / timeDomData.length);
    }

    // Shorten longer file
    private static AudioData shortenLongerFile(double[] firstArray, double[] secondArray) {
        int lengthDif = firstArray.length - secondArray.length;

        if (lengthDif > 0) { // the first array is longer -> shorten the first array
            AudioData audioDatComb = new AudioData(secondArray.length);
            double[] nest = new double[secondArray.length];
 
            // put second array in the nest
            System.arraycopy(firstArray, 0, nest, 0, secondArray.length);
            audioDatComb.timeDomDataFirstChannel = nest;
            audioDatComb.timeDomDataSecondChannel = secondArray;
            return audioDatComb;
        }

        if (lengthDif < 0) { // the second array is longer -> shorten the second array
            AudioData audioDatComb = new AudioData(firstArray.length);
            // fill a nest with beautiful zeros
            double[] nest = new double[firstArray.length];

            // put second array in this nest
            System.arraycopy(secondArray, 0, nest, 0, firstArray.length);
            audioDatComb.timeDomDataFirstChannel = firstArray;
            audioDatComb.timeDomDataSecondChannel = nest;
            return audioDatComb;
        }

        if (lengthDif == 0) {
            AudioData audioDatComb = new AudioData(secondArray.length);
            audioDatComb.timeDomDataFirstChannel = firstArray;
            audioDatComb.timeDomDataSecondChannel = secondArray;
            return audioDatComb;
        }
        AudioData audioDatComb = new AudioData(0);
        return audioDatComb;
    }

    // discrete Fourier transform
    private static double[] dft(double[] timeDomData) {
        // Prep for transform
        int nFramesInt = timeDomData.length;
        double[] freqDomData = new double[nFramesInt];
        freqDomData = timeDomData.clone();
        DoubleFFT_1D fftObj = new DoubleFFT_1D(nFramesInt);
        // Transform
        fftObj.realForward(freqDomData);
        // square
        double[] freqDomDataSq = freqDomData;
        for (int i = 0; i < freqDomData.length; i++) {
            if (freqDomData[i] == 0) {
                freqDomData[i] = Double.MIN_VALUE;
            }
            freqDomDataSq[i] = (int) Math.pow(freqDomData[i], 2);
        }
        return freqDomDataSq;
    }

    // in dB
    private static double[] dB(double[] data) {
        double[] dataInDb = data;
        for (int i = 0; i < data.length; i++) {
            dataInDb[i] = 10 * Math.log10(data[i]);
        }
        return dataInDb;
    }

    private static double dB(double data) {
        double dataInDb = 10 * Math.log10(data);
        return dataInDb;
    }

    // make a frequency vector for plots
    private static double[] makeFreqVec(int samplingRate, int nSamples) {
        double[] freqVec = new double[nSamples];
        double deltaF = (double) samplingRate / (2 * nSamples);
        for (int i = 1; i < (int) nSamples; i++) {
            freqVec[i] = Math.log10(deltaF * i);
        }
        return freqVec;
    }

    // Really basic smoothing function
    private static double[] smooth(double[] data, int smoothingBwBins) {
        double[] tempArray = new double[smoothingBwBins];

        int nNewBins = data.length / smoothingBwBins;
        double[] smoothedData = new double[nNewBins];

        for (int i = 1; i < nNewBins; i++) {
            System.arraycopy(data, (i - 1) * smoothingBwBins, tempArray, 0, smoothingBwBins);
            smoothedData[i] = Arrays.stream(tempArray).sum() / smoothingBwBins;
        }
        return smoothedData;
    }

    // convert frequencies to indices
    private static int[] hz2Idx(double[] freqVec, int[] range) {
        double[] tempDiff = new double[freqVec.length];
        int[] idxArray = new int[range.length];
        Object min = new Object();

        for (int u = 0; u < range.length; u++) {
            for (int i = 0; i < freqVec.length; i++) {
                tempDiff[i] = java.lang.Math.abs(freqVec[i] - Math.log10(range[u]));
            }

            min = Arrays.stream(tempDiff).min();

            int i = 0;
            while (tempDiff[i] != ((OptionalDouble) min).getAsDouble()) {
                i++;
            }
            idxArray[u] = i;
        }

        return idxArray;
    }

}
