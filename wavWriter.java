import java.io.*;

public class wavWriter {
	
	   public static void write(String name, double[] writeData, int sampleRate)	   {
	      try
	      {
	         // Create a wav file with the name specified as the first argument
	         WavFile wavFile = WavFile.newWavFile(new File(name), 1, writeData.length, 16, sampleRate);

	         // Write the buffer
	         wavFile.writeFrames(writeData, writeData.length);
	         

	         // Close the wavFile
	         wavFile.close();
	      }
	      catch (Exception e)
	      {
	         System.err.println(e);
	      }
	   }
	}
