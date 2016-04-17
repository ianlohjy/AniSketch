import java.util.ArrayList;

import processing.core.*;
import processing.event.MouseEvent;

public class Primitive
{	// Primitive represents an animatable object within AniSketch
	
	//===========//
	// ESSENTIAL //
	//===========//
	AniSketch p;
	Stage stage;
	
	//======================//
	// PRIMITIVE PROPERTIES //
	//======================//
	float x, y; // Position of the pivot point
	PVector pivot; // Pivot point represents the local offset from x,y before bounding box is drawn/rotated
	float rotation; // Rotation of the Primitive in degrees
	float t, b, l, r; // The top, bottom, left & right edges of the bounding boxes, relative to the x,y position
	PVector[] bounding_points; // The calculated "true" position of the 4 bounding points that make up the primitive.
	SpriteLibrary.Sprite sprite; // *Unused at the moment* Sprite Object
	
	//=========// 
	// HANDLES //
	//=========//
	boolean handles_enabled; // If handles are being used
	// Shape (Width/Height) Handles
	Handle wh_top_left;
	Handle wh_top_right;
	Handle wh_bottom_left;
	Handle wh_bottom_right;
	// Rotation Handles
	Handle rt_top_left;
	Handle rt_top_right;
	Handle rt_bottom_left;
	Handle rt_bottom_right;
	
	//==============//
	// MOUSE STATES //
	//==============//
	boolean pressed, hover, selected;
	
	//==================//
	// TRANSFORM STATES //
	//==================//
	// Transform States
	int transform_mode;
	final static int NONE = 0;
	final static int MOVE = 1;
	final static int ROTATE = 2;
	final static int WIDTH_HEIGHT = 3;
	// Transform Data
	PVector transform_offset; // General purpose var for holding transform info
	float transform_rotate_last_angle;
	float transform_init_t, transform_init_b;
	float transform_init_l, transform_init_r;
	
	//================//
	// PARENT CONTROL //
	//================//
	Primitive parent;
	ArrayList<Primitive> children;
	// Parent transforms that are kept track of for parent control
	float parent_last_x, parent_last_y;
	float parent_last_rot;
	float parent_last_t, parent_last_b; 
	float parent_last_l, parent_last_r;
	float parent_last_w, parent_last_h;
	PVector parent_last_centroid; // Bounding box center
	
	//========//
	// STYLES //
	//========//
	Style style_default;
	Style style_hover;
	Style style_selected;
	Style style_outline;
	Style style_outline_selected;
	
	Primitive(float x, float y, float w, float h, Stage stage, AniSketch p)
	{
		this.p = p;
		this.stage = stage;
		
		this.x = x;
		this.y = y;
		this.t = h/2;
		this.b = h/2;
		this.l = w/2;
		this.r = w/2;
		pivot = new PVector(0,0);
		bounding_points = new PVector[4];
		
		this.selected = false;
		this.pressed = false;
		this.hover = false;
		
		setupHandles();
		setupStyles();
		children = new ArrayList<Primitive>();
	}
	
