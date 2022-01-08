import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.Color;

import javax.swing.JFrame;


public class JFrameExtension extends JFrame{
	private static final long serialVersionUID = 1234;

	
	public void makePlot(XYSeriesCollection dataset, int[] toleranceHz, int[] toleranceDb)
	{
	    int[] rangeHz               = new int[2];
	    rangeHz[0]                  = (int) Math.log10(toleranceHz[0]);
	    rangeHz[1]                  = (int) Math.log10(toleranceHz[toleranceHz.length-1]);
	
	    JFreeChart chart = ChartFactory.createXYLineChart(
	    "DUT minus REF",
	    "",
	    "Sensitivity Difference in dB",
        dataset,
	    PlotOrientation.VERTICAL,
	    true, false, false);
	    
	    XYPlot plot = chart.getXYPlot();
	    
        StandardXYItemRenderer renderer = new StandardXYItemRenderer();
        renderer.setSeriesPaint(0, Color.BLACK);
        renderer.setSeriesPaint(1, Color.RED);
        renderer.setSeriesPaint(2, Color.RED);
        plot.setRenderer(0, renderer);
	    
        NumberAxis xAxis = new NumberAxis("Frequency in 10^x Hz");
	    xAxis.setRange(rangeHz[0], rangeHz[1]+1);
        plot.setDomainAxis(xAxis);

	    NumberAxis yAx = (NumberAxis) plot.getRangeAxis();
	    yAx.setRange(-3*toleranceDb[0], 3*toleranceDb[0]);
        
	    ChartPanel panel = new ChartPanel(chart);
	    setContentPane(panel);
	    
	  }

}
