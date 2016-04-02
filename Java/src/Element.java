import processing.event.MouseEvent;
import processing.core.*;

public class Element 
{
	
	// Generic interface element
	// Has very basic parameters
	// Handles simple mouse detection
	
	boolean hover,pressed;
	int x,y,w,h;
	
	PApplet p;
	Object parent;
	
	int x_offset;
	int y_offset;
	
	Element(int x, int y, int w, int h, PApplet p)
	{	
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.p = p;
		
		this.hover = false;
		this.pressed = false;
		this.parent = parent;
		
		int x_offset = 0;
		int y_offset = 0;
	}
	
	void debug()
	{
		drawBoundingBox();
	}
	
	void checkMouseEvent(MouseEvent e)
	{
		boolean inBounds = withinBounds(e.getX(),e.getY());
		
		if(e.getAction() == 1)
		{
			if(inBounds)
			{pressed = true;}
			else 
			{pressed = false;}
		}
		else if(e.getAction() == 2)
		{
			pressed = false;
		}
		else if(e.getAction() == 5)
		{
			if(inBounds)
			{hover = true;}
			else
			{hover = false;}
		}
		mouseInputResponse();
	}
	
	void mouseInputResponse()
	{
		// This method serves as a mouse event "callback" for any class that extends "Element".
	}
	
	boolean withinBounds(int input_x, int input_y)
	{
		// Returns true if input x/y is inside element's bounding box
		
		
		if(input_x > x+x_offset && input_x < x+w+x_offset)
		{
			if(input_y > y+y_offset && input_y < y+h+y_offset)
			{
				return true;
			}
		}
		return false;
	}
	
	void drawBoundingBox()
	{
		//updateResponsive();
		if(pressed)
		{
			pressStyle();
		} else if(hover)
		{
			hoverStyle();
		} else
		{
			normalStyle();
		}
		
		p.rectMode(p.CORNER);
		p.rect(x+x_offset, y+y_offset, w, h);

	}
	
	void normalStyle()
	{
		p.noFill();
		p.stroke(0);
		p.strokeWeight(1);
	}
	
	void hoverStyle()
	{
		p.noFill();
		p.stroke(255);
		p.strokeWeight(2);
	}
	
	void pressStyle()
	{
		p.noFill();
		p.stroke(255,0,0);
		p.strokeWeight(2);
	}
	
}

