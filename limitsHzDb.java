public class limitsHzDb 
{
	public int    smoothingBwBins;
	public int    noiseLimitDb;
	public int[]  toleranceHz;
	public int[]  toleranceDb;
	public double disregardSecs;
	public double recLenSec;
	public double pauseSecs;
    
    public limitsHzDb(int length)
	{
    	this.smoothingBwBins= 1;
	    this.toleranceHz    = new int[length];
	    this.toleranceDb    = new int[length];
	}

	public void extend(int length) {
		this.smoothingBwBins= 1;
		this.noiseLimitDb   = 3;
	    this.toleranceHz    = new int[length];
	    this.toleranceDb    = new int[length];	
	}
}


