import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.util.Arrays;
import java.awt.*;

/**
	This plugin convolves a kernel with a greyscale image
	
 **/

public class nogFloat implements PlugInFilter {
    
    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL+DOES_STACKS+SUPPORTS_MASKING;
    }
    
    public void run(ImageProcessor ip) {
        
        //x-derivative kernel
        float[][] xder = {
            {-0.00172742f, -0.0034983f, -0.00340688f, 0.f, 0.00340688f, 0.0034983f, 0.00172742f},
            
            {-0.00524744f, -0.0106269f, -0.0103492f, 0.f, 0.0103492f, 0.0106269f, 0.00524744f},
            
            {-0.0102206f, -0.0206983f, -0.0201574f, 0.f, 0.0201574f, 0.0206983f, 0.0102206f},
            
            {-0.0127641f, -0.0258491f, -0.0251736f, 0.f, 0.0251736f, 0.0258491f, 0.012764f},
            
            {-0.0102206f, -0.0206983f, -0.0201574f, 0.f, 0.0201574f, 0.0206983f, 0.0102206f},
            
            {-0.00524744f, -0.0106269f, -0.0103492f, 0.f, 0.0103492f, 0.0106269f, 0.00524744f},
            
            {-0.00172742f, -0.0034983f, -0.00340688f, 0.f, 0.00340688f, 0.0034983f, 0.00172742f}
        };
        
        //y-derivative kernel
        float[][] yder ={
            {-0.00125956f, -0.0045099f, -0.00969472f, -0.0125119f, -0.00969472f, -0.0045099f, -0.00125956f},
           
            {-0.0030066f, -0.0107652f, -0.0231415f, -0.0298662f, -0.0231415f, -0.0107652f, -0.0030066f},
            
            {-0.00323157f, -0.0115707f, -0.0248731f, -0.032101f, -0.0248731f, -0.0115707f, -0.00323157f},
            
            {0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f},
            
            {0.00323157f, 0.0115707f, 0.0248731f, 0.032101f, 0.0248731f, 0.0115707f, 0.00323157f},
            
            {0.0030066f, 0.0107652f, 0.0231415f, 0.0298662f, 0.0231415f, 0.0107652f, 0.0030066f},
            
            {0.00125956f, 0.0045099f, 0.00969472f, 0.0125119f, 0.00969472f, 0.0045099f, 0.00125956f}
        };
        
        //Convolve the image with the x-derivative and y-derivate kernel
        float[][] xderconvolved = convolve(ip, xder);
        float[][] yderconvolved = convolve(ip, yder);
        
        //Obtain the Norm of Gaussian from the convolved x&y derivative convolved kernels
        float[][] convolvedImage = calcNog(xderconvolved, yderconvolved);
        
        //Set the current ImageProcessor's pixel array to the new convoluted output
        ip.setFloatArray(convolvedImage);
        
        //Create a new ImagePlus instance of the modified ImageProcessor and display it
        ImagePlus imp = new ImagePlus("output", ip);
        imp.show();
    }
    
    //This function is used to calculate the norm of gradient guassian of an image
    //It takes as inputs convoled x & y int[][] arrays and returns the Norm of Gradient of Gaussian
    public float[][] calcNog(float[][] xderconvolved, float[][] yderconvolved) {
        
        //Initialize 2d int array that will be of the same size as one of the input arrays
        float[][] nog = new float[xderconvolved.length][xderconvolved[0].length];
        
        //xderconvolved and yderconvolved will be of the same size
        for (int i=0; i<xderconvolved.length; i++) {
            for (int j=0; j<yderconvolved.length; j++) {
                
                //Norm of Gaussian is the square root of the sum of the x-der and y-der values squared respectively
                //normalize by dividing by sqrt of 2
                nog[i][j] = (float)(Math.sqrt((xderconvolved[i][j] * xderconvolved[i][j]) + (yderconvolved[i][j] * yderconvolved[i][j]))/ 1.41);
            }
        }
        
        return nog;
    }
 
    public float[][] convolve(ImageProcessor ip, float[][] kernel) {
        
        //Get image's pixel data as a float array
        float[][] image = ip.getFloatArray();
        
        //Kernel has MxN dimensions
        int m = ((kernel.length) - 1)/2;
        int n = ((kernel[m].length)-1)/2;
        
        //Initialize new float array with same length as input image
        float[][] output = new float[image.length][image[0].length];

        //for each image row
        for (int i=m; i<image.length-m; i++) {
            
            //for each pixel
            for (int j=n; j<image[i].length-n; j++) {
               
                //default sum used for convolutions
                float sum = 0.0f;
                
                //for each kernel row
                for (int k=0; k<kernel.length; k++) {
                    
                    //for each kernel element
                    for (int l=0; l<kernel[k].length; l++) {
                        
                        //If the picture's current element corresponds to the kernel's current element...then multiply and accumulate
                        sum += image[i-k+m][j-l+n] * kernel[k][l];
                    }
                }
                
                //set running sum to output array
                output[i][j] = sum;
            }
        }

        return output;
    }

    
}





