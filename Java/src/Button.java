import processing.event.MouseEvent;

public class Button{

	float x;
	float y;
	float w;
	float h;
	AniSketch p;
	String label;
	
	boolean pressed;
	boolean hover;
	
	final static int PRESS   = 0;
	final static int TOGGLE  = 1;
	
	int behavior = PRESS;
	int font_size;
	
	Button(int x, int y, int w, int h, AniSketch p) 
	{
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.p = p;
		this.label = "";
		this.pressed = false;
		this.hover = false;
		this.font_size = 12;
	}
	
	void setToToggle()
	{
		behavior = TOGGLE;
	}
	
	void setToPress()
	{
		behavior = PRESS;
	}
	
	void update()
	{
	}
	
	void setLabel(String new_label)
	{
		label = new_label;
	}
	
	void setLabelSize(int size)
	{
		font_size = size;
	}
	
	void draw()
	{
		p.noStroke();
		p.fill(0,50);
		
		if(pressed)
		{
			p.fill(0);
		}
		else if(hover)
		{
			p.fill(0,100);
		}
		p.rect(x, y, w, h);
		
		p.fill(255);
		p.textFont(p.consolas_b, font_size);
		p.textAlign(p.CENTER, p.CENTER);
		p.text(label, x+(w/2), y+(h/2)-2);
	}
	
	void checkMouseEvent(MouseEvent e)
	{
		boolean within_bounds = Utilities.withinBounds(x, y, w, h, e.getX(),e.getY());
		
		if(e.getAction() == 1) // When mouse is pressed (down)
		{
			if(within_bounds)
			{
				if(behavior == TOGGLE)
				{
					if(pressed == false) 
					{
						toggleOnAction();
						pressed = true;
					}
					else 
					{
						pressed = false;
						toggleOffAction();
					}
				}
				else if(behavior == PRESS)
				{
					pressed = true;
				}
			}
		}
		else if(e.getAction() == 2) // When mouse is released
		{
			if(behavior == PRESS)
			{
				if(pressed = true)
				{
					pressed = false;
					if(within_bounds)
					{
						pressAction();
					}
				}
			}
		}
		else if(e.getAction() == 5) // When mouse is moved
		{
			if(within_bounds)
			{hover = true;}
			else
			{hover = false;}
		}
	}
	
	void pressAction()
	{
		Utilities.printAlert("PRESS ACTION");
	}
	
	void toggleOnAction()
	{
		Utilities.printAlert("TOGGLE ON ACTION");
	}
	
	void toggleOffAction()
	{
		Utilities.printAlert("TOGGLE OFF ACTION");
	}
	

}
