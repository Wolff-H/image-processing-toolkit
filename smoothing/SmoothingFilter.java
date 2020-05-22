package smoothing;

/*
	student: Tingrui Hu - th8361 - 201513025
*/

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

// Main class **********************************************************************************************************
public class SmoothingFilter extends Frame implements ActionListener {
	/**
     *
     */
    private static final long serialVersionUID = 1L;
    BufferedImage input;
	BufferedImage output = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
	BufferedImage output2 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
	ImageCanvas source, target;
	TextField texSigma;
	int width, height;

	// Constructor
    public SmoothingFilter(String name) 
    {
		super("Smoothing Filters");
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
		target = new ImageCanvas(input);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Update source");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Add noise");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 mean");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Sigma:"));
		texSigma = new TextField("1", 1);
		controls.add(texSigma);
		button = new Button("5x5 Gaussian");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 median");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("5x5 Kuwahara");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(width*2+100, height+100);
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
			case "Update source":
				updateSource();
				break;
			case "Add noise":
				addNoise();
				break;
			case "5x5 mean":
				meanFilter();
				break;
			case "5x5 Gaussian":
				gaussianFilter();
				break;
			case "5x5 median":
				medianFilter();
				break;
			case "5x5 Kuwahara":
				kuwaharaFilter();
				break;
		}
	}

	// functionality: update source image using filtered image //
	public void updateSource()
	{
		int[][][] pixel_matrix = getPixelMatrix(output);

		for(int y=0; y<height; y++)
        {
            for( int x=0; x<width; x++ )
            {
                source.image.setRGB(x, y, pixel_matrix[y][x][0]<<16 | pixel_matrix[y][x][1]<<8 | pixel_matrix[y][x][2]);
            }
		}

		source.repaint();
	}

	// functionality: add noise to source image //
	public void addNoise()
	{
		Random rand = new Random();
		int dev = 64;
		for ( int y=0, i=0 ; y<height ; y++ )
		{
			for ( int x=0 ; x<width ; x++, i++ )
			{
				Color clr = new Color(source.image.getRGB(x, y));
				int red = clr.getRed() + (int)(rand.nextGaussian() * dev);
				int green = clr.getGreen() + (int)(rand.nextGaussian() * dev);
				int blue = clr.getBlue() + (int)(rand.nextGaussian() * dev);
				red = red < 0 ? 0 : red > 255 ? 255 : red;
				green = green < 0 ? 0 : green > 255 ? 255 : green;
				blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;
				source.image.setRGB(x, y, (new Color(red, green, blue)).getRGB());
			}
		}
		source.repaint();
	}

	// filters /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// mean ------------------------------------------------------------------------------------------------------------
	public void meanFilter()
	{
		// mapping algrithm //
		double mapping_coeff =
			1.0/25.0;

		double[][] mapping_matrix =
			new double[][]
			{
				{1, 1, 1, 1, 1},
				{1, 1, 1, 1, 1},
				{1, 1, 1, 1, 1},
				{1, 1, 1, 1, 1},
				{1, 1, 1, 1, 1},
			};

		// mapping //
		linearMapping(this.output, mapping_coeff, mapping_matrix);

		// apply //
		target.resetImage(output);
	}

	// gaussian --------------------------------------------------------------------------------------------------------
	public void gaussianFilter()
	{
		// mapping algrithm //
		double mapping_coeff =
			0.0;

		double[][] mapping_matrix = new double[5][5];

		double sigma = Double.parseDouble(texSigma.getText());

		for(int y=0; y<5; y++)
		{
			for(int x=0; x<5; x++)
			{
				mapping_matrix[y][x] = calc2dGaussian(x-2, y-2, sigma);
			}
		}

		// mapping //
		linearMapping(this.output, mapping_coeff, mapping_matrix);

		// apply //
		target.resetImage(output);
	}

	// median ----------------------------------------------------------------------------------------------------------
	public void medianFilter()
	{
		int[][][] pixel_matrix_extended = getPixelMatrixExtended(input);

		int[][] kernel_R = new int[5][5];
		int[][] kernel_G = new int[5][5];
		int[][] kernel_B = new int[5][5];

		// mapping //
		for(int y=0; y<height; y++)
		{
			for(int x=0; x<width; x++)
			{
				for(int ky=0; ky<5; ky++)
				{
					for(int kx=0; kx<5; kx++)
					{
						kernel_R[ky][kx] = pixel_matrix_extended[y+ky][x+kx][0];
						kernel_G[ky][kx] = pixel_matrix_extended[y+ky][x+kx][1];
						kernel_B[ky][kx] = pixel_matrix_extended[y+ky][x+kx][2];
					}
				}

				int r_val = calcKernelMedian(kernel_R);
				int g_val = calcKernelMedian(kernel_G);
				int b_val = calcKernelMedian(kernel_B);

				int rgb_value = r_val<<16 | g_val<<8 | b_val;

				this.output.setRGB(x, y, rgb_value);
			}
		}

		target.resetImage(output);
	}

	// kuwahara --------------------------------------------------------------------------------------------------------
	public void kuwaharaFilter()
	{
		int[][][] pixel_matrix_extended = getPixelMatrixExtended(input);

		int[][] kernel_R = new int[5][5];
		int[][] kernel_G = new int[5][5];
		int[][] kernel_B = new int[5][5];

		int[][][] regions = new int[4][3][9];
		double[] variances = new double[4];

        // mapping //
		for(int y=0; y<height; y++)
		{
			for(int x=0; x<width; x++)
			{
				for(int ky=0; ky<5; ky++)
				{
					for(int kx=0; kx<5; kx++)
					{
						kernel_R[ky][kx] = pixel_matrix_extended[y+ky][x+kx][0];
						kernel_G[ky][kx] = pixel_matrix_extended[y+ky][x+kx][1];
						kernel_B[ky][kx] = pixel_matrix_extended[y+ky][x+kx][2];
					}
				}

				for(int rn=0; rn<4; rn++)
				{
					for(int i=0; i<3; i++)
					{
						for(int j=0; j<3; j++)
						{
							regions[rn][0][i*3+j] = kernel_R[i][j];    // LT
							regions[rn][1][i*3+j] = kernel_G[i][j];
							regions[rn][2][i*3+j] = kernel_B[i][j];

							regions[rn][0][i*3+j] = kernel_R[i][5-1-j];    // RT
							regions[rn][1][i*3+j] = kernel_G[i][5-1-j];
							regions[rn][2][i*3+j] = kernel_B[i][5-1-j];

							regions[rn][0][i*3+j] = kernel_R[5-1-i][j];    // LB
							regions[rn][1][i*3+j] = kernel_G[5-1-i][j];
							regions[rn][2][i*3+j] = kernel_B[5-1-i][j];

							regions[rn][0][i*3+j] = kernel_R[5-1-i][5-1-j];    // RB
							regions[rn][1][i*3+j] = kernel_G[5-1-i][5-1-j];
							regions[rn][2][i*3+j] = kernel_B[5-1-i][5-1-j];
						}
					}
				}

				int region_num;
				// find min_var_rn for r,g,b, use this region's r,g,b mean
				variances[0] = calcVariance(regions[0][0]);
				variances[1] = calcVariance(regions[1][0]);
				variances[2] = calcVariance(regions[2][0]);
				variances[3] = calcVariance(regions[3][0]);
				region_num = getRegionNumWithMinVar(variances);
				int r_val = (int)calcMean(regions[region_num][0]);

				variances[0] = calcVariance(regions[0][1]);
				variances[1] = calcVariance(regions[1][1]);
				variances[2] = calcVariance(regions[2][1]);
				variances[3] = calcVariance(regions[3][1]);
				region_num = getRegionNumWithMinVar(variances);
				int g_val = (int)calcMean(regions[region_num][1]);

				variances[0] = calcVariance(regions[0][2]);
				variances[1] = calcVariance(regions[1][2]);
				variances[2] = calcVariance(regions[2][2]);
				variances[3] = calcVariance(regions[3][2]);
				region_num = getRegionNumWithMinVar(variances);
				int b_val = (int)calcMean(regions[region_num][2]);

				int rgb_value = r_val<<16 | g_val<<8 | b_val;

				this.output.setRGB(x, y, rgb_value);
			}
		}

        // apply //
		target.resetImage(output);
	}

	// helpers /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public int[][][] getPixelMatrix(BufferedImage image)
	{
		int[][][] pixel_matrix = new int[height][width][3];

		for(int y=0; y<height; y++)
		{
			for(int x=0; x<width; x++)
			{
				Color color = new Color( image.getRGB(x,y) );

                pixel_matrix[y][x][0] = color.getRed();
                pixel_matrix[y][x][1] = color.getGreen();
                pixel_matrix[y][x][2] = color.getBlue();
			}
		}

		return pixel_matrix;
	}

	public int[][][] getPixelMatrixExtended(BufferedImage image)
	{
		int[][][] pixel_matrix = getPixelMatrix(image);
		int[][][] pixel_matrix_extended = new int[height+4][width+4][3];

		for(int rgb_channel=0; rgb_channel<3; rgb_channel++)
		{
			// fill corners extended //
			for(int i=0; i<2; i++)
			{
				for(int j=0; j<2; j++)
				{
					pixel_matrix_extended[i][j][rgb_channel] = pixel_matrix[0][0][rgb_channel];
					pixel_matrix_extended[width-1-i][j][rgb_channel] = pixel_matrix[width-1][0][rgb_channel];
					pixel_matrix_extended[i][height-1-j][rgb_channel] = pixel_matrix[0][height-1][rgb_channel];
					pixel_matrix_extended[width-1-i][height-1-i][rgb_channel] = pixel_matrix[width-1][height-1][rgb_channel];
				}
			}
			// fill borders extended //
			for(int y=0; y<width; y++)
			{
				pixel_matrix_extended[y+2][0][rgb_channel] = pixel_matrix[y][0][rgb_channel];
				pixel_matrix_extended[y+2][1][rgb_channel] = pixel_matrix[y][0][rgb_channel];
				pixel_matrix_extended[y+2][height+4-1][rgb_channel] = pixel_matrix[y][height-1][rgb_channel];
				pixel_matrix_extended[y+2][height+4-1-1][rgb_channel] = pixel_matrix[y][height-1][rgb_channel];
			}
			for(int x=0; x<width; x++)
			{
				pixel_matrix_extended[0][x+2][rgb_channel] = pixel_matrix[0][x][rgb_channel];
				pixel_matrix_extended[1][x+2][rgb_channel] = pixel_matrix[0][x][rgb_channel];
				pixel_matrix_extended[width+4-1][x+2][rgb_channel] = pixel_matrix[width-1][x][rgb_channel];
				pixel_matrix_extended[width+4-1-1][x+2][rgb_channel] = pixel_matrix[width-1][x][rgb_channel];
			}
			// copy the mid block //
			for(int y=0; y<height; y++)
			{
				for(int x=0; x<width; x++)
				{
					pixel_matrix_extended[y+2][x+2][rgb_channel] = pixel_matrix[y][x][rgb_channel];
				}
			}
		}

		return pixel_matrix_extended;
	}

	public int tensorProduct(int[][] kernel, double mapping_coeff, double[][] mapping_matrix)
	{
		double product_of_position;
		double sum = 0.0;

		for(int ky=0; ky<5; ky++)
		{
			for(int kx=0; kx<5; kx++)
			{
				product_of_position = kernel[ky][kx] * mapping_matrix[ky][kx];
				sum = sum + product_of_position;
			}
		}

		if(!(mapping_coeff==0))
		{
			return (int)(mapping_coeff * sum);
		}

		return (int)sum;
	}

	public void linearMapping(BufferedImage output, double mapping_coeff, double[][] mapping_matrix)
	{
		int[][][] pixel_matrix_extended = getPixelMatrixExtended(input);

		int[][] kernel_R = new int[5][5];
		int[][] kernel_G = new int[5][5];
		int[][] kernel_B = new int[5][5];

		for(int y=0; y<height; y++)
		{
			for(int x=0; x<width; x++)
			{
				for(int ky=0; ky<5; ky++)
				{
					for(int kx=0; kx<5; kx++)
					{
						kernel_R[ky][kx] = pixel_matrix_extended[y+ky][x+kx][0];
						kernel_G[ky][kx] = pixel_matrix_extended[y+ky][x+kx][1];
						kernel_B[ky][kx] = pixel_matrix_extended[y+ky][x+kx][2];
					}
				}

				int r_val = tensorProduct(kernel_R, mapping_coeff, mapping_matrix);
				int g_val = tensorProduct(kernel_G, mapping_coeff, mapping_matrix);
				int b_val = tensorProduct(kernel_B, mapping_coeff, mapping_matrix);

				int rgb_value = r_val<<16 | g_val<<8 | b_val;

				this.output.setRGB(x, y, rgb_value);
			}
		}
	}

	public double calc2dGaussian(double x, double y, double sigma)
    {
        double sigma_sq = Math.pow(sigma, 2);
        double coe      = 1.0/( 2 * Math.PI * sigma_sq );
        double exp      = (-x*x -y*y)/(2.0 * sigma_sq);
        double gaussian = coe * Math.pow(Math.E, exp);

        return gaussian;
	}

	public int calcKernelMedian(int[][] kernel)
	{
		ArrayList<Integer> flattened = new ArrayList<Integer>();

		for(int y=0; y<5; y++)
		{
			for(int x=0; x<5; x++)
			{
				flattened.add(kernel[y][x]);
			}
		}

		Collections.sort(flattened);

		return flattened.get(12);
	}

	public double calcMean(double[] sequence)
	{
		double sum = 0;

		for (double num : sequence)
		{
			sum = sum + num;
		}

		return sum/sequence.length;
	}

	public double calcMean(int[] sequence)
	{
		double sum = 0;

		for (int num : sequence)
		{
			sum = sum + num;
		}

		return sum/sequence.length;
	}

	public double calcVariance(int[] sequence)
	{
		int n = sequence.length;

		double mean = calcMean(sequence);
		double diff_sq;
		double sum_diff_sq = 0;

		for(int i=0; i<n; i++)
		{
			diff_sq = Math.pow(sequence[i] - mean, 2);
			sum_diff_sq += diff_sq;
		}

		return sum_diff_sq/n;
	}

	public int getRegionNumWithMinVar(double[] variances)
	{
		double min = variances[0];
		int min_var_rn = 0;

		int n = variances.length;

		for(int i=1; i<n; i++)
		{
			if(variances[i]<min)
			{
				min = variances[i];
				min_var_rn = i;
			}
		}

		return min_var_rn;
	}

	// main ************************************************************************************************************
    public static void main(String[] args) 
    {
		new SmoothingFilter(args.length==1 ? args[0] : "baboon.png");
	}
}
