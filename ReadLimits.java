import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadLimits {

    public static limitsHzDb read(String csvFile) throws IOException {

        BufferedReader br = null;
        limitsHzDb out = new limitsHzDb(0);

        try {
            String line = "";
            String csvSplitBy = ",";
            int len = 0;
            int count = 0;

            // extend output
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                len++;
            }
            br.close();
            br = new BufferedReader(new FileReader(csvFile));
            out.extend(len - 5);

            // read values
            while ((line = br.readLine()) != null) {
                String[] lineRead = line.split(csvSplitBy);
                if (count == len) {
                    break;
                }
                switch (count) {
                    case 0:
                        out.smoothingBwBins = Integer.parseInt(lineRead[0]);
                        break;
                    case 1:
                        out.noiseLimitDb = Integer.parseInt(lineRead[0]);
                        break;
                    case 2:
                        out.recLenSec = Double.parseDouble(lineRead[0]);
                        break;
                    case 3:
                        out.disregardSecs = Integer.parseInt(lineRead[0]);
                        break;
                    case 4:
                        out.pauseSecs = Integer.parseInt(lineRead[0]);
                        break;
                    default: {
                        out.toleranceHz[count - 5] = Integer.parseInt(lineRead[0]);
                        out.toleranceDb[count - 5] = Integer.parseInt(lineRead[1]);
                    }
                }
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        return out;
    }

}