	public void enableParentControl()
	{
		// Parent controls allow the parent to acts a controller for the primitive
		// This essentially constrains the primitive to the parent's properties
		// Parent values that affect the child are x,y,rotation,shape changes
		
		if(parent != null)
		{
			// DRAW A DOTTED LINE FROM THE CHILD TO THE PARENT
			style_default.apply();
			Utilities.dottedLine(x+stage.camera.x, y+stage.camera.y, parent.x+stage.camera.x, parent.y+stage.camera.y, 5, 10, p);
			
			// CENTROID & SHAPE PRECALCULATION
			// Find the centroid of the bounding box
			//              _____
			//             |     |
			//          |- |  o  | <-- centroid
			//  pivot __|  |_____|  
			//          |           
			//          |-    x <-- pivot/x/y
			//
			PVector parent_cur_centroid = new PVector( parent.pivot.x + ((-parent.l+parent.r)/2), parent.pivot.y + ((-parent.t+parent.b)/2) );
			parent_cur_centroid = parent_cur_centroid.rotate(PApplet.radians(parent.rotation));
			parent_cur_centroid = parent_cur_centroid.add(parent.x,parent.y);
			// Find width and shape height		
			float parent_cur_w = parent.l + parent.r;
			float parent_cur_h = parent.t + parent.b;
			// Draw centroid
			p.ellipse(parent_cur_centroid.x+stage.camera.x, parent_cur_centroid.y+stage.camera.y, 15, 15);
			
			// TRANSLATION CHANGE
			float x_diff = parent.x - parent_last_x;
			float y_diff = parent.y - parent_last_y;
			// ROTATION CHANGE
			float rot_diff = parent.rotation - parent_last_rot;
			// CENTROID & SHAPE CHANGE
			float t_diff = parent.t - parent_last_t;
			float b_diff = parent.b - parent_last_b;
			float l_diff = parent.l - parent_last_l;
			float r_diff = parent.r - parent_last_r;

			// TRANSLATION CONTROL
			// If a change in parent position was detected
			if(x_diff !=0 || y_diff !=0)
			{
				// Add the position difference to the child
				this.x = this.x + x_diff; 
				this.y = this.y + y_diff;
			}
			
			// ROTATION CONTROL
			// If the rotation has been changed
			if(rot_diff != 0)
			{
				PVector child_to_parent = new PVector(this.x - parent.x, this.y - parent.y); // Get the vector betwen pivot points
				PVector rot_vector = child_to_parent.copy();
				rot_vector = rot_vector.rotate(PApplet.radians(rot_diff)); // Rotate the vector by the change in rotation
				rot_vector = rot_vector.sub(child_to_parent); // Find the change in position needed
				// Apply the position change, add rotation difference to the current rotation
				this.x = this.x + rot_vector.x; 
				this.y = this.y + rot_vector.y;
				this.rotation = this.rotation + rot_diff;
			}
			
			// CENTROID & SHAPE CONTROL
			// If there has a been a change in the shape of the parent
			if(l_diff != 0 || r_diff != 0 || t_diff != 0 || b_diff != 0)
			{
				float scale_factor_w = parent_cur_w/parent_last_w; // Find the ratio of change for the shape
				float scale_factor_h = parent_cur_h/parent_last_h;
				
				PVector centroid_diff = parent_cur_centroid.copy().sub(parent_last_centroid); // Get the vector difference of the centroid's change.
				centroid_diff = centroid_diff.rotate(PApplet.radians(-parent.rotation)); // Reset rotation from the vector
				
				PVector child_to_centroid = new PVector(parent_last_centroid.x - this.x, parent_last_centroid.y - this.y); // Find the vector between the child and centroid
				child_to_centroid = child_to_centroid.rotate(PApplet.radians(-parent.rotation)); // Reset the rotation
				
				PVector child_to_centroid_scaled = new PVector(child_to_centroid.x*scale_factor_w, child_to_centroid.y*scale_factor_h); // Scale distance to centroid with the shape change ratio
				PVector extra_distance = new PVector(child_to_centroid.x - child_to_centroid_scaled.x, child_to_centroid.y - child_to_centroid_scaled.y); // Get the difference between the original distance and the scaled to find the amount to add
				centroid_diff = centroid_diff.add(extra_distance); // Add extra distance to centroid difference
				centroid_diff = centroid_diff.rotate(PApplet.radians(parent.rotation)); // Rotate centroid difference vector to its original angle
				
				// Apply the difference to child's position
				this.x = this.x + centroid_diff.x;
				this.y = this.y + centroid_diff.y;
			}
			
			// UPDATE LAST KNOWN PARENT PROPERTIES
			parent_last_x = parent.x;
			parent_last_y = parent.y;
			parent_last_t = parent.t;
			parent_last_b = parent.b;
			parent_last_l = parent.l;
			parent_last_r = parent.r;
			parent_last_w = parent_cur_w;
			parent_last_h = parent_cur_h;
			parent_last_rot = parent.rotation;
			parent_last_centroid = parent_cur_centroid; 
		}
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
		
		enableParentControl();
		
		//p.println("PO: " + parent_offset + "LO:" + parent_local_offset + "X: " + x);
		//p.ellipse(stage.camera.x+x-parent_local_offset.x+parent_offset.x+parent.x, stage.camera.y+y-parent_local_offset.y+parent_offset.y+parent.y, 25, 25);

	}
	
