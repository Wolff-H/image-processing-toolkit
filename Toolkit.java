
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

import histogram.*;
import smoothing.*;
import threshold.*;
import hough.*;
import corner.*;

// main class **********************************************************************************************************
public class Toolkit extends JFrame 
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JTabbedPane tabbedPane = new JTabbedPane();

    public Toolkit()
    {
        setTitle("Image Processing Toolkit");
        setBounds(10, 200, 400, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        TextField image_path = new TextField("", 40);
        Panel controls = new Panel();
        controls.add(image_path);
        controls.setSize(384, 30);
        controls.setLocation(1, 22);
        add(controls);

        Button save_button = new Button("save current output");
        Panel save_to = new Panel();
        save_to.add(save_button);
        save_to.setSize(384, 30);
        save_to.setLocation(1, 252);
        add(save_to);
        
        // tools & entries /////////////////////////////////////////////////////////////////////////////////////////////
        // histogram ---------------------------------------------------------------------------------------------------
        Frame tool_histogram = new ImageHistogram("baboon.png");

        tabbedPane.addTab("histogram", null,
            new JTextArea(
                "\n\n\ninstruction - image histogram",
                20, 80
            ),
            "image histogram operations"
        );
        
        // smoothing ---------------------------------------------------------------------------------------------------
        Frame tool_smoothing = new SmoothingFilter("baboon.png");
        
        tabbedPane.addTab("smooth", null,
            new JTextArea(
                "\n\n\ninstruction - smoothing filter",
                20, 80
            ),
            "apply smoothing filter"
        );

        // threshold ---------------------------------------------------------------------------------------------------
        Frame tool_threshold = new ImageThreshold("fingerprint.png");
        
        tabbedPane.addTab("threshold", null,
            new JTextArea(
                "\n\n\ninstruction - thresholding filter",
                20, 80
            ),
            "apply thresholding filter"
        );
        
        // hough -------------------------------------------------------------------------------------------------------
        Frame tool_hough = new HoughTransform("rectangle.png");
        
        tabbedPane.addTab("hough", null,
            new JTextArea(
                "\n\n\ninstruction - hough transformation",
                20, 80
            ),
            "apply hough transformation"
        );
        // corner ------------------------------------------------------------------------------------------------------
        Frame tool_corner = new CornerDetection("signal_hill.png");
        
        tabbedPane.addTab("corner", null,
            new JTextArea(
                "\n\n\ninstruction - corner detection",
                20, 80
            ),
            "detect corners"
        );
        
        // entry listener //////////////////////////////////////////////////////////////////////////////////////////////
        tabbedPane.addChangeListener(
            (ChangeListener) new ChangeListener() 
            {
                @Override
                public void stateChanged(ChangeEvent e) 
                {
                    int selectedIndex = tabbedPane.getSelectedIndex();
                    String title = tabbedPane.getTitleAt(selectedIndex);
                    System.out.println(title);

                    if (title.equals("histogram"))
                    {
                        tool_histogram.setVisible(true);
                        tool_smoothing.setVisible(false);
                        tool_threshold.setVisible(false);
                        tool_hough.setVisible(false);
                        tool_corner.setVisible(false);
                    }
                    else if (title.equals("smooth"))
                    {
                        tool_histogram.setVisible(false);
                        tool_smoothing.setVisible(true);
                        tool_threshold.setVisible(false);
                        tool_hough.setVisible(false);
                        tool_corner.setVisible(false);
                    }
                    else if (title.equals("threshold"))
                    {
                        tool_histogram.setVisible(false);
                        tool_smoothing.setVisible(false);
                        tool_threshold.setVisible(true);
                        tool_hough.setVisible(false);
                        tool_corner.setVisible(false);
                    }
                    else if (title.equals("hough"))
                    {
                        tool_histogram.setVisible(false);
                        tool_smoothing.setVisible(false);
                        tool_threshold.setVisible(false);
                        tool_hough.setVisible(true);
                        tool_corner.setVisible(false);
                    }
                    else if (title.equals("corner"))
                    {
                        tool_histogram.setVisible(false);
                        tool_smoothing.setVisible(false);
                        tool_threshold.setVisible(false);
                        tool_hough.setVisible(false);
                        tool_corner.setVisible(true);
                    }
                }
            }
        );

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setSelectedIndex(0); 
        tabbedPane.setLocation(0, 200);
		// tabbedPane.setEnabledAt(0, false); 
		
		add(tabbedPane);
		setVisible(true);
	}

    // main ************************************************************************************************************
    public static void main(String[] args) 
    {
        // Toolkit toolkit_window = 
        new Toolkit();
 
	}
}