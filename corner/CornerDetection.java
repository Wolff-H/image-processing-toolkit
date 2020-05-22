package corner;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.*;

// Main class
public class CornerDetection extends Frame implements ActionListener 
{
	/**
     *
     */
    private static final long serialVersionUID = 1L;

    BufferedImage input;
	BufferedImage output;
	int width, height;
	double sensitivity=.1;
	int threshold=20;
	
	// 2D array to hold the pixel value of an image
	double[][] pixel_matrix;
	
	// ArrayList to hold the x coordinate of a pixel value
	ArrayList<Integer> x_points;
	
	// ArrayList to hold the y coordinate of a pixel value
	ArrayList<Integer> y_points;
	
	ImageCanvas source, target;
	CheckboxGroup metrics = new CheckboxGroup();

	// Constructor //
    public CornerDetection(String name) 
    {
		super("Corner Detection");
		// load image //
        try 
        {
			input = ImageIO.read(new File("signal_hill.png"));
		}
        catch ( Exception ex ) 
        {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		pixel_matrix = new double[height][width];
		x_points = new ArrayList<Integer>();
		y_points = new ArrayList<Integer>();
		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		target = new ImageCanvas(output);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Derivatives");
		button.addActionListener(this);
		controls.add(button);
		// Use a slider to change sensitivity
		JLabel label1 = new JLabel("sensitivity=" + sensitivity);
		controls.add(label1);
		JSlider slider1 = new JSlider(1, 25, (int)(sensitivity*100));
		slider1.setPreferredSize(new Dimension(50, 20));
		controls.add(slider1);
		slider1.addChangeListener(changeEvent -> {
			sensitivity = slider1.getValue() / 100.0;
			label1.setText("sensitivity=" + (int)(sensitivity*100)/100.0);
		});
		button = new Button("Corner Response");
		button.addActionListener(this);
		controls.add(button);
		JLabel label2 = new JLabel("threshold=" + threshold);
		controls.add(label2);
		JSlider slider2 = new JSlider(0, 100, threshold);
		slider2.setPreferredSize(new Dimension(50, 20));
		controls.add(slider2);
		slider2.addChangeListener(changeEvent -> {
			threshold = slider2.getValue();
			label2.setText("threshold=" + threshold);
		});
		button = new Button("Thresholding");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Non-max Suppression");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Display Corners");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(Math.max(width*2+100,850), height+110);
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
	
	// Action listener for button click events
    public void actionPerformed(ActionEvent e) 
    {
        String action_name = ((Button)e.getSource()).getLabel();

        switch(action_name)
		{
            case "Derivatives":
                derivativesProducts();
                break;
            case "Corner Response":
                cornerResponse();
                resetImage();
                break;
            case "Thresholding":
                thresholding();
                resetImage();
                break;
            case "Non-max Suppression":
                nonMaxSuppression();
                resetImage();
                break;
            case "Display Corners":
                addCornerPoints();
                target.copyImage(source.image);
                target.repaint();
                break;
        }
    }

    // derivatives products --------------------------------------------------------------------------------------------
    void derivativesProducts()
    {	 
        int[] gaussianX =
            { 
                -1, - 2,   0,   2,   1,
                -4, -10,   0,  10,   4,
                -7, -17,   0,  17,   7,
                -4, -10,   0,  10,   4,
                -1, - 2,   0,   2,   1,
            };
        int[] gaussianY = 
            { 
                -1, - 4, - 7, - 4,  -1,
                -2, -10, -17, -10,  -2, 
                 0,   0,   0,   0,   0, 
                 2,  10,  17,  10,   2,
                 1,   4,   7,   4,   1,
            };
        int[] gaussianXY = 
            {
                 1,   4,   6,   4,   1,
                 4,  16,  24,  16,   1,
                 6,  24,  36,  24,   6, 
                 4,  16,  24,  16,   4,
                 1,   4,   6,   4,   1,
            };
		
		int span = 2;
		Color clr;
		int gray;
		int[] tempx = new int[25];
		int[] tempy = new int[25];
		int[] tempxy = new int[25];
		int temp_dx, temp_dy;
		double dx, dy, dxy, dx2, dy2;
		
        for ( int q = span; q < height - span; q++ )
        {
            for ( int p = span; p < width - span; p++ )
            {
				int i = 0;
				temp_dx = 0;
				temp_dy = 0;
                for ( int v = -span; v <= span; v++ )
                {
                    for ( int u = -span; u <= span; u++ )
                    {
						clr = new Color(source.image.getRGB(q+v,p+u));
						gray = (clr.getRed() + clr.getGreen() + clr.getBlue())/3;
						tempx[i] = gray*gaussianX[i];
						tempy[i] = gray*gaussianY[i];
						tempxy[i] = gray*gaussianXY[i];
						i++;
					}
				}
				for ( int t = 0; t < gaussianX.length; t++ )    {   temp_dx += tempx[t];   }
				for ( int t = 0; t < gaussianY.length; t++ )    {   temp_dy += tempy[t];   }

				dx = temp_dx/58;
                dx2 = nomalize(dx*dx*0.05);
				dy = temp_dy/58;
                dy2 = nomalize(dy*dy*0.05);
				dxy = nomalize(dx*dy*0.09);
                
                int rgb_val = (int)dx2<<16 | (int)dy2<<8 | (int)dxy;
				target.image.setRGB(q, p, rgb_val);
			}
		}
		target.repaint();
	}

    // corner response -------------------------------------------------------------------------------------------------
    void cornerResponse()
    {
		//target.copyImage(source.image);
        clearImage();
        
        int[] gaussianX =
            { 
                -1, - 2,   0,   2,   1,
                -4, -10,   0,  10,   4,
                -7, -17,   0,  17,   7,
                -4, -10,   0,  10,   4,
                -1, - 2,   0,   2,   1,
            };
        int[] gaussianY = 
            { 
                -1, - 4, - 7, - 4,  -1,
                -2, -10, -17, -10,  -2, 
                 0,   0,   0,   0,   0, 
                 2,  10,  17,  10,   2,
                 1,   4,   7,   4,   1,
            };
        int[] gaussianXY = 
            {
                 1,   4,   6,   4,   1,
                 4,  16,  24,  16,   1,
                 6,  24,  36,  24,   6, 
                 4,  16,  24,  16,   4,
                 1,   4,   6,   4,   1,
            };

		double[] A = new double[4];
		
		int span = 2;
		
		double[] temp_x = new double[25];
		double[] temp_y = new double[25];
        double[] temp_xy = new double[25];
        
        double temp_dx, temp_dy, temp_dxy;
		double dx, dy, dxy, dx2, dy2;;
        double R;
		double gray;
		Color clr;
		
        for ( int q = span; q < height - span; q++ )
        {
            for ( int p = span; p < width - span; p++ )
            {
				int i = 0;
				temp_dx = 0;
				temp_dy = 0;
                temp_dxy = 0;
                
                for ( int v = -span; v <= span; v++ )
                {
                    for ( int u = -span; u <= span; u++ )
                    {
						clr = new Color(source.image.getRGB(q+v,p+u));
						gray = (clr.getRed() + clr.getGreen() + clr.getBlue())/3;
						temp_x[i] = gray*gaussianX[i];
						temp_y[i] = gray*gaussianY[i];
						temp_xy[i] = gray*gaussianXY[i];
						i++;
					}
				}
				for ( int t = 0; t < gaussianX.length; t++ )     {   temp_dx += temp_x[t];     }
				for ( int t = 0; t < gaussianY.length; t++ )     {   temp_dy += temp_y[t];     }
                for ( int t = 0; t < gaussianXY.length; t++ )    {   temp_dxy += temp_xy[t];   }
                
				dx = temp_dx/58;
				dy = temp_dy/58;
                dx2 = nomalize(dx*dx);
                dy2 = nomalize(dy*dy);
                dxy = nomalize(temp_dxy/256);
                
                A[0] = dx2;
				A[1] = dxy;
				A[2] = dxy;
				A[3] = dy2;
                
                R = ((A[0]*A[3]-A[1]*A[2]) - sensitivity*Math.pow(A[0]+A[3], 2));
				if ( R < 0 ){    R = 0;    }
                
                pixel_matrix[q][p] = R;
			}
		}
		scaleResponseValue();
	}
	
    // non-max suppression to corner response --------------------------------------------------------------------------
    void nonMaxSuppression()
    {
		int suppression = 3;
		int xIndex = 0;
		int yIndex = 0;
		
        for ( int y = suppression, maxY = height - y; y < maxY; y++ )
        {
            for ( int x = suppression, maxX = width - x; x < maxX; x++ )
            {
                double currentValue = pixel_matrix[y][x];
                
                for ( int i = -suppression; (currentValue != 0) && (i <= suppression); i++ )
                {
                    for ( int j = -suppression; j <= suppression; j++ )
                    {
						
                        if ( pixel_matrix[y+i][x+j] < currentValue )
                        {
							pixel_matrix[y+i][x+j] = 0;
						}
                        else if ( pixel_matrix[y+i][x+j] > currentValue)
                        {
							xIndex = x+j;
							yIndex = y+i;
						}
					}
				}
				x_points.add(yIndex);
				y_points.add(xIndex);
			}
		}
	}
	
    // helpers /////////////////////////////////////////////////////////////////////////////////////////////////////////
    void resetImage()
    {
        double r, g, b;
            
        for ( int y = 0; y < height; y++)
        {
            for ( int x = 0; x < width; x++ )
            {
                r = nomalize(pixel_matrix[y][x]);
                g = nomalize(pixel_matrix[y][x]);
                b = nomalize(pixel_matrix[y][x]);
                
                int rgb_val = (int)r<<16 | (int)g<<8 | (int)b;
                output.setRGB(y, x, rgb_val);
            }
        }
            
		target.copyImage(output);
    }

    void addCornerPoints()
    {
        int size = x_points.size();
        
        for ( int i = 0; i < size; i++ )
        {
			target.addXpoints(x_points.get(i));
			target.addYpoints(y_points.get(i));
        }
        
		x_points.clear();
		y_points.clear();
	}
	
    void clearImage()
    {
        for ( int y = 0; y < height; y++ )
        {
            for ( int x = 0; x < width; x++ )
            {
				pixel_matrix[y][x] = 0;
			}
		}
	}
	
    void scaleResponseValue()
    {
        for ( int y = 0; y < height; y++ )
        {
            for ( int x = 0; x < width; x++ )
            {
				pixel_matrix[y][x] = (int)(pixel_matrix[y][x]*0.01);
			}
		}
	}
	
    void thresholding()
    {
        for ( int y = 0; y < height; y++ )
        {
            for ( int x = 0; x < width; x++ )
            {
                if ( pixel_matrix[y][x] > threshold*5 ) 
                {
					continue;
				}
                else 
                {
					pixel_matrix[y][x] = 0;
				}
			}
		}	
	}
	
    double findMean()
    {
		double sum = 0;
		double mean = 0;
		double count = 0;
        for ( int y = 0; y < height; y++ )
        {
            for ( int x = 0; x < width; x++ )
            {
                if ( pixel_matrix[y][x] > 0)
                {
					sum += pixel_matrix[y][x];
					count++;
				}
			}
		}
		mean = sum/count;
		return mean;
	}
	
    double findMax()
    {
		double max = 0;
        for ( int y = 0; y < height; y++ )
        {
            for ( int x = 0; x < width; x++ )
            {
                if ( pixel_matrix[y][x] > max )
                {
					max  = pixel_matrix[y][x];
				}
			}
		}
		return max;
	}
	
    double nomalize(double num)
    {
		if ( num < 0 ){ num = 0; }
		if ( num > 255 ){ num = 255; }
		return num;
	}
	
	
    public static void main(String[] args) 
    {
		new CornerDetection(args.length==1 ? args[0] : "signal_hill.png");
	}
	
	// moravec implementation
    void derivatives() 
    {
		int l, t, r, b, dx, dy;
		Color clr1, clr2;
		int gray1, gray2;
		int valx, valy, valxy;

        for ( int q=0 ; q<height ; q++ ) 
        {
			t = q==0 ? q : q-1;
			b = q==height-1 ? q : q+1;
            for ( int p=0 ; p<width ; p++ ) 
            {
				l = p==0 ? p : p-1;
				r = p==width-1 ? p : p+1;
				clr1 = new Color(source.image.getRGB(l,q));
				clr2 = new Color(source.image.getRGB(r,q));
				gray1 = clr1.getRed() + clr1.getGreen() + clr1.getBlue();
				gray2 = clr2.getRed() + clr2.getGreen() + clr2.getBlue();
				dx = (gray2 - gray1) / 3;
				clr1 = new Color(source.image.getRGB(p,t));
				clr2 = new Color(source.image.getRGB(p,b));
				gray1 = clr1.getRed() + clr1.getGreen() + clr1.getBlue();
				gray2 = clr2.getRed() + clr2.getGreen() + clr2.getBlue();
				dy = (gray2 - gray1) / 3;
				dx = Math.max(-128, Math.min(dx, 127));
				dy = Math.max(-128, Math.min(dy, 127));
				target.image.setRGB(p, q, new Color(dx+128, dy+128, 128).getRGB());
			}
		}
		target.repaint();
	}
}
