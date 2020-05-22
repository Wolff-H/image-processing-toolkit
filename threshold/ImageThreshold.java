
// Skeletal program for the "Image Threshold" assignment
// Written by:  Minglun Gong
/*
	student: Tingrui Hu - th8361 - 201513025
*/

package threshold;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.imageio.*;


// Main class **********************************************************************************************************
public class ImageThreshold extends Frame implements ActionListener {
	BufferedImage input;
	BufferedImage output;
	int width, height;
	TextField texThres, texOffset;
	ImageCanvas source, target;
	PlotCanvas2 plot;
	// Constructor
	public ImageThreshold(String name) {
		super("Image Histogram");
		// load image
		try {
			input = ImageIO.read(new File(name));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		target = new ImageCanvas(output);
		plot = new PlotCanvas2(256, 200);
		target = new ImageCanvas(width, height);
        target.resetImage(input);            /**********/
		main.setLayout(new GridLayout(1, 3, 10, 10));
		main.add(source);
		main.add(plot);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		controls.add(new Label("Threshold:"));
		texThres = new TextField("128", 2);
		controls.add(texThres);
		Button button = new Button("Manual Selection");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Automatic Selection");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Otsu's Method");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Offset:"));
		texOffset = new TextField("10", 2);
		controls.add(texOffset);
		button = new Button("Adaptive Mean-C");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(width*2+400, height+100);
        setVisible(false);
        setLocation(397, 200);
	}
    class ExitListener extends WindowAdapter 
    {
        public void windowClosing(WindowEvent e) 
        {
            // System.exit(0);
            setVisible(false);
		}
	}

    // thresholding mothods ////////////////////////////////////////////////////////////////////////////////////////////
	// Mean-C adaptive -------------------------------------------------------------------------------------------------
    public int[][] mean_c(int[][] ImageMatrix, int[][] rmatrix, int[][] gmatrix, int[][] bmatrix) 
    {
		int blue, red, green, rmean, gmean, bmean, count;
		int maskSize = 7; 
		int C = Integer.parseInt(texOffset.getText());
        for(int q = 0; q < height; q++)
        {
            for(int p = 0; p < width; p++)
            {
				red = green = blue = 0;
				count = 0;
                for(int v = q - (maskSize / 2); v <= q + (maskSize / 2); v++)
                {
                    for(int u = p - (maskSize / 2); u <= p + (maskSize / 2); u++)
                    {
                        if(v < 0 || v >= height || u < 0 || u >= width)
                        {
							// some portion of the mask is outside the image
							continue;
						}
                        else
                        {
                            try
                            {
								// sum value for each color //
								red += (new Color(source.image.getRGB(u, v))).getRed();
								green += (new Color(source.image.getRGB(u, v))).getGreen();
								blue += (new Color(source.image.getRGB(u, v))).getBlue();
								count++;
                            }
                            catch(ArrayIndexOutOfBoundsException e)
                            {
                                // nothing yet
							}
						}
					}
				}
				// calculate the mean value for each color //
				rmean = red/count - C;
				gmean = green/count - C;
				bmean = blue/count - C;
				// Use mean value as the threshold value //
                if((new Color(source.image.getRGB(p, q))).getBlue() >= rmean)
                {
					rmatrix[q][p] = 0xffffffff;     // White 
                }
                else{
					rmatrix[q][p] = 0xff000000;     // Black
				}
                if((new Color(source.image.getRGB(p, q))).getBlue() >= gmean)
                {
					gmatrix[q][p] = 0xffffffff;     // White
                }
                else{
					gmatrix[q][p] = 0xff000000;     // Black
				}
                if((new Color(source.image.getRGB(p, q))).getBlue() >= bmean)
                {
					bmatrix[q][p] = 0xffffffff;     // White
                }
                else
                {
					bmatrix[q][p] = 0xff000000;     // Black
                }
                
				ImageMatrix[q][p] = rmatrix[q][p]<<16 | gmatrix[q][p]<<8 | bmatrix[q][p];
			}
		}
		return ImageMatrix;
    }
    
	// Ostu's threshold approach ---------------------------------------------------------------------------------------
    public int OstuThreshold (float[] PDF)
    {
		double max_var = 0; 
		int threshold = 0;
		// Calculate w0, w1, u0, u1 //
        for (int i = 0; i < 256; i++)
        {
			double w0 = 0; 
			double w1 = 0; 
			double u0 = 0; 
			double u1 = 0; 
			double var = 0; 
			double u = 0; 
			double u0temp = 0; 
            double u1temp = 0;
            
            for (int j = 0; j < 256; j++)
            {
                if (j < i)
                {
					w0 += PDF[j];
					u0temp += j*PDF[j];
				}
                if (j >= i)
                {
					w1 += PDF[j];
					u1temp += j*PDF[j];
				}
            }
            
			u0 = u0temp/w0;
			u1 = u1temp/w1;
			u = u0temp + u1temp;
            var = (w0*Math.pow(u0-u, 2))+(w1*Math.pow(u1-u, 2));
            
			// get the minimum //
            if (var > max_var)
            {
				max_var = var;
				threshold = i;
			}
        }
        
		return threshold;
    }
    
