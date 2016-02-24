/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lmvd_ntf;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 *
 * @author ives
 */
public class LMVD_NTF {

    static int width;
    static int height;
    static int size;
    static Image image;
    static BufferedImage bufferedImage;
    static final String a1 = "Original";
    static final String b1 = "Noise_Pmin_Pmax_0.05";
    static final String c1 = "Noise_Pmin_Pmax_0.45";
    static final String d1 = "Noise_Pmax_0.10";
    static final float density = (float) 0.049;
    public ArrayList<Integer> a1_B = new ArrayList<Integer>();
    public ArrayList<Integer> b1_B = new ArrayList<Integer>();
    public ArrayList<Integer> c1_B = new ArrayList<Integer>();
    public ArrayList<Integer> d1_B = new ArrayList<Integer>();
    static Boolean is_G0 = false;
    
    public BufferedImage bufferedImagea1;
    public BufferedImage bufferedImageb1;
    public BufferedImage bufferedImagec1;
    public BufferedImage bufferedImaged1;
    
    public BufferedImage bufferedImagea1_remove;
    public BufferedImage bufferedImageb1_remove;
    public BufferedImage bufferedImagec1_remove;
    public BufferedImage bufferedImaged1_remove;
    
    public float psnr_a1;
    public float psnr_b1;
    public float psnr_c1;
    public float psnr_d1;
    
