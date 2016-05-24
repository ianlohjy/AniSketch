import processing.core.PImage;
import processing.core.PShape;
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
	
	PImage on_image;
	PImage off_image;
	float[] on_image_size;
	float[] off_image_size;		
	
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
	
	void setOnImage(String path, float width, float height)
	{
		on_image = p.loadImage(path);
		on_image_size = new float[2];
		on_image_size[0] = width;
		on_image_size[1] = height;
	}
	
	void setOffImage(String path, float width, float height)
	{
		off_image = p.loadImage(path);
		off_image_size = new float[2];
		off_image_size[0] = width;
		off_image_size[1] = height;
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
		
		if(behavior == PRESS)
		{
			p.fill(0,50);	
			
			if(pressed)
			{
				p.fill(0);
			}
			else if(hover)
			{
				p.fill(0,150);
			}
		}
		else if(behavior == TOGGLE)
		{	
			if(pressed)
			{
				p.fill(0,200);
				if(hover)
				{
					p.fill(0,255);
				}
			}
			else if(!pressed)
			{
				p.fill(0,50);
				if(hover)
				{
					p.fill(0,150);
				}
			}
		}	
		p.rect(x, y, w, h);
		
		p.fill(255);
		p.textFont(p.default_font, font_size);
		p.textAlign(p.CENTER, p.CENTER);
		p.text(label, x+(w/2), y+(h/2)-2);
		
		if(behavior == PRESS)
		{
			if(hover || pressed)
			{
				drawOnImage();
			}
			else
			{
				drawOffImage();
			}
		}
		else if(behavior == TOGGLE)
		{
			if(pressed)
			{
				drawOnImage();
			}
			else 
			{
				drawOffImage();
			}
		}
	}
	
	void drawOnImage()
	{
		if(on_image != null)
		{
			p.image(on_image, (this.x)+(this.w/2f)-(on_image_size[0]/2f), (this.y)+(this.h/2f)-(on_image_size[1]/2f), on_image_size[0], on_image_size[1]);
		}
	}
	
	void drawOffImage()
	{
		if(off_image != null)
		{
			p.image(off_image, (this.x)+(this.w/2f)-(off_image_size[0]/2f), (this.y)+(this.h/2f)-(off_image_size[1]/2f), off_image_size[0], off_image_size[1]);
		}
	}
	
	MouseEvent checkMouseEvent(MouseEvent e)
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
		
		if(within_bounds)
		{
			mouseEventCallback(e);
		}
		else
		{
			mouseEventCallback(e);
		}
		
		return e;
	}
	
	void mouseEventCallback(MouseEvent e)
	{
		
	}
	
	void on()
	{
		pressed = true;
	}
	
	void off()
	{
		pressed = false;
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
	
	void setHoverStyle(Style style)
	{
		
	}
	
	void setPressedStyle(Style style)
	{
		
	}


	
}
