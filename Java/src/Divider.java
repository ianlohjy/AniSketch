import processing.core.PApplet;
import processing.event.MouseEvent;

public class Divider extends Element{

	Style default_style;
	Style hover_style;
	
	static int HORIZONTAL = 0;
	static int VERTICAL   = 1;
	
	int thickness;
	int type;
	
	float ratio_position; // Position of divider within bounds
	float lower_bounds; // The bounds is the size of the thing that the divider is "dividing"
	float upper_bounds;
	float start_range; // Range within which the divider is allowed to move in, value is relative to bounds 
	float stop_range;
	
	Object parent;
	
	Divider(PApplet p, Object parent)
	{
		super(0,0,0,0,p);
		
		hover_style   = new Style(p);
		default_style = new Style(p);
		setupStyles();
		
		this.thickness       = 1;
		this.lower_bounds    = 0;
		this.lower_bounds    = 1;
		this.ratio_position  = 0;
		this.start_range     = 0;
		this.stop_range      = 1;
		
		this.parent = parent;
	}
	
	void setVertical(float lower_bounds, float upper_bounds, float start_range, float stop_range, float ratio_position, int thickness)
	{
		this.ratio_position = ratio_position;
		this.x = (int)(lower_bounds+(ratio_position*(upper_bounds-lower_bounds)));
		this.w = thickness;
		this.thickness = thickness;
		this.x_offset = (int)(-thickness/2f);
		this.y_offset = 0;
		this.upper_bounds = upper_bounds;
		this.lower_bounds = lower_bounds;
		this.start_range = start_range;
		this.stop_range = stop_range;
		type = VERTICAL;
	}
	
	void setHorizontal(float lower_bounds, float upper_bounds, float start_range, float stop_range, float ratio_position, int thickness)
	{
		this.ratio_position = ratio_position;
		this.y = (int)(lower_bounds+(ratio_position*(upper_bounds-lower_bounds)));
		this.h = thickness;
		this.thickness = thickness;
		this.x_offset = 0;
		this.y_offset = (int)(-thickness/2f);
		this.upper_bounds = upper_bounds;
		this.lower_bounds = lower_bounds;
		this.start_range = start_range;
		this.stop_range = stop_range;
		type = HORIZONTAL;
	}
		
	void setBounds(float lower_bounds, float upper_bounds)
	{
		this.lower_bounds = lower_bounds;
		this.upper_bounds = upper_bounds;
	}
	
	void updateRatioPosition()
	{	// Calculates and updates the position of the divider, as a ratio between lower and upper bounds 
		// This is used whenever the divider is moved
		if(type == VERTICAL)
		{
			ratio_position = x/(upper_bounds-lower_bounds);	
		}
		else if(type == HORIZONTAL)
		{
			ratio_position = y/(upper_bounds-lower_bounds);
		}
	}
	
	void updateCurrentPosition()
	{	// Calculates the "hard"/static position of the divider
		if(type == VERTICAL)
		{
			x = (int)(lower_bounds+(ratio_position*(upper_bounds-lower_bounds)));
		}
		else if(type == HORIZONTAL)
		{
			y = (int)(lower_bounds+(ratio_position*(upper_bounds-lower_bounds)));
		}
		
		if(parent instanceof MainWindows)
		{
			((MainWindows) parent).callbackDividersUpdatePosition();
		}
	}
	
	@Override
	void mouseInputResponse(MouseEvent e)
	{
		if(pressed)
		{
			if(type == VERTICAL)
			{
				x = p.mouseX;
				if(x < start_range*(upper_bounds-lower_bounds))
				{
					x = (int)(start_range*(upper_bounds-lower_bounds));
				}
				else if(x > stop_range*(upper_bounds-lower_bounds))
				{
					x = (int)(stop_range*(upper_bounds-lower_bounds));
				}
				updateRatioPosition();
			}
			
			else if(type == HORIZONTAL)
			{
				y = p.mouseY;
				if(y < start_range*(upper_bounds-lower_bounds))
				{
					y = (int)(start_range*(upper_bounds-lower_bounds));
				}
				else if(y > stop_range*(upper_bounds-lower_bounds))
				{
					y = (int)(stop_range*(upper_bounds-lower_bounds));
				}
				updateRatioPosition();
			}
		}
	}
	
	void setupStyles()
	{
		hover_style.fill(80,80,80,255);
		hover_style.noStroke();
		default_style.fill(20,20,20,255);
		default_style.noStroke();
	}
	
	void draw()
	{
		updateCurrentPosition();
		if(hover || pressed)
		{
			hover_style.apply();
		}
		else 
		{
			default_style.apply();
		}
		
		p.rectMode(p.CORNER);
		if(type == VERTICAL)
		{
			p.rect(x+x_offset, y+y_offset, w, h);
		}
		else if(type == HORIZONTAL)
		{
			p.rect(x+x_offset, y+y_offset, w, h);
		}
	}
}
