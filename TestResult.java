public class TestResult 
{
	public boolean isPass;
    public String comment;
    public double[] freqVec;
    public double[] diffVecDB;
    public double[] diffVecDBThird;
    public double dutValue;

	public TestResult(int length)
	{
		this.isPass                   = true;
	    this.comment                  = ("Yay :-) !");
	    this.freqVec                  = new double[length];
	    this.diffVecDB                = new double[length];
	    this.diffVecDBThird           = new double[16];
	    this.dutValue                 = 0;
	}

}
