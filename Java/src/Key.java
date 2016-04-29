import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Key {

	float x;
	float y;
	float d;
	float deviation_width;
	float[] color = new float[3];
	
	AniSketch p;
	ArrayList<DeltaData> deltas;
	
	float[][] shape;

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
	// Transform Data
	PVector transform_offset; // General purpose var for holding transform info

	// SELECTION // 
	long last_time_selected = 0;
	
	Key(float x, float y, float d, AniSketch p)
	{
		this.x = x;
		this.y = y;
		this.d = d;
		this.p = p;
		
		deltas = new ArrayList<DeltaData>();
		color = Utilities.randomColorPallete();
		cacheCircle(30);
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
		for(DeltaData data: deltas)
		{
			data.printDeltaData();
		}
	}
	
	// DATA HANDLING //
	
	// Add the stored deltas from a primtive to the key, and reset the stored deltas from the primitive to 0
	// If the primitive data does not exist, it is created first
	public void mergeDeltasFromPrimitive(Primitive primitive)
	{
		DeltaData found_data = doesDeltaDataExistForPrimitive(primitive);
		
		if(found_data == null)
		{
			DeltaData new_data = new DeltaData(primitive);
			new_data.mergeCurrentStoredDeltas();
			deltas.add(new_data);
		}
		else if(found_data != null)
		{
			found_data.mergeCurrentStoredDeltas();
		}
	}
	
	// Removes the data object for a primitive (usually in cases where the primitive is deleted)
	public void removeDeltaDataForPrimitive(Primitive primitive)
	{	
		for(DeltaData data: deltas)
		{
			DeltaData found_data = doesDeltaDataExistForPrimitive(primitive);
			
			if(found_data != null)
			{
				deltas.remove(deltas.indexOf(found_data));
			}
		}
	}
	
	// Checks if delta data for a primitive already exists
	public DeltaData doesDeltaDataExistForPrimitive(Primitive primitive)
	{
		for(DeltaData data: deltas)
		{
			if(data.matchesPrimitive(primitive))
			{
				return data;
			}
		}
		return null;
	}
	
	// MAIN //
	public void update()
	{
		draw();
	}
	
	// DRAWING // 
	public void draw()
	{
		p.noStroke();
		p.fill(color[0],color[1],color[2],255);		
		// Insert center vertex
		p.beginShape(p.TRIANGLE_FAN);
		p.vertex(x,y);
		
		//p.fill(color[0],color[1],color[2],0);
		p.fill(color[0]+150,color[1]+150,color[2]+150);
		for(float[] point: shape)
		{
			p.vertex((d/2*point[0]) + x,(d/2*point[1]) + y);
		}
		p.endShape();
		
		if(hover)
		{
			p.noFill();
			p.strokeWeight(3);
			p.stroke(230);
			p.ellipse(x,y,d,d);
		}
		
		if(selected)
		{
			p.noFill();
			p.strokeWeight(3);
			p.stroke(50);
			p.ellipse(x,y,d,d);
		}
		
		p.noStroke();
		if(p.main_windows.sheet.active_key_selection == this)
		{
			p.fill(255,0,0);
		}
		else
		{
			p.fill(0,0,0);
		}
		
		p.rectMode(p.CENTER);
		p.rect(x, y, 5, 5);
		p.rectMode(p.CORNER);
		
		p.text(Long.toString(last_time_selected), x, y);
		//p.println(last_time_selected);
	}
	
	public float getWeight(float x_input, float y_input)
	{
		return Utilities.gaussian1d(x_input, this.x, this.d/6f) * Utilities.gaussian1d(y_input, this.y, this.d/6f);
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
	public boolean lastSelectionTimeIsLowest(ArrayList<Key> selectable_keys)
	{
		// Checks if the current Key's last selected time is the lowest of all selectable keys
		// Returns true if it is the lowest
		p.println("HI " + selectable_keys.size());
		for(Key other_keys: selectable_keys)
		{
			p.println("HI");
			//p.println(other_keys.last_time_selected);
			if(other_keys.last_time_selected < this.last_time_selected)
			{
				
				return false;
			}
		}
		return true;
	}
	
	public boolean selectableKeysContainsKey(ArrayList<Key> selectable_keys, Key key)
	{
		return selectable_keys.contains(key);
	}
	
	public int[] checkMouseEvent(MouseEvent e, Key active_key_selection, ArrayList<Key> selectable_keys, boolean allow_switching)
	{
		boolean within_bounds = withinBounds(e.getX(), e.getY());
		// Mouse state communicates how the Key has processed the mouse event
		// mouse_status[0] = if the mouse is within the bounds of the object
		// mouse_status[1] = if the Key has become the active selection
		
		int[] mouse_status = {0,0}; 
		
	
		if(e.getAction() == 1)// Mouse Pressed
		{
			if(e.getButton() == 37) // If it was a left-click
			{
				// If withing bounds
				if(within_bounds)
				{
					// If the active key is NOT selectable 
					if(!selectableKeysContainsKey(selectable_keys, active_key_selection))
					{
						// If there is no active key OR the active key is out of bounds
						if(active_key_selection == null || !active_key_selection.withinBounds(e.getX(), e.getY()))
						{
							if(lastSelectionTimeIsLowest(selectable_keys))
							{
								selected = true;
								last_time_selected = System.currentTimeMillis();
								mouse_status[1] = 1;
								p.println("PRESS");
							}
						}
					}
				}
				else
				{
					selected = false;
					mouse_status[1] = -1;
				}
			}
		}
		else if(e.getAction() == 2)// Mouse Released
		{
			if(e.getButton() == 37) // If it was a left-click
			{
				endTranslate(e.getX(), e.getY());
			}
		}
		else if(e.getAction() == 3) // Mouse Clicked
		{
			if(e.getButton() == 37) // If it was a left-click
			{			
				// Clicking will cycle through selections
				if(within_bounds)
				{
					if(active_key_selection == this)
					{
						p.println("CLICK");
						if(selectableKeysContainsKey(selectable_keys, this) && selectable_keys.size() != 1)
						{
							selected = false;
							mouse_status[1] = -1;
						}
						//selected = false;
						//mouse_status[1] = 0;
					}
					else
					{
						if(lastSelectionTimeIsLowest(selectable_keys))
						{
							if(allow_switching)
							{
								for(Key other_keys: selectable_keys)
								{
									other_keys.selected = false;
								}
								
								selected = true;
								last_time_selected = System.currentTimeMillis();
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
				
				if(selected)
				{
					if(e.getButton() == 37)
					{
						doTranslate(e.getX(), e.getY());
					}	
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
		
		return mouse_status;
	}
	
	class DeltaData
	{
		
		Primitive primitive;
		float x, y, t, l, b, r, rotation;
		
		DeltaData(Primitive primitive)
		{
			this.x = 0; // Typically, delta x and y are local to the primitive
			this.y = 0;
			this.l = 0;
			this.r = 0;
			this.t = 0;
			this.b = 0;
			this.rotation = 0;
			this.primitive = primitive;
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
			this.rotation += primitive.delta_rotation;
			
			this.primitive.delta_local_x = 0;
			this.primitive.delta_local_y = 0;
			this.primitive.delta_l = 0;
			this.primitive.delta_r = 0;
			this.primitive.delta_t = 0;
			this.primitive.delta_b = 0;
			this.primitive.delta_rotation = 0;
		}
		
		void printDeltaData()
		{
			PApplet.println("DELTA DATA");
			PApplet.println("==========");	
			PApplet.println("LOCAL X  | " + this.x);
			PApplet.println("LOCAL Y  | " + this.y);
			PApplet.println("ROTATION | " + this.rotation);
			PApplet.println("TOP      | " + this.t);
			PApplet.println("BOTTOM   | " + this.b);
			PApplet.println("LEFT     | " + this.l);
			PApplet.println("RIGHT    | " + this.r);
			
		}
		
	}
}
