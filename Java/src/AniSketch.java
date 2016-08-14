
//import java.awt.Dimension;
//import java.awt.event.*;
//import processing.awt.PSurfaceAWT.SmoothCanvas;
import java.io.File;

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
	
	final boolean export = false;
	String version_info = "AniSketch 0.8 (24/05/2016)";
	
	// RENDER & EXPORT
	boolean rendering = false;
	boolean init_render = false;
	int last_rendered_frame = 0;
	String render_path = "";
	String render_name = "";
	
	
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
		setIconAndTitle();
		
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
		handleRender();
	}

	public void drawCursorMessage()
	{
		if(cursor_message != "")
		{
			textFont(default_font);
			textSize(12);
			float message_width = textWidth(cursor_message)+20;
			
			pushMatrix();
			translate(0,0,100);
			noStroke();
			fill(0);
			rect(mouseX-(message_width/2), mouseY-30, message_width, 25);
			fill(255);
			textAlign(CENTER, BOTTOM);
			text(cursor_message,mouseX, mouseY-10);
			popMatrix();
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
	
	public void passMouseEvents(MouseEvent e)
	{
		if(!rendering)
		{
			main_windows.checkMouseEvent(e);
			gesture_handler.checkMouseEvent(e);
		}
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
		if(!rendering)
		{
			animation.checkKeyEvent(e);
		}
		
		// Divert escape key press
		if(e.getKeyCode() == 27)
		{
			key=0;
			println("ESC key pressed");
			
			if(rendering)
			{
				Utilities.printAlert("Stopping render");
				stopRendering();
			}
		}
	}
	
	public void setIconAndTitle()
	{
		// Adapted from https://forum.processing.org/one/topic/how-to-change-the-icon-of-the-app.html
		// Icons are not set by replacing the icon pngs in processing.core jar file
		/*
		PImage icon;
		PGraphics icon_graphics;
		
		String icon_url = getResource("/resources/icons/icon.png");
		icon = loadImage(icon_url);
		icon_graphics = createGraphics(16, 16);
		icon_graphics.beginDraw();
		icon_graphics.image(icon, 0, 0, 16, 16);
		icon_graphics.endDraw();
		frame.setIconImage(icon_graphics.image);
		*/
		surface.setTitle("AniSketch Developer Alpha");

	}
	
	public void startRender(String input_path)
	{
		if(!rendering)
		{
			// Make the directory
			if(new File(input_path).exists())
			{
				Utilities.printAlert("Render folder already exists");
			}
			else
			{
				boolean success = new File(input_path).mkdir();
				
				if(success)
				{
					Utilities.printAlert("Render folder created");
				}
				else
				{
					Utilities.printAlert("Could not create render folder. Check permissions?");
					return;
				}
			}
			
			init_render = false;
			rendering = true;
			last_rendered_frame = 0;
			render_path = input_path;
			render_name = new File(input_path).getName();
		}
	}
	
	public void stopRendering()
	{
		rendering = false;
		last_rendered_frame = 0;
		render_path = "";
		render_name = "";
		init_render = false;
	}
	
	public void handleRender()
	{ 
		if(rendering)
		{
			// If rendering was just started, reset the timeline and let AniSketch update the timeline before saving out.
			if(init_render)
			{
				animation.current_frame = last_rendered_frame;
				
				int frame_digits = String.valueOf(animation.frame_range[1]).length();
				String post_fix  = "_" + String.format("%0"+frame_digits+"d", animation.current_frame);
				Utilities.printAlert("Saving " + render_name + post_fix);
				
				save(render_path + "/" + render_name + post_fix + ".png");
				
				last_rendered_frame++;
				if(last_rendered_frame > animation.frame_range[1])
				{
					stopRendering();
				}
			}
			else if(!init_render)
			{
				animation.current_frame = 0;
				init_render = true;
			}
		}
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
