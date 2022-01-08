import javax.swing.SwingUtilities;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.WindowConstants;

public class Plotter 
{
	
	public void plotStuff(double[] x, double[] y, limitsHzDb limits)
	{
		XYSeriesCollection dataset = new XYSeriesCollection();

	    XYSeries dataSeries        = new XYSeries("Measurement Result");
	    XYSeries upTolSeries       = new XYSeries("");
	    XYSeries loTolSeries       = new XYSeries("Tolerance");
	    for (int i=1; i<y.length;
	    	     i++) {
	    	dataSeries.add(x[i], y[i]);
	    }
	    
	    upTolSeries.add(Math.log10(limits.toleranceHz[0]), limits.toleranceDb[0]);
	    loTolSeries.add(Math.log10(limits.toleranceHz[0]), -limits.toleranceDb[0]);
	    upTolSeries.add(Math.log10(limits.toleranceHz[1]), limits.toleranceDb[0]);
	    loTolSeries.add(Math.log10(limits.toleranceHz[1]), -limits.toleranceDb[0]);
	    
	    for (int i=1; i<limits.toleranceHz.length-2;
	    	     i++) {
	    dataSeries.add(x[i], y[i]);
	    upTolSeries.add(Math.log10(limits.toleranceHz[i]), limits.toleranceDb[i]);
	    upTolSeries.add(Math.log10(limits.toleranceHz[i+1]), limits.toleranceDb[i]);
	    loTolSeries.add(Math.log10(limits.toleranceHz[i]), -limits.toleranceDb[i]);
	    loTolSeries.add(Math.log10(limits.toleranceHz[i+1]), -limits.toleranceDb[i]);
	    }
	    upTolSeries.add(Math.log10(limits.toleranceHz[limits.toleranceHz.length-2]), limits.toleranceDb[limits.toleranceHz.length-1]);
	    loTolSeries.add(Math.log10(limits.toleranceHz[limits.toleranceHz.length-2]), -limits.toleranceDb[limits.toleranceHz.length-1]);
	    upTolSeries.add(Math.log10(limits.toleranceHz[limits.toleranceHz.length-1]), limits.toleranceDb[limits.toleranceHz.length-1]);
	    loTolSeries.add(Math.log10(limits.toleranceHz[limits.toleranceHz.length-1]), -limits.toleranceDb[limits.toleranceHz.length-1]);

	    //Add series to data set
	    dataset.addSeries(dataSeries);
	    dataset.addSeries(upTolSeries);
	    dataset.addSeries(loTolSeries);
		    
	    JFrameExtension plotMaker   = new JFrameExtension();
        plotMaker.makePlot(dataset, limits.toleranceHz, limits.toleranceDb);
	        
	    SwingUtilities.invokeLater(() -> {
	    plotMaker.setSize(800, 400);
	    plotMaker.setLocationRelativeTo(null);
	    plotMaker.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    plotMaker.setVisible(true);
	    });
	  }
}
