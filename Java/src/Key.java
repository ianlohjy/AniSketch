
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Key {

	float x;
	float y;
	float d;
	float deviation_width;
	int[] colour = new int[3];
	float min_d = 50;
	float weight = 1; // Holds weight data. Only used when compiling key weights. Set to 1 by default for Stage.goToActiveKey()
	
	AniSketch p;
	ArrayList<PrimitiveData> primitive_data;
	
	float[][] shape;

	boolean marked_for_deletion = false;
	boolean key_opened = false;
	
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
	final static int WIDTH = 2;
	// Transform Data
	PVector transform_offset; // General purpose var for holding transform info

	// GRAPHICS // 
	float opacity_scale = 1.0f;
	
	// HANDLES // 
	HandleRing ring_handle = new HandleRing(this);
	
	// SELECTION // 
	long last_time_selected = 0;
	
	// CONNECTIONS //
	ArrayList<Key> connections;
	
	Key(float x, float y, float d, AniSketch p)
	{
		this.x = x;
		this.y = y;
		this.d = d;
		this.p = p;
		
		primitive_data = new ArrayList<PrimitiveData>();
		connections = new ArrayList<Key>();
		colour = Utilities.randomColorPallete();
		cacheCircle(30);
	}
	
	public void connectToKey(Key key)
	{
		p.animation.connectKeys(this, key);
	}
	
	public void disconnectKey(Key key)
	{
		p.animation.disconnectKeys(this, key);
	}
	
	public void disconnectAllKeys()
	{
		while(connections.size() > 0)
		{
			p.println("!!#!#");
			disconnectKey(connections.get(0));
		}
		
		/*
		for(Key connected: connections)
		{
			disconnectKey(connected);
		}
		*/
	}
	
	// SETUP //
	public void cacheCircle(int sides)
	{
		shape = new float[sides+1][2];
		
		for(int a=0; a<=sides; a++)
		{
			shape[a][0] = PApplet.cos(PApplet.radians(360/sides*a));
			shape[a][1] = PApplet.sin(PApplet.radians(360/sides*a));
		}
	}
	
	// DEBUGGING //
	public void printDeltaData()
	{
		p.println("NUMBER OF PRIMITIVE DATA OBJECTS: " + primitive_data.size());
		for(PrimitiveData data: primitive_data)
		{
			data.printData();
		}
	}
	
	// PRIMITIVE DATA HANDLING //
	
	// Add the stored deltas from a primtive to the key, and reset the stored deltas from the primitive to 0
	// If the primitive data does not exist, it is created first
	public void mergeDataFromPrimitive(Primitive primitive)
	{
		PrimitiveData found_data = primitiveDataExists(primitive);
		
		if(found_data == null)
		{
			PrimitiveData new_data = new PrimitiveData(primitive);
			new_data.mergeCurrentStoredDeltas();
			primitive_data.add(new_data);
		}
		else if(found_data != null)
		{
			found_data.mergeCurrentStoredDeltas();
		}
	}
		
	// Removes the data object for a primitive (usually in cases where the primitive is deleted)
	public void removeDataObjectForPrimitive(Primitive primitive)
	{	
		for(PrimitiveData data: primitive_data)
		{
			PrimitiveData found_data = primitiveDataExists(primitive);
			
			if(found_data != null)
			{
				primitive_data.remove(primitive_data.indexOf(found_data));
			}
		}
	}
	
	// Checks if delta data for a primitive already exists
	public PrimitiveData primitiveDataExists(Primitive primitive)
	{
		for(PrimitiveData data: primitive_data)
		{
			if(data.matchesPrimitive(primitive))
			{
				return data;
			}
		}
		return null;
	}
	
	// Adds primitive data from another key
	public Key add(Key key_to_add)
	{
		for(PrimitiveData data_to_add: key_to_add.primitive_data)
		{
			PrimitiveData found_data = primitiveDataExists(data_to_add.primitive);
			
			if(found_data == null)
			{
				primitive_data.add(data_to_add);
			}
			else if(found_data != null)
			{
				found_data.add(data_to_add);
			}
		}
		return this;
	}
	
	// Set the data property for a primitive
	public PrimitiveData getData(Primitive primitive)
	{
		PrimitiveData found_data = primitiveDataExists(primitive);
		
		if(found_data == null)
		{
			return null;
		}
		else
		{
			return found_data;
		}
	}
	
	// Set the data property for a primitive
	public void setDataProperty(Primitive primitive, int property, float value)
	{
		PrimitiveData found_data = primitiveDataExists(primitive);
		if(found_data == null)
		{
			found_data = new PrimitiveData(primitive);
			primitive_data.add(found_data);
		}
		
		switch(property) 
		{
			case Primitive.PROP_X:
			found_data.setX(value);
			return;
			
			case Primitive.PROP_Y:
			found_data.setY(value);
			return;
			
			case Primitive.PROP_LEFT:
			found_data.setLeft(value);
			return;
			
			case Primitive.PROP_RIGHT:
			found_data.setRight(value);
			return;
			
			case Primitive.PROP_TOP:
			found_data.setTop(value);
			return;
			
			case Primitive.PROP_BOTTOM:
			found_data.setBottom(value);
			return;
			
			case Primitive.PROP_ROTATION:
			found_data.setRotation(value);
			return;
		
			default:
			return;
		}
	}
	
	// Make a copy of a PrimitiveData
	public PrimitiveData copyData(PrimitiveData data)
	{
		PrimitiveData data_copy = new PrimitiveData(data.primitive);
		data_copy.set(data);
		data_copy.sprite = data.sprite;
		return data_copy;
	}
	
	// Overloaded method for setting sprites
	public void setDataProperty(Primitive primitive, int property, PImage sprite)
	{
		PrimitiveData found_data = primitiveDataExists(primitive);
		if(found_data == null)
		{
			found_data = new PrimitiveData(primitive);
			primitive_data.add(found_data);
		}
		
		switch(property) 
		{
			case Primitive.PROP_SPRITE:
			found_data.setSprite(sprite);
			return;
			
			default:
			return;
		}
	}
	
	// Add a data property for a primitive
	public void addDataProperty(Primitive primitive, int property, float value)
	{
		PrimitiveData found_data = primitiveDataExists(primitive);
		if(found_data == null)
		{
			found_data = new PrimitiveData(primitive);
			primitive_data.add(found_data);
		}
		
		switch(property) 
		{
			case Primitive.PROP_X:
			found_data.setX(found_data.x+value);
			return;
			
			case Primitive.PROP_Y:
			found_data.setY(found_data.y+value);
			return;
			
			case Primitive.PROP_LEFT:
			found_data.setLeft(found_data.l+value);
			return;
			
			case Primitive.PROP_RIGHT:
			found_data.setRight(found_data.r+value);
			return;
			
			case Primitive.PROP_TOP:
			found_data.setTop(found_data.t+value);
			return;
			
			case Primitive.PROP_BOTTOM:
			found_data.setBottom(found_data.b+value);
			return;
			
			case Primitive.PROP_ROTATION:
			found_data.setRotation(found_data.rt+value);
			return;
			
			default:
			return;
		}
	}
	
	// Adds input primitive data to the current key, it it does not exist, then a new Primitive Data will be created
	public void addPrimitiveData(PrimitiveData data)
	{
		PrimitiveData found_data = primitiveDataExists(data.primitive);
		
		if(found_data == null)
		{
			found_data = new PrimitiveData(data.primitive);
			primitive_data.add(found_data);
		}
		
		if(found_data != null)
		{
			found_data.add(data);
		}
	}
	
	// Opening a Key allows it gp
	void openKey()
	{
		key_opened = true;
	}
	
	void closeKey()
	{
		key_opened = false;
	}
	
	// MAIN //
	public void update()
	{
	}
	
	// DRAWING // 
	public void draw()
	{
		p.noStroke();
		p.fill(colour[0]+100,colour[1]+100,colour[2]+100,255);		
		// Insert center vertex
		p.beginShape(p.TRIANGLE_FAN);
		p.vertex(x,y);
		
		//p.fill(color[0],color[1],color[2],0);
		
		float faded1 = PApplet.lerp(colour[0],255,0.90f);
		float faded2 = PApplet.lerp(colour[1],255,0.90f);
		float faded3 = PApplet.lerp(colour[2],255,0.90f);
		
		p.fill(faded1,faded2,faded3);
		//p.fill(colour[0]+180,colour[1]+180,colour[2]+180);
		for(float[] point: shape)
		{
			p.vertex((d/2*point[0]) + x,(d/2*point[1]) + y);
		}
		p.endShape();
		
		if(hover && !selected)
		{
			p.noFill();
			p.strokeWeight(2);
			p.stroke(230);
			p.ellipse(x,y,d,d);
		}
		
		if(selected)
		{
			ring_handle.draw();
			p.noFill();
			p.strokeWeight(3);
			p.stroke(colour[0]+50,colour[1]+50,colour[2]+50);
			//p.ellipse(x,y,d,d);
		}
		
		p.noStroke();
		if(p.main_windows.sheet.active_key_selection == this)
		{
			p.fill(colour[0],colour[1],colour[2]);
		}
		else
		{
			p.fill(0,0,0);
		}
		
		p.fill(colour[0],colour[1],colour[2]);
		p.rectMode(p.CENTER);
		p.rect(x, y, 5, 5);
		p.rectMode(p.CORNER);
		
		//p.text(this.toString(), x, y);
		//p.text(Long.toString(last_time_selected), x, y);
		//p.println(last_time_selected);
	}
	
	public float getWeight(float x_input, float y_input)
	{
		/*
		if(PApplet.dist(x_input, y_input, x, y) < this.d/2)
		{
			float weight = -PApplet.dist(x_input, y_input, x, y) + (d/2);
			weight /= (d/2);
			return weight;
		}
		
		else
		{
			return 0;
		}
		*/
		
		float weight = Utilities.gaussian1d(x_input, this.x, this.d/6f) * Utilities.gaussian1d(y_input, this.y, this.d/6f);
		weight *= 1;
		
		if(weight > 1)
		{
			weight = 1;
		}
		
		return weight;
		 
	}
	
	public void doTranslate(float x_input, float y_input)
	{
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
		}
	}
	
	public void endTranslate(float x_input, float y_input)
	{
		if(transform_mode == MOVE)
		{
			transform_mode = NONE;
		}
	}
	
	public void doWidth(float x_input, float y_input)
	{
		if(transform_mode == NONE)
		{
			transform_mode = WIDTH;
			transform_offset = new PVector(x_input, y_input);
		}
		
		if(transform_mode == WIDTH)
		{
			float cur_dist = PApplet.dist(this.x, this.y, x_input, y_input);
			float last_dist = PApplet.dist(this.x, this.y, transform_offset.x, transform_offset.y);
			
			this.d = cur_dist * 2;	
			
			if(this.d < this.min_d)
			{
				this.d = min_d;
			}
			
			transform_offset = new PVector(x_input, y_input);
		}
	}
	
	public void endWidth(float x_input, float y_input)
	{
		if(transform_mode == WIDTH)
		{
			transform_mode = NONE;
		}
	}
	
	public void delete()
	{
		// Check if the key is the active selection for the sheet/stage
		if(p.main_windows.sheet.active_key_selection == this)
		{
			p.main_windows.stage.exitActiveKey();
			p.main_windows.sheet.active_key_selection = null;
			
			if(p.main_windows.sheet.active_selection == this)
			{
				p.main_windows.sheet.active_selection = null;
			}
			this.selected = false;
		}
		marked_for_deletion = true;
	}
	
	void updateColour(int[] new_colour)
	{
		colour = new_colour;
		
		if(p.main_windows.stage.opened_key == this)
		{
			p.main_windows.stage.setBackgroundColour(new_colour[0], new_colour[1], new_colour[2]);
		}
	}
	
	public boolean withinBounds(float x_input, float y_input)
	{
		if(PApplet.dist(x_input, y_input, this.x, this.y) < d/2)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	// SELECTION HANDLING //
	public boolean oldestSelectionOf(ArrayList<Object> selectables)
	{
		// Checks if the current object's last selected time is the lowest of all selectables
		// Returns true if it is the lowest

		for(Object other_selectable: selectables)
		{
			if(other_selectable instanceof Key)
			{
				Key other_key = (Key)other_selectable;
				if(other_key.last_time_selected < this.last_time_selected){return false;}
			}
			else if(other_selectable instanceof Stroke)
			{
				Stroke other_stroke = (Stroke)other_selectable;
				if(other_stroke.last_time_selected < this.last_time_selected){return false;}
			}
		}
		return true;
	}
	
	/*
	public boolean selectableKeysContainsKey(ArrayList<Key> selectable_keys, Key key)
	{
		return selectable_keys.contains(key);
	}
	*/
	
	void updateSelectionTime()
	{
		last_time_selected = System.currentTimeMillis();
	}
	
	public int[] checkMouseEvent(MouseEvent e, 
								 Object active_selection,
								 ArrayList<Object> selectables, 
								 boolean allow_switching)
	{
		boolean within_bounds = withinBounds(e.getX(), e.getY());
		boolean handles_mouse_event_state = false;
		int[] mouse_status = {0,0}; 
		
		// Mouse state communicates how the Key has processed the mouse event
		// mouse_status[0] = if the mouse is within the bounds of the object
		// mouse_status[1] = if the Key has become the active selection (1) if the key has been deselected (-1)
		
		if(selected)
		{
			handles_mouse_event_state = checkMouseEventHandle(e);
		}

		if(!handles_mouse_event_state)
		{
			if(e.getAction() == 1)// Mouse Pressed
			{
				if(e.getButton() == 37) // If it was a left-click
				{
					// If within bounds
					if(within_bounds)
					{
						if(!selectables.contains(active_selection))
						{
							if(oldestSelectionOf(selectables))
							{
								selected = true;
								updateSelectionTime();
								mouse_status[1] = 1;
								p.println("PRESS");
							}
						}
							
						/*
						// If the active key is NOT selectable, meaning that the current selected key is not in the possible selection list 
						if(!selectable_keys.contains(active_key_selection))
						{
							// And if there is no active key OR the active key is out of bounds
							if(active_key_selection == null || !active_key_selection.withinBounds(e.getX(), e.getY()))
							{
								// Select this key is this has the oldest last selection time
								if(oldestSelectionOf(selectable_keys))
								{
									selected = true;
									updateSelectionTime();
									mouse_status[1] = 1;
									p.println("PRESS");
								}
							}
						}
						*/
					}
					else
					{
						selected = false;
						mouse_status[1] = -1;
					}
				}
				// REGISTER GESTURE EVENT //
				if(within_bounds && e.getButton() == 39)// && selected)
				{
					p.gesture_handler.registerObject(this, e);
				}
				/*
				else if(selected)
				{
					p.gesture_handler.registerObject(this, e);
				}*/
				////////////////////////////
			}
			else if(e.getAction() == 2)// Mouse Released
			{
				if(e.getButton() == 37) // If it was a left-click
				{
					endTranslate(e.getX(), e.getY());
				}
				// REGISTER GESTURE EVENT //
				if(within_bounds && e.getButton() == 39)// && selected)
				{
					p.gesture_handler.registerObject(this, e);
				}
				////////////////////////////
			}
			else if(e.getAction() == 3) // Mouse Clicked
			{
				if(e.getButton() == 37) // If it was a left-click
				{			
					// Clicking will cycle through selections
					if(within_bounds)
					{
						if(active_selection == this)
						{
							p.println("CLICK");
							if(selectables.contains(this) && selectables.size() != 1)
							{
								selected = false;
								mouse_status[1] = -1;
							}
							//selected = false;
							//mouse_status[1] = 0;
						}
						else
						{
							if(oldestSelectionOf(selectables))
							{
								if(allow_switching)
								{
									for(Object others: selectables)
									{
										if(others instanceof Key)
										{
											Key other_key = (Key)others;
											other_key.selected = false;
										}
										else if(others instanceof Stroke)
										{
											Stroke other_stroke = (Stroke)others;
											other_stroke.selected = false;
										}
									}
									
									selected = true;
									updateSelectionTime();
									mouse_status[1] = 1;
									p.println("PRESS");
								}
							}
						}
					}
				}
			}
			else if(e.getAction() == 4) // Mouse Dragged
			{
				if(e.getButton() == 37) // If it was a left-click
				{
					if(within_bounds)
					{
						hover = true;
					}
					
					if(selected && p.main_windows.sheet.withinBounds(e.getX(), e.getY()))
					{
						// Make sure that the left mouse button is pressed, and that the drag event started inside the sheet
						if(e.getButton() == 37 && p.main_windows.mouseDraggedStartedIn(0))
						{
							doTranslate(e.getX(), e.getY());
						}	
						else
						{
							endTranslate(e.getX(), e.getY());
						}
					}
				}
				if(e.getButton() == 39) // If it was a right-click
				{
					if(within_bounds)
					{
						hover = true;
					}
				}
			}
			else if(e.getAction() == 5) // When mouse is moved
			{
				if(within_bounds) 
				{
					hover = true;
				}
				else 
				{
					hover = false;
				}
			}
			
			if(within_bounds)
			{
				mouse_status[0] = 1;
			}
			else
			{
				mouse_status[0] = -1;
			}
		}
		return mouse_status;
	}
	
	public boolean checkMouseEventHandle(MouseEvent e)
	{
		return ring_handle.checkMouseEvent(e);
	}
	
	// HANDLES //
	
	class HandleRing
	{
		Key key;
		boolean selected, hover;
		float ring_width = 10;
		
		HandleRing(Key key)
		{
			this.selected = false;
			this.hover = false;
			this.key = key;
		}
		
		void draw()
		{
			//p.stroke();
			if(p.main_windows.sheet.isCompositionMode())
			{
				p.stroke(colour[0]+70,colour[1]+70,colour[2]+70);
				p.strokeWeight(ring_width);
				p.noFill();
				p.ellipse(key.x, key.y, (key.d)-(ring_width/2), (key.d)-(ring_width/2));
			}
		}
		
		boolean withinBounds(float x_input, float y_input)
		{
			float dist_to_mouse = PApplet.dist(key.x, key.y, x_input, y_input); 
			
			if(dist_to_mouse >= (d/2)-8 && dist_to_mouse < (d/2))
			{
				return true;
			}
			return false;
		}
		
		boolean checkMouseEvent(MouseEvent e)
		{
			boolean within_bounds = withinBounds(e.getX(), e.getY());
			boolean mouse_state = false;
			
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
				key.endWidth(e.getX(), e.getY());
			}
			else if(e.getAction() == 4) // When mouse is dragged
			{
				if(selected)
				{
					mouse_state = true;
					key.doWidth(e.getX(), e.getY());
				}
			}
			else if(e.getAction() == 5) // When mouse is moved
			{
				if(within_bounds) {hover = true;}
				else {hover = false;}
			}
			return mouse_state;
		}
		
	}
	
	
	
	//=======================//
	// PRIMITIVE DATA OBJECT //
	//=======================//
	
	class PrimitiveData
	{
		Primitive primitive;
		float x, y, t, l, b, r, rt;
		PImage sprite;
		//float x, y, t, l, b, r, rotation;
		
		PrimitiveData(Primitive primitive)
		{
			this.x = 0; // Typically, delta x and y are local to the primitive
			this.y = 0;
			this.l = 0;
			this.r = 0;
			this.t = 0;
			this.b = 0;
			this.rt = 0;
			this.primitive = primitive;
		}
	
		void set(PrimitiveData data)
		{
			this.x = data.x;
			this.y = data.y;
			this.t = data.t;
			this.b = data.b;
			this.l = data.l;
			this.r = data.r;
			this.rt = data.rt;
		}
		
		void add(PrimitiveData data)
		{
			this.x += data.x;
			this.y += data.y;
			this.t += data.t;
			this.b += data.b;
			this.l += data.l;
			this.r += data.r;
			this.rt += data.rt;
		}
		
		PrimitiveData mult(float value)
		{
			PrimitiveData return_data = new PrimitiveData(primitive);
			return_data.setX(this.x*value);
			return_data.setY(this.y*value);
			return_data.setTop(this.t*value);
			return_data.setBottom(this.b*value);
			return_data.setLeft(this.l*value);
			return_data.setRight(this.r*value);
			return_data.setRotation(this.rt*value);
			
			return return_data;
		}
		
		void setX(float value)
		{
			this.x = value;
		}
		
		void setY(float value)
		{
			this.y = value;
		}
		
		void setLeft(float value)
		{
			this.l = value;
		}
		
		void setRight(float value)
		{
			this.r = value;
		}
		
		void setTop(float value)
		{
			this.t = value;
		}
		
		void setBottom(float value)
		{
			this.b = value;
		}
		
		void setRotation(float value)
		{
			this.rt = value;
		}
		
		void setSprite(PImage input_sprite)
		{
			this.sprite = input_sprite;
		}
		
		boolean matchesPrimitive(Primitive primitive)
		{
			if(this.primitive == primitive)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		
		void mergeCurrentStoredDeltas()
		{
			this.x += primitive.delta_local_x;
			this.y += primitive.delta_local_y;
			this.l += primitive.delta_l;
			this.r += primitive.delta_r;
			this.t += primitive.delta_t;
			this.b += primitive.delta_b;
			this.rt += primitive.delta_rotation;
			
			this.primitive.delta_local_x = 0;
			this.primitive.delta_local_y = 0;
			this.primitive.delta_l = 0;
			this.primitive.delta_r = 0;
			this.primitive.delta_t = 0;
			this.primitive.delta_b = 0;
			this.primitive.delta_rotation = 0;
		}
		
		void printData()
		{
			PApplet.println("\nDATA FOR " + this.primitive);
			PApplet.println("X/Y/ROT | " + this.x + ", " + this.y + ", " + this.rt);
			PApplet.println("T/B/L/R | " + this.t + ", " + this.b + ", " + this.l + ", " + this.r);
			
		}

	}
}
