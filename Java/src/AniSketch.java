
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
	PFont default_font;
	
	String cursor_message = "";
	
	final boolean export = true;
	String version_info = "AniSketch 0.8 (24/05/2016)";
	
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
		String font_url = getResource("/resources/fonts/OpenSans-Semibold.ttf");
		//File font_file = new File(font_url);
		//println("!!! " + font_url);
		default_font = createFont(font_url, 32);
	}
	
	public void setup()
	{
		ortho();
		frameRate(250);
		loadFonts();
		initialisePAppletFrame();
		initialiseAnimationController(); // Animation Controller needs to be initialised first so that it can be passed to other windows
		initialiseMainWindows();
		initialiseGestureHandler();
		
		//animation.addKey(100, 100, 100);
		//animation.addKey(200, 200, 100);
		//animation.delta_keys.get(0).connectToKey(animation.delta_keys.get(1));
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
		
		/*
		for(int x=0; x<513; x++)
		{
			for(int y=0; y<513; y++)
			{
				noStroke();
				float value = Utilities.gaussian1d(x, 0, 0.3333333333333333333f*512) * Utilities.gaussian1d(y, 0, 0.3333333333333333333f*512);;
				fill(255*value);
				rect(x,y,1,1);
			}
		}
		*/
		//println(Utilities.gaussian1d(0, 0, 0.333333f));
		//println(Utilities.gaussian1d(0.5f, 0, 0.333333f));
		//println(Utilities.gaussian1d(1f, 0,0.333333f));
	}

	public void drawCursorMessage()
	{
		if(cursor_message != "")
		{
			textFont(default_font);
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
	
	//===================//
	// RESOURCE HANDLING //
	//===================//
	
	public String getResource(String resource_path)
	{
		if(export)
		{
			return AniSketch.class.getResource(resource_path).toString();
		}
		else
		{
			return AniSketch.class.getResource(resource_path).getPath();
		}
	}
	
}
