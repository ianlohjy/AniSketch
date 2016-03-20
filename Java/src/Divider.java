import processing.core.PApplet;

public class Divider extends Element{

	Style default_style;
	Style hover_style;
	
	static int HORIZONTAL = 0;
	static int VERTICAL   = 1;
	
	int thickness;
	int type;
	
	float bounds[]; // The bounds is the size of the thing that the divider is "dividing"
	float current_postion; // Position of divider within bounds
	float movement_range[]; // Range within which the divider is allowed to move in, value is relative to bounds 
	
	

	Divider(PApplet p)
	{
		super(0,0,0,0,p);
		default_style = new Style(p);
		default_style.setDefault();
		hover_style = new Style(p);
		hover_style.setDefault();
		
		bounds = new float[2];
		movement_range = new float[2];
		current_postion = 0;
	}
	
	void setVertical(int x, int y, int h, int thickness)
	{
		this.x = x;
		this.y = y;
		this.h = h;
		this.w = thickness;
		this.thickness = thickness;
		x_offset = (int)(-thickness/2f);
		y_offset = 0;
		type = VERTICAL;
	}
	
	void setBounds(float lower, float upper)
	{
		bounds[0] = lower;
		bounds[1] = upper;
	}
	
	void setMovementRange(float start, float stop)
	{
		movement_range[0] = start;
		movement_range[1] = stop;
	}
	
	void updateRatioPosition()
	{
		if(type == VERTICAL)
		{
			current_postion = x/(bounds[1]-bounds[0]);	
		}
		else if(type == HORIZONTAL)
		{
			current_postion = y/(bounds[1]-bounds[0]);
		}
	}
	
	void updateCurrentPosition()
	{
		if(type == VERTICAL)
		{
			//current_postion = x/(bounds[1]-bounds[0]);	
			//p.println(current_postion);
			x = (int)(bounds[0]+(current_postion*(bounds[1]-bounds[0])));
		}
		else if(type == HORIZONTAL)
		{
			//current_postion = y/(bounds[1]-bounds[0]);
			y = (int)(bounds[0]+(current_postion*(bounds[1]-bounds[0])));
		}
	}
	
	void setHorizontal(int x, int y, int w, int thickness)
	{
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = thickness;
		this.thickness = thickness;
		x_offset = 0;
		y_offset = (int)(-thickness/2f);
		type = HORIZONTAL;
	}
		
	void mouseInputResponse()
	{
		if(pressed)
		{
			if(type == VERTICAL)
			{
				x = p.mouseX;
				if(x < movement_range[0]*(bounds[1]-bounds[0]))
				{
					x = (int)(movement_range[0]*(bounds[1]-bounds[0]));
				}
				else if(x > movement_range[1]*(bounds[1]-bounds[0]))
				{
					x = (int)(movement_range[1]*(bounds[1]-bounds[0]));
				}
				updateRatioPosition();
			}
			
			else if(type == HORIZONTAL)
			{
				y = p.mouseY;
				if(y < movement_range[0]*(bounds[1]-bounds[0]))
				{
					y = (int)(movement_range[0]*(bounds[1]-bounds[0]));
				}
				else if(y > movement_range[1]*(bounds[1]-bounds[0]))
				{
					y = (int)(movement_range[1]*(bounds[1]-bounds[0]));
				}
				updateRatioPosition();
			}
		}
		//p.println(current_postion);
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
