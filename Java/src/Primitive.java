import processing.core.*;
import processing.event.MouseEvent;

public class Primitive
{	// Primitive represents an animatable object within AniSketch
	AniSketch p;
	Stage stage;
	SpriteLibrary.Sprite sprite;
	
	float x, y; // Centered x,y
	float w, h;
	float rotation;
	PVector pivot; // Pivot point x,y relative to the object's x,y center
	PVector pivot_offset; // Offset required to move the object to give the appearance that the pivot's center point is moving, instead of the object itself. Expressed in cartesian coords.
	// PVector position_offset; // General position offset parameter. Currently used for position correction after setting a new pivot.
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
	
	// Transform parameters and flags
	PVector transform_offset; // General parameter for holding initial data from further transforms
	PVector transform_init_wh;
	int transform_mode;
	float transform_rotate_last_angle;
	PVector local_position_offset; // Position offset, local to the rotation
	
	final static int NONE = 0;
	final static int MOVE = 1;
	final static int ROTATE = 2;
	final static int WIDTH_HEIGHT = 3;
	
	// Parenting and attachment values
	PVector parent_pos_offset; 
	PVector parent_init_pos; // The x/y position when object was parented
	
	Primitive(float x, float y, float w, float h, Stage stage, AniSketch p)
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
		pivot_offset = new PVector(0,0);
		setupHandles();
		local_position_offset = new PVector();
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
		
		// Draw the primitive's x,y position without offsets
		if(parent == null)
		{
			p.ellipse(stage.x+x, stage.y+y, 5, 5);
		}
		else
		{
			p.ellipse(stage.x+x-parent_init_pos.x+parent_pos_offset.x+parent.x, stage.y+y-parent_init_pos.y+parent_pos_offset.y+parent.y, 5, 5);
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
		
		if(parent != null)
		{
			//p.translate(parent_init_pos.x+parent.x, parent_init_pos.y+parent.y);
			p.translate(-parent_init_pos.x, -parent_init_pos.y);
			p.translate(parent.x+parent_pos_offset.x, parent.y+parent_pos_offset.y);
			//p.println("PARENT XY " + parent.x, parent.y);
			//p.println("PARENT OFFSET " + parent_pos_offset.x, parent_pos_offset.y);
			//p.translate(parent.x + parent_pos_offset.x, parent.y + parent_pos_offset.y);
			//p.rotate(parent.rotation);
			//p.rotate(p.radians(parent.rotation));
		}
		p.translate(stage.x, stage.y);
		p.translate(x, y);
		p.translate(-pivot_offset.x, -pivot_offset.y);
		
		p.rotate(PApplet.radians(rotation));
		if(parent != null)
		{
			p.rotate(p.radians(parent.rotation));
		}
		
		p.translate(pivot.x, pivot.y);
		p.translate(local_position_offset.x, local_position_offset.y);
		
		p.rectMode(PApplet.CENTER);
		p.noFill();
		if(hover)
		{
			p.strokeWeight(5);
			p.stroke(255, 100);
			p.rect(0, 0, w, h);
			
			p.stroke(0, 150);
		}
		else
		{
			p.stroke(0, 150);
		}
		
		if(selected)
		{
			p.strokeWeight(5);
			p.stroke(255, 200);
			p.rect(0, 0, w, h);
			p.stroke(0, 255);
		}
		
		p.strokeWeight(1);
		p.rect(0, 0, w, h);
		p.rectMode(PApplet.CORNER);
		
		p.popMatrix();
	}
	
	public void drawPivot()
	{
		p.pushMatrix();
		p.translate(stage.x + x, stage.y + y);
		p.translate(-pivot_offset.x, -pivot_offset.y);
		
		if(parent != null) 
		{ 
			p.translate(-parent_init_pos.x, -parent_init_pos.y);
			p.translate(parent.x+parent_pos_offset.x, parent.y+parent_pos_offset.y);
		}
		
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
		
		float pivot_x = stage.x+x-pivot_offset.x;
		float pivot_y = stage.y+y-pivot_offset.y;
		
		if(parent != null) 
		{ 
			//p.translate(-parent_init_pos.x, -parent_init_pos.y);
			//p.translate(parent.x+parent_pos_offset.x, parent.y+parent_pos_offset.y);
			pivot_x += -parent_init_pos.x + parent.x + parent_pos_offset.x;
			pivot_y += -parent_init_pos.y + parent.y + parent_pos_offset.y;
		}
		
		p.line(p.mouseX, p.mouseY, pivot_x, pivot_y);
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
		bounding_points[0] = bounding_points[0].add(local_position_offset.x, local_position_offset.y);
		bounding_points[0] = bounding_points[0].rotate(PApplet.radians(rotation));
		bounding_points[0] = bounding_points[0].add(-pivot_offset.x, -pivot_offset.y);
		bounding_points[0] = bounding_points[0].add(stage.x + x, stage.y + y);
		if(parent != null) 
		{ 
			bounding_points[0] = bounding_points[0].add(-parent_init_pos.x, -parent_init_pos.y);
			bounding_points[0] = bounding_points[0].add(parent.x+parent_pos_offset.x, parent.y+parent_pos_offset.y);
		}
		//p.ellipse(bounding_points[0].x, bounding_points[0].y, 5, 5);
		// Bottom left
		bounding_points[1] = new PVector(-w/2, h/2); 
		bounding_points[1] = bounding_points[1].add(pivot.x, pivot.y);
		bounding_points[1] = bounding_points[1].add(local_position_offset.x, local_position_offset.y);
		bounding_points[1] = bounding_points[1].rotate(PApplet.radians(rotation));
		bounding_points[1] = bounding_points[1].add(-pivot_offset.x, -pivot_offset.y);
		bounding_points[1] = bounding_points[1].add(stage.x + x, stage.y + y);
		if(parent != null) 
		{ 
			bounding_points[1] = bounding_points[1].add(-parent_init_pos.x, -parent_init_pos.y);
			bounding_points[1] = bounding_points[1].add(parent.x+parent_pos_offset.x, parent.y+parent_pos_offset.y);
		}
		//p.ellipse(bounding_points[1].x, bounding_points[1].y, 5, 5);
		// Bottom right
		bounding_points[2] = new PVector(w/2, h/2); 
		bounding_points[2] = bounding_points[2].add(pivot.x, pivot.y);
		bounding_points[2] = bounding_points[2].add(local_position_offset.x, local_position_offset.y);
		bounding_points[2] = bounding_points[2].rotate(PApplet.radians(rotation));
		bounding_points[2] = bounding_points[2].add(-pivot_offset.x, -pivot_offset.y);
		bounding_points[2] = bounding_points[2].add(stage.x + x, stage.y + y);
		if(parent != null) 
		{ 
			bounding_points[2] = bounding_points[2].add(-parent_init_pos.x, -parent_init_pos.y);
			bounding_points[2] = bounding_points[2].add(parent.x+parent_pos_offset.x, parent.y+parent_pos_offset.y);
		}
		//p.ellipse(bounding_points[2].x, bounding_points[2].y, 5, 5);
		// Top right
		bounding_points[3] = new PVector(w/2, -h/2); 
		bounding_points[3] = bounding_points[3].add(pivot.x, pivot.y);
		bounding_points[3] = bounding_points[3].add(local_position_offset.x, local_position_offset.y);
		bounding_points[3] = bounding_points[3].rotate(PApplet.radians(rotation));
		bounding_points[3] = bounding_points[3].add(-pivot_offset.x, -pivot_offset.y);
		bounding_points[3] = bounding_points[3].add(stage.x + x, stage.y + y);
		if(parent != null) 
		{ 
			bounding_points[3] = bounding_points[3].add(-parent_init_pos.x, -parent_init_pos.y);
			bounding_points[3] = bounding_points[3].add(parent.x+parent_pos_offset.x, parent.y+parent_pos_offset.y);
		}
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
				if(e.getButton() == 37)
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
				
				// REGISTER GESTURE EVENT //
				if(within_bounds && e.getButton() == 39 && selected)
				{
					p.gesture_handler.registerObject(this, e);
				} 
				else if(selected)
				{
					p.gesture_handler.registerObject(this, e);
				}
				////////////////////////////
				
			}
			else if(e.getAction() == 2) // When mouse is released
			{
				// If translate mode was started, end it
				endTranslate(e.getX(), e.getY());
				
				// REGISTER GESTURE EVENT //
				if(within_bounds && e.getButton() == 39 && selected)
				{
					p.gesture_handler.registerObject(this, e);
				}
				else if(selected)
				{
					p.gesture_handler.registerObject(this, e);
				}
				////////////////////////////
			}
			else if(e.getAction() == 3) // When mouse is clicked (down then up)
			{
				/*
				if(selected)
				{
					PApplet.println("CLICKED");
					setPivotUsingGlobalPosition(e.getX(), e.getY());
				}
				*/

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
			
			PApplet.println("Started translate");
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
			transform_offset  = new PVector(x_input-x, y_input-y); // Unused & does not account for parenting
			transform_mode    = ROTATE;
			
			float transform_angle_x = x_input - (x + stage.x - pivot_offset.x);
			float transform_angle_y = y_input - (y + stage.y - pivot_offset.y);
			
			if(parent != null) 
			{
				transform_angle_x = x_input - (x-parent_init_pos.x + stage.x - pivot_offset.x + parent.x + parent_pos_offset.x);
				transform_angle_y = y_input - (y-parent_init_pos.y + stage.y - pivot_offset.y + parent.y + parent_pos_offset.y);
			}
			
			p.println("Angle Pos " + transform_angle_x + " " + transform_angle_y);
			
			transform_rotate_last_angle = PApplet.degrees(PApplet.atan2(transform_angle_x, transform_angle_y));
		}
		if(transform_mode == ROTATE)
		{
			transform_offset  = new PVector(x_input-x, y_input-y); // Unused & does not account for parenting
			
			float transform_angle_x = x_input-x-stage.x+pivot_offset.x;
			float transform_angle_y = y_input-y-stage.y+pivot_offset.y;
			
			if(parent != null) 
			{
				transform_angle_x = x_input - (x-parent_init_pos.x + stage.x - pivot_offset.x + parent.x + parent_pos_offset.x);
				transform_angle_y = y_input - (y-parent_init_pos.y + stage.y - pivot_offset.y + parent.y + parent_pos_offset.y);
			}

			float current_transform_angle    = PApplet.degrees(PApplet.atan2(transform_angle_x, transform_angle_y));		
			
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
	
	public void doWidthHeight(float x_input, float y_input, Handle handle)
	{ // Does translation of primitive based on the position of x_start & y_start
		// Needs to be cleaned up
		if(transform_mode == NONE) // If translate has not been started, initialise it
		{
			transform_init_wh = new PVector(w,h);
			transform_offset  = new PVector(handle.handle_center.x, handle.handle_center.y);
			transform_mode    = WIDTH_HEIGHT;
			
			PApplet.println("Started width height");
		}
		if(transform_mode == WIDTH_HEIGHT)
		{
			PVector transform_amount = new PVector(x_input-transform_offset.x, y_input-transform_offset.y);
			transform_amount = transform_amount.rotate(PApplet.radians(-rotation));
			PApplet.println("Transform Amount" + transform_amount);
			
			if(handle.handle_postion == Handle.TOP_LEFT)
			{
				setHeightTop(transform_init_wh.y - transform_amount.y);
				setHeightLeft(transform_init_wh.x - transform_amount.x);
			}
			if(handle.handle_postion == Handle.TOP_RIGHT)
				
			{
				setHeightTop(transform_init_wh.y - transform_amount.y);
				setHeightRight(transform_init_wh.x + transform_amount.x);
				
				//this.h -= transform_amount.y;
				//this.w += transform_amount.x;
			}
			if(handle.handle_postion == Handle.BOTTOM_RIGHT)
			{
				setHeightBottom(transform_init_wh.y + transform_amount.y);
				setHeightRight(transform_init_wh.x + transform_amount.x);
				
				//this.h += transform_amount.y;
				//this.w += transform_amount.x;
			}
			if(handle.handle_postion == Handle.BOTTOM_LEFT)
			{
				setHeightBottom(transform_init_wh.y + transform_amount.y);
				setHeightLeft(transform_init_wh.x - transform_amount.x);
				
				//this.h += transform_amount.y;
				//this.w -= transform_amount.x;
			}
			
			if(this.w < 30)
			{
				this.w = 30;
			}
			if(this.h < 30)
			{
				this.h = 30;
			}
			
			handle.updateHandlePosition();
			//transform_offset  = new PVector(handle.handle_center.x, handle.handle_center.y);
		}
	}
	
	public void endWidthHeight(float x_input, float y_input)
	{ // Ends translation of primitive
		if(transform_mode == WIDTH_HEIGHT)
		{
			transform_mode = NONE;
		}
	}
	
	public void setHeightTop(float amount)
	{
		if(amount < 30)
		{
			amount = 30;
		}
		
		float difference = this.h - amount;
		local_position_offset.y += difference/2;
		this.h = amount;
		p.println(amount);
		
	}
	
	public void setHeightLeft(float amount)
	{
		if(amount < 30)
		{
			amount = 30;
		}
		
		float difference = this.w - amount;
		local_position_offset.x += difference/2;
		this.w = amount;
		p.println(amount);
		
	}
	
	public void setHeightRight(float amount)
	{
		if(amount < 30)
		{
			amount = 30;
		}
		
		float difference = this.w - amount;
		local_position_offset.x -= difference/2;
		this.w = amount;
		p.println(amount);
		
	}
	
	public void setHeightBottom(float amount)
	{
		if(amount < 30)
		{
			amount = 30;
		}
		
		float difference = this.h - amount;
		local_position_offset.y -= difference/2;
		this.h = amount;
		p.println(amount);
	}

	/* Depreciated
	public void stretchTransformTop(float amount)
	{
		this.h += amount;
		local_position_offset.y -= (amount/2f);
	}
	
	public void stretchTransformBottom(float amount)
	{
		this.h += amount;
		local_position_offset.y += (amount/2f);
	}
	
	public void stretchTransformLeft(float amount)
	{
		this.w += amount;
		local_position_offset.x -= (amount/2f);
	}
	
	public void stretchTransformRight(float amount)
	{
		this.w += amount;
		local_position_offset.x += (amount/2f);
	}
	
	*/
	
	//=========//
	// EDITING //
	//=========//
	public void setPivot(float x_input, float y_input)
	{
		// Sets pivot relative to center of object
		// Check if new pivot values are the same, ignore changes if they are
		// Offset (ALL?, this seems to be what 3ds max does) x/y values to maintain offset

		if(x_input != pivot.x || y_input != pivot.y)
		{
			PVector offset_amount;
			//float x_pivot_difference = x_input-pivot.x;
			//float y_pivot_difference = y_input-pivot.y;
			
			pivot.x = x_input;
			pivot.y = y_input;
			
			//offset_amount = new PVector(x_pivot_difference, y_pivot_difference);
			offset_amount = new PVector(x_input, y_input);
			offset_amount = offset_amount.rotate(PApplet.radians(this.rotation));
			
			pivot_offset.x = offset_amount.x;
			pivot_offset.y = offset_amount.y;
		}
	}
	
	public void setPivotUsingGlobalPosition(float x_input, float y_input)	
	{	
		/*
		float transform_angle_x = x_input - (x + stage.x - pivot_offset.x);
		float transform_angle_y = y_input - (y + stage.y - pivot_offset.y);
		
		if(parent != null) 
		{
			transform_angle_x = x_input - (x-parent_init_pos.x + stage.x - pivot_offset.x + parent.x + parent_pos_offset.x);
			transform_angle_y = y_input - (y-parent_init_pos.y + stage.y - pivot_offset.y + parent.y + parent_pos_offset.y);
		}
		*/
		
		//=================================================================================//
		// Same as setPivot() but uses a global point to set the pivot (ie.mouse position) //
		//=================================================================================//
		
		// Find the vector relative to the current pivot point
		float new_pivot_x = x_input - (x + stage.x - pivot_offset.x);
		float new_pivot_y = y_input - (y + stage.y - pivot_offset.y);
		
		if(parent != null)
		{
			new_pivot_x = x_input - (x + stage.x - pivot_offset.x - parent_init_pos.x + parent.x + parent_pos_offset.x);
			new_pivot_y = y_input - (y + stage.y - pivot_offset.y - parent_init_pos.y + parent.y + parent_pos_offset.y);
		}
		PVector new_pivot = new PVector(new_pivot_x, new_pivot_y);
		
		// Rotate vector back to zero the primitive's rotation
		new_pivot = new_pivot.rotate(PApplet.radians(-rotation));
		
		// Subtract the existing pivot's offset to get new pivot offset relative to the primitive
		new_pivot = new_pivot.add(-pivot.x, -pivot.y);
		
		// Set the new pivot
		this.setPivot(-new_pivot.x, -new_pivot.y);
		
		//===============================================================//
		// Setting a new pivot point creates a secondary problem. If a   //
		// primitive already has rotation, it will create a "jump" in    //
		// position, since the primitive is rotated from a new position. //
		// To maintain continuity in position, we offset the anticipated //
		// position change.                                              //
		//===============================================================//
		
		// Find the vector between the input and the original x position (without offsets and pivot adjustments)
		
		float offset_x = x_input-(stage.x+x);
		float offset_y = y_input-(stage.y+y);
		
		if(parent != null)
		{
			offset_x = x_input - (x + stage.x - parent_init_pos.x + parent.x + parent_pos_offset.x);
			offset_y = y_input - (y + stage.y - parent_init_pos.y + parent.y + parent_pos_offset.y);
		//	//new_pivot_x = x_input - (x + stage.x - pivot_offset.x - parent_init_pos.x + parent.x + parent_pos_offset.x);
		//	//new_pivot_y = y_input - (y + stage.y - pivot_offset.y - parent_init_pos.y + parent.y + parent_pos_offset.y);
		}
		
		PVector position_offset = new PVector(offset_x, offset_y);
		
		// We take the new pivot offset vector, and rotate it by the primitive's rotation
		new_pivot = new_pivot.rotate(PApplet.radians(rotation));
		
		// Subtract the rotated pivot vector to find the positional offset
		position_offset = position_offset.add(-new_pivot.x, -new_pivot.y);
		
		// Apply the positional offset to x & y
		// (For now this offset is baked in)
		
		if(parent == null)
		{
			this.x += position_offset.x;
			this.y += position_offset.y;
		}
		else
		{
			this.x += position_offset.x;
			this.y += position_offset.y;
		}
		
		
	}
	
	public void setParent(Primitive parent)
	{
		// These values represent the offset needed to 
		parent_pos_offset = new PVector(this.x-parent.x, this.y-parent.y);
		parent_init_pos = new PVector(this.x, this.y);
		
		
		this.parent = parent;
		p.println("PARENTED TO " + parent);
		p.println("Initial position when parented " + parent_init_pos);
		p.println("Offset between parent and child " + parent_pos_offset);
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
			p.rectMode(PApplet.CENTER);
			p.translate(handle_center.x, handle_center.y);
			p.rotate(PApplet.radians(rotation));
			p.rect(0, 0, width_height_handle_size, width_height_handle_size);
			p.rectMode(PApplet.CORNER);
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
				if(e.getButton() == 37)
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
					if(handle_type == Primitive.Handle.ROTATION)
					{
						doRotate(e.getX(), e.getY());
					}
					else if(handle_type == Primitive.Handle.WIDTH_HEIGHT)
					{
						doWidthHeight(e.getX(), e.getY(), this);
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
					if(PApplet.dist(handle_center.x, handle_center.y, input_x, input_y) < rotation_handle_size)
					{
						return true;
					}
				}
				else if(handle_type == WIDTH_HEIGHT)
				{
					if(PApplet.dist(handle_center.x, handle_center.y, input_x, input_y) < width_height_handle_size)
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
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					handle_center = handle_center.add(bounding_points[0].x, bounding_points[0].y);
				}
				else if(handle_postion == BOTTOM_LEFT)
				{
					handle_center = handle_center.add(-width_height_handle_size, width_height_handle_size);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					handle_center = handle_center.add(bounding_points[1].x, bounding_points[1].y);
				}
				else if(handle_postion == BOTTOM_RIGHT)
				{
					handle_center = handle_center.add(width_height_handle_size, width_height_handle_size);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					handle_center = handle_center.add(bounding_points[2].x, bounding_points[2].y);
				}
				else if(handle_postion == TOP_RIGHT)
				{
					handle_center = handle_center.add(width_height_handle_size, -width_height_handle_size);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					handle_center = handle_center.add(bounding_points[3].x, bounding_points[3].y);
				}
			}
			else if(handle_type == WIDTH_HEIGHT)
			{
				handle_center = new PVector();
				
				if(handle_postion == TOP_LEFT)
				{
					handle_center = handle_center.add(width_height_handle_size/2, width_height_handle_size/2);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					handle_center = handle_center.add(bounding_points[0].x, bounding_points[0].y);
				}
				else if(handle_postion == BOTTOM_LEFT)
				{
					handle_center = handle_center.add(width_height_handle_size/2, -width_height_handle_size/2);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					handle_center = handle_center.add(bounding_points[1].x, bounding_points[1].y);
				}
				else if(handle_postion == BOTTOM_RIGHT)
				{
					handle_center = handle_center.add(-width_height_handle_size/2, -width_height_handle_size/2);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					handle_center = handle_center.add(bounding_points[2].x, bounding_points[2].y);
				}
				else if(handle_postion == TOP_RIGHT)
				{
					handle_center = handle_center.add(-width_height_handle_size/2, width_height_handle_size/2);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					handle_center = handle_center.add(bounding_points[3].x, bounding_points[3].y);
				}
			}
		}
	} 

}
