import java.util.ArrayList;

import javax.print.attribute.standard.PrinterMoreInfoManufacturer;
import javax.xml.bind.annotation.XmlElementDecl.GLOBAL;

import processing.core.*;
import processing.event.MouseEvent;

public class Primitive
{	// Primitive represents an animatable object within AniSketch
	
	//===========//
	// ESSENTIAL //
	//===========//
	AnimationController a;
	AniSketch p;
	Stage stage;
	Sheet sheet;
	
	//======================//
	// PRIMITIVE PROPERTIES //
	//======================//
	float x, y; // Position of the pivot point
	PVector pivot; // Pivot point represents the local offset from x,y before bounding box is drawn/rotated
	float rotation; // Rotation of the Primitive in degrees
	float t, b, l, r; // The top, bottom, left & right edges of the bounding boxes, relative to the x,y position
	PVector[] bounding_points; // The calculated "true" position of the 4 bounding points that make up the primitive.
	//SpriteLibrary.Sprite sprite; // *Unused at the moment* Sprite Object
	boolean marked_for_deletion = false;
	//PShape sprite;
	PImage sprite;
	int colour[] = {0,0,0};
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
	float transform_init_t, transform_init_b;
	float transform_init_l, transform_init_r;
	
	//================//
	// PARENT CONTROL //
	//================//
	boolean parent_control = false;
	Primitive parent;
	ArrayList<Primitive> children;
	// Parent transforms that are kept track of for parent control
	float parent_last_x, parent_last_y;
	float parent_last_rot;
	float parent_last_t, parent_last_b; 
	float parent_last_l, parent_last_r;
	float parent_last_w, parent_last_h;
	PVector parent_last_centroid; // Bounding box center
	
	final static int NORMAL = 0;
	
	final static int DEFAULT_KEY = 1;
	final static int USE_LOCAL = 2;
	final static int USE_GLOBAL = 3;
	final static int TRANSLATION = 4;
	final static int ROTATION = 5;
	final static int SHAPE = 6;
	
	//=====================//
	// KEY DELTA RECORDING //
	//=====================//
	boolean delta_recording_start = false;
	float delta_x; // UNUSED
	float delta_y; // UNUSED
	float delta_local_x;
	float delta_local_y;
	float delta_rotation;
	float delta_t;
	float delta_b;
	float delta_l;
	float delta_r;
	
	//====================//
	// PROPERTY CONSTANTS //
	//====================//
	
	static final int PROP_SHAPE             = -3; // Shape consists of the Top, Left, Bottom, Right values
	static final int PROP_POSITION          = -2; // Position consists of the X, Y values
	static final int PROP_ALL_EXCEPT_SPRITE = -1;
	static final int PROP_X        			= 0;
	static final int PROP_Y        			= 1;
	static final int PROP_ROTATION 			= 2;
	static final int PROP_LEFT     			= 3;
	static final int PROP_RIGHT    			= 4;
	static final int PROP_TOP      			= 5;
	static final int PROP_BOTTOM   			= 6;
	static final int PROP_SPRITE   			= 7;
	static final int PROP_COLOUR   			= 8;
	
	//========//
	// STYLES //
	//========//
	Style style_light;
	Style style_default;
	Style style_hover;
	Style style_selected;
	Style style_outline;
	Style style_outline_selected;
	
	//===========//
	// SELECTION //
	//===========//
	long last_time_selected = 0;
	
	Primitive(float x, float y, float w, float h, Stage stage, Sheet sheet, AnimationController a, AniSketch p)
	{
		this.p = p;
		this.a = a;
		this.stage = stage;
		this.sheet = sheet;
		
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
		
		setPropertiesToDefaultKey();
	}
	
	//======//
	// MAIN //
	//======//
	public void update()
	{
		calculateBoundingPoints();
		if(selected)
		{
			if(a.isPlaying() && delta_recording_start || !a.isPlaying() && !delta_recording_start || !a.isPlaying() && delta_recording_start)
			{
				if(!p.rendering)
				{
					if(!sheet.isDrawMode())
					{
						drawHandles();
					}	
				}
			}
		}
		if(transform_mode == ROTATE)
		{
			drawRotationGizmo();
		}
		
		if(delta_recording_start)
		{
			drawDefaultKeyPosition();
		}
		drawBoundingBox();
		if(delta_recording_start)
		{
			drawActiveKeyPosition();
		}
		
		if(a.isPlaying() && delta_recording_start || !a.isPlaying() && !delta_recording_start || !a.isPlaying() && delta_recording_start)
		{
			if(!p.rendering)
			{
				drawPivot();
			}
		}
		
		if(a.lock_frame_update && !delta_recording_start)
		{
			//p.println("FRAME LOCKED");
			//parentControl(true);
		}
		else
		{
			parentControl(NORMAL, NORMAL, NORMAL);
		}
		
		// Ideally we want to make sure that the base (default) key is up top date as much as possible
		// 
		if(!delta_recording_start && !a.isPlaying() && sheet.isCompositionMode() && a.current_frame == 0) //!!! current_frame == 0 is not a good idea, as it means that we can only adjust the default key at the beginning
		{
			//setPropertiesToDefaultKey();
		}
		//p.println("PO: " + parent_offset + "LO:" + parent_local_offset + "X: " + x);
		//p.ellipse(stage.camera.x+x-parent_local_offset.x+parent_offset.x+parent.x, stage.camera.y+y-parent_local_offset.y+parent_offset.y+parent.y, 25, 25);
		
	}
	
	public void setupStyles()
	{
		style_default = new Style(p);
		style_default.noFill();
		style_default.stroke(0,0,0,255);
		style_default.strokeWeight(1);
		
		style_hover = new Style(p);
		style_hover.noFill();
		style_hover.stroke(0,0,0,255);
		style_hover.strokeWeight(1);
		
		style_selected = new Style(p);
		style_selected.noFill();
		style_selected.stroke(0,0,0,255);
		style_selected.strokeWeight(1);
		
		style_outline = new Style(p);
		style_outline.noFill();
		style_outline.stroke(255,255,255,100);
		style_outline.strokeWeight(5);
		
		style_outline_selected = new Style(p);
		style_outline_selected.noFill();
		style_outline_selected.stroke(255,255,255,200);
		style_outline_selected.strokeWeight(5);
		
		style_light = new Style(p);
		style_light.noFill();
		style_light.stroke(0,0,0,100);
		style_light.strokeWeight(1);
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
	
	//===========//
	// ANIMATION //
	//===========//
	// Adds a property value from a key to the current primtive
	// Use the Primitive.PROP value to indicate the desired property
	public void addPropertyFromKey(Key key, int property)
	{
		if(key != null)
		{
			Key.PrimitiveData found_data = key.primitiveDataExists(this);
	
			if(found_data != null)
			{
				switch(property) 
				{
					case Primitive.PROP_X:
						if(parent != null)
						{
							// If a parent exists, apply the transform relative to the parent's rotation
							PVector x_rotated = new PVector(found_data.x, 0);
							x_rotated = x_rotated.rotate(PApplet.radians(parent.rotation));
							this.x += x_rotated.x;
							this.y += x_rotated.y;
						}
						else{this.x += found_data.x;}
						return;
					
					case Primitive.PROP_Y:
						if(parent != null)
						{
							PVector y_rotated = new PVector(0, found_data.y);
							y_rotated = y_rotated.rotate(PApplet.radians(parent.rotation));
							this.y += y_rotated.y;
							this.x += y_rotated.x;
						}
						else{this.y += found_data.y;}
						return;
					
					case Primitive.PROP_POSITION:
						if(parent != null)
						{
							PVector xy_rotated = new PVector(found_data.x, found_data.y);
							xy_rotated = xy_rotated.rotate(PApplet.radians(parent.rotation));
							this.y += xy_rotated.y;
							this.x += xy_rotated.x;
						}
						else
						{
							this.x += found_data.x;
							this.y += found_data.y;
						}
						return;
					
					case Primitive.PROP_LEFT:
						this.l += found_data.l;
						return;
					
					case Primitive.PROP_RIGHT:
						this.r += found_data.r;
						return;
					
					case Primitive.PROP_TOP:
						this.t += found_data.t;
						return;
					
					case Primitive.PROP_BOTTOM:
						this.b += found_data.b;
						return;
					
					case Primitive.PROP_SHAPE:
						this.t += found_data.t;
						this.r += found_data.r;
						this.b += found_data.b;
						this.l += found_data.l;
						return;
						
					case Primitive.PROP_ROTATION:
						this.rotation += found_data.rt;
						return;
					
					case Primitive.PROP_SPRITE:
						// "Adding" a sprite property actually replaces the current sprite if the Key's sprite is not null
						// AND if the provided key's weight is greater than a certain amount
						if(found_data.sprite != null && key.weight > 0.1f)
						{
							this.sprite = found_data.sprite;
						}
						return;
						
					default:
						Utilities.printError("Could not add property code " + property + " to " + this.toString());
						return;
				}			
			}
		}
	}

	// Adds all property values from a key to the current primitive
	// If the primitive has children, parent transformation will be calculated 
	// seperately for the children after shape, position, rotation to prevent transformation errors
	public void addAllPropertiesFromKey(Key key)
	{
		addPropertyFromKey(key, Primitive.PROP_SHAPE);
		if(hasChildren())
		{
			for(Primitive child: children){child.parentControl(NORMAL, NORMAL, NORMAL);}
		}
		
		addPropertyFromKey(key, Primitive.PROP_POSITION);
		if(hasChildren())
		{
			for(Primitive child: children){child.parentControl(NORMAL, NORMAL, NORMAL);}
		}
		
		addPropertyFromKey(key, Primitive.PROP_ROTATION);
		if(hasChildren())
		{
			for(Primitive child: children){child.parentControl(NORMAL, NORMAL, NORMAL);}
		}
		addPropertyFromKey(key, Primitive.PROP_SPRITE);
	}
	
	//  Overrides the primitive's properties with the properties from the key
	public void setPropertiesFromKey(Key key)
	{
		Key.PrimitiveData found_data = key.primitiveDataExists(this);
		
		if(found_data != null)
		{
			this.x = found_data.x;
			this.y = found_data.y;
			this.rotation = found_data.rt;
			this.t = found_data.t;
			this.b = found_data.b;
			this.l = found_data.l;
			this.r = found_data.r;
			this.sprite = found_data.sprite;
		}
	}
	
	// Overrides properties from primitives to key
	public void setPropertiesToKey(Key key)
	{
		key.setDataProperty(this, PROP_X, this.x);
		key.setDataProperty(this, PROP_Y, this.y);
		key.setDataProperty(this, PROP_ROTATION, this.rotation);
		key.setDataProperty(this, PROP_TOP, this.t);
		key.setDataProperty(this, PROP_LEFT, this.l);
		key.setDataProperty(this, PROP_BOTTOM, this.b);
		key.setDataProperty(this, PROP_RIGHT, this.r);
		// * Sprites are not handled here, instead, they are set and removed in loadSprite() & clearSprite()
	}
	
	public void addPropertyToDefaultKey(int property, float value)
	{
		a.default_key.addDataProperty(this, property, value);
	}
	
	// Updates the primitive properties to the animations controller's default key
	public void setPropertiesToDefaultKey()
	{
		setPropertiesToKey(a.default_key);
	}

	// Starts delta recording, resets the recorded delta values
	public void startDeltaRecording()
	{
		if(!delta_recording_start)
		{
			Utilities.printAlert("Delta recording started for " + this.toString());
			delta_recording_start = true;
			delta_local_x = 0;
			delta_local_y = 0;
			delta_rotation = 0;
			delta_t = 0;
			delta_b = 0;
			delta_l = 0;
			delta_r = 0;
		}
	}
	
	// Stops the delta recording
	public void endDeltaRecording()
	{ 
		delta_recording_start = false;
	}
	
	//===========//
	// PARENTING //
	//===========//
	// Returns true primitive has children
	public boolean hasChildren()
	{
		if(children.size() > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	// Returns true if it has a parent
	public boolean hasParent()
	{
		if(parent == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	// Resets last parent offset, and enables parent controls
	public void enableParentControl()
	{
		if(parent != null)
		{
			resetLastParentOffset();
			parent_control = true;
		}
	}
	
	// Disables parent control, but DOES NOT remove the parent
	public void disableParentControl()
	{
		parent_control = false;
	}
	
	// Sets the last parent to the current parent properties
	// Parent controls are based on checking changes to the parent properties
	// By setting the last checked parent properties to the current parent properties, we avoid child objects jumping around the next time the child is parented
	public void resetLastParentOffset()
	{
		if(parent != null)
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
		}
	}
	
	// Parent controls allow the parent to acts a controller for the primitive
	// This essentially constrains the primitive to the parent's properties
	// Parent values that affect the child are x,y,rotation,shape changes
	public void parentControl(int control_mode, int transform_type, int transform_mode)
	{
		// Normally, parent control is applied to the primitive's properties
		// Using 'applyChangesToDefaultKey' argument will also apply changes to the default key as well

		if(parent_control)
		{
			if(parent != null)
			{
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
				// Find width and shape height	
				float parent_cur_w = parent.l + parent.r;
				float parent_cur_h = parent.t + parent.b;
				
				// CENTROID & SHAPE CONTROL
				// If there has a been a change in the shape of the parent
				
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
					
				// Draw centroid 
				// p.ellipse(parent_cur_centroid.x+stage.camera.x, parent_cur_centroid.y+stage.camera.y, 15, 15);
				
				if(l_diff != 0 || r_diff != 0 || t_diff != 0 || b_diff != 0)
				{
					
					float scale_factor_w = parent_cur_w/parent_last_w; // Find the ratio of change for the shape
					float scale_factor_h = parent_cur_h/parent_last_h;
					
					// If there a position difference, make sure to remove it from centroid calculation, we are only concerned on how the shape changes
					parent_cur_centroid = parent_cur_centroid.add(-x_diff, -y_diff);
					
					PVector centroid_diff = parent_cur_centroid.copy().sub(parent_last_centroid); // Get the vector difference of the centroid's change.
					centroid_diff = centroid_diff.rotate(PApplet.radians(-parent.rotation)); // Reset rotation from the vector
					
					PVector child_to_centroid = new PVector(parent_last_centroid.x - this.x, parent_last_centroid.y - this.y); // Find the vector between the child and centroid
					child_to_centroid = child_to_centroid.rotate(PApplet.radians(-parent.rotation)); // Reset the rotation
					
					PVector child_to_centroid_scaled = new PVector(child_to_centroid.x*scale_factor_w, child_to_centroid.y*scale_factor_h); // Scale distance to centroid with the shape change ratio
					PVector extra_distance = new PVector(child_to_centroid.x - child_to_centroid_scaled.x, child_to_centroid.y - child_to_centroid_scaled.y); // Get the difference between the original distance and the scaled to find the amount to add
					centroid_diff = centroid_diff.add(extra_distance); // Add extra distance to centroid difference
					centroid_diff = centroid_diff.rotate(PApplet.radians(parent.rotation)); // Rotate centroid difference vector to its original angle
					
					float live_rotation = this.rotation;
					
					// Apply the difference to child's position
					this.x = this.x + centroid_diff.x;
					this.y = this.y + centroid_diff.y;
					
					
					if(control_mode == DEFAULT_KEY)
					{
						Key.PrimitiveData default_child = a.default_key.getData(this);
						PVector computed_pos = new PVector(centroid_diff.x, centroid_diff.y);
						computed_pos = computed_pos.rotate(PApplet.radians(-live_rotation));
						//p.println("XDEFF2: " + computed_pos.x + " YDEFF2: " + computed_pos.y);
						computed_pos = computed_pos.rotate(PApplet.radians(default_child.rt));
						
						//p.println("XDEFF3: " + computed_pos.x + " YDEFF3: " + computed_pos.y);
						//p.println("!");
						addPropertyToDefaultKey(PROP_X, computed_pos.x);
						addPropertyToDefaultKey(PROP_Y, computed_pos.y);
						
						//addPropertyToDefaultKey(PROP_X, centroid_diff.x);
						//addPropertyToDefaultKey(PROP_Y, centroid_diff.y);
						
						/*
						if(hasParent())
						{
							PVector parent_adjustment = new PVector(centroid_diff.x, centroid_diff.y);
							parent_adjustment = parent_adjustment.rotate(PApplet.radians(-parent.rotation));
							
							addPropertyToDefaultKey(PROP_X, parent_adjustment.x);
							addPropertyToDefaultKey(PROP_Y, parent_adjustment.y);
						}
						else
						{
							addPropertyToDefaultKey(PROP_X, centroid_diff.x);
							addPropertyToDefaultKey(PROP_Y, centroid_diff.y);
						}
						*/
					}
				}
				
				// TRANSLATION CONTROL
				// If a change in parent position was detected
				if(x_diff !=0 || y_diff !=0)
				{
					// Add the position difference to the child
					//Key.PrimitiveData default_parent = a.default_key.getData(parent);
					Key.PrimitiveData default_child = a.default_key.getData(this);
					
					float live_rotation = this.rotation;
					//PVector live_child_parent = new PVector(this.x - parent.x, this.y - parent.y);
					//PVector default_child_parent = new PVector(default_child.x - default_parent.x, default_child.y - default_parent.y);
					this.x = this.x + x_diff; 
					this.y = this.y + y_diff;
					
					PVector computed_pos = new PVector(x_diff, y_diff);
					
					//p.println(PApplet.degrees(default_child_parent.heading()-live_child_parent.heading()));
					//p.println("XDIFF: " + x_diff + " YDIFF: " + y_diff);
					
					if(control_mode == DEFAULT_KEY)
					{
						if(transform_mode == USE_GLOBAL)
						{
							//p.println("PARENT HAS PARENT");
							
							addPropertyToDefaultKey(PROP_X, computed_pos.x);
							addPropertyToDefaultKey(PROP_Y, computed_pos.y);
						}
						else if(transform_mode != USE_GLOBAL)
						{
							if(transform_type == TRANSLATION)
							{
								//p.println("PARENT HAS NO PARENT");
								//p.println("XDEFF1: " + computed_pos.x + " YDEFF1: " + computed_pos.y);
								computed_pos = computed_pos.rotate(PApplet.radians(-live_rotation));
								//p.println("XDEFF2: " + computed_pos.x + " YDEFF2: " + computed_pos.y);
								computed_pos = computed_pos.rotate(PApplet.radians(default_child.rt));
								
								//p.println("XDEFF3: " + computed_pos.x + " YDEFF3: " + computed_pos.y);
								p.println("!");
								addPropertyToDefaultKey(PROP_X, computed_pos.x);
								addPropertyToDefaultKey(PROP_Y, computed_pos.y);
							}
							else if(transform_type == ROTATION || transform_type == SHAPE)
							{
								//p.println("XDEFF1: " + computed_pos.x + " YDEFF1: " + computed_pos.y);
								//computed_pos = computed_pos.rotate(PApplet.radians(-live_rotation));
								//p.println("XDEFF2: " + computed_pos.x + " YDEFF2: " + computed_pos.y);
								//computed_pos = computed_pos.rotate(PApplet.radians(default_child.rt));
								p.println("?");
								computed_pos = computed_pos.rotate(PApplet.radians(-live_rotation));
								//p.println("XDEFF2: " + computed_pos.x + " YDEFF2: " + computed_pos.y);
								computed_pos = computed_pos.rotate(PApplet.radians(default_child.rt));
								
								//p.println("XDEFF3: " + computed_pos.x + " YDEFF3: " + computed_pos.y);
								p.println("!");
								addPropertyToDefaultKey(PROP_X, computed_pos.x);
								addPropertyToDefaultKey(PROP_Y, computed_pos.y);
								
								
								
								//p.println("XDEFF3: " + computed_pos.x + " YDEFF3: " + computed_pos.y);
								//addPropertyToDefaultKey(PROP_X, computed_pos.x);
								//addPropertyToDefaultKey(PROP_Y, computed_pos.y);
							}
							else
							{
								//p.println("XDEFF1: " + computed_pos.x + " YDEFF1: " + computed_pos.y);
								//computed_pos = computed_pos.rotate(PApplet.radians(-live_rotation));
								//p.println("XDEFF2: " + computed_pos.x + " YDEFF2: " + computed_pos.y);
								//computed_pos = computed_pos.rotate(PApplet.radians(default_child.rt));
								//p.println("XDEFF3: " + computed_pos.x + " YDEFF3: " + computed_pos.y);
								addPropertyToDefaultKey(PROP_X, computed_pos.x);
								addPropertyToDefaultKey(PROP_Y, computed_pos.y);
							}
						}
						//PVector local = 
						
						//if(hasParent())
						//{
						//	PVector parent_adjustment = new PVector(x_diff, y_diff);
						//	parent_adjustment = parent_adjustment.rotate(PApplet.radians(-parent.rotation));
							
						//	addPropertyToDefaultKey(PROP_X, parent_adjustment.x);
						//	addPropertyToDefaultKey(PROP_Y, parent_adjustment.y);
						//}
						//else
						//{
							//p.println("!!!!");
							
						//}
					}
				}
				
				// ROTATION CONTROL
				// If the rotation has been changed
				if(rot_diff != 0)
				{
					float live_rotation = this.rotation;
					PVector child_to_parent = new PVector(this.x - parent.x, this.y - parent.y); // Get the vector between pivot points
					PVector rot_vector = child_to_parent.copy();
					rot_vector = rot_vector.rotate(PApplet.radians(rot_diff)); // Rotate the vector by the change in rotation
					rot_vector = rot_vector.sub(child_to_parent); // Find the change in position needed
					
					// Apply the position change, add rotation difference to the current rotation
					this.x = this.x + rot_vector.x; 
					this.y = this.y + rot_vector.y;
					this.rotation = this.rotation + rot_diff;	
					
					if(control_mode == DEFAULT_KEY)
					{
						Key.PrimitiveData default_parent = a.default_key.getData(parent);
						Key.PrimitiveData default_child = a.default_key.getData(this);
						
						//p.fill(0);
						//p.ellipse(default_parent.x + stage.camera.x,default_parent.y + stage.camera.y,10,10);
						//p.ellipse(default_child.x + stage.camera.x,default_child.y + stage.camera.y,10,10);
						
						
						//p.noFill();
						//p.strokeWeight(2);
						
						
						PVector default_child_parent =  new PVector(default_child.x - default_parent.x, default_child.y - default_parent.y); 
						PVector default_rot_vector = default_child_parent.copy();
						default_rot_vector = default_rot_vector.rotate(PApplet.radians(rot_diff)); 
						default_rot_vector = default_rot_vector.sub(default_child_parent);
						
						default_rot_vector = rot_vector.copy().rotate(PApplet.radians(-live_rotation+default_child.rt));
						
						//p.stroke(255,0,0);
						//p.line(default_child.x +stage.camera.x, default_child.y + stage.camera.y, default_child.x + (default_rot_vector.x)+ stage.camera.x, default_child.y + (default_rot_vector.y)+ stage.camera.y);
						
						
						addPropertyToDefaultKey(PROP_X, default_rot_vector.x);
						addPropertyToDefaultKey(PROP_Y, default_rot_vector.y);
						
						addPropertyToDefaultKey(PROP_ROTATION, rot_diff);
						
						
						
					}
					
					/*
					if(applyChangesToDefaultKey)
					{
						if(hasParent())
						{
							Key.PrimitiveData parent_data = a.default_key.getData(parent);
							Key.PrimitiveData child_data = a.default_key.getData(this);
							
							if(parent_data != null && child_data != null)
							{
								float default_parent_rotation = parent_data.rt;
								
								PVector default_child_to_parent = new PVector(child_data.x - parent_data.x, child_data.y - parent_data.y); // Get the vector between pivot points
								PVector default_rot_vector = default_child_to_parent.copy();
								default_rot_vector = default_rot_vector.rotate(PApplet.radians(rot_diff)); // Rotate the vector by the change in rotation
								default_rot_vector = default_rot_vector.sub(default_child_to_parent); // Find the change in position needed
								
								addPropertyToDefaultKey(PROP_X, default_rot_vector.x);
								addPropertyToDefaultKey(PROP_Y, default_rot_vector.y);
								addPropertyToDefaultKey(PROP_ROTATION, rot_diff);
							}
						}
						else
						{
							p.println("????");
							addPropertyToDefaultKey(PROP_X, rot_vector.x);
							addPropertyToDefaultKey(PROP_Y, rot_vector.y);
							addPropertyToDefaultKey(PROP_ROTATION, rot_diff);
						}
					}*/
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
	}
	
	public void removeChildFromChildren(Primitive child)
	{
		int child_index = children.indexOf(child);

		if(child_index != -1)
		{
			children.remove(child_index);		
		}
	}
	
	/*// Depreciated
	public void forceUpdateParentControlToAllChildren()
	{
		// Applies the parent effects to all children in the tree, in order. This includes all subchildren as well
		int num_children = 0;
		
		if(hasChildren())
		{
			p.println("Updating parent effects for all subchildren");
			
			ArrayList<Primitive> update_order = new ArrayList<Primitive>(); // Update order is important to ensure that parent controls are applied correctly
			ArrayList<Primitive> children_in_cur_level = (ArrayList<Primitive>)children.clone();
			
			while(children_in_cur_level.size() != 0)
			{
				ArrayList<Primitive> children_in_next_level = new ArrayList<Primitive>();
				
				for(Primitive child: children_in_next_level)
				{
					update_order.add(child);
					if(child.children.size() > 0)
					{
						for(Primitive subchild: child.children)
						{
							children_in_next_level.add(subchild);
						}
					}
				}
				children_in_cur_level = children_in_next_level;
			}
			
			for(Primitive primitives_to_update: update_order)
			{
				primitives_to_update.parentControl();
			}
		}
		
		p.println("NUM OF DETECTED CHILDREN " + num_children);
	}*/
	
	void forceParentControlAndSetToDefaultKey(int control_mode, int transform_type, int transform_mode)
	{
		if(hasChildren())
		{
			ArrayList<Primitive> cur_objects = (ArrayList<Primitive>)children.clone();
			ArrayList<Primitive> next_objects = new ArrayList<Primitive>();
					
			while(cur_objects.size() > 0)
			{
				for(Primitive child: cur_objects)
				{
					child.parentControl(control_mode, transform_type, transform_mode);
					//child.setPropertiesToDefaultKey();
					
					if(child.hasChildren())
					{
						for(Primitive subchild: child.children)
						{
							next_objects.add(subchild);
						}
					}
				}
				cur_objects.clear();
				cur_objects = (ArrayList<Primitive>)next_objects.clone();
				next_objects.clear();
			}
		}
	}
	
	//==========//
	// GRAPHICS //
	//==========//
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
		
		//p.line(x-l, y-t, x+r, y-t);
		//p.line(x+r, y-t, x+r, y+b);
		//p.line(x+r, y+b, x-l, y+b);
		//p.line(x-l, y+b, x-l, y-t);
		p.quad(x-l, y-t, x+r, y-t, x+r, y+b, x-l, y+b);
	}

	public void drawHandles()
	{
		p.pushMatrix();
		p.translate(0, 0, 10);
		wh_top_left.drawHandle();
		wh_bottom_left.drawHandle();
		wh_bottom_right.drawHandle();
		wh_top_right.drawHandle();
		rt_top_left.drawHandle();
		rt_bottom_left.drawHandle();
		rt_bottom_right.drawHandle();
		rt_top_right.drawHandle();
		p.popMatrix();
	}
	
	public void drawDefaultKeyPosition()
	{
		Key.PrimitiveData def_data = a.default_key.getData(this);
		
		if(def_data == null)
		{
			return;
		}
		
		p.noFill();
		p.stroke(255,100);
		p.strokeWeight(2);
		
		p.pushMatrix();
		p.translate(stage.camera.x + def_data.x, stage.camera.y + def_data.y, 10);
		
		p.rotate(PApplet.radians(def_data.rt));
		
		//style_default.apply();
		
		//style_outline_selected.apply();
		
		drawStretchRect(pivot.x, pivot.y, def_data.t, def_data.b, def_data.l, def_data.r);
		
		//style_selected.apply();
		
		p.popMatrix();
	}
	
	public void drawActiveKeyPosition()
	{
		Key.PrimitiveData active_data = stage.opened_key.getData(this);
		Key.PrimitiveData def_data = a.default_key.getData(this);
		
		PVector centroid = new PVector( this.pivot.x + ((-this.l+this.r)/2), this.pivot.y + ((-this.t+this.b)/2) );
		centroid = centroid.rotate(PApplet.radians(this.rotation));	
		
		PVector centroid_no_rotation = new PVector( this.pivot.x + ((-this.l+this.r)/2), this.pivot.y + ((-this.t+this.b)/2) );
		//PApplet.println(PApplet.degrees(centroid_no_rotation.heading()));	
		
		p.noFill();
		p.stroke(255,100);
		p.strokeWeight(2);
		
		float delta_rotation_live = delta_rotation;
		float delta_local_x_live = delta_local_x;
		float delta_local_y_live = delta_local_y;
		
		if(active_data != null)
		{
			delta_rotation_live += active_data.rt;
			delta_local_x_live += active_data.x;
			delta_local_y_live += active_data.y;
		}
	
		// Draw rotation change
		if(delta_rotation_live < -0.5 || delta_rotation_live > 0.5)
		{
			p.pushMatrix();
			
			p.translate(0, 0, 10);
			p.translate(stage.camera.x + this.x, stage.camera.y + this.y);
			p.rotate(centroid_no_rotation.heading() + PApplet.HALF_PI); 
			p.rotate(PApplet.radians(-90));
	
			int rotation_direction = 1;
			float delta_rotation_adjusted = (delta_rotation_live);
			float rotation_arc_radius = (PApplet.dist(0, 0, centroid.x, centroid.y)*2) + 50;
			
			// Arcs in processing only accept a positive, so we need to adjust the values if the angle is negative 
			if(delta_rotation_adjusted < 0)
			{
				delta_rotation_adjusted = 360 + delta_rotation_adjusted;
				rotation_direction = -1;
			}
			
			// Draw the rotatation delta arc
			if(rotation_direction == 1)
			{
				p.arc(0, 0, rotation_arc_radius, rotation_arc_radius, 0, PApplet.radians(delta_rotation_adjusted));
			}
			else if(rotation_direction == -1)
			{
				p.arc(0, 0, rotation_arc_radius, rotation_arc_radius, PApplet.radians(delta_rotation_adjusted), PApplet.TWO_PI);
			}
		
			//p.pushMatrix();
			if((int)Math.abs(delta_rotation_live)/360 != 0)
			{
				p.fill(255,150);
				p.textFont(p.default_font);
				p.textAlign(p.LEFT, p.CENTER);
				p.textSize(14);
				p.text( ((int)Math.abs(delta_rotation_live)/360) + "x", (rotation_arc_radius/2)+6, -2);
				//p.text( String.format( "%.2f", (active_data.rt + delta_rotation)) + "�", (rotation_arc_radius/2)+6, 0);
				//p.popMatrix();
			}
			
			// Draw the start line
			p.line((rotation_arc_radius/2)+3, 0, (rotation_arc_radius/2)-3, 0);
			
			// Draw the arrow
			p.pushMatrix();
			p.rotate(PApplet.radians(delta_rotation_live));
			p.translate(rotation_arc_radius/2, 0, 10);
			if(rotation_direction == 1)
			{
				p.line(0, 0, -7, -7);
				p.line(0, 0, 7, -7);
			}
			else if(rotation_direction == -1)
			{
				p.line(0, 0, -7, 7);
				p.line(0, 0, 7, 7);
			}
			p.popMatrix();
			// Arrow pop
			p.translate(0, 0, 10);
			p.popMatrix();
		}
		
		// Draw translation change
		PVector delta_translate_live = new PVector(delta_local_x_live, delta_local_y_live);
		if(parent != null)
		{
			delta_translate_live = delta_translate_live.rotate(PApplet.radians(parent.rotation));
		}
		
		if(delta_translate_live.mag() > 5)
		{
			p.pushMatrix();
			p.translate(0, 0, 10);		
			p.translate(stage.camera.x + this.x, stage.camera.y + this.y);		
			
			p.line(0, 0, -delta_translate_live.x, -delta_translate_live.y);
			
			p.pushMatrix();
			p.translate(-delta_translate_live.x/2, -delta_translate_live.y/2);
			p.rotate(delta_translate_live.heading()+p.HALF_PI);
			p.line(0, 0, 7, 7);
			p.line(0, 0, -7, 7);
			//p.ellipse(-delta_translate_live.x/2, -delta_translate_live.y/2, 5, 5);
			p.popMatrix();
			
			p.popMatrix();
		}
		
	}
	
	public void drawBoundingBox()
	{
		//updateColours(255,255,255,50);
		
		// DRAW A DOTTED LINE FROM THE CHILD TO THE PARENT
		if(parent != null)
		{		
			if(a.isPlaying() && delta_recording_start || !a.isPlaying() && !delta_recording_start || !a.isPlaying() && delta_recording_start)
			{	
				if(!p.rendering)
				{
					if(!sheet.isDrawMode())
					{
						p.pushMatrix();
						p.translate(0, 0, stage.primitives.size()+1);
						style_light.apply();
						Utilities.dottedLine(x+stage.camera.x, y+stage.camera.y, parent.x+stage.camera.x, parent.y+stage.camera.y, 5, 10, p);
						p.popMatrix();
					}
				}
			}
		}
		
		p.pushMatrix();
		p.translate(stage.camera.x+x, stage.camera.y+y);
		p.rotate(PApplet.radians(rotation));

		style_default.apply();
		
		if(sprite != null)
		{
			if(delta_recording_start){p.tint(255, 200);}
			else{p.tint(255, 255);}
			
			style_light.apply();
			p.pushMatrix();
			p.translate(0, 0, 0);
			p.image(sprite, pivot.x-l, pivot.y-t, r+l, t+b);
			p.popMatrix();
		}
		
		// DRAW AN EXTRA RECT AS AN OUTLINE
		if(a.isPlaying() && delta_recording_start || !a.isPlaying() && !delta_recording_start || !a.isPlaying() && delta_recording_start)
		{	
			if(!p.rendering)
			{
				if(!sheet.isDrawMode())
				{
					if(selected)
					{
						p.pushMatrix();
						p.translate(0, 0, stage.primitives.size()+1);
						style_outline_selected.apply();
						drawStretchRect(pivot.x, pivot.y, t, b, l, r);
						p.popMatrix();
						style_selected.apply();
					}
					if(hover && !selected)
					{
						// Draw an outline
						p.pushMatrix();
						p.translate(0, 0, stage.primitives.size()+1);
						style_outline.apply();
						drawStretchRect(pivot.x, pivot.y, t, b, l, r);
						p.popMatrix();
						style_hover.apply();
					}
				}
			}
		}
		
		if(a.isPlaying() && delta_recording_start || !a.isPlaying() && !delta_recording_start || !a.isPlaying() && delta_recording_start || sprite == null)
		{	
			if(!p.rendering && sprite != null)
			{
				if(!sheet.isDrawMode())
				{
					p.pushMatrix();
					p.translate(0, 0, stage.primitives.size()+1);
					drawStretchRect(pivot.x, pivot.y, t, b, l, r);
					p.popMatrix();
				}
			}
		}		
		
		// If there is no sprite attached, we will always want to draw the primitive bounding box
		if(sprite == null)
		{
			p.pushMatrix();
			p.translate(0, 0, stage.primitives.size()+1);
			style_default.apply();
			drawStretchRect(pivot.x, pivot.y, t, b, l, r);
			p.popMatrix();
		}
		
		
		//p.fill(0);
		//p.textAlign(p.LEFT);
		//p.text(Long.toString(last_time_selected), 0, -25);
		
		p.popMatrix();
	}
	
	public void updateColours(int red, int green, int blue, int alpha)
	{
		style_default.fill(red,green,blue,alpha);
		style_hover.fill(red,green,blue,alpha);
		style_selected.fill(red,green,blue,alpha);
		style_outline.fill(red,green,blue,alpha);
		style_outline_selected.fill(red,green,blue,alpha);
		style_light.fill(red,green,blue,alpha);
	}
	
	public void drawPivot()
	{
		p.pushMatrix();
		p.translate(0, 0, 10);
		p.translate(stage.camera.x + x, stage.camera.y + y);
		//p.translate(-pivot_offset.x, -pivot_offset.y);
		
		style_light.apply();
		
		p.strokeWeight(2);
		p.line(-5, -5, 5, 5);
		p.line(-5, 5, 5, -5);
		
		p.popMatrix();
	}
	
	public void drawRotationGizmo()
	{
		p.pushMatrix();
		p.translate(0, 0, stage.primitives.size()+1);
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
		p.popMatrix();
		//p.ellipse(p.mouseX, p.mouseY, 10, 10);
	}

	public void loadSprite(PImage image)
	{
		sprite = image;
		a.default_key.setDataProperty(this, PROP_SPRITE, sprite);
	}
	
	public void loadSprite(String file)
	{
		sprite = p.loadImage(file);
		
		if(delta_recording_start) // If delta recording is started
		{
			if(sheet.active_key_selection != null)
			{
				sheet.active_key_selection.setDataProperty(this, PROP_SPRITE, sprite);
			}
		}	
		else if(!delta_recording_start) // Else, if we are working with the default key
		{
			a.default_key.setDataProperty(this, PROP_SPRITE, sprite);
		}
	}
	
	public void clearSprite()
	{
		sprite = null;
		
		if(delta_recording_start) // If delta recording is started
		{
			if(sheet.active_key_selection != null)
			{
				sheet.active_key_selection.setDataProperty(this, PROP_SPRITE, sprite);
			}
		}	
		else if(!delta_recording_start) // Else, if we are working with the default key
		{
			a.default_key.setDataProperty(this, PROP_SPRITE, sprite);
		}
	}
	
	//======================//
	// COLLISION AND BOUNDS //
	//======================//
	public boolean withinBounds(float input_x, float input_y)
	{
		try{
			if(!Utilities.isPointLeftOfLine(bounding_points[0], bounding_points[1], input_x, input_y))
			{
				if(!Utilities.isPointLeftOfLine(bounding_points[1], bounding_points[2], input_x, input_y))
				{
					if(!Utilities.isPointLeftOfLine(bounding_points[2], bounding_points[3], input_x, input_y))
					{
						if(!Utilities.isPointLeftOfLine(bounding_points[3], bounding_points[0], input_x, input_y))
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
		catch(Exception e)
		{
			System.err.println("BOUNDING BOX CHECK FAILED");
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
	// MOUSE HANDLING //
	//================//
	public int[] checkMouseEvent(MouseEvent e, Primitive active_selection, ArrayList<Primitive> selectables, boolean allowed_to_select)
	{	
		// Response details:
		// [within bounds, selection status]
		// within bounds : 0 is outside bounds, 1 is within bounds
		// selection status : 0 is no change is status, 1 is has become selected, -1 has become deselected
		
		int[] response = {0,0};
		boolean handles_mouse_event_state = false;
		boolean within_bounds = withinBounds(e.getX(), e.getY());
		
		if(within_bounds){response[0] = 1;}
		
		// If the primitive is already selected, pass the mouse event to the handles first
		if(selected)
		{
			handles_mouse_event_state = checkMouseEventHandles(e);
		}
		if(!allowed_to_select && !handles_mouse_event_state){selected = false;}
		
		// If the handles do not register any mouse events, continue to pass the mouse event to the primitive proper
		if(!handles_mouse_event_state)
		{
			boolean selection_triggered = false;
			
			if(e.getAction() == 1) // When mouse is pressed (down)
			{
				if(e.getButton() == 37)
				{
					if(within_bounds) 
					{
						// We are only allowed to select an object when
						// 1. The active selection cannot be selected
						// 2. The object's last selection time is the lowest
						if(!selectables.contains(active_selection))
						{
							if(this.lastSelectionTimeIsLowestOf(selectables))
							{
								if(allowed_to_select)
								{
									selected = true;
									response[1] = 1;
									selection_triggered = true;
									last_time_selected = System.currentTimeMillis();
								}
							}
						}
					}
					else if(!within_bounds)
					{
						if(selected)
						{
							selected = false;
							response[1] = -1;
						}	
					}
				}
				
				// REGISTER GESTURE EVENT //
				if(within_bounds && e.getButton() == 39)// && selected)
				{
					p.gesture_handler.registerObject(this, e);
				} 
				/*else if(selected)
				{
					p.gesture_handler.registerObject(this, e);
				}*/
				////////////////////////////
				
			}
			else if(e.getAction() == 2) // When mouse is released
			{
				// If translate mode was started, end it
				endTranslate(e.getX(), e.getY());
				
				// REGISTER GESTURE EVENT //
				if(within_bounds && e.getButton() == 39)// && selected)
				{
					p.gesture_handler.registerObject(this, e);
				}
				//else if(selected)
				//{
				//	p.gesture_handler.registerObject(this, e);
				//}
				////////////////////////////
			}
			else if(e.getAction() == 3) // When mouse is clicked (down then up)
			{
				if(within_bounds)
				{
					if(e.getButton() == 37)
					{
						if(active_selection == this && selectables.size() > 1) // Clicking a selected object will deselect it and cycle the selections
						{	
							selected = false;
							response[1] = -1;
						}	
						else
						{
							if(lastSelectionTimeIsLowestOf(selectables))
							{
								if(allowed_to_select)
								{
									selected = true;
									last_time_selected = System.currentTimeMillis();
									response[1] = 1;
								}
							}
						}
					}
				}
			}
			else if(e.getAction() == 4) // When mouse is dragged
			{
				if(within_bounds)
				{
					hover = true;
				}
				
				if(selected && stage.withinBounds(e.getX(), e.getY()))
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
		
		return response;
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
	// SELECTION HANDLING //
	//====================//
	
	public boolean lastSelectionTimeIsLowestOf(ArrayList<Primitive> selectables)
	{
		// Checks if the current Primitive's last selected time is the lowest of all selectables
		// Returns true if it is the lowest
		for(Primitive other_primitives: selectables)
		{
			if(other_primitives.last_time_selected < this.last_time_selected)
			{
				return false;
			}
		}
		return true;
	}
	
	//====================//
	// TRANSFORM HANDLING //
	//====================//
	public void doTranslate(float x_input, float y_input)
	{ 
		if(a.current_frame != 0 && !delta_recording_start) 
		{
			p.setCursorMessage("OBJECTS CAN ONLY BE EDITED AT FRAME 0");
			stage.cursor_message_active = true;
			return;
		}
		
		// Does translation of primitive based on the position of x_start & y_start
		if(transform_mode == NONE) // If translate has not been started, initialise it
		{
			transform_offset  = new PVector(x_input-x, y_input-y);			
			transform_mode    = MOVE;
			
			PApplet.println("Started translate");
		}
		if(transform_mode == MOVE)
		{
			if(a.current_frame == 0) {a.lockFrameUpdate();} // This prevents the animation controller from modifying the primitive properties
			
			float amount_x = this.x - (x_input - transform_offset.x);
			float amount_y = this.y - (y_input - transform_offset.y);

			float live_rotation = this.rotation; // !!!
			
			this.x = this.x - amount_x;
			this.y = this.y - amount_y;
			
			if(hasParent()) // FINDING THE TRANSLATION RELATIVE TO PARENT
			{
				// If there is a parent, find the movement relative to the rotation of the parent
				PVector global_to_local = new PVector(-amount_x, -amount_y);
				global_to_local = global_to_local.rotate(PApplet.radians(-parent.rotation));
				
				if(delta_recording_start) // If delta recording is active
				{
					delta_local_x += global_to_local.x;
					delta_local_y += global_to_local.y;
				}
				else if(!delta_recording_start && !a.isPlaying() && sheet.isCompositionMode() && a.current_frame == 0)
				{
					Key.PrimitiveData default_data = a.default_key.getData(this);
					PVector to_default_rotation = new PVector(-amount_x, -amount_y);
					to_default_rotation = to_default_rotation.rotate(PApplet.radians(-live_rotation));
					//to_default_rotation = to_default_rotation.rotate(PApplet.radians(-live_parent_rotation));
					to_default_rotation = to_default_rotation.rotate(PApplet.radians(default_data.rt));
					
					//addPropertyToDefaultKey(PROP_X, global_to_local.x);
					//addPropertyToDefaultKey(PROP_Y, global_to_local.y);
					
					//addPropertyToDefaultKey(PROP_X, -amount_x);
					//addPropertyToDefaultKey(PROP_Y, -amount_y);
					
					addPropertyToDefaultKey(PROP_X, to_default_rotation.x);
					addPropertyToDefaultKey(PROP_Y, to_default_rotation.y);

					
					if(hasChildren())
					{
						forceParentControlAndSetToDefaultKey(DEFAULT_KEY, TRANSLATION, USE_LOCAL);
					}
				}
			}
			else if(delta_recording_start) // If there is no parent, use the global transform for delta recording
			{
				delta_local_x -= amount_x;
				delta_local_y -= amount_y;
			}			
			else if(!delta_recording_start && !a.isPlaying() && sheet.isCompositionMode() && a.current_frame == 0)
			{
				//p.println("!!!!");
				Key.PrimitiveData default_data = a.default_key.getData(this);
				PVector to_default_rotation = new PVector(-amount_x, -amount_y);
				
				//to_default_rotation = to_default_rotation.rotate(PApplet.radians(-live_rotation));
				//to_default_rotation = to_default_rotation.rotate(PApplet.radians(default_data.rt));
				
				//addPropertyToDefaultKey(PROP_X, global_to_local.x);
				//addPropertyToDefaultKey(PROP_Y, global_to_local.y);
				
				//addPropertyToDefaultKey(PROP_X, -amount_x);
				//addPropertyToDefaultKey(PROP_Y, -amount_y);
				
				addPropertyToDefaultKey(PROP_X, to_default_rotation.x);
				addPropertyToDefaultKey(PROP_Y, to_default_rotation.y);
				
				//addPropertyToDefaultKey(PROP_X, -amount_x);
				//addPropertyToDefaultKey(PROP_Y, -amount_y);
				
				if(hasChildren())
				{
					forceParentControlAndSetToDefaultKey(DEFAULT_KEY, TRANSLATION, USE_GLOBAL);
				}
			}
		}
	}
	
	public void endTranslate(float x_input, float y_input)
	{ 
		// Ends translation of primitive
		a.unlockFrameUpdate();
			
		if(transform_mode == MOVE)
		{
			transform_mode = NONE;
		}
	}
	
	public void doRotate(float x_input, float y_input)
	{
		if(a.current_frame != 0 && !delta_recording_start) 
		{
			p.setCursorMessage("OBJECTS CAN ONLY BE EDITED AT FRAME 0");
			stage.cursor_message_active = true;
			return;
		}
		
		if(transform_mode == NONE)
		{
			transform_offset  = new PVector(x_input - (x + stage.camera.x), y_input - (y + stage.camera.y));
			transform_mode    = ROTATE;
		}
		if(transform_mode == ROTATE)
		{
			if(a.current_frame == 0) {a.lockFrameUpdate();} // This prevents the animation controller from modifying the primitive properties
			
			PVector cur_vector = new PVector(x_input - (x + stage.camera.x), y_input - (y + stage.camera.y));
			int direction = 0;

			if(Utilities.isPointLeftOfLine(new PVector(0,0), transform_offset, x_input - (x + stage.camera.x) , y_input - (y + stage.camera.y)))
			{
				direction = 1;
			}
			else 
			{
				direction = -1;
			}
			
			float transform_angle_difference = PApplet.degrees(PVector.angleBetween(cur_vector, transform_offset));
			transform_offset = cur_vector;
			rotation += transform_angle_difference * direction;
			
			// DELTA RECORDING
			if(delta_recording_start)
			{
				delta_rotation += transform_angle_difference * direction;
			}
			else if(!delta_recording_start && !a.isPlaying() && sheet.isCompositionMode() && a.current_frame == 0)
			{
				addPropertyToDefaultKey(PROP_ROTATION, transform_angle_difference * direction);
				
				if(hasChildren())
				{
					forceParentControlAndSetToDefaultKey(DEFAULT_KEY, ROTATION, NORMAL);
				}
			}
		}
	}
	
	public void endRotate(float x_input, float y_input)
	{
		a.unlockFrameUpdate();
		
		if(transform_mode == ROTATE)
		{
			transform_mode = NONE;
		}
	}
	
	public void doWidthHeight(float x_input, float y_input, Handle handle)
	{ 
		if(a.current_frame != 0 && !delta_recording_start) 
		{
			p.setCursorMessage("OBJECTS CAN ONLY BE EDITED AT FRAME 0");
			stage.cursor_message_active = true;
			return;
		}
		
		// Does translation of primitive based on the position of x_start & y_start
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
			if(a.current_frame == 0) {a.lockFrameUpdate();} // This prevents the animation controller from modifying the primitive properties
			
			PVector transform_amount = new PVector(x_input-transform_offset.x, y_input-transform_offset.y);
			transform_amount = transform_amount.rotate(PApplet.radians(-rotation));
			
			//PApplet.println("Transform Amount" + transform_amount);
			
			float local_init_t = this.t;
			float local_init_b = this.b;
			float local_init_l = this.l;
			float local_init_r = this.r;
			
			if(handle.handle_position == Handle.TOP_LEFT)
			{
				local_init_t = this.t;
				local_init_l = this.l;
				setLeft(transform_init_l - transform_amount.x);
				setTop(transform_init_t - transform_amount.y);
				
				if(delta_recording_start)
				{
					delta_l = this.l - transform_init_l; // DELTA RECORDING
					delta_t = this.t - transform_init_t; // DELTA RECORDING
				}
			}
			if(handle.handle_position == Handle.TOP_RIGHT)
			{
				local_init_r = this.r;
				local_init_t = this.t;
				setRight(transform_init_r + transform_amount.x);
				setTop(transform_init_t - transform_amount.y);
				
				if(delta_recording_start)
				{
					delta_r = this.r - transform_init_r; // DELTA RECORDING
					delta_t = this.t - transform_init_t; // DELTA RECORDING
				}
			}
			if(handle.handle_position == Handle.BOTTOM_RIGHT)
			{
				local_init_r = this.r;
				local_init_b = this.b;
				setRight(transform_init_r + transform_amount.x);
				setBottom(transform_init_b + transform_amount.y);
				
				if(delta_recording_start)
				{
					delta_r = this.r - transform_init_r; // DELTA RECORDING
					delta_b = this.b - transform_init_b; // DELTA RECORDING
				}
			}
			if(handle.handle_position == Handle.BOTTOM_LEFT)
			{
				local_init_b = this.b;
				local_init_l = this.l;
				setLeft(transform_init_l - transform_amount.x);
				setBottom(transform_init_b + transform_amount.y);
				
				if(delta_recording_start)
				{
					delta_l = this.l - transform_init_l; // DELTA RECORDING
					delta_b = this.b - transform_init_b; // DELTA RECORDING
				}
			}
			
			if(!delta_recording_start && !a.isPlaying() && sheet.isCompositionMode() && a.current_frame == 0)
			{
				addPropertyToDefaultKey(PROP_TOP   , this.t-local_init_t);
				addPropertyToDefaultKey(PROP_BOTTOM, this.b-local_init_b);
				addPropertyToDefaultKey(PROP_LEFT  , this.l-local_init_l);
				addPropertyToDefaultKey(PROP_RIGHT , this.r-local_init_r);

				if(hasChildren())
				{
					forceParentControlAndSetToDefaultKey(DEFAULT_KEY, SHAPE, NORMAL);
				}
			}
			
			handle.updateHandlePosition();
		}
	}
	
	public void endWidthHeight(float x_input, float y_input)
	{ // Ends translation of primitive
		a.unlockFrameUpdate();
		
		if(transform_mode == WIDTH_HEIGHT)
		{
			transform_mode = NONE;
		}
	}
	
	//===================//
	// PRIMITIVE EDITING //
	//===================//
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
		
		//p.println(this.x, this.y);
		
		this.x -= pivot_difference.x;
		this.y -= pivot_difference.y;
		
		//p.println(this.x, this.y);
		
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
	
	public void delete()
	{	
		// Unparent children
		for(Primitive child: children)
		{
			child.parent = null;
		}
		marked_for_deletion = true;
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
	
	public void setParent(Primitive parent)
	{
		boolean safe = true;
		ArrayList<Primitive> cur_children = this.children;
		ArrayList<Primitive> next_children = new ArrayList<Primitive>();	
		int total_children = 0;
		
		while(cur_children.size()>0)
		{	
			if(cur_children.contains(parent))
			{
				Utilities.printAlert("UNSAFE LINKAGE DETECTED");
				safe = false;
				break;
			}
			for(Primitive cur_child: cur_children)
			{
				if(cur_child.hasChildren())
				{
					next_children.addAll(cur_child.children);
				}
				total_children++;
			}
			
			// Update children lists
			cur_children = next_children;
			next_children = new ArrayList<Primitive>();
		}
		Utilities.printAlert("Number of children: " + total_children);
		
		
		if(safe)
		{
			this.parent = parent;
			enableParentControl();
			
			parent.children.add(this);
		}
		else
		{
			Utilities.printAlert("Cannot Set Parent Due To Link Dependancy");
		}
	}
	
	public void unparent()
	{
		if(parent != null)
		{
			disableParentControl();
			parent.removeChildFromChildren(this);
			parent = null;
		}
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
