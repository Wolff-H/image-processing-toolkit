// Skeletal program for the "Image Histogram" processing
// Written by:  Minglun Gong
/*
    Tingrui Hu - th8361 - 201513025

    NOTE:
        I'm assumimg the number "cutoff" is a magnitude (rather than a ratio) that cuts down both left and right side on the x-axis.
*/
package histogram;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.JPanel;

// Main class //
public class ImageHistogram extends Frame implements ActionListener
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    BufferedImage input;
    BufferedImage output = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
    int width, height;
    TextField texRad, texThres;
    ImageCanvas source, target;
    PlotCanvas plot;

    // Constructor //
    public ImageHistogram(String name)
    {
        super("Image Histogram");
        // load image //
        try
        {
            input = ImageIO.read(new File(name));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
		}
		
        width = input.getWidth();
		height = input.getHeight();
		
        // prepare the panel for image canvas. //
        Panel main = new Panel();
        source = new ImageCanvas(input);
        plot = new PlotCanvas();
        target = new ImageCanvas(input);
        main.setLayout(new GridLayout(1, 3, 10, 10));
        main.add(source);
        main.add(plot);
		main.add(target);
		
        // prepare the panel for buttons. //
        Panel controls = new Panel();
        Button button = new Button("Display Histogram");
        button.addActionListener(this);
        controls.add(button);
        button = new Button("Histogram Stretch");
        button.addActionListener(this);
        controls.add(button);
        controls.add(new Label("Cutoff fraction:"));
        texThres = new TextField("10", 2);
        controls.add(texThres);
        button = new Button("Aggressive Stretch");
        button.addActionListener(this);
        controls.add(button);
        button = new Button("Histogram Equalization");
        button.addActionListener(this);
		controls.add(button);
		
        // add two panels //
        add("Center", main);
        add("South", controls);
        // addWindowListener(new ExitListener());
        setSize(width * 2 + 400, height + 100);
        setVisible(true);
        setLocation(397, 200);

        addWindowListener(new ExitListener());
	}
	
    class ExitListener extends WindowAdapter
    {
        public void windowClosing(WindowEvent e)
        {
            // System.exit(0);
            setVisible(false);
        }
	}
	
    // @click=... //////////////////////////////////////////////////////////////////////////////////////////////////////
    public void actionPerformed(ActionEvent e)
    {
		String button_label_name = ( (Button)e.getSource() ).getLabel();
		
        if      ( button_label_name.equals("Display Histogram")      )    {   displayHistogram();            }
		else if ( button_label_name.equals("Histogram Stretch")      )    {   histogramStretch(false);       }
		else if ( button_label_name.equals("Aggressive Stretch")     )    {   histogramStretch(true);        }
		else if ( button_label_name.equals("Histogram Equalization") )    {   histogramEqualization();       }
    }
    
    // helpers /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // getPixelMatrix --------------------------------------------------------------------------------------------------
	/*
        pixel_matrix = 
            x >
          y |-------------------
          v |
            |   [r,g,b]
            |
            |
    */
    public int[][][] getPixelMatrix() 
	{
		int[][][] pixel_matrix = new int[height][width][3];

		for(int y=0; y<height; y++)
		{
			for(int x=0; x<width; x++) 
			{
				Color color = new Color( input.getRGB(x,y) );

                pixel_matrix[y][x][0] = color.getRed();
                pixel_matrix[y][x][1] = color.getGreen();
                pixel_matrix[y][x][2] = color.getBlue();
			}
		}
		return pixel_matrix;
	}

	// getIntensityMatrix ----------------------------------------------------------------------------------------------
	/*
		intensity_matrix = 
			r: [num_i=0, num_i=1, num_i=2, ..., num_i=255]
			g: [num_i=0, num_i=1, num_i=2, ..., num_i=255]
			b: [num_i=0, num_i=1, num_i=2, ..., num_i=255]
	*/
	public int[][] getIntensityMatrix(BufferedImage img_source)
	{
		int[][] intensity_matrix = new int[3][256];

		for(int y=0; y<height; y++)
		{
			for(int x=0; x<width; x++) 
			{
				Color color = new Color( img_source.getRGB(x,y) );

				intensity_matrix[0][color.getRed()]++;
				intensity_matrix[1][color.getGreen()]++;
				intensity_matrix[2][color.getBlue()]++;
			}
		}
		return intensity_matrix;
    }
    
    // RGB to HSL ------------------------------------------------------------------------------------------------------
    public float[] RGB_to_HSL(int inR, int inG, int inB) 
    {
        float h, s, l;

		float r = inR / 255f;
		float g = inG / 255f;
		float b = inB / 255f;

		float max = Math.max(r,Math.max(g,b));
		float min = Math.min(r,Math.min(g,b));

		float d = max - min;
		// find l //
		l = (max + min) / 2.0f;
		// find s //
		if (max == 0 || min == 1)    {   s = 0.0f;                        }
		else                         {   s = (max - l)/Math.min(l,1-l);   }
		// find h //
		if      (min == max)    {   h = 0f;                   }
		else if (max == r)      {   h = (60f*((g-b)/d));      }
		else if (max == g)      {   h = (60f*(2f+(b-r)/d));   }
		else                    {   h = (60f*(4f+(r-g)/d));   }
        
        float[] hsl = {h, s, l};
        
        return hsl;
	}

	// HSL to RGB ------------------------------------------------------------------------------------------------------
    public int[] HSL_to_RGB(float h, float s, float l) 
    {
		float r = 0, g = 0, b = 0;
		float h60 = h/60f;
		float c = (1 - Math.abs(2*l-1)) * s;
		float x = c * (1-Math.abs(h60%2-1));
        float m = l - c/2f;
        
		if      (h60 >= 0 && h60 <= 1)    {   r = c; g = x; b = 0;   }
		else if (h60 >= 1 && h60 <= 2)    {   r = x; g = c; b = 0;   }
		else if (h60 >= 2 && h60 <= 3)    {   r = 0; g = c; b = x;   }
		else if (h60 >= 3 && h60 <= 4)    {   r = 0; g = x; b = c;   }
		else if (h60 >= 4 && h60 <= 5)    {   r = x; g = 0; b = c;   }
        else if (h60 >= 5 && h60 <= 6)    {   r = c; g = 0; b = x;   }
        
        int[] rgb = new int[]{   H_to_RGB(r+m), H_to_RGB(g+m), H_to_RGB(b+m)   };
        
		return rgb;
    }

    // Hue to RGB ------------------------------------------------------------------------------------------------------
    public int H_to_RGB(float v)
    {
        int rgb_value = (int)Math.min(255,256*v);
        return rgb_value;
    }

    // cdf for a certain grey_level ------------------------------------------------------------------------------------
    public int cdf_GreyLevel(int[] grey_levels, int grey_level)
    {
        int total = 0;

        for(int i=0; i<grey_level; i++)
        {
            total = total + grey_levels[i];
        }

        return total;
    }
	
    // events handlers /////////////////////////////////////////////////////////////////////////////////////////////////
    // -----------------------------------------------------------------------------------------------------------------
	public void displayHistogram() 
	{
        target.resetImage(input);
        plot.drawCurves( getIntensityMatrix(input) );
	}

    // -----------------------------------------------------------------------------------------------------------------
	public void histogramStretch(boolean if_aggressive)
	{
        
        int min[] = new int[]{0, 0, 0};    // darkest endpoints for r,g,b
        int max[] = new int[]{255, 255, 255};    // lightest endpoints for r,g,b

        int[][] intensity_matrix = getIntensityMatrix(input);
        int[][][] pixel_matrix = getPixelMatrix();

        // use which policy //
        if(!if_aggressive)    
        {
            for(int j=0; j<3; j++)    // index j for one color channel
            {
                while( intensity_matrix[j][min[j]]==0 )    {   min[j]++;   }
                while( intensity_matrix[j][max[j]]==0 )    {   max[j]--;   }
            }
        }
        else
        {
            int cutoff = Integer.parseInt( texThres.getText() );

            min[0] = min[1] = min[2] = 0   + cutoff;
            max[0] = max[1] = max[2] = 255 - cutoff;
        }

        // stretch //
        for(int y=0; y<height; y++)
        {
            for( int x=0; x<width; x++ )
            {
                pixel_matrix[y][x][0] = (pixel_matrix[y][x][0]-min[0])*255/(max[0]-min[0]);
                pixel_matrix[y][x][1] = (pixel_matrix[y][x][1]-min[1])*255/(max[1]-min[1]);
                pixel_matrix[y][x][2] = (pixel_matrix[y][x][2]-min[2])*255/(max[2]-min[2]); 

                this.output.setRGB(x, y, pixel_matrix[y][x][0]<<16 | pixel_matrix[y][x][1]<<8 | pixel_matrix[y][x][2]);
            }
        }

        plot.drawCurves( getIntensityMatrix(output) );
        target.resetImage(output);            
	}

    // -----------------------------------------------------------------------------------------------------------------
	public void histogramEqualization() 
	{
        target.resetImage(input);

        int[][] intensity_matrix = getIntensityMatrix(input);
        int[][][] pixel_matrix = getPixelMatrix();
        float[][][] pixel_matrix_HSL = new float[height][width][3];

        int[] grey_levels = new int[256];

        // create pixel_matrix_HSL from pixel_matrix //
        for(int y=0; y<height; y++)
        {
            for( int x=0; x<width; x++ )
            {
                float[] HSL_array = RGB_to_HSL( pixel_matrix[y][x][0], pixel_matrix[y][x][1], pixel_matrix[y][x][2] );

                pixel_matrix_HSL[y][x][0] = HSL_array[0];
                pixel_matrix_HSL[y][x][1] = HSL_array[1];
                pixel_matrix_HSL[y][x][2] = HSL_array[2];

                int grey_level = Math.round(HSL_array[2]*255);
                
                grey_levels[grey_level]++;
            }
        }

        // normalize L in pixel_matrix_HSL //
        for(int y=0; y<height; y++)
        {
            for( int x=0; x<width; x++ )
            {
                int grey_level_current = Math.round( pixel_matrix_HSL[y][x][2]*255 );
                float grey_level_normalized = 255 * cdf_GreyLevel(grey_levels, grey_level_current) / (width*height);
                
                float L_normalized = grey_level_normalized/255;

                pixel_matrix_HSL[y][x][2] = L_normalized;
            }
        }

        // pixel_matrix_HSL to output image //
        for(int y=0; y<height; y++)
        {
            for( int x=0; x<width; x++ )
            {
                int[] RGB_array = HSL_to_RGB(pixel_matrix_HSL[y][x][0], pixel_matrix_HSL[y][x][1], pixel_matrix_HSL[y][x][2]);

                int rgb_value = RGB_array[0]<<16 | RGB_array[1]<<8 | RGB_array[2];

                this.output.setRGB(x, y, rgb_value);
            }
        }

        plot.drawCurves( getIntensityMatrix(output) );
        target.resetImage(output);            
    }
    
	// main ////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args)
    {
        new ImageHistogram(args.length == 1 ? args[0] : "baboon.png");
	}
}






















