import  java.io.File;

public class Main{
 
    public static void main(String args[]){
      try
      {
        // Read limits file
        limitsHzDb limits            = ReadLimits.read("testData/limits.csv");
        System.out.println("rec length: " + limits.recLenSec +"s");
        System.out.println("pause length: " + limits.pauseSecs +"s");
        System.out.println("disregard length: " + limits.disregardSecs +"s");

        // Data for sensitivity test
        File userDir                 = new File(System.getProperty("user.dir"));
        File fileSensGolden          = new File(userDir,"testData/golden.wav");
        File fileSensDut             = new File(userDir,"testData/dut.wav");

        // Data for noise test
        File fileNoiseGolden         = new File(userDir,"testData/noiseGolden.wav");
        File fileNoiseDut            = new File(userDir,"testData/noiseDut.wav");
            
        // Here is the actual test
        TestResult TestResultObj     = AudioTest.testSensitivity(fileSensGolden,fileSensDut,limits);
        TestResult TestResultObjN    = AudioTest.testNoise(fileNoiseGolden,fileNoiseDut,limits,"testData/firFilterCoeffs.CSV");
        
        // Debug Plot Results        
        Plotter plotter     = new Plotter();
        plotter.plotStuff(TestResultObj.freqVec,TestResultObj.diffVecDB,limits);
        
        System.out.println(TestResultObj.comment);
        System.out.println("sens test pass: " + TestResultObj.isPass);
        System.out.println(TestResultObjN.comment);
        System.out.println("noise test pass: " + TestResultObjN.isPass);
        System.out.println("noise difference in dB: " + TestResultObjN.dutValue);
                    
        }
        catch (Exception e)
        {
          System.err.println(e);
        }
    }
}