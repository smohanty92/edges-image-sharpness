import java.util.*;
import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.process.ImageStatistics;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

/** 
	This filter determines parts of an image that are least/most in focus
*/

public class Focus implements PlugInFilter {

	public int setup(String arg, ImagePlus imp) {
		return DOES_ALL+DOES_STACKS+SUPPORTS_MASKING;
	}

	public void run(ImageProcessor ip) {
		
        //Get the current instance of the ROI Manager
        RoiManager roiMng = RoiManager.getInstance();

        //Get all selected ROIs in the current instance
        Roi[] rois = roiMng.getRoisAsArray();
        
        //Initialize this hashmap to later be used for logging back to the user
        HashMap<Double,String> hm=new HashMap<Double,String>();
        
        //Initializiation of each ROI's name
        String roiName = new String("");
        
        //Per every ROI
        for (int i=0; i<rois.length; i++) {
            
            //Obtain its ImageStatistics object
            ImageStatistics is = rois[i].getStatistics();
            
            //Retrieve a histogram of pixel data for each ROI
            int[] histogram = is.histogram;
            
            //Initialize this sum
            int histogramSum = 0;
            
            //Initialize all these values to later be used for computing within loops
            double histogramAvg, sumOfDiffOfAvg, variance, stdDev;
            histogramAvg = sumOfDiffOfAvg = variance = stdDev = 0.0;
            
            //Calculate sum of all histogram values
            for (int j=0; j<histogram.length; j++) {
                histogramSum += histogram[j];
            }
            
            //Get average of histogram values
            histogramAvg = histogramSum/histogram.length;
            
            
            //Obtain the sum of the average distances
            //NOTE: ------- This loop could've easily been in the one above. However, I'm abstracting it for clarity of the alogithm in detecting a standard deviation ---------
            for (int j=0; j<histogram.length; j++) {
                
                //We square the average distance values bc standard deviation is always positive
                sumOfDiffOfAvg += (histogram[j] - histogramAvg) * (histogram[j] - histogramAvg);
            }
            
            //The variance is the average of the average distances
            variance = sumOfDiffOfAvg/histogram.length;
            
            //Reverse the squaring by taking the square root
            stdDev = Math.sqrt(variance);
            
            //Get the name of this current ROI
            roiName = rois[i].getName();
            
            //Store the standard deviations and ROI names into our HashMap. Where the keys are the standard deviations
            hm.put(stdDev, roiName);
        }
        
        //Sort from least focused to most
        Collection<Double> stdDevs = hm.keySet();
        Double[] standardDevs = stdDevs.toArray(new Double[stdDevs.size()]);
        Arrays.sort(standardDevs);
        
        //Log back to user....
        IJ.log("Now listing selected regions from least to most focused");
        for (int i=0; i<standardDevs.length; i++) {
            IJ.log(hm.get(standardDevs[i]) + " : standard deviation is " + standardDevs[i]);
        }
        
	}

}
