import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.util.Arrays;
import java.awt.*;

/**
	This plugin convolves a kernel with a greyscale image
	
 **/

public class log implements PlugInFilter {
    
    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL+DOES_STACKS+SUPPORTS_MASKING;
    }
    
    public void run(ImageProcessor ip) {
        
        //LoG kernel
        int[][] kernel = {
            {1, 1, 2, 2, 2, 1, 1},
            {1, 2, 1, 0, 1, 2, 1},
            {2, 1, -4, -9, -4, 1, 2},
            {2, 0, -9, -15, -9, 0, 2},
            {2, 1, -4, -9, -4, 1, 2},
            {1, 2, 1, 0, 1, 2, 1},
            {1, 1, 2, 2, 2, 1, 1}
        };
        
        //call convolve function on this image with the aforementioned kernel
        int[][] convolvedImage = convolve(ip, kernel);
        
        //Set the current ImageProcessor's pixel array to the new convoluted output
        ip.setIntArray(convolvedImage);
        
        //Create a new ImagePlus instance of the modified ImageProcessor and display it
        ImagePlus imp = new ImagePlus("output", ip);
        imp.show();
    }
    
    public int[][] convolve(ImageProcessor ip, int[][] kernel) {
       
        //Get image's pixel data as int array for processing
        int[][] image = ip.getIntArray();
        
        //Kernel has MxN dimensions
        int m = ((kernel.length) - 1)/2;
        int n = ((kernel[m].length)-1)/2;
        
        int[][] output = new int[image.length][image[0].length];
        
        //for each image row
        for (int i=m; i<image.length-m; i++) {
            
            //for each pixel
            for (int j=n; j<image[i].length-n; j++) {
               
                //default sum used for convolutions
                int sum = 0;
                
                //for each kernel row
                for (int k=0; k<kernel.length; k++) {
                    
                    //for each kernel element
                    for (int l=0; l<kernel[k].length; l++) {
                        
                        //If the picture's current element corresponds to the kernel's current element...then multiply and accumulate
                        sum += image[i-k+m][j-l+n] * kernel[k][l];
                    }
                }
                
                //Scaling has to be applied to achieve results that can be conveyed by humans
                output[i][j] = (int)Math.round((sum+85)*255/(151*180));
            }
        }
        
        return output;
    }
    
}





