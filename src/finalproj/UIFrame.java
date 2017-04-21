package finalproj;

import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

@SuppressWarnings("serial")
public class UIFrame extends Frame {
	ImageFilter_smooth filter;
	ImageFilter_enhancement filter2;
	
	public UIFrame() {
		loadImage(1, "beauty_with_freckle.bmp");
		loadImage(2, "1.jpg");
		
		this.setTitle("空间滤波器");  
        Panel pdown;  
        Button guidedFilter_smooth, guidedFilter_enhancement;  
        //添加窗口监听事件  
        addWindowListener(new WindowAdapter(){  
            public void windowClosing(WindowEvent e){  
                System.exit(0);  
            }  
        });  
        pdown = new Panel();  
        pdown.setBackground(Color.LIGHT_GRAY);  
        //按钮名称  
        guidedFilter_smooth = new Button("GuidedFilter_smooth"); 
        guidedFilter_enhancement = new Button("enhancement");
        
        this.add(pdown);  
        //增加按钮  
        pdown.add(guidedFilter_smooth);  
        pdown.add(guidedFilter_enhancement);
        
        //执行计算直方图函数并绘图
        guidedFilter_smooth.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				//filter.GuidedFilter_smooth(8, 0.16);
				//filter.GuidedFilter_smooth(8, 0.04);
				filter.GuidedFilter_smooth(2, 0.04);
//				filter.GuidedFilter_smooth(2, 0.04);
				filter.GuidedFilter_smooth(4, 0.16);
				//filter.GuidedFilter_smooth(4, 0.04);
				System.out.println("finish!!!!");
			}
		});
        
        guidedFilter_enhancement.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				filter2.GuidedFilter_enhancement(8, 0.16);
				System.out.println("finish!!");
			}
		});

	}
	
	private void loadImage(int who, String name) {
		int[] sourcePixels;
		BufferedImage sourceBufferedImage;
		Image sourceImage;
		
		File file = new File("");
		String path = file.getAbsolutePath();
		try {
			sourceBufferedImage = ImageIO.read(new File(path + "\\" + name));
			sourceImage = (Image)sourceBufferedImage;
			int width = sourceImage.getWidth(null);
			int height = sourceImage.getHeight(null);
			
			//构造图像的一维数组
			sourcePixels = new int[width * height];
			PixelGrabber pg = new PixelGrabber(sourceImage, 0, 0, width, height, sourcePixels, 0,
					width);
			pg.grabPixels();
			
			//初始化滤波器设置
			if (who == 1)
				filter = new ImageFilter_smooth(width, height, sourcePixels, path, name);
			else if (who == 2)
				filter2 = new ImageFilter_enhancement(width, height, sourcePixels, path, name);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
		UIFrame frame = new UIFrame();
		frame.setSize(300, 200);
		frame.setVisible(true);
	}

}