    // auto selection --------------------------------------------------------------------------------------------------
    // red - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public int auto_red(int rthreshold)
    {
		int temp = 0;
		int sum0 = 0;
		int sum1 = 0;
		double u0 = 0;
		double u1 = 0;
		int count0 = 0;
		int count1 = 0;
        double num = 0.1;
        
		// two different value for red //
        while (Math.abs(rthreshold - temp) > num) 
        {
			temp = rthreshold;
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    if ((new Color(source.image.getRGB(x, y))).getRed() < rthreshold)
                    {
						sum0 += (new Color(source.image.getRGB(x, y))).getRed();
						count0++;
					}
                    if ((new Color(source.image.getRGB(x, y))).getRed() >= rthreshold)
                    {
						sum1 += (new Color(source.image.getRGB(x,y))).getRed();
						count1++;
					}
				}
			}
			// Red threshold Auto-Selection function //
			u0 = sum0 / count0;
			u1 = sum1 / count1;
			rthreshold = (int)(u0+u1)/2;
        }
        
		return rthreshold;
	}
	
    // green - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public int auto_green(int gthreshold)
    {
		int temp = 0;
		int sum0 = 0;
		int sum1 = 0;
		double u0 = 0;
		double u1 = 0;
		int count0 = 0;
		int count1 = 0;
        double num = 0.1;
        
		// Calculate two different green value //
        while (Math.abs(gthreshold - temp) > num)
        {
			temp = gthreshold;
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    if ((new Color(source.image.getRGB(x, y))).getGreen() < gthreshold)
                    {
						sum0 += (new Color(source.image.getRGB(x, y))).getGreen();
						count0++;
					}
                    if ((new Color(source.image.getRGB(x, y))).getGreen() >= gthreshold)
                    {
						sum1 += (new Color(source.image.getRGB(x, y))).getGreen();
						count1++;
					}
				}
			}
			// Green threshold Auto-Selection function //
			u0 = sum0 / count0;
			u1 = sum1 / count1;
			gthreshold = (int)(u0+u1)/2;
		}
		return gthreshold;
	}

	// blue - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public int auto_blue(int bthreshold)
    {
		int temp = 0;
		int sum0 = 0;
		int sum1 = 0;
		double u0 = 0;
		double u1 = 0;
		int count0 = 0;
		int count1 = 0;
		double num = 0.1;
		// Calculate two different  blue value //
        while (Math.abs(bthreshold) - temp > num)
        {
			temp = bthreshold;
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    if ((new Color(source.image.getRGB(x, y))).getBlue() < bthreshold)
                    {
						sum0 += (new Color(source.image.getRGB(x, y))).getBlue();
						count0++;
					}
                    if ((new Color(source.image.getRGB(x, y))).getBlue() >= bthreshold)
                    {
						sum1 += (new Color(source.image.getRGB(x, y))).getBlue();
						count1++;
					}
				}
			}
			// Blue threshold Auto-Selection function //
			u0 = sum0 / count0;
			u1 = sum1 / count1;
			bthreshold = (int)(u0+u1)/2;
        }
        
		return bthreshold;
	}

	

		
	
	// Action listener for button click events /////////////////////////////////////////////////////////////////////////
    public void actionPerformed(ActionEvent e) 
    {
		// Manual Selection //
        if ( ((Button)e.getSource()).getLabel().equals("Manual Selection"))
        {
			int threshold = Integer.parseInt(texThres.getText());
			int[][] ImageMatrix = new int[height][width];
			int[][] red = new int[height][width];
			int[][] green = new int[height][width];
            int[][] blue = new int[height][width];
            
            for (int y = 0; y < height; y++ )
            {
                for (int x = 0; x < width; x++)
                {
					if ((new Color(source.image.getRGB(x, y))).getRed() < threshold)    {   red[y][x]    = 0;     }
					if ((new Color(source.image.getRGB(x, y))).getRed() >= threshold)   {   red[y][x]    = 255;   }
					if ((new Color(source.image.getRGB(x, y))).getGreen() < threshold)  {   green[y][x]  = 0;     }
					if ((new Color(source.image.getRGB(x, y))).getGreen() >= threshold) {   green[y][x]  = 255;   }
					if ((new Color(source.image.getRGB(x , y))).getBlue() < threshold)  {   blue[y][x]   = 0;     }
                    if ((new Color(source.image.getRGB(x ,y))).getBlue() >= threshold)  {   blue[y][x]   = 255;   }
                    
					ImageMatrix[y][x] = red[y][x]<<16 | green[y][x]<<8 | blue[y][x];
				}
			}
			plot.clearObjects();
			plot.addObject(new VerticalBar(Color.BLACK, threshold, 100));
			/**Get the image of output**/
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
					output.setRGB(x, y, ImageMatrix[y][x]);
				}
			}
			target.resetImage(output);
		}
		// Automatic Selection //
        if (((Button)e.getSource()).getLabel().equals("Automatic Selection"))
        {
			int[][] red = new int[height][width];
			int[][] green = new int[height][width];
			int[][] blue = new int[height][width];
			int[][] ImageMatrix = new int[height][width];
			// defaults //
			int rthreshold = 128;
			int gthreshold = 128; 
			int bthreshold = 128;

			rthreshold = auto_red(rthreshold);
			gthreshold = auto_green(gthreshold);
			bthreshold = auto_blue(bthreshold);

            for ( int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
					if ((new Color(source.image.getRGB(x, y))).getRed() < rthreshold)       {   red[y][x] = 0;      }
					if ((new Color(source.image.getRGB(x, y))).getRed() >= rthreshold)      {   red[y][x] = 255;    }
					if ((new Color(source.image.getRGB(x, y))).getGreen() < gthreshold)     {   green[y][x] = 0;    }
					if ((new Color(source.image.getRGB(x, y))).getGreen() >= gthreshold)    {   green[y][x] = 255;  }
					if ((new Color(source.image.getRGB(x, y))).getBlue() < bthreshold)      {   blue[y][x] = 0;     }
					if ((new Color(source.image.getRGB(x, y))).getBlue() >= bthreshold)     {   blue[y][x] = 255;   }
                    
                    ImageMatrix[y][x] = red[y][x]<<16 | green[y][x]<<8 | blue[y][x];
				}
			}
			// Create the threshold line //
			plot.clearObjects();
			plot.addObject(new VerticalBar(Color.RED, rthreshold, 100));
			plot.addObject(new VerticalBar(Color.GREEN, gthreshold, 100));
			plot.addObject(new VerticalBar(Color.BLUE, bthreshold, 100));
			// output //
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
					output.setRGB(x, y, ImageMatrix[y][x]);
				}
			}
			target.resetImage(output);
		}

		// The selection of Otsu's Method //
        if (((Button)e.getSource()).getLabel().equals("Otsu's Method")) 
        {
			int[][] ImageMatrix = new int[height][width];
			int[] rhist = new int[256];
			int[] ghist = new int[256];
			int[] bhist = new int[256];
			int[][] red = new int[height][width];
			int[][] green = new int[height][width];
			int[][] blue = new int[height][width];
			float[] rPDF = new float[256];
			float[] gPDF = new float[256];
			float[] bPDF = new float[256];
			// values on every pixel from the image //
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
					rhist[(new Color(source.image.getRGB(x, y))).getRed()]++;
					ghist[(new Color(source.image.getRGB(x, y))).getGreen()]++;
					bhist[(new Color(source.image.getRGB(x, y))).getBlue()]++;
				}
			}
			// pdfs //
			for (int i = 0; i < 256; i++){
				rPDF[i] = (float)rhist[i]/(height/width);
				gPDF[i] = (float)ghist[i]/(height/width);
				bPDF[i] = (float)bPDF[i]/(height/width);
			}
			
			int bthreshold = OstuThreshold(bPDF);
			int rthreshold = OstuThreshold(rPDF);
            int gthreshold = OstuThreshold(gPDF);
            
            for (int y = 0; y < height; y++) 
            {
                for (int x = 0; x < width; x++)
                {
					if ((new Color(source.image.getRGB(x, y))).getRed() < rthreshold)       {   red[y][x] = 0;      }
					if ((new Color(source.image.getRGB(x, y))).getRed() >= rthreshold)      {   red[y][x] = 255;    }
					if ((new Color(source.image.getRGB(x, y))).getGreen() < gthreshold)     {   green[y][x] = 0;    }
					if ((new Color(source.image.getRGB(x, y))).getGreen() >= gthreshold)    {   green[y][x] = 255;  }
					if ((new Color(source.image.getRGB(x, y))).getBlue() < bthreshold)      {   blue[y][x] = 0;     }
					if ((new Color(source.image.getRGB(x, y))).getBlue() >= bthreshold)     {   blue[y][x] = 255;   }
					ImageMatrix[y][x] = red[y][x]<<16 | green[y][x]<<8 | blue[y][x];
				}
            }
            
			plot.clearObjects();
			plot.addObject(new VerticalBar(Color.RED, rthreshold, 100));
			plot.addObject(new VerticalBar(Color.GREEN, gthreshold, 100));
            plot.addObject(new VerticalBar(Color.BLUE, bthreshold, 100));
            
			// output //
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
					output.setRGB(x, y, ImageMatrix[y][x]);
				}
            }
            
			target.resetImage(output);
		}
		
		// Selection of Adaptive Mean-C //
        if (((Button)e.getSource()).getLabel().equals("Adaptive Mean-C"))
        {
			int[][] ImageMatrix = new int[height][width];
			int[][] red = new int[height][width];
			int[][] green = new int[height][width];
			int[][] blue = new int[height][width];
			ImageMatrix = mean_c(ImageMatrix, red, green, blue);
            
            // catch the output of image //
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
					output.setRGB(x, y, ImageMatrix[y][x]);
				}
            }
            
			target.resetImage(output);
			plot.clearObjects();
		}

	}
	
	
	public static void main(String[] args) {
		new ImageThreshold(args.length==1 ? args[0] : "fingerprint.png");
	}
}
