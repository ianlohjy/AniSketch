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
	
	Style hover_style;
	Style pressed_style;
	Style default_style;
	
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
		setupStyles();
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
	
	void setupStyles(){
		default_style = new Style(p);
		default_style.fill(0,0,0,50);
		default_style.noStroke();
		
		hover_style = new Style(p);
		hover_style.fill(0,0,0,150);
		hover_style.noStroke();
		
		pressed_style = new Style(p);
		pressed_style.fill(0,0,0,255);
		pressed_style.noStroke();
	}
	
	void draw()
	{
		if(behavior == PRESS)
		{
			default_style.apply();
		
			if(pressed)
			{
				pressed_style.apply();
			}
			else if(hover)
			{
				hover_style.apply();
			}
		}
		else if(behavior == TOGGLE)
		{	
			if(pressed)
			{
				hover_style.apply();
				if(hover)
				{
					pressed_style.apply();
				}
			}
			else if(!pressed)
			{
				default_style.apply();
				if(hover)
				{
					hover_style.apply();
				}
			}
		}	
		p.rect(x, y, w, h);
		
		// Draw the text label over the button
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
	
	boolean withinBounds(float x_input, float y_input)
	{
		return Utilities.withinBounds(x, y, w, h, x_input, y_input);
	}
	
	MouseEvent checkMouseEvent(MouseEvent e)
	{
		boolean within_bounds = withinBounds(e.getX(),e.getY());
		
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
	
	void setDefaultStyle(Style style)
	{
		default_style = style;
	}
	
	void setHoverStyle(Style style)
	{
		hover_style = style;
	}
	
	void setPressedStyle(Style style)
	{
		pressed_style = style;
	}


	
}
