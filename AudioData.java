public class AudioData {
    public AudioData(int length) {
        this.samplingRate = 16000;
        this.timeDomDataFirstChannel = new double[length];
        this.timeDomDataSecondChannel = new double[length];
    }

    int samplingRate;
    double[] timeDomDataFirstChannel;
    double[] timeDomDataSecondChannel;
}