    public static float currentNoise = 0;
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        LMVD_NTF lmvd_ntf = new LMVD_NTF();
    }
    
    public LMVD_NTF() throws IOException {
    
        image = ImageIO.read(new File("lena.jpg"));
        width = image.getWidth(null);
        height = image.getHeight(null);
        size = width * height;
        System.out.println("Iamge Size: " + size + " pixels");
        
        int[][] pixels = conver2Pixels(image);
        a1_B = getNoiseGrayLevel(pixels, density, a1);
        psnr_a1 = getPSNR(pixels, removeNoise(pixels, a1_B));
        outputImageFromPixels(pixels, a1);
        bufferedImagea1 = getBufImage(pixels);
        bufferedImagea1_remove = getBufImage(removeNoise(pixels, a1_B));
        
        int[][] noisePixelsBipolarLow = addNosieImpulse((float) 0.05, (float) 0.05, pixels);
        outputImageFromPixels(noisePixelsBipolarLow, b1);
        bufferedImageb1 = getBufImage(noisePixelsBipolarLow);
        b1_B = getNoiseGrayLevel(noisePixelsBipolarLow, density, b1);
        psnr_b1 = getPSNR(pixels, removeNoise(noisePixelsBipolarLow, b1_B));
        bufferedImageb1_remove = getBufImage(removeNoise(noisePixelsBipolarLow, b1_B));
        
        int[][] noisePixelsBipolarHigh = addNosieImpulse((float) 0.40, (float) 0.40, pixels);
        outputImageFromPixels(noisePixelsBipolarHigh, c1);
        bufferedImagec1 = getBufImage(noisePixelsBipolarHigh);
        c1_B = getNoiseGrayLevel(noisePixelsBipolarHigh, density, c1);
        psnr_c1 = getPSNR(pixels, removeNoise(noisePixelsBipolarHigh, c1_B));
        bufferedImagec1_remove = getBufImage(removeNoise(noisePixelsBipolarHigh, c1_B));
        
        int[][] noisePixelsUnipolarLow = addNosieImpulse(0, (float) 0.10, pixels);
        outputImageFromPixels(noisePixelsUnipolarLow, d1);
        bufferedImaged1 = getBufImage(noisePixelsUnipolarLow);
        d1_B = getNoiseGrayLevel(noisePixelsUnipolarLow, density, d1);
        psnr_d1 = getPSNR(pixels, removeNoise(noisePixelsUnipolarLow, d1_B));
        bufferedImaged1_remove = getBufImage(removeNoise(noisePixelsUnipolarLow, d1_B));

        
        outputImageFromPixels(removeNoise(pixels, a1_B), a1 + "_remove");
        outputImageFromPixels(removeNoise(noisePixelsBipolarLow, b1_B), b1 + "_remove");
        outputImageFromPixels(removeNoise(noisePixelsBipolarHigh, c1_B), c1 + "_remove");
        outputImageFromPixels(removeNoise(noisePixelsUnipolarLow, d1_B), d1 + "_remove");

//    int[][] pixel = {
//        {  0, 128,  45,   3,  76},
//        {122, 255, 130,  76,  54},
//        {  0, 255, 255, 255,  67},
//        {  0, 122, 255, 255,  67},
//        {  0, 122, 130,  78,  67}
//            };
//    int[][] grid = removeNoise(pixel, b);   
    
    }
    
    public static int[][] conver2Pixels(Image image) throws IOException {
        int[] pixel = new int[width * height];
        int[][] pixelMap = new int[width][height];
        int count = 0;
                
        PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixel, 0, width);
        try { pg.grabPixels();
        } catch (Exception e) { System.err.println(e);}
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int grayLevel = (int) ((0.299 * ((pixel[count] >> 16) & 0xff)) + (0.587 * ((pixel[count] >> 8) & 0xff)) + (0.114 * (pixel[count] & 0xff)));
                pixelMap[i][j] = grayLevel;
                count++;
            }
        }
        return  pixelMap;
    }
    
    public static void printPixels (int[][] pixels) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) { 
                if (j != height - 1) { System.out.print(pixels[i][j] + ", "); } 
                else { System.out.println(pixels[i][j]); }
            }
        }
    }
    
    public static void outputImageFromPixels(int[][] pixelsMap, String name) throws IOException {
        int[][] pixels = new int[width][height];
        pixels = pixelsMap;
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.OPAQUE);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int gray = (pixels[i][j] << 16) + (pixels[i][j] << 8) + pixels[i][j];
                bi.setRGB(j, i, gray);
            }
        }
        try (FileOutputStream out = new FileOutputStream("ResultImage/" + name + ".jpg")) {
            ImageIO.write(bi, "jpg", out);
        } catch (IOException e) {
            System.err.println(e);
        } 
    }
    
    public static BufferedImage getBufImage (int[][] pixelsMap) {
        int[][] pixels = new int[width][height];
        pixels = pixelsMap;
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.OPAQUE);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int gray = (pixels[i][j] << 16) + (pixels[i][j] << 8) + pixels[i][j];
                bi.setRGB(j, i, gray);
            }
        }
        return bi;
    }
    
    public static int[][] addNosieImpulse(float pmin, float pmax, int[][] pixels) {
         int[] pixelMap = new int[width * height];
         int[][] resultPixels = new int[width][height];
         int count = 0;
        
        int numberPmin = Math.round(width * height * pmin);
        int numberPmax = Math.round(width * height * pmax);
        int[] randomList = new int[width * height];
        ArrayList<Integer> random_numbers = new ArrayList<Integer>();
        Random rand = new Random();
        int value; 
        int randomCount = 0;
        int noise = 0;
        
        currentNoise = pmin + pmax;
        for (int i = 0; i < width; i++) { // 2D to 1D
            for (int j = 0; j < height; j++) {
              pixelMap[count] = pixels[i][j];
              count++;
            }
        }
        
        while(randomCount < (numberPmin + numberPmax)) {
            value = rand.nextInt(width * height);
            if (randomList[value] == 0) {
                randomList[value] = 1;
                random_numbers.add(value);
                randomCount++;
            }   
        }
        
        for (int i = 0; i < random_numbers.size(); i++) {
                pixelMap[random_numbers.get(i)] = noise;
                if (noise == 0 || pmin == 0) { noise = 255; }
                else { noise = 0; }
        }
        
        count = 0;
        for (int i = 0; i < width; i++) { // 1D to 2D
            for (int j = 0; j < height; j++) {
              resultPixels[i][j] = pixelMap[count];
              count++;
            }
        }
        
        //System.out.println("Pmin: " + pmin + ", Pmax: " + pmax + ", noise number: " + randomCount);
        return resultPixels;
    }
    
    public static ArrayList<Integer> getNoiseGrayLevel(int[][] pixels, float alpha, String name) {
        System.out.println();
        System.out.println("============LMVD Result: " + name + " ============");
        int[][] grid = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        int[] noise_A = new int[256];
        ArrayList<Integer> noise_B = new ArrayList<Integer>();
        int[] grayLevelCount = new int[256];
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) { grayLevelCount[pixels[i][j]]++; }
        }
               
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (((float)grayLevelCount[pixels[i][j]] / (width * height) >= alpha) && (i != 0 && j != 0 && (i != width -1) && (j != height - 1))) {
                    float mean = 0;
                    float sigma = 0;
                    
                    for (int k = 0; k < 8; k++) { mean += pixels[i + grid[k][0]][j + grid[k][1]]; }
                    mean = mean / 9;
                    for (int k = 0; k < 8; k++) {
                        sigma += Math.pow(pixels[i + grid[k][0]][j + grid[k][1]] - mean, 2);
                    }
                    sigma = (float) Math.sqrt(sigma / 9);
                    
                    if (Math.abs(pixels[i][j] - mean) > sigma) { noise_A[pixels[i][j]]++; }
                }
            }
            
        }
        
        
        int maxA = -1;
        int h = -1;
        for (int l = 0; l < noise_A.length; l++) {
            if (noise_A[l] > maxA) {
                maxA = noise_A[l];
                h = grayLevelCount[l];
            }
        }
            
        float t = -1;
        if (maxA >= (h * 1/3)) { 
            t = (float) (0.1 * maxA);
            System.out.println("A[n]: " + maxA + ", H[n]: " + h);
            System.out.println("T: " + t);
            for (int l = 0; l < 256; l++) { if (noise_A[l] > t) { noise_B.add(l); } }
        }

        System.out.print("B = { ");
        for (int i = 0; i < noise_B.size(); i++) {
            System.out.print(noise_B.get(i) + " "); 
        }
        System.out.println("}");
        
