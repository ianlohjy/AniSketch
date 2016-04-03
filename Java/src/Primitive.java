import processing.core.*;
import processing.event.MouseEvent;

public class Primitive
{	// Primitive represents an animatable object within AniSketch
	PApplet p;
	Stage stage;
	SpriteLibrary.Sprite sprite;
	
	float x, y; // Centered x,y
	float w, h;
	float x_offset, y_offset;
	float rotation;
	float pivot[]; // Pivot point x,y relative to the object's x,y center
	
	PVector[] bounding_points; // The calculated "live" position of the 4 bounding points that make up the primitive.
	
	Primitive parent;
	
	Handle wh_top_left;
	Handle wh_bottom_left;
	Handle wh_bottom_right;
	Handle wh_top_right;
	Handle rt_top_left;
	Handle rt_bottom_left;
	Handle rt_bottom_right;
	Handle rt_top_right;
	
	boolean pressed, hover, selected;
	boolean handles_enabled;
	
	PVector transform_start_point;
	PVector transform_offset;
	boolean transform_started;
	int transform_mode;
	final static int NONE = 0;
	final static int MOVE = 1;
	final static int ROTATE = 2;
	final static int SCALE = 3;
	
	Primitive(float x, float y, float w, float h, Stage stage, PApplet p)
	{
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.stage = stage;
		this.p = p;
		this.selected = false;
		this.pressed = false;
		this.hover = false;
		bounding_points = new PVector[4];
		transform_started = false;
		setupHandles();
	}
	
	public void update()
	{
		calculateBoundingPoints();
		drawBoundingBox();
		drawPivot();
		if(selected)
		{
			drawHandles();
		}
	}
	
	public void setupHandles()
	{
		wh_top_left = new Handle(Handle.WIDTH_HEIGHT, Handle.TOP_LEFT);
		wh_bottom_left = new Handle(Handle.WIDTH_HEIGHT, Handle.BOTTOM_LEFT);
		wh_bottom_right = new Handle(Handle.WIDTH_HEIGHT, Handle.BOTTOM_RIGHT);
		wh_top_right = new Handle(Handle.WIDTH_HEIGHT, Handle.TOP_RIGHT);
		rt_top_left = new Handle(Handle.ROTATION, Handle.TOP_LEFT);
		rt_bottom_left = new Handle(Handle.ROTATION, Handle.BOTTOM_LEFT);
		rt_bottom_right = new Handle(Handle.ROTATION, Handle.BOTTOM_RIGHT);
		rt_top_right = new Handle(Handle.ROTATION, Handle.TOP_RIGHT);
	}
	
	public void drawHandles()
	{
		wh_top_left.drawHandle();
		wh_bottom_left.drawHandle();
		wh_bottom_right.drawHandle();
		wh_top_right.drawHandle();
		rt_top_left.drawHandle();
		rt_bottom_left.drawHandle();
		rt_bottom_right.drawHandle();
		rt_top_right.drawHandle();
	}
	
	public void calculateBoundingPoints()
	{
		// Top left
		bounding_points[0] = new PVector(-w/2, -h/2); 
		bounding_points[0] = bounding_points[0].rotate(p.radians(rotation));
		bounding_points[0] = bounding_points[0].add(stage.x + x, stage.y + y);
		// Bottom left
		bounding_points[1] = new PVector(-w/2, h/2); 
		bounding_points[1] = bounding_points[1].rotate(p.radians(rotation));
		bounding_points[1] = bounding_points[1].add(stage.x + x, stage.y + y);
		// Bottom right
		bounding_points[2] = new PVector(w/2, h/2); 
		bounding_points[2] = bounding_points[2].rotate(p.radians(rotation));
		bounding_points[2] = bounding_points[2].add(stage.x + x, stage.y + y);
		// Top right
		bounding_points[3] = new PVector(w/2, -h/2); 
		bounding_points[3] = bounding_points[3].rotate(p.radians(rotation));
		bounding_points[3] = bounding_points[3].add(stage.x + x, stage.y + y);
	}
	
	public void drawBoundingBox()
	{
		p.pushMatrix();
		
		p.translate(stage.x, stage.y);
		p.translate(x, y);
		p.rotate(p.radians(rotation));
		
		p.rectMode(p.CENTER);
		p.noFill();
		if(hover)
		{
			p.strokeWeight(5);
			p.stroke(255, 150);
			p.rect(0, 0, w, h);
			
			p.stroke(0, 150);
		}
		else
		{
			p.stroke(0, 50);
		}
		
		if(selected)
		{
			p.stroke(0, 255);
		}
		
		p.strokeWeight(1);
		p.rect(0, 0, w, h);
		p.rectMode(p.CORNER);
		
		p.popMatrix();
	}
	
	public void drawPivot()
	{
		p.pushMatrix();
		p.translate(stage.x + x, stage.y + y);
		//p.rotate(p.radians(rotation));
		
		p.noFill();
		p.stroke(0);
		p.strokeWeight(1);
		p.line(-5, -5, 5, 5);
		p.line(-5, 5, 5, -5);
		
		p.popMatrix();
	}
	
	public boolean isPointLeftOfLine(PVector a, PVector b, float input_x, float input_y)
	{
		return ((b.x - a.x)*(input_y - a.y) - (b.y - a.y)*(input_x - a.x)) > 0;
	}
	
	public boolean withinBounds(float input_x, float input_y)
	{
		if(!isPointLeftOfLine(bounding_points[0], bounding_points[1], input_x, input_y))
		{
			if(!isPointLeftOfLine(bounding_points[1], bounding_points[2], input_x, input_y))
			{
				if(!isPointLeftOfLine(bounding_points[2], bounding_points[3], input_x, input_y))
				{
					if(!isPointLeftOfLine(bounding_points[3], bounding_points[0], input_x, input_y))
					{
						return true;
					}
				}
				return false;
			}
			return false;
		}
		else
		{
			return false;
		}
	}
	
	public void checkMouseEvent(MouseEvent e)
	{
		if(e.getButton() == 37)
		{ // If its a left click
			if(e.getAction() == 1)
			{
				// Mouse Pressed
				if(withinBounds(e.getX(), e.getY()))
				{
					selected = true;
				}
				else
				{
					selected = false;
				}
			}
			else if(e.getAction() == 4)
			{
				// Mouse Dragged
				if(selected)
				{
					if(!transform_started)
					{
						p.println("MOVING");
						transform_started = true;
						transform_offset  = new PVector(e.getX() - x, e.getY() - y);
						p.println(transform_offset);
					}
					if(transform_started)
					{
						x = e.getX() - transform_offset.x;
						y = e.getY() - transform_offset.y;
					}
				}
			}
			else if(e.getAction() == 2)
			{
				// Mouse Released
				if(transform_started)
				{
					transform_started = false;
				}
			}
		}
		else if(e.getAction() == 5)
		{
			// Mouse Moved
			if(withinBounds(e.getX(), e.getY()))
			{
				hover = true;
			}
			else
			{
				hover = false;
			}
		}
	}
	
	public void transformTranslate()
	{
		
	}
	
	public void startTransform(int mode)
	{
		
	}
	
	public void endTransform()
	{
		
	}
	
	public void transformRotate()
	{
		
	}
	
	public void transformScale()
	{
		
	}
	
	// Handle class
	class Handle
	{
		final static int WIDTH_HEIGHT = 0;
		final static int ROTATION     = 1;
		
		final static int TOP_LEFT     = 0;
		final static int TOP_RIGHT    = 1;
		final static int BOTTOM_LEFT  = 2;
		final static int BOTTOM_RIGHT = 3;
		
		Style rotation_style_default;
		Style rotation_style_hover;
		Style width_height_style_default;
		Style width_height_style_hover;
		
		boolean selected;
		int handle_postion;
		int handle_type;
		int width_height_handle_size = 10;
		int rotation_handle_size = 10;
		
		PVector handle_center;
		
		
		Handle(int handle_type, int handle_position)
		{
			this.handle_postion = handle_position;
			this.handle_type = handle_type;
			this.selected = false;
			setupStyles();
		}
		
		// Drawing and rendering
		void setupStyles()
		{
			rotation_style_default = new Style(p);
			rotation_style_default.noFill();
			rotation_style_default.stroke(0, 0, 0, 50);
			rotation_style_default.strokeWeight(2);
			
			width_height_style_default = new Style(p);
			width_height_style_default.fill(0, 0, 0, 50);
			width_height_style_default.noStroke();
		}
		
		void drawHandle()
		{
			updateHandlePosition();
			if(handle_type == WIDTH_HEIGHT)
			{
				drawWidthHeightHandles();
			} 
			else if(handle_type == ROTATION)
			{
				drawRotationHandles();
			}
		}
		
