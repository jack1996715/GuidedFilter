package finalproj;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.transform.Templates;

public class ImageFilter_smooth {
	private int width, height;
	private int[] sourcePixels;
	private String path;
	private String imageName ;
	
	public ImageFilter_smooth(int width, int height, int[] source, String path, String imageName) {
		this.height = height;
		this.width = width;
		this.sourcePixels = source;
		this.path = path;
		this.imageName = imageName;
	}
	
	public void GuidedFilter_smooth(int radius, double episilon) {
		int[] result = new int[height * width];
		double[] meanOfI = new double[height * width];
		double[] varianceOfI = new double[height * width];
		double[] ak = new double[height * width];
		double[] bk = new double[height * width];
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				meanOfI[h * width + w] = caulateMeanOfI(w, h, radius);
				varianceOfI[h * width + w] = caculateVariance(w, h, radius, meanOfI[h * width + w]);
				ak[h * width + w] = caculateAK(w, h, radius, episilon, meanOfI[h * width + w], varianceOfI[h * width + w]);
				bk[h * width + w] = caculateBK(ak[h * width + w], meanOfI[h * width + w]);
			}
		}
		
		
		//开始滤波
		for (int h = 0; h < height; h++)
			for (int w = 0; w < width; w++) {
				double gray = (double)(sourcePixels[h * width + w] & 0x000000FF) / 255;
				double newGray = 0;
				double temp = 0;
				int t = 0;
				for (int i = h - radius; i <= h + radius; i++)
					for (int j = w - radius; j <= w + radius; j++) {
						if ((i < 0 || i >= height) || (j < 0 || j >= width))
							t++;
						else 
							temp += ak[i * width + j] * gray + bk[i * width + j];
					}
				
				newGray = temp / (Math.pow(radius * 2 + 1, 2) - t); 
				//newGray = meanOfI;
				newGray *= 255;
			result[h * width + w] = 0xFF000000 | (int)newGray << 16 | (int)newGray << 8 | (int)newGray;
			}
		
		save(result, radius, imageName, (float) episilon);
		
	}
	
	private double caculateVariance(int w, int h, int radius, double meanOfI) {
		double variance = 0;
		int t = 0;
		double windowSize = Math.pow((double)(radius * 2 + 1), 2);
		for (int i = h - radius; i <= h + radius; i++)
			for (int j = w - radius; j <= w + radius; j++) {
				double temp;
				//点不足时要补零
				if ((i < 0 || i >= height) || (j < 0 || j >= width)) {
					temp = 0;
					t++;
				}
				else {
					temp = (double)(sourcePixels[i * width + j] & 0x000000FF) / 255;
					variance = variance + ((double)temp - meanOfI) * ((double)temp - meanOfI); 
				}
			}
		variance = variance / (windowSize - t);
		return variance;
	}

	private double caulateMeanOfI(int w, int h, int radius) {
		double mean = 0;
		int t = 0;
		double windowSize =  Math.pow((double)(radius * 2 + 1), 2);
		for (int i = h - radius; i <= h + radius; i++)
			for (int j = w - radius; j <= w + radius; j++) {
				double temp;
				//点不足时要补零
				if ((i < 0 || i >= height) || (j < 0 || j >= width)) {
					temp = 0;
					t++;
				}
				else
					temp = (double)(sourcePixels[i * width + j] & 0x000000FF) / 255;
				mean += temp;
			}
		mean /= (windowSize - t);
		return mean;
	}

	private double caculateBK(double ak, double meanOfI) {
		double bk = meanOfI - meanOfI * ak;
		
		return bk;
	}

	private double caculateAK(int w, int h, int radius, double episilon, double uk, double varianceOfI) {
		double windowSize = Math.pow((double)(radius * 2 + 1), 2);
		double ak = 0;
		for (int i = h - radius; i <= h + radius; i++)
			for (int j = w - radius; j <= w + radius; j++) {
				double temp;
				//点不足时要补零
				if ((i < 0 || i >= height) || (j < 0 || j >= width))
					temp = 0;
				else
					temp = (double)(sourcePixels[i * width + j] & 0x000000FF) / 255;
				ak += (temp * temp);
			}
		ak = ((ak / windowSize) - uk * uk) / (varianceOfI + episilon);
		return ak;
	}

	private void save(int[] resultPixels, int dimension, String mode, float episilon) {
		BufferedImage saveImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
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