//        if (maxA == 0) {
//            System.out.println("A[n]: null, H[n]: null, T: null");
//            System.out.println("Noise-Free..."); 
//        }
        
        System.out.println("=========================================================");        
        return noise_B;
    }
    
    public static int[][] removeNoise(int[][] pixels, ArrayList<Integer> b) {
        int[][] new_pixels = pixels;
        int[][] grid = new int[6][3];
        int[][] element = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 0}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        
        for (int i = 0; i < pixels.length; i++) { 
            for (int j = 0; j < pixels.length; j++) { 
                if ((b.indexOf(pixels[i][j]) != -1) && (i != 0 && j != 0 && i != width -1 && j != height - 1)) { // corrupted pixel
                    int[][] near_pixel = new int[9][3];
                    for (int k= 0; k < 9; k++) {
                        near_pixel[k][0] = element[k][0];
                        near_pixel[k][1] = element[k][1];
                        near_pixel[k][2] = pixels[near_pixel[k][0] + i][near_pixel[k][1] + j];
                    }
                    grid = choose_g0_g1(near_pixel, b);
                                       
                    for (int k = 0; k < 6; k++) { // determine corrupted in grid
                        int[] gird_pixels = new int[9];
                        if (b.indexOf(grid[k][2]) != -1) {
                            for (int m = 0; m < 9; m++) {
                                if (i + grid[k][0] + element[m][0] >= 0 && i + grid[k][0] + element[m][0] < width) {
                                    if (j + grid[k][1] + element[m][1] >= 0 && j + grid[k][1] + element[m][1] > height) {
                                        gird_pixels[m] = pixels[i + grid[k][0] + element[m][0]][j + grid[k][1] + element[m][1]];
                                    }
                                }
                                
                            }
                            grid[k][2] = (int) fixPixelInGrid(gird_pixels);
                        }                     
                    }
                   
                    
                    new_pixels[i][j] = fixNoisePixel(pixels[i][j], grid);

                }
            }
        }        
        return new_pixels;
    }
    
    public static int[][] choose_g0_g1 (int[][] pixels, ArrayList<Integer> b) {
        int[][] grid = new int[6][3];
        int[][] element = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 0}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        int[] g0_order = {0, 1, 2, 6, 7, 8};
        int[] g1_order = {0, 2, 3, 5, 6, 8};
        int[] g_order = new int[6];
        int g0_count_NF = 0;
        int g1_count_NF = 0;
        for (int i = 0; i < 6; i++) { // determine g0 or g1
            if (b.indexOf(pixels[g0_order[i]][2]) == -1) { g0_count_NF++; }
            if (b.indexOf(pixels[g1_order[i]][2]) == -1) { g1_count_NF++; }
        }
        if (g0_count_NF >= g1_count_NF) { g_order = g0_order;
            is_G0 = true;
        }
        else { g_order = g1_order; }
        for (int i = 0; i < 6; i++) {
            grid[i][0] = element[g_order[i]][0];
            grid[i][1] = element[g_order[i]][1];
            grid[i][2] = pixels[g_order[i]][2];
        }
        
        return grid;
    }
    
    public static float fixPixelInGrid(int[] pixels) {
        float newPixel = -1;
        float[] w = new float[4];
        float[] l = new float[4];
        float[] d = new float[4];
        float sumD = 0;
        int[][] lrCoor = {{1, 7}, {0, 8}, {3, 5}, {6, 2}};
        
        for (int i = 0; i < 4; i++) {
            d[i] = (float) Math.exp(-(Math.abs((pixels[lrCoor[i][0]] - pixels[lrCoor[i][1]]) / (-2))));
            l[i] = pixels[lrCoor[i][0]] + ((pixels[lrCoor[i][1]] - pixels[lrCoor[i][0]]) / 2);
            sumD += d[i];
        }
        
        for (int i = 0; i < 4; i++) { w[i] = d[i] / sumD; }
        newPixel = (w[0] * l[0]) + (w[1] * l[1]) + (w[2] * l[2]) + (w[3] * l[3]);
        if (newPixel > 255) { newPixel = 255; }
        if (newPixel < 0) { newPixel = 0; }
        return newPixel;
    }
    
    public static int fixNoisePixel(int noise_pixel, int[][] grid) {
        int pixel = 0;
        int[][] m = new int[2][3];
        int[] g0_order = {0, 1, 2, 3, 4, 5};
        int[] g1_order = {1, 3, 5, 0, 2, 4};
        int[] g_order = new int[6];
        int index = 0;
        int consta = 45;
        
        if (is_G0) {
            g_order = g0_order;
        } else { g_order = g1_order; }
        
        int max = 0;
        int min = 255;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                m[i][j] = grid[g_order[index]][2];
                if (m[i][j] > max) { max = m[i][j]; }
                if (m[i][j] < min) { min = m[i][j]; }
                index++;
            }
        }
        
        float def_1 = (m[1][1] - m[0][0]) - (m[0][1] - m[0][0]);
        float def_2 = (m[0][2] - m[0][0]) - def_1;

        pixel = (int) (m[0][0] + (m[0][1] - m[0][0]) + def_1 - def_2 + noise_pixel);
        if (pixel <= min || pixel > max) {
            pixel = 0;
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) { 
                   if (currentNoise > 0.6) {
                       pixel += m[i][j] + consta;
                   }
                   else { pixel += m[i][j]; }
                }
            }
            pixel /= 6;
        }
        
        if (pixel > 255) { pixel = 255; }
        if (pixel < 0) { pixel = 0; }
        
        return pixel;
    }
    
    public static float getPSNR(int[][] pixels1, int[][] pixels2) {
        float psnr;
        float mse = 0;
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                mse += Math.pow(pixels1[i][j] - pixels2[i][j], 2);
            }
        }
        mse = mse / (width * height);
        psnr = (float) (10 * (Math.log10(Math.pow(255, 2) / mse)));
        System.out.println("Restore PSNR:" + psnr);
        
        return psnr;
    }
    
//    public static BufferedImage makeGray(BufferedImage img)
//{
//    for (int x = 0; x < img.getWidth(); ++x)
//    for (int y = 0; y < img.getHeight(); ++y)
//    {
//        int rgb = img.getRGB(x, y);
//        int r = (rgb >> 16) & 0xFF;
//        int g = (rgb >> 8) & 0xFF;
//        int b = (rgb & 0xFF);
//
//        int grayLevel = (r + g + b) / 3;
//        int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel; 
//        img.setRGB(x, y, gray);
//        System.err.println(gray);
//    }
//    return img;
//}
    
}