	public void setupStyles()
	{
		style_default = new Style(p);
		style_default.noFill();
		style_default.stroke(0,0,0,100);
		style_default.strokeWeight(1);
		
		style_hover = new Style(p);
		style_hover.noFill();
		style_hover.stroke(0,0,0,200);
		style_hover.strokeWeight(1);
		
		style_selected = new Style(p);
		style_selected.noFill();
		style_selected.stroke(0,0,0,255);
		style_selected.strokeWeight(1);
		
		style_outline = new Style(p);
		style_outline.noFill();
		style_outline.stroke(255,255,255,50);
		style_outline.strokeWeight(5);
		
		style_outline_selected = new Style(p);
		style_outline_selected.noFill();
		style_outline_selected.stroke(255,255,255,100);
		style_outline_selected.strokeWeight(5);
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
	
	public void drawStretchRect(float x, float y, float t, float b, float l, float r)
	{
		// Draws a box at x,y with t,b,l,r being offsets that define the edges from the center of x,y
		//        t 
		//    _________
		//   |         |
		// l |  (x,y)  | r
		//   |_________| 
		//
		//        b	
		p.quad(x-l, y-t, x+r, y-t, x+r, y+b, x-l, y+b);
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
	
	public void drawBoundingBox()
	{
		p.pushMatrix();
		p.translate(stage.camera.x+x, stage.camera.y+y);
		
		p.rotate(PApplet.radians(rotation));
		
		style_default.apply();
		
		if(selected)
		{
			style_outline_selected.apply();
			drawStretchRect(pivot.x, pivot.y, t, b, l, r);
			style_selected.apply();
		}
		if(hover)
		{
			// Draw an outline
			style_outline.apply();
			drawStretchRect(pivot.x, pivot.y, t, b, l, r);
			style_hover.apply();
		}

		drawStretchRect(pivot.x, pivot.y, t, b, l, r);
		
		p.popMatrix();
	}
	
	public void drawPivot()
	{
		p.pushMatrix();
		p.translate(stage.camera.x + x, stage.camera.y + y);
		//p.translate(-pivot_offset.x, -pivot_offset.y);
		
		if(!selected)
		{
			style_default.apply();
		}
		else
		{
			style_selected.apply();
		}

		p.strokeWeight(1);
		p.line(-5, -5, 5, 5);
		p.line(-5, 5, 5, -5);
		
		p.popMatrix();
	}
	
	public void drawRotationGizmo()
	{
		p.noFill();
		p.stroke(0);
		p.strokeWeight(1);
		
		float pivot_x;
		float pivot_y;
		
		pivot_x = stage.camera.x + x;// - pivot_offset.x;
		pivot_y = stage.camera.y + y;//- pivot_offset.y;
		
		p.strokeWeight(1);
		p.line(p.mouseX, p.mouseY, pivot_x, pivot_y);
		p.strokeWeight(3);
		p.point(p.mouseX, p.mouseY);
		//p.ellipse(p.mouseX, p.mouseY, 10, 10);
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
		// Top Left Bounding Point
		bounding_points[0] = new PVector(-l, -t); 

		// Bottom Left Bounding Point
		bounding_points[1] = new PVector(-l, b); 

		// Bottom Right Bounding Point
		bounding_points[2] = new PVector(r, b); 

		// Top Right Bounding Point
		bounding_points[3] = new PVector(r, -t); 

		// Calculations that are applied to all points 
		for(PVector bounding_point: bounding_points)
		{
			bounding_point = bounding_point.add(pivot);
			bounding_point = bounding_point.rotate(PApplet.radians(rotation));
			bounding_point = bounding_point.add(stage.camera.x+x, stage.camera.y+y);
		}
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
			float amount_x = this.x - (x_input - transform_offset.x);
			float amount_y = this.y - (y_input - transform_offset.y);
			
			this.x = this.x - amount_x;
			this.y = this.y - amount_y;
			
			/*
			if(parent != null)
			{
				p.println(parent_local_offset);
				
				parent_local_offset = new PVector(this.x, this.y);
				PVector amount = new PVector(-amount_x, -amount_y);
				amount = amount.rotate(PApplet.radians(-parent.rotation));
				
				parent_start_offset = parent_start_offset.add(amount);
			}
			*/
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
			
			float transform_angle_x = x_input - (x + stage.camera.x);
			float transform_angle_y = y_input - (y + stage.camera.y);
			/*
			if(parent != null) 
			{
				transform_angle_x = x_input - ((x + stage.camera.x - pivot_offset.x) + (-parent_local_offset.x + parent.x + parent_offset.x));
				transform_angle_y = y_input - ((y + stage.camera.y - pivot_offset.y) + (-parent_local_offset.y + parent.y + parent_offset.y));
			}
			*/	
			p.println("Angle Pos " + transform_angle_x + " " + transform_angle_y);
			
			transform_rotate_last_angle = PApplet.degrees(PApplet.atan2(transform_angle_x, transform_angle_y));
		}
		if(transform_mode == ROTATE)
		{
			transform_offset  = new PVector(x_input-x, y_input-y); // Unused & does not account for parenting

			float transform_angle_x = x_input - (x + stage.camera.x);
			float transform_angle_y = y_input - (y + stage.camera.y);
			/*
			if(parent != null) 
			{
				transform_angle_x = x_input - ((x + stage.camera.x - pivot_offset.x) + (-parent_local_offset.x + parent.x + parent_offset.x));
				transform_angle_y = y_input - ((y + stage.camera.y - pivot_offset.y) + (-parent_local_offset.y + parent.y + parent_offset.y));
			}
			*/
			//p.ellipse(transform_angle_x, transform_angle_x, 16, 16);
			
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
			if(handle.handle_position == Handle.TOP_LEFT)
			{
				transform_init_t  = t;
				transform_init_l  = l;
			}
			else if(handle.handle_position == Handle.BOTTOM_LEFT)
			{
				transform_init_b  = b;
				transform_init_l  = l;
			}
			if(handle.handle_position == Handle.BOTTOM_RIGHT)
			{
				transform_init_b  = b;
				transform_init_r  = r;
			}
			else if(handle.handle_position == Handle.TOP_RIGHT)
			{
				transform_init_t  = t;
				transform_init_r  = r;
			}
			
			transform_offset  = handle.handle_center;
			transform_mode    = WIDTH_HEIGHT;
			
			PApplet.println("Started width height");
		}
		
		if(transform_mode == WIDTH_HEIGHT)
		{
			PVector transform_amount = new PVector(x_input-transform_offset.x, y_input-transform_offset.y);
			transform_amount = transform_amount.rotate(PApplet.radians(-rotation));
			
			/*if(parent != null)
			{
				transform_amount = transform_amount.rotate(PApplet.radians(-parent.rotation));
			}
			*/
			
			//PApplet.println("Transform Amount" + transform_amount);
			
			if(handle.handle_position == Handle.TOP_LEFT)
			{
				setLeft(transform_init_l - transform_amount.x);
				setTop(transform_init_t - transform_amount.y);
			}
			if(handle.handle_position == Handle.TOP_RIGHT)
			{
				setRight(transform_init_r + transform_amount.x);
				setTop(transform_init_t - transform_amount.y);
			}
			if(handle.handle_position == Handle.BOTTOM_RIGHT)
			{
				setRight(transform_init_r + transform_amount.x);
				setBottom(transform_init_b + transform_amount.y);
			}
			if(handle.handle_position == Handle.BOTTOM_LEFT)
			{
				setLeft(transform_init_l - transform_amount.x);
				setBottom(transform_init_b + transform_amount.y);
			}
			
			handle.updateHandlePosition();
		}
	}
	
	public void endWidthHeight(float x_input, float y_input)
	{ // Ends translation of primitive
		if(transform_mode == WIDTH_HEIGHT)
		{
			transform_mode = NONE;
		}
	}
	
	public void setTop(float amount)
	{
		if( (amount+b) < 30)
		{
			amount = 30-b;
		}
		t = amount;
	}
	
	public void setLeft(float amount)
	{
		if( (amount+r) < 30)
		{
			amount = 30-r;
		}
		l = amount;
	}
	
	public void setRight(float amount)
	{
		if( (amount+l) < 30)
		{
			amount = 30-l;
		}
		r = amount;
	}
	
	public void setBottom(float amount)
	{
		if( (amount+t) < 30)
		{
			amount = 30-t;
		}
		b = amount;
	}
	
	//=========//
	// EDITING //
	//=========//
	public void setPivot(float new_pivot_x, float new_pivot_y)
	{
		// Sets pivot relative to center of object
		// Check if new pivot values are the same, ignore changes if they are
		// Offset (ALL?, this seems to be what 3ds max does) x/y values to maintain offset

		// Changing pivot position actually means:

		PVector pivot_difference = new PVector();
		pivot_difference.x = new_pivot_x - pivot.x;
		pivot_difference.y = new_pivot_y - pivot.y;
		
		pivot.x = new_pivot_x;
		pivot.y = new_pivot_y;
		
		pivot_difference = pivot_difference.rotate(PApplet.radians(this.rotation));
		this.x -= pivot_difference.x;
		this.y -= pivot_difference.y;
		
		/*
		if(parent != null)
		{
			p.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
			parent_start_offset = parent_start_offset.add(-pivot_difference.x, -pivot_difference.y);
			parent_local_offset = parent_local_offset.add(-pivot_difference.x, -pivot_difference.y);
			calculateParentOffset();
		}
		*/
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
		float new_pivot_x = x_input - (x + stage.camera.x);
		float new_pivot_y = y_input - (y + stage.camera.y);
		
		if(parent != null)
		{
			//new_pivot_x = x_input - (x + stage.x - pivot_offset.x - parent_init_pos.x + parent.x + parent_pos_offset.x);
			//new_pivot_y = y_input - (y + stage.y - pivot_offset.y - parent_init_pos.y + parent.y + parent_pos_offset.y);
		}
		else
		{
			
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
		
		/*
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
		*/
	}
	
	public void setParent(Primitive parent)
	{
		parent_last_x = parent.x;
		parent_last_y = parent.y;
		parent_last_rot = parent.rotation;
		
		parent_last_t = parent.t;
		parent_last_l = parent.l;
		parent_last_b = parent.b;
		parent_last_r = parent.r;
		
		parent_last_centroid = new PVector( parent.pivot.x + ((-parent.l+parent.r)/2), parent.pivot.y + ((-parent.t+parent.b)/2) );
		parent_last_centroid = parent_last_centroid.rotate(PApplet.radians(parent.rotation));
		parent_last_centroid = parent_last_centroid.add(parent.x,parent.y);
		
		parent_last_w = parent.l + parent.r;
		parent_last_h = parent.t + parent.b;
		
		this.parent = parent;
	}
	
	/*
	public void setParent(Primitive parent)
	{
		// These values represent the offset needed to 
		parent_start_offset = new PVector(this.x-parent.x, this.y-parent.y); // Initial positional offset
		parent_local_offset = new PVector(this.x, this.y);
		parent_rotation_offset = this.rotation - parent.rotation;
		this.parent = parent;
		
		calculateParentOffset();
		
		p.println("PARENTED TO " + parent);
		p.println("Offset between parent and child " + parent_offset);
	}
	
	public PVector calculateParentOffset()
	{	
		// This parent offset is 'parent_start_offset' + parent rotation accounted for
		if(parent != null)
		{
			PVector new_offset = parent_start_offset.copy();
			//new_offset = new_offset.add(pivot);
			new_offset = new_offset.rotate(PApplet.radians(parent.rotation - parent_rotation_offset));
			parent_offset = new_offset;
			//p.println(parent_offset);
			
			//p.stroke(0);
			//p.ellipse(parent.x, parent.y, 25, 25);
			p.line(parent.x + stage.camera.x, parent.y + stage.camera.y, parent.x+stage.camera.x+new_offset.x, parent.y+stage.camera.y+new_offset.y);
			
			return parent_offset;
		}
		else
		{
			return null;
		}
	}
	*/
	
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
		int handle_position;
		int handle_type;
		int width_height_handle_size = 10;
		int rotation_handle_size = 10;
		
		PVector handle_center;
		
		
		Handle(int handle_type, int handle_position)
		{
			this.handle_position = handle_position;
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
			rotation_style_default.stroke(0, 0, 0, 100);
			rotation_style_default.strokeWeight(2);
			
			width_height_style_default = new Style(p);
			width_height_style_default.fill(0, 0, 0, 100);
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
			/*if(parent != null)
			{
				p.rotate(PApplet.radians(parent.rotation));
			}*/
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
			// There must be a cleaner way to write this...
			if(handle_type == ROTATION)
			{
				handle_center = new PVector();
				
				if(handle_position == TOP_LEFT)
				{
					handle_center = handle_center.add(-width_height_handle_size, -width_height_handle_size);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					/*if(parent != null)
					{
						handle_center = handle_center.rotate(PApplet.radians(parent.rotation));
					}*/
					handle_center = handle_center.add(bounding_points[0].x, bounding_points[0].y);
				}
				else if(handle_position == BOTTOM_LEFT)
				{
					handle_center = handle_center.add(-width_height_handle_size, width_height_handle_size);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					/*if(parent != null)
					{
						handle_center = handle_center.rotate(PApplet.radians(parent.rotation));
					}*/
					handle_center = handle_center.add(bounding_points[1].x, bounding_points[1].y);
				}
				else if(handle_position == BOTTOM_RIGHT)
				{
					handle_center = handle_center.add(width_height_handle_size, width_height_handle_size);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					/*if(parent != null)
					{
						handle_center = handle_center.rotate(PApplet.radians(parent.rotation));
					}*/
					handle_center = handle_center.add(bounding_points[2].x, bounding_points[2].y);
				}
				else if(handle_position == TOP_RIGHT)
				{
					handle_center = handle_center.add(width_height_handle_size, -width_height_handle_size);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					/*if(parent != null)
					{
						handle_center = handle_center.rotate(PApplet.radians(parent.rotation));
					}*/
					handle_center = handle_center.add(bounding_points[3].x, bounding_points[3].y);
				}
				
			}
			else if(handle_type == WIDTH_HEIGHT)
			{
				handle_center = new PVector();
				
				if(handle_position == TOP_LEFT)
				{
					handle_center = handle_center.add(width_height_handle_size/2, width_height_handle_size/2);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					/*if(parent != null)
					{
						handle_center = handle_center.rotate(PApplet.radians(parent.rotation));
					}*/
					handle_center = handle_center.add(bounding_points[0].x, bounding_points[0].y);
				}
				else if(handle_position == BOTTOM_LEFT)
				{
					handle_center = handle_center.add(width_height_handle_size/2, -width_height_handle_size/2);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					/*if(parent != null)
					{
						handle_center = handle_center.rotate(PApplet.radians(parent.rotation));
					}*/
					handle_center = handle_center.add(bounding_points[1].x, bounding_points[1].y);
				}
				else if(handle_position == BOTTOM_RIGHT)
				{
					handle_center = handle_center.add(-width_height_handle_size/2, -width_height_handle_size/2);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					/*if(parent != null)
					{
						handle_center = handle_center.rotate(PApplet.radians(parent.rotation));
					}*/
					handle_center = handle_center.add(bounding_points[2].x, bounding_points[2].y);
				}
				else if(handle_position == TOP_RIGHT)
				{
					handle_center = handle_center.add(-width_height_handle_size/2, width_height_handle_size/2);
					handle_center = handle_center.rotate(PApplet.radians(rotation));
					/*if(parent != null)
					{
						handle_center = handle_center.rotate(PApplet.radians(parent.rotation));
					}*/
					handle_center = handle_center.add(bounding_points[3].x, bounding_points[3].y);
				}
			}
		}
	} 

}
