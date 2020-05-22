package corner;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

// Canvas for image display
class ImageCanvas extends Canvas 
{
	/**
     *
     */
    private static final long serialVersionUID = 1L;

    BufferedImage image;
	
	// Array lists to store the (x,y) coordinates for the corner value //
	ArrayList<Integer> xpoints = new ArrayList();
	ArrayList<Integer> ypoints = new ArrayList();
 
	// initialize the image and mouse control //
    public ImageCanvas(BufferedImage input) 
    {
		image = input;
		addMouseListener(new ClickListener());
	}
    public ImageCanvas(int width, int height) 
    {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		addMouseListener(new ClickListener());
	}

	// redraw the canvas //
    public void paint(Graphics g) 
    {
		// draw boundary
		g.setColor(Color.gray);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
		// compute the offset of the image.
		int xoffset = (getWidth() - image.getWidth()) / 2;
		int yoffset = (getHeight() - image.getHeight()) / 2;
		g.drawImage(image, xoffset, yoffset, this);
		
		//draw cirlce
        for ( int i = 0; i < xpoints.size(); i++ )
        {
			g.setColor(Color.BLUE);
			g.drawOval((xpoints.get(i)+xoffset-2), (ypoints.get(i)+yoffset-2), 4, 4);
		}
		xpoints.clear();
		ypoints.clear();
	}

	// reset an empty image //
    public void resetBuffer(int width, int height) 
    {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		repaint();
	}
	// reset image based on the input //
    public void copyImage(BufferedImage input) 
    {
		Graphics2D g2D = image.createGraphics();
		g2D.drawImage(input, 0, 0, null);
		repaint();
	}
	
	//add x coordinate to the array list
    public void addXpoints(int x)
    {
		xpoints.add(x);
	}
	
	//add y coordinate to the array list
    public void addYpoints(int y)
    {
		ypoints.add(y);
	}

	// listen to mouse click
    class ClickListener extends MouseAdapter 
    {
        public void mouseClicked(MouseEvent e) 
        {
            if ( e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON3 )
            {
                try 
                {
					ImageIO.write(image, "png", new File("saved.png"));
                } 
                catch ( Exception ex ) 
                {
					ex.printStackTrace();
				}
            }
		}
	}
}