		void drawRotationHandles()
		{
			p.pushMatrix();
			rotation_style_default.apply();

			if(handle_postion == TOP_LEFT)
			{
				p.translate(bounding_points[0].x, bounding_points[0].y);
				p.rotate(p.radians(rotation));
				p.ellipse(-rotation_handle_size, -rotation_handle_size, rotation_handle_size, rotation_handle_size);
			}
			else if(handle_postion == BOTTOM_LEFT)
			{
				p.translate(bounding_points[1].x, bounding_points[1].y);
				p.rotate(p.radians(rotation));
				p.ellipse(-rotation_handle_size, rotation_handle_size, rotation_handle_size, rotation_handle_size);
			}
			else if(handle_postion == BOTTOM_RIGHT)
			{
				p.translate(bounding_points[2].x, bounding_points[2].y);
				p.rotate(p.radians(rotation));
				p.ellipse(rotation_handle_size, rotation_handle_size, rotation_handle_size, rotation_handle_size);
			}
			else if(handle_postion == TOP_RIGHT)
			{
				p.translate(bounding_points[3].x, bounding_points[3].y);
				p.rotate(p.radians(rotation));
				p.ellipse(rotation_handle_size, -rotation_handle_size, rotation_handle_size, rotation_handle_size);
			}
			p.popMatrix();
		}
		
		void drawWidthHeightHandles()
		{
			p.pushMatrix();
			p.rectMode(p.CENTER);
			width_height_style_default.apply();

			if(handle_postion == TOP_LEFT)
			{
				p.translate(bounding_points[0].x, bounding_points[0].y);
				p.rotate(p.radians(rotation));
				p.rect(width_height_handle_size/2, width_height_handle_size/2, width_height_handle_size, width_height_handle_size);
			}
			else if(handle_postion == BOTTOM_LEFT)
			{
				p.translate(bounding_points[1].x, bounding_points[1].y);
				p.rotate(p.radians(rotation));
				p.rect(width_height_handle_size/2, -width_height_handle_size/2, width_height_handle_size, width_height_handle_size);
			}
			else if(handle_postion == BOTTOM_RIGHT)
			{
				p.translate(bounding_points[2].x, bounding_points[2].y);
				p.rotate(p.radians(rotation));
				p.rect(-width_height_handle_size/2, -width_height_handle_size/2, width_height_handle_size, width_height_handle_size);
			}
			else if(handle_postion == TOP_RIGHT)
			{
				p.translate(bounding_points[3].x, bounding_points[3].y);
				p.rotate(p.radians(rotation));
				p.rect(-width_height_handle_size/2, width_height_handle_size/2, width_height_handle_size, width_height_handle_size);
			}
			p.rectMode(p.CORNER);
			p.popMatrix();
		}

		void checkMouseEvent()
		{
			
		}
		
		void updateHandlePosition()
		{
			// Updates the handle's position for drawing and mouse event checking
			if(handle_type == ROTATION)
			{
				handle_center = new PVector();
				
				if(handle_postion == TOP_LEFT)
				{
					handle_center = handle_center.add(-width_height_handle_size, -width_height_handle_size);
					handle_center = handle_center.add(-w/2, -h/2);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(x+stage.x, y+stage.y);
				}
				else if(handle_postion == BOTTOM_LEFT)
				{
					handle_center = handle_center.add(-width_height_handle_size, width_height_handle_size);
					handle_center = handle_center.add(-w/2, h/2);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(x+stage.x, y+stage.y);
				}
				else if(handle_postion == BOTTOM_RIGHT)
				{
					handle_center = handle_center.add(width_height_handle_size, width_height_handle_size);
					handle_center = handle_center.add(w/2, h/2);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(x+stage.x, y+stage.y);
				}
				else if(handle_postion == TOP_RIGHT)
				{
					handle_center = handle_center.add(width_height_handle_size, -width_height_handle_size);
					handle_center = handle_center.add(w/2, -h/2);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(x+stage.x, y+stage.y);
				}
			}
			else if(handle_type == WIDTH_HEIGHT)
			{
				handle_center = new PVector();
				
				if(handle_postion == TOP_LEFT)
				{
					handle_center = handle_center.add(width_height_handle_size/2, width_height_handle_size/2);
					handle_center = handle_center.add(-w/2, -h/2);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(x+stage.x, y+stage.y);
				}
				else if(handle_postion == BOTTOM_LEFT)
				{
					handle_center = handle_center.add(width_height_handle_size/2, -width_height_handle_size/2);
					handle_center = handle_center.add(-w/2, h/2);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(x+stage.x, y+stage.y);
				}
				else if(handle_postion == BOTTOM_RIGHT)
				{
					handle_center = handle_center.add(-width_height_handle_size/2, -width_height_handle_size/2);
					handle_center = handle_center.add(w/2, h/2);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(x+stage.x, y+stage.y);
				}
				else if(handle_postion == TOP_RIGHT)
				{
					handle_center = handle_center.add(-width_height_handle_size/2, width_height_handle_size/2);
					handle_center = handle_center.add(w/2, -h/2);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(x+stage.x, y+stage.y);
				}
			}
			p.fill(255,0,0,50);
			p.ellipse(handle_center.x, handle_center.y, width_height_handle_size+2, width_height_handle_size+2);
		}
		
	}
	
}
