import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.util.Arrays;
import java.awt.*;

/**
	This plugin convolves a kernel with a greyscale image
	
 **/

public class MarrHildreth implements PlugInFilter {
    
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

        //First convolve the image with the Laplacian of Gaussian Kernel
        int[][] convolvedImage = convolve(ip, kernel);

        //Then use the LoG output to apply the Marr-Hildreth algorithm and detect edges
        int[][] marrHildreth = findEdges(convolvedImage);
        
        //Set the current ImageProcessor's pixel array to the new convoluted output
        ip.setIntArray(marrHildreth);
        
        //Create a new ImagePlus instance of the modified ImageProcessor and display it
        ImagePlus imp = new ImagePlus("output", ip);

        imp.show();
    }

    //This function applies the Marr-Hildreth function to detect for edges.
    //It takes as input a 2d int array that should've already been convolved with the Laplacian of Gaussian
    public int[][] findEdges(int[][] convolvedImage) {
        
        //output image of same length as input one
        int[][] output = new int [convolvedImage.length][convolvedImage[0].length];
        
        //for each image row
        for (int i=1; i<convolvedImage.length-1; i++) {
            
            //for each pixel
            for (int j=1; j<convolvedImage[i].length-1; j++) {
                
                //Black if not a zero-crossing neighbor
                output[i][j] = 0;
               
                //Find zero-crossings by comparing signs of neighbors in all possible directions
                //If the product of opposite neighbors is negative...then we know we have a zero-crossing neighbor
                if ( (convolvedImage[i-1][j-1] * convolvedImage[i+1][j+1] < 0) || (convolvedImage[i-1][j+1] * convolvedImage[i+1][j-1] < 0) || (convolvedImage[i-1][j] * convolvedImage[i+1][j] < 0) || (convolvedImage[i][j-1] * convolvedImage[i][j+1] < 0)) {
                    
                    //white if zero-crossing neighbor
                    output[i][j] = 255;
                }
            }
        }
        
        return output;
    }

    public int[][] convolve(ImageProcessor ip, int[][] kernel) {
        
        //Get image's pixel data as int array for processing
        int[][] image = ip.getIntArray();
        
        //Kernel has MxN dimensions
        int m = ((kernel.length) - 1)/2;
        int n = ((kernel[m].length)-1)/2;
        
        //output image of same length as input one
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
                
                //Set to output image
                output[i][j] = (int)Math.round(sum);
            }
        }
        
        return output;
    }
}





