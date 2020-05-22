package hough;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;

import javax.imageio.*;

// Canvas for image display
class ImageCanvas extends Canvas {
	BufferedImage image;

	//Array list to store the set of line points. 
	ArrayList<Integer> x0list = new ArrayList(), x1list = new ArrayList(), y0list = new ArrayList(), y1list = new ArrayList();
	
	//Array list to store the set of circle points and radius.
	ArrayList<Integer> xlist = new ArrayList(), ylist = new ArrayList(), rlist = new ArrayList();
			
	// initialize the image and mouse control
	public ImageCanvas(BufferedImage input) {
		image = input;
		addMouseListener(new ClickListener());
	}
        
    public ImageCanvas(int width, int height) {
		image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		addMouseListener(new ClickListener());
	}

	// redraw the canvas
	public void paint(Graphics g) {
		// draw boundary
		g.setColor(Color.gray);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
		// compute the offset of the image.
		int xoffset = (getWidth() - image.getWidth()) / 2;
		int yoffset = (getHeight() - image.getHeight()) / 2;
		g.drawImage(image, xoffset, yoffset, this);
	
		//draw line
		int size1 = x0list.size();
		for ( int i = 0; i < size1; i++ ){
			g.setColor(Color.RED);
			g.drawLine(x0list.get(i), y0list.get(i), x1list.get(i), y1list.get(i));
		}
		x0list.clear();
		x1list.clear();
		y0list.clear();
		y1list.clear();
		
		//draw circle
		int size2 = xlist.size();
		for ( int i = 0; i < size2; i++ ){
			g.setColor(Color.RED);
			g.drawOval(xlist.get(i), ylist.get(i), rlist.get(i)*2, rlist.get(i)*2);
		}
		xlist.clear();
		ylist.clear();
		rlist.clear();
	}
	
	// change the image and redraw the canvas
	public void resetImage(Image input) {
		image = new BufferedImage(input.getWidth(null), input.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2D = image.createGraphics();
		g2D.drawImage(input, 0, 0, null);
		repaint();
	}
       
    // change the image and redraw the canvas
	public void resetBuffer(int width, int height) {
		image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		Graphics2D g2D = image.createGraphics();
	}
        
	// listen to mouse click
	class ClickListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if ( e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON3 )
				try {
					ImageIO.write(image, "png", new File("saved.png"));
				} catch ( Exception ex ) {
					ex.printStackTrace();
				}
		}
	}

	//create two points for a line.
	public void create_line_point(int x0, int y0, int x1, int y1){
		int xoffset = (getWidth() - image.getWidth()) / 2;
		int yoffset = (getHeight() - image.getHeight()) / 2;
		x0list.add(x0+xoffset);
		y0list.add(y0+yoffset);
		x1list.add(x1+xoffset);
		y1list.add(y1+yoffset);
	}

	//create point and radius for a circle.
	public void create_circle_point(int x, int y, int R ){
		int xoffset = (getWidth() - image.getWidth()) / 2;
		int yoffset = (getHeight() - image.getHeight()) / 2;
		xlist.add((x+xoffset)-R);
		ylist.add((y+yoffset)-R);
		rlist.add(R);
	}
	
	
}
