package finalproj;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageFilter_enhancement {
	private int width, height;
	private int[] sourcePixels;
	private String path;
	private String imageName;
	
	public ImageFilter_enhancement(int width, int height, int[] source, String path, String imageName) {
		this.height = height;
		this.width = width;
		this.sourcePixels = source;
		this.path = path;
		this.imageName = imageName;
	}
	
	public void GuidedFilter_enhancement(int radius, double episilon) {
		int[] result = new int[height * width];
		double[][] meanOfI = new double[height * width][3];
		double[][] varianceOfI = new double[height * width][3];
		double[][] ak = new double[height * width][3];
		double[][] bk = new double[height * width][3];
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				
				double[] meanOfITemp = caulateMeanOfI(w, h, radius);
				meanOfI[h * width + w][0] = meanOfITemp[0];
				meanOfI[h * width + w][1] = meanOfITemp[1];
				meanOfI[h * width + w][2] = meanOfITemp[2];
				
				double[] varianceOfITemp = caculateVariance(w, h, radius, meanOfITemp);
				varianceOfI[h * width + w][0] = varianceOfITemp[0];
				varianceOfI[h * width + w][1] = varianceOfITemp[1];
				varianceOfI[h * width + w][2] = varianceOfITemp[2];
				
				double[] akTemp = caculateAK(w, h, radius, episilon, meanOfITemp, varianceOfITemp);
				ak[h * width + w][0] = akTemp[0];
				ak[h * width + w][1] = akTemp[1];
				ak[h * width + w][2] = akTemp[2];
				
				double[] bkTemp = caculateBK(akTemp, meanOfITemp);
				bk[h * width + w][0] = bkTemp[0];
				bk[h * width + w][1] = bkTemp[1];
				bk[h * width + w][2] = bkTemp[2];
			}
		}
		
		//开始滤波
		for (int h = 0; h < height; h++)
			for (int w = 0; w < width; w++) {
				double[] rgb = new double[3];
				rgb[0] = (double)((sourcePixels[h * width + w] & 0x00FF0000) >> 16) / 255;
				rgb[1] = (double)((sourcePixels[h * width + w] & 0x0000FF00) >> 8) / 255;
				rgb[2] = (double)(sourcePixels[h * width + w] & 0x000000FF) / 255;
				
				double[] newRGB = new double[3];
				double[] temp = new double [3];
				for (int i = 0; i < 3; i++)
					temp[i] =0;
				int t = 0;
				for (int i = h - radius; i <= h + radius; i++)
					for (int j = w - radius; j <= w + radius; j++) {
						if ((i < 0 || i >= height) || (j < 0 || j >= width))
							t++;
						else {
							for (int k = 0; k < 3; k++)
								temp[k] += ak[i * width + j][k] * rgb[k] + bk[i * width + j][k];
						}
					}
				for (int i = 0; i < 3; i++) {
					newRGB[i] = temp[i] / (Math.pow(radius * 2 + 1, 2) - t);
					newRGB[i] *= 255;
					//newRGB[i] = rgb[i] * 255 + 2 * (rgb[i] * 255 - newRGB[i]);
					
					if (newRGB[i] < 0)
						newRGB[i] = 0;
					else if (newRGB[i] > 255)
						newRGB[i] = 255;
				}
			
				
			result[h * width + w] = 0xFF000000 | (int)newRGB[0] << 16 | (int)newRGB[1]  << 8 | (int)newRGB[2] & 0xff;
			}
		
		save(result, radius, imageName, (float) episilon);
		
	}
	
	private double[] caculateVariance(int w, int h, int radius, double[] meanOfITemp) {
		double[] variance = new double[3];
		for (int i = 0; i < 3; i++)
			variance[i] = 0;
		
		int t = 0;
		double windowSize = Math.pow((double)(radius * 2 + 1), 2);
		for (int i = h - radius; i <= h + radius; i++)
			for (int j = w - radius; j <= w + radius; j++) {
				double[] rgb = new double[3];
				if ((i < 0 || i >= height) || (j < 0 || j >= width)) {
					t++;
				}
				else {
					rgb[0] = (double)((sourcePixels[i * width + j] & 0x00FF0000) >> 16) / 255;
					rgb[1] = (double)((sourcePixels[i * width + j] & 0x0000FF00) >> 8) / 255;
					rgb[2] = (double)(sourcePixels[i * width + j] & 0x000000FF) / 255;
					
					for (int k = 0; k < 3; k++)
						variance[k] += ((double)rgb[k] - meanOfITemp[k]) * ((double)rgb[k] - meanOfITemp[k]);
				}
			}
		
		for (int i = 0; i < 3; i++)
			variance[i] /= (windowSize - t);
		return variance;
	}

	private double[] caulateMeanOfI(int w, int h, int radius) {
		double[] mean = new double[3];
		for (int i = 0; i < 3; i++)
			mean[i] = 0;
		int t = 0;
		double windowSize =  Math.pow((double)(radius * 2 + 1), 2);
		
		for (int i = h - radius; i <= h + radius; i++)
			for (int j = w - radius; j <= w + radius; j++) {
				double[] rgb = new double[3];
				if ((i < 0 || i >= height) || (j < 0 || j >= width)) {
					t++;
				}
				else {
					rgb[0] = (double)((sourcePixels[i * width + j] & 0x00FF0000) >> 16) / 255;
					rgb[1] = (double)((sourcePixels[i * width + j] & 0x0000FF00) >> 8) / 255;
					rgb[2] = (double)(sourcePixels[i * width + j] & 0x000000FF) / 255;
					
					for (int k = 0; k < 3; k++)
						mean[k] += rgb[k]; 
				}
			}
		for (int i = 0; i < 3; i++)
			mean[i] /= (windowSize - t);

		return mean;
	}

	private double[] caculateBK(double[] akTemp, double[] meanOfITemp) {
		double[] bk = new double[3];
		for (int i = 0; i < 3; i++)
			bk[i] = meanOfITemp[i] - akTemp[i] * meanOfITemp[i];
		
		return bk;
	}

	private double[] caculateAK(int w, int h, int radius, double episilon, double[] meanOfITemp, double[] varianceOfITemp) {
		double windowSize = Math.pow((double)(radius * 2 + 1), 2);
		double[] ak = new double[3];
		for (int i = 0; i < 3; i++)
			ak[i] = 0;
		int t = 0;
		
		for (int i = h - radius; i <= h + radius; i++)
			for (int j = w - radius; j <= w + radius; j++) {
				double[] rgb = new double[3];
				//点不足时要补零
				if ((i < 0 || i >= height) || (j < 0 || j >= width)) {
					t++;
				}
				else {
					rgb[0] = (double)((sourcePixels[i * width + j] & 0x00FF0000) >> 16) / 255;
					rgb[1] = (double)((sourcePixels[i * width + j] & 0x0000FF00) >> 8) / 255;
					rgb[2] = (double)(sourcePixels[i * width + j] & 0x000000FF) / 255;
					
					for (int k = 0; k < 3; k++)
						ak[k] += rgb[k] * rgb[k]; 
				}
			}
		
		for (int i = 0; i < 3; i++)
			ak[i] = (ak[i] / (windowSize - t) - meanOfITemp[i] * meanOfITemp[i]) / (varianceOfITemp[i] + episilon);
		
		return ak;
	}

	private void save(int[] resultPixels, int dimension, String mode, float episilon) {
		BufferedImage saveImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		saveImage.setRGB(0, 0, width, height, resultPixels, 0, width);
		
		//均衡化的图像保存在project项目的根目录下
		File outFile = new File(path + "\\" + dimension + "_" + episilon +  mode);
		try {
			ImageIO.write(saveImage, "png", outFile);
		} catch (IOException erro) {
			erro.printStackTrace();
		}
		
		System.out.println(mode + " finish!");
	}

}
