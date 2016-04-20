
import javax.swing.JFrame;

import java.awt.Dimension;
import java.awt.event.*;

import processing.awt.PSurfaceAWT.SmoothCanvas;
import processing.core.*;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class AniSketch extends PApplet 
{
	final int[] init_resolution = {1280,720}; 
	final int[] min_resolution  = {1024,640};
	
	int cur_width;
	int cur_height;
	
	GestureHandler gesture_handler;
	MainWindows main_windows;
	AnimationController animation;
	
	public static void main(String args[])
	{
		PApplet.main(new String[] { "AniSketch" });
	}
	
	public void settings()
	{
		println("TEST");
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
		frameRate(250);
		initialiseAnimationController(); // Animation Controller needs to be initialised first
		initialisePAppletFrame();
		initialiseMainWindows();
		initialiseGestureHandler();
		
	}
	
	public void initialiseAnimationController()
	{
		animation = new AnimationController(this);
	}
	
	public void draw()
	{
		background(200);
		screenResized();
		animation.update();
		main_windows.update();
		gesture_handler.update();
		
		text("Framerate " + (int)frameRate, 5, 15);
	}

	public void initialiseGestureHandler()
	{
		gesture_handler = new GestureHandler(this);
	}
	
	public void setupWindows()
	{
		
	}
	
	public void passMouseEvents(MouseEvent e)
	{
		main_windows.checkMouseEvent(e);
		gesture_handler.checkMouseEvent(e);
	}
	
	public void mouseClicked(MouseEvent e)
	{
		passMouseEvents(e);
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
		passMouseEvents(e);
	}
	
	public void keyPressed(KeyEvent e) 
	{
		animation.checkKeyEvent(e);
	}
	
}
