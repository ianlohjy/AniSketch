
//import java.awt.Dimension;
//import java.awt.event.*;

//import processing.awt.PSurfaceAWT.SmoothCanvas;

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
	PFont consolas_b;
	
	String cursor_message = "";
	
	public static void main(String args[])
	{
		PApplet.main(new String[] { "AniSketch" });
	}
	
	public void settings()
	{
		println("TEST");
		size(init_resolution[0],init_resolution[1],P3D);
		smooth(8);
	}
	
	public void screenResized()
	{
		if(cur_width != width || cur_height != height) 
		{
			println("RESIZED");
			cur_width = width;
			cur_height = height;
			ortho();
			main_windows.onScreenResize();
		}
	}
	
	public void initialisePAppletFrame()
	{
		// Setups up PApplet frame to be resizable with a minimum size to stop it from getting too small.
		// This methods ensures that resizing happens smoothly and consistently.
		// https://forum.processing.org/two/discussion/15398/limiting-window-resize-to-a-certain-minimum
		
		//SmoothCanvas canvas = (SmoothCanvas)getSurface().getNative();
		//JFrame       jframe = (JFrame)canvas.getFrame();
		
		//jframe.setMinimumSize(new Dimension(min_resolution[0],min_resolution[1]));
		
		cur_width = width;
		cur_height = height;
		surface.setResizable(true);	
	}
	
	public void initialiseMainWindows()
	{
		main_windows = new MainWindows(this);
	}
	
	public void loadFonts()
	{
		consolas_b = createFont("./consolab", 32);
	}
	
	public void setup()
	{
		ortho();
		frameRate(250);
		loadFonts();
		initialisePAppletFrame();
		initialiseAnimationController(); // Animation Controller needs to be initialised first
		initialiseMainWindows();
		initialiseGestureHandler();
		
		animation.addKey(100, 100, 100);
		animation.addKey(200, 200, 100);
		animation.delta_keys.get(0).connectToKey(animation.delta_keys.get(1));
	}
	
	public void initialiseAnimationController()
	{
		animation = new AnimationController(this);
	}
	
	public void draw()
	{
		background(200);
		screenResized();
		main_windows.update();
		gesture_handler.update();
		animation.update(); // Animation controller is called last. This gives everything else a chance to update any parameters. (esp. Primtives)
		fill(0);
		
		textSize(14);
		textAlign(RIGHT, TOP);
		text((int)frameRate, width-5, 3);	
		
		drawCursorMessage();
	}

	public void drawCursorMessage()
	{
		if(cursor_message != "")
		{
			textFont(consolas_b);
			textSize(12);
			float message_width = textWidth(cursor_message)+20;
			
			noStroke();
			fill(0, 50);
			rect(mouseX-(message_width/2), mouseY-30, message_width, 25);
			fill(0);
			textAlign(CENTER, BOTTOM);
			text(cursor_message,mouseX, mouseY-10);
		}
	}
	
	public void setCursorMessage(String message)
	{
		cursor_message = message;
	}
	
	public void clearCursorMessage()
	{
		cursor_message = "";
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
