
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.event.*;

import processing.awt.PSurfaceAWT.SmoothCanvas;
import processing.core.*;
import processing.event.MouseEvent;

public class AniSketch extends PApplet 
{
	final int[] init_resolution = {1280,720}; 
	final int[] min_resolution  = {1024,640};
	
	int cur_width;
	int cur_height;
	
	//UI hori_resize_bar;
	GestureEngine gesture_engine = new GestureEngine();
	
	MainWindows main_windows;
	
	public static void main(String args[])
	{
		PApplet.main(new String[] { "AniSketch" });
	}
	
	public void settings()
	{
		size(init_resolution[0],init_resolution[1]);
	}
	
	public void screenResized()
	{
		if (cur_width != width || cur_height != height) 
		{
			println("RESIZED");
			cur_width = width;
			cur_height = height;
			
			main_windows.onScreenResize();
		}
	}
	
	public void initialisePAppletFrame()
	{
		// Setups up PApplet frame to be resizable with a minimum size to stop it from getting too small.
		// This methods ensures that resizing happens smoothly and consistently.
		// https://forum.processing.org/two/discussion/15398/limiting-window-resize-to-a-certain-minimum
		
		SmoothCanvas canvas = (SmoothCanvas)getSurface().getNative();
		JFrame       jframe = (JFrame)canvas.getFrame();
		
		jframe.setMinimumSize(new Dimension(min_resolution[0],min_resolution[1]));
		getSurface().setResizable(true);
		
		cur_width = width;
		cur_height = height;
	}
	
	public void initialiseMainWindows()
	{
		main_windows = new MainWindows(this);
	}
	
	public void setup()
	{
		//frameRate(30);
		initialisePAppletFrame();
		initialiseMainWindows();
	}
	
	public void draw()
	{
		background(200);
		screenResized();
		main_windows.update();
	}

	public void setupWindows()
	{
		
	}
	
	public void passMouseEvents(MouseEvent e)
	{
		main_windows.checkMouseEvent(e);
		
	}
	
	public void mousePressed(MouseEvent e)
	{	
		passMouseEvents(e);
	}
	
	public void mouseMoved(MouseEvent e)
	{
		passMouseEvents(e);
	}
	
	public void mouseReleased(MouseEvent e)
	{
		passMouseEvents(e);
	}
	
	public void mouseDragged(MouseEvent e)
	{
		//println(e.getAction());
		passMouseEvents(e);
	}
	
}
