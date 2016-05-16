import processing.event.MouseEvent;
import processing.core.*;

public class Element 
{
	// Generic interface element
	// Has very basic parameters
	// Handles simple mouse detection
	
	boolean hover,pressed;
	int x,y,w,h;
	
	AniSketch p;
	
	int x_offset;
	int y_offset;
	
	Element(int x, int y, int w, int h, AniSketch p)
	{	
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.p = p;
		
		this.hover = false;
		this.pressed = false;
		
		int x_offset = 0;
		int y_offset = 0;
	}
	
	void debug()
	{
		drawBoundingBox();
	}
	
	void checkMouseEvent(MouseEvent e)
	{
		boolean within_bounds = withinBounds(e.getX(),e.getY());
		
		if(e.getAction() == 1) // When mouse is pressed (down)
		{
			if(within_bounds)
			{pressed = true;}
			else 
			{pressed = false;}
		}
		else if(e.getAction() == 2) // When mouse is released
		{
			pressed = false;
		}
		else if(e.getAction() == 5) // When mouse is moved
		{
			if(within_bounds)
			{hover = true;}
			else
			{hover = false;}
		}
		mouseInputResponse(e);
	}
	
	void mouseInputResponse(MouseEvent e)
	{
		// This method serves as a mouse event "callback" for any class that extends "Element".
	}
	
	boolean withinBounds(int x_input, int y_input)
	{
		// Returns true if input x/y is inside element's bounding box
		if(x_input > x+x_offset && x_input < x+w+x_offset)
		{
			if(y_input > y+y_offset && y_input < y+h+y_offset)
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

