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
	PVector pivot; // Pivot point x,y relative to the object's x,y center
	
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
	
	// Transforms
	PVector transform_offset;
	int transform_mode;
	float transform_rotate_last_angle;
	
	final static int NONE = 0;
	final static int MOVE = 1;
	final static int ROTATE = 2;
	final static int WIDTH_HEIGHT = 3;
	
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
		pivot = new PVector(0,0);
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
		if(transform_mode == ROTATE)
		{
			drawRotationGizmo();
		}
	}
	
	//=======//
	// SETUP //
	//=======//
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
	
	//=====================//
	// DRAWING / RENDERING //
	//=====================//
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
	
	public void drawBoundingBox()
	{
		p.pushMatrix();
		
		p.translate(stage.x, stage.y);
		p.translate(x, y);
		p.rotate(p.radians(rotation));
		p.translate(pivot.x, pivot.y);
		
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
			p.strokeWeight(5);
			p.stroke(255, 150);
			p.rect(0, 0, w, h);
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
	
	public void drawRotationGizmo()
	{
		p.noFill();
		p.stroke(0);
		p.strokeWeight(2);
		p.line(p.mouseX, p.mouseY, stage.x+x, stage.y+y);
		p.ellipse(p.mouseX, p.mouseY, 10, 10);
	}
	
	//========================================//
	// COLLISION AND BOUNDING BOX CALCULATION //
	//========================================//
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
	
	public void calculateBoundingPoints()
	{
		// Top left
		bounding_points[0] = new PVector(-w/2, -h/2); 
		bounding_points[0] = bounding_points[0].add(pivot.x, pivot.y);
		bounding_points[0] = bounding_points[0].rotate(p.radians(rotation));
		bounding_points[0] = bounding_points[0].add(stage.x + x, stage.y + y);
		//p.ellipse(bounding_points[0].x, bounding_points[0].y, 5, 5);
		// Bottom left
		bounding_points[1] = new PVector(-w/2, h/2); 
		bounding_points[1] = bounding_points[1].add(pivot.x, pivot.y);
		bounding_points[1] = bounding_points[1].rotate(p.radians(rotation));
		bounding_points[1] = bounding_points[1].add(stage.x + x, stage.y + y);
		//p.ellipse(bounding_points[1].x, bounding_points[1].y, 5, 5);
		// Bottom right
		bounding_points[2] = new PVector(w/2, h/2); 
		bounding_points[2] = bounding_points[2].add(pivot.x, pivot.y);
		bounding_points[2] = bounding_points[2].rotate(p.radians(rotation));
		bounding_points[2] = bounding_points[2].add(stage.x + x, stage.y + y);
		//p.ellipse(bounding_points[2].x, bounding_points[2].y, 5, 5);
		// Top right
		bounding_points[3] = new PVector(w/2, -h/2); 
		bounding_points[3] = bounding_points[3].add(pivot.x, pivot.y);
		bounding_points[3] = bounding_points[3].rotate(p.radians(rotation));
		bounding_points[3] = bounding_points[3].add(stage.x + x, stage.y + y);
		//p.ellipse(bounding_points[3].x, bounding_points[3].y, 5, 5);
	}

	//================//
	// EVENT HANDLING //
	//================//
	public void checkMouseEvent(MouseEvent e)
	{
		boolean handles_mouse_event_state = false;
		boolean within_bounds = withinBounds(e.getX(), e.getY());
		
		// If the primitive is already selected, pass the mouse event to the handles first
		if(selected)
		{
			handles_mouse_event_state = checkMouseEventHandles(e);
		}
		
		// If the handles do not register any mouse events, continue to pass the mouse event to the primitive proper
		if(!handles_mouse_event_state)
		{
			if(e.getAction() == 1) // When mouse is pressed (down)
			{
				if(within_bounds) 
				{
					selected = true;
				}
				else if(!within_bounds)
				{
					selected = false;
				}
			}
			else if(e.getAction() == 2) // When mouse is released
			{
				// If translate mode was started, end it
				endTranslate(e.getX(), e.getY());
			}
			else if(e.getAction() == 3) // When mouse is clicked (down then up)
			{
			}
			else if(e.getAction() == 4) // When mouse is dragged
			{
				if(within_bounds)
				{
					hover = true;
				}
				
				if(selected)
				{
					if(e.getButton() == 37)
					{
						doTranslate(e.getX(), e.getY());
					}	
				}
			}
			else if(e.getAction() == 5) // When mouse is moved
			{
				if(within_bounds) {hover = true;}
				else {hover = false;}
			}
		}
			// Regardless of state of handles, pass these mouse events
			//
			// ...
			//
	}
	
	public boolean checkMouseEventHandles(MouseEvent e)
	{
		// Returns true if any of the handles registers a mouse event within its bounds
		boolean mouse_state = false;
		
		if(wh_top_left.checkMouseEvent(e))
		{
			mouse_state = true;
		}
		if(wh_bottom_left.checkMouseEvent(e))
		{
			mouse_state = true;
		}
		if(wh_bottom_right.checkMouseEvent(e))
		{
			mouse_state = true;
		}
		if(wh_top_right.checkMouseEvent(e))
		{
			mouse_state = true;
		}
		if(rt_top_left.checkMouseEvent(e))
		{
			mouse_state = true;
		}
		if(rt_bottom_left.checkMouseEvent(e))
		{
			mouse_state = true;
		}
		if(rt_bottom_right.checkMouseEvent(e))
		{
			mouse_state = true;
		}
		if(rt_top_right.checkMouseEvent(e))
		{
			mouse_state = true;
		}
		return mouse_state;
	}
	
	//====================//
	// TRANSFORM HANDLING //
	//====================//
	public void doTranslate(float x_input, float y_input)
	{ // Does translation of primitive based on the position of x_start & y_start
		if(transform_mode == NONE) // If translate has not been started, initialise it
		{
			transform_offset  = new PVector(x_input-x, y_input-y);
			transform_mode    = MOVE;
			
			p.println("Started translate");
		}
		if(transform_mode == MOVE)
		{
			x = x_input - transform_offset.x;
			y = y_input - transform_offset.y;
		}
	}
	
	public void endTranslate(float x_input, float y_input)
	{ // Ends translation of primitive
		if(transform_mode == MOVE)
		{
			transform_mode = NONE;
		}
	}
	
	public void doRotate(float x_input, float y_input)
	{
		if(transform_mode == NONE)
		{
			transform_offset  = new PVector(x_input-x, y_input-y);
			transform_mode    = ROTATE;
			
			transform_rotate_last_angle = p.degrees(p.atan2(x_input-x-stage.x, y_input-y-stage.y));
			
			//p.println("Started rotate at " + transform_rotate_last_angle);
		}
		if(transform_mode == ROTATE)
		{
			transform_offset  = new PVector(x_input-x, y_input-y);
			float current_transform_angle    = p.degrees(p.atan2(x_input-x-stage.x, y_input-y-stage.y));			
			
			if(current_transform_angle > 0 && transform_rotate_last_angle < 0)
			{ 	// If the mouse moves from -180 to +180
				transform_rotate_last_angle = 180+(180-(-1*transform_rotate_last_angle));
				//p.println("Moved between -/+ " + transform_rotate_last_angle);
			}
			else if(current_transform_angle < 0 && transform_rotate_last_angle > 0)
			{	// If the mouse moves from +180 to -180
				transform_rotate_last_angle = -1*(180+(180-transform_rotate_last_angle));
				//p.println("Moved between +/- " + transform_rotate_last_angle);
			}
			
			float transform_angle_difference = current_transform_angle-transform_rotate_last_angle;
			rotation -= transform_angle_difference;
			
			//p.println("Last Angle " + transform_rotate_last_angle);
			//p.println("Transform Difference " + transform_angle_difference);
			//p.println("Current Primitive Angle " + rotation);
			
			transform_rotate_last_angle = current_transform_angle;
		}
	}
	
	public void endRotate(float x_input, float y_input)
	{
		if(transform_mode == ROTATE)
		{
			transform_mode = NONE;
		}
	}
	
	public void doWidthHeight(float x_input, float y_input)
	{ // Does translation of primitive based on the position of x_start & y_start
		if(transform_mode == NONE) // If translate has not been started, initialise it
		{
			transform_offset  = new PVector(x_input-x, y_input-y);
			transform_mode    = WIDTH_HEIGHT;
			
			p.println("Started width height");
		}
		if(transform_mode == WIDTH_HEIGHT)
		{
		}
	}
	
	public void endWidthHeight(float x_input, float y_input)
	{ // Ends translation of primitive
		if(transform_mode == WIDTH_HEIGHT)
		{
			transform_mode = NONE;
		}
	}
	
	//=========//
	// EDITING //
	//=========//
	public void setPivot(float x, float y)
	{
		pivot.x = x;
		pivot.y = y;
	}
	
	//========================//
	// PRIMITIVE HANDLE CLASS //
	//========================//
	class Handle
	{
		final static int WIDTH_HEIGHT = 0;
		final static int ROTATION     = 1;
		
		final static int TOP_LEFT     = 0;
		final static int TOP_RIGHT    = 1;
		final static int BOTTOM_LEFT  = 2;
		final static int BOTTOM_RIGHT = 3;
		
		Style rotation_style_default;
		Style rotation_style_selected;
		Style width_height_style_default;
		Style width_height_style_selected;
		
		boolean selected, hover;
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
			this.hover = false;
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
			
			rotation_style_selected = new Style(p);
			rotation_style_selected.noFill();
			rotation_style_selected.stroke(0, 0, 0, 200);
			rotation_style_selected.strokeWeight(2);
			
			width_height_style_selected = new Style(p);
			width_height_style_selected.fill(0, 0, 0, 200);
			width_height_style_selected.noStroke();
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
			if(hover || selected)
			{
				rotation_style_selected.apply();
			}
			else
			{
				rotation_style_default.apply();
			}
			p.ellipse(handle_center.x, handle_center.y, rotation_handle_size, rotation_handle_size);
			p.popMatrix();
		}
		
		void drawWidthHeightHandles()
		{
			p.pushMatrix();
			if(hover || selected)
			{
				width_height_style_selected.apply();
			}
			else
			{
				width_height_style_default.apply();
			}
			p.rectMode(p.CENTER);
			p.translate(handle_center.x, handle_center.y);
			p.rotate(p.radians(rotation));
			p.rect(0, 0, width_height_handle_size, width_height_handle_size);
			p.rectMode(p.CORNER);
			p.popMatrix();
		}
	
		boolean checkMouseEvent(MouseEvent e)
		{
			boolean within_bounds = withinBounds(e.getX(), e.getY());
			boolean mouse_state   = false;
			
			//if(within_bounds)
			//{
			//	mouse_state = true;
			//}
			
			if(e.getAction() == 1) // When mouse is pressed (down)
			{
				if(within_bounds) 
				{
					selected = true;
					mouse_state = true;
				}
				else 
				{
					selected = false;
				}
				
			}
			else if(e.getAction() == 2) // When mouse is released
			{
				selected = false;	
				endRotate(e.getX(), e.getY());
				endWidthHeight(e.getX(), e.getY());
			}
			else if(e.getAction() == 3) // When mouse is clicked (down then up)
			{
			}
			else if(e.getAction() == 4) // When mouse is dragged
			{
				if(selected)
				{
					if(handle_type == this.ROTATION)
					{
						doRotate(e.getX(), e.getY());
					}
					else if(handle_type == this.WIDTH_HEIGHT)
					{
						doWidthHeight(e.getX(), e.getY());
					}
					mouse_state = true;
				}
			}
			else if(e.getAction() == 5) // When mouse is moved
			{
				if(within_bounds) {hover = true;}
				else {hover = false;}
			}
			
			// If there was at least on mouse event that registered, return true
			return mouse_state;
		}
		
		boolean withinBounds(float input_x, float input_y)
		{
			if(handle_center != null) // Make sure that handle_center has been calculated
			{
				if(handle_type == ROTATION)
				{
					if(p.dist(handle_center.x, handle_center.y, input_x, input_y) < rotation_handle_size)
					{
						return true;
					}
				}
				else if(handle_type == WIDTH_HEIGHT)
				{
					if(p.dist(handle_center.x, handle_center.y, input_x, input_y) < width_height_handle_size)
					{
						return true;
					}
				}
			}
			
			return false;
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
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(bounding_points[0].x, bounding_points[0].y);
				}
				else if(handle_postion == BOTTOM_LEFT)
				{
					handle_center = handle_center.add(-width_height_handle_size, width_height_handle_size);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(bounding_points[1].x, bounding_points[1].y);
				}
				else if(handle_postion == BOTTOM_RIGHT)
				{
					handle_center = handle_center.add(width_height_handle_size, width_height_handle_size);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(bounding_points[2].x, bounding_points[2].y);
				}
				else if(handle_postion == TOP_RIGHT)
				{
					handle_center = handle_center.add(width_height_handle_size, -width_height_handle_size);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(bounding_points[3].x, bounding_points[3].y);
				}
			}
			else if(handle_type == WIDTH_HEIGHT)
			{
				handle_center = new PVector();
				
				if(handle_postion == TOP_LEFT)
				{
					handle_center = handle_center.add(width_height_handle_size/2, width_height_handle_size/2);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(bounding_points[0].x, bounding_points[0].y);
				}
				else if(handle_postion == BOTTOM_LEFT)
				{
					handle_center = handle_center.add(width_height_handle_size/2, -width_height_handle_size/2);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(bounding_points[1].x, bounding_points[1].y);
				}
				else if(handle_postion == BOTTOM_RIGHT)
				{
					handle_center = handle_center.add(-width_height_handle_size/2, -width_height_handle_size/2);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(bounding_points[2].x, bounding_points[2].y);
				}
				else if(handle_postion == TOP_RIGHT)
				{
					handle_center = handle_center.add(-width_height_handle_size/2, width_height_handle_size/2);
					handle_center = handle_center.rotate(p.radians(rotation));
					handle_center = handle_center.add(bounding_points[3].x, bounding_points[3].y);
				}
			}
		}
	} 

}