// Canvas for plotting histogram //
class PlotCanvas extends Canvas
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // lines for plotting axes and mean color locations //
    LineSegment x_axis, y_axis;
    LineSegment red, green, blue;

    Color[] COLORS= new Color[]{   Color.RED, Color.GREEN, Color.BLUE   };

    LineSegment[][] lines_matrix = new LineSegment[3][256];

	boolean showMean = false;
	boolean if_display = false;

    public PlotCanvas()
    {   //                                    x0   y0     x1    y1    (x0,y0)=left&bottom | (x1,y1)=right&up
        x_axis = new LineSegment(Color.BLACK, -10, 0, 256 + 10, 0);
        y_axis = new LineSegment(Color.BLACK, 0, -10, 0, 200 + 10);
    }
    // set mean image color for plot //
    public void setMeanColor(Color clr)
    {
        red = new LineSegment(Color.RED, clr.getRed(), 100, clr.getRed(), 100);
        green = new LineSegment(Color.GREEN, clr.getGreen(), 100, clr.getGreen(), 100);
        blue = new LineSegment(Color.BLUE, clr.getBlue(), 100, clr.getBlue(), 100);
        showMean = true;
        repaint();
    }

    // redraw the canvas //
    public void paint(Graphics g)
    {
        // draw axis //
        int xoffset = (getWidth() - 256) / 2;
        int yoffset = (getHeight() - 200) / 2;

        x_axis.draw(g, xoffset, yoffset, getHeight());
        y_axis.draw(g, xoffset, yoffset, getHeight());

        if (showMean)
        {
            red.draw(g, xoffset, yoffset, getHeight());
            green.draw(g, xoffset, yoffset, getHeight());
            blue.draw(g, xoffset, yoffset, getHeight());
        }

        // draw lines from lines_matrix //
        if(this.if_display)
        {
            for(int k=0; k<3; k++)
            {
                for(int i=0; i<255; i++)
                {
                    this.lines_matrix[k][i].draw(g, xoffset, yoffset, getHeight());
                }
            }
        }
	}
	
	// set curves //
	public void drawCurves(int[][] intensity_matrix)
	{
        if_display = true;

        for(int j=0; j<3; j++)    // j is the index of one particular color
        {
            for(int i=0; i<255; i++)
            {
                double d_y_of_i_curr = intensity_matrix[j][i]/2.5;    // number of curr intensity
                int y_of_i_curr = (int)d_y_of_i_curr;
                double d_y_of_i_next = intensity_matrix[j][i+1]/2.5;    // number of next intensity
                int y_of_i_next = (int)d_y_of_i_next;
    
                this.lines_matrix[j][i] = new LineSegment(COLORS[j], i, y_of_i_curr, i+1, y_of_i_next);
            }
        }
        
        repaint();
	}
}

// LineSegment class defines line segments to be plotted
class LineSegment
{
    // location and color of the line segment
    int x0, y0, x1, y1;
    Color color;
    // Constructor
    public LineSegment(Color clr, int x0, int y0, int x1, int y1)
    {
        color = clr;
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
    }
    public void draw(Graphics g, int xoffset, int yoffset, int height)
    {
        g.setColor(color);
        g.drawLine(x0 + xoffset, height - y0 - yoffset, x1 + xoffset, height - y1 - yoffset);
    }
}