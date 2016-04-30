import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Sheet extends Element{

	Style default_style;
	//ArrayList<PVector> drawn_points;
	PVector camera;
	boolean drawing;
	long time_since_last_point = System.currentTimeMillis();
	
	static final int UP = 0;
	static final int DOWN = 1;
	
	int framerate = 25; // The framerate sets limits the number of points per second that you are able to draw
	int millis_per_frame = 1000/25;
	
	int mouse_state = UP;
	AnimationController a;
	
	// SELECTION //
	Key active_key_selection; // Currently selected key (There can only be one key selected at a time)
	int number_keys_under_mouse = 0;
	ArrayList<Key> possible_selections;
	
	
	Sheet(int x, int y, int w, int h, AniSketch p, AnimationController a)
	{
		super(x,y,w,h,p);
		default_style = new Style(p);
		default_style.fill(230,230,230,255);
		//drawn_points = new ArrayList<PVector>();
		drawing = false;
		this.a = a;
		possible_selections = new ArrayList<Key>();
		a.addKey(200, 200, 150);
	}
	
	void draw()
	{
		p.clip(x, y, w, h);
		default_style.apply();
		p.rect(x, y, w, h);
		drawStrokes();
		p.blendMode(p.MULTIPLY);
		drawKeys();
		p.blendMode(p.NORMAL);
		p.noClip();
		p.fill(0);
		
		p.text("Number of keys under mouse: " + possible_selections.size(), 5, this.h - 10);
		
		update();
	}
	
	void drawKeys()
	{
		for(Key key: a.delta_keys)
		{
			key.draw();
		}
	}
	
	void drawStrokes()
	{
		for(Stroke stroke: a.strokes)
		{
			stroke.drawSimple();
		}
		
		if(a.recorded_stroke != null)
		{
			a.recorded_stroke.drawSimple();
		}
	}
	
	void update()
	{
		if(drawing)
		{
			a.recordStroke(p.mouseX, p.mouseY);
		}
		else
		{
			a.stopStroke();
		}
		//drawMotionLine();
	}
	
	// If an object is selected, we wont select anything new UNLESS it is a click
	// Mouse event checking needs to return a true 
	
	void checkMouseEvent(MouseEvent e)
	{
		// Check event, if it is a click, we know to change selection, 
		// 
		boolean within_bounds = withinBounds(e.getX(), e.getY());
		boolean allow_selection_switch = false;
		ArrayList<Key> t_possible_selections = new ArrayList<Key>();
		int[] mouse_status;
		
		boolean selection_has_switched = false;

		// Check key mouse selection incrementally
		// If there is an active key selection, DO NOT switch selection unless:
		// 1. A mouse click event is detected
		// 2. Or the active key selection is NOT in the selectable object list
		// If there is no active selection:
		// 1. The key with the oldest last selected time becomes the new selected key
		
		if(within_bounds)
		{
			for(Key key: a.delta_keys)
			{	
				mouse_status = key.checkMouseEvent(e, active_key_selection, possible_selections, !selection_has_switched);	
				
				if(mouse_status[0] == 1)
				{
					t_possible_selections.add(key);
				}
				
				if(mouse_status[1] == 1)
				{
					if(!selection_has_switched)
					{
						p.println("SWITCHING ACTIVE KEY");
						//found_selection = true;
						active_key_selection = key;
					}
					selection_has_switched = true;
				}	
				else if(mouse_status[1] == -1 && active_key_selection == key)
				{
					active_key_selection = null;
				}
			}
		}
		possible_selections = t_possible_selections;
		p.main_windows.stage.goToActiveKey(active_key_selection);
		
		/*
		if(withinBounds(e.getX(), e.getY()))
		{
			if(e.getButton() == 37)
			{ // If its a left click
				if(e.getAction() == 1)// Mouse Pressed
				{
					drawing = true;
				}
				else if(e.getAction() == 2)// Mouse Released
				{
					//a.stopStroke();
					drawing = false;
				}
				else if(e.getAction() == 3) // Mouse Clicked
				{
				}
				else if(e.getAction() == 4) // Mouse Dragged
				{
					//a.recordStroke(e.getX(), e.getY());
				}
				else if(e.getAction() == 5)
				{
				}
			}
		}
		*/
	}
	
	/*
	void drawMotionLine()
	{
		p.stroke(0);
		p.strokeWeight(10);
		
		if(drawn_points != null && drawn_points.size()>1)
		{
			for(int l=0; l<drawn_points.size()-1; l++)
			{
				p.line(drawn_points.get(l).x, drawn_points.get(l).y, drawn_points.get(l+1).x, drawn_points.get(l+1).y);
			}
		}
	}
	*/
	
	
}
