import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Stage extends Element{

	Style default_style;
	//Primitive test_child;
	//Primitive test_subchild;
	//Primitive test_parent;
	float count = 1;
	ArrayList<Primitive> primitives;
	ArrayList<Primitive> primitive_delete_list;
	ArrayList<Primitive> primitive_selection_rank; // Selection rank determines which primitive to select when the mouse is over multiple primitives
	Primitive last_selected_primitive;
	
	PVector camera;
	
	
	Key active_key; // Currently selected key
	
	Stage(int x, int y, int w, int h, AniSketch p)
	{
		super(x,y,w,h,p);
		
		this.camera = new PVector(x,y);
		
		default_style = new Style(p);
		//default_style.fill(255,70,0,255); // Orange
		//default_style.fill(250,228,78,255); // Mellow Yellow
		
		default_style.fill(150,150,150,255);
		
		primitives = new ArrayList<Primitive>();
		
	}
	
	void exitActiveKey()
	{
		if(active_key != null)
		{
			for(Primitive primitive: primitives)
			{
				active_key.mergeDeltasFromPrimitive(primitive);
				primitive.endDeltaRecording();
				active_key.printDeltaData();
			}	
		}
		// Set active key to null
		active_key = null;
	}
	
	void goToActiveKey(Key key)
	{
		// If the key we are going to is NOT already the active key
		if(active_key != key)
		{
			// If there is no active key, set the new key
			if(active_key == null)
			{
				active_key = key;
			}
			// If there is an active key open, close the active key and set the new one
			else if(active_key != null) 
			{
				exitActiveKey();
				active_key = key;
			}
		}
		
		if(active_key != null)
		{
			// Reset and begin a new delta recording for all primitives
			for(Primitive primitive: primitives)
			{
				primitive.startDeltaRecording();
			}
		}
	}
	
	void deletePrimitive(Primitive to_delete)
	{
		int index_to_delete = primitives.indexOf(to_delete);
		
		if(index_to_delete != -1)
		{
			primitives.remove(index_to_delete);
		}
		else
		{
			PApplet.println("Primitive does not exist on stage. Cannot delete");
		}
	}
	
	void draw()
	{
		p.clip(x, y, w, h);

		default_style.apply(); // Apply style for Stage window
		
		if(active_key != null)
		{
			p.fill(active_key.color[0],active_key.color[1],active_key.color[2]); // Override background color if a key is active (selected)
		}
		
		p.rect(x, y, w, h);
		updatePrimitives();
		p.noClip();
	}

	void handleDelete()
	{
		for(int p=0; p<primitives.size(); p++)
		{
			if(primitives.get(p).marked_for_deletion)
			{
				deletePrimitive(primitives.get(p));
				p--;
			}
		}		
		buildPrimitiveSelectionRank();
	}
	
	void updatePrimitives()
	{
		handleDelete();
		
		for(int p=0; p<primitives.size(); p++)
		{
			primitives.get(p).update();
		}
	}
	
	void addPrimitive(float x, float y, float w, float h, Stage stage, AnimationController a, AniSketch p)
	{
		primitives.add(new Primitive(x, y, w, h, this, a, p));
		buildPrimitiveSelectionRank();
	}
	
	void buildPrimitiveSelectionRank()
	{
		primitive_selection_rank = (ArrayList<Primitive>) primitives.clone();
		//last_selected_primitive = null;
	}
	
	void checkMouseEvent(MouseEvent e)
	{
		if(withinBounds(e.getX(), e.getY()))
		{
			for(int p=0; p<primitive_selection_rank.size(); p++)
			{
				primitives.get(p).checkMouseEvent(e, false);
			}
				
			// Handle selection
			boolean found_selection = false;
			boolean keep_selected = false;
			int selected_primitive_index = 0;
			
			/*
			if(last_selected_primitive != null)
			{
				if(last_selected_primitive.selected)
				{
					keep_selected = last_selected_primitive.checkMouseEventHandles(e);
				}
			}
			*/
			
			/*
			for(int p=0; p<primitive_selection_rank.size(); p++)
			{
				// If a primitive is selected and its handles are active, do not check for selection on primitives
				if(keep_selected || found_selection)
				{
					primitive_selection_rank.get(p).checkMouseEvent(e, true); 
				}
				else if(!keep_selected) // If selected primitive's handles are not active, check other objects for selection
				{
					primitive_selection_rank.get(p).checkMouseEvent(e, false);
					
					if(primitive_selection_rank.get(p).selected) // If object is selected
					{
						found_selection = true;
						selected_primitive_index = p;
						last_selected_primitive = primitive_selection_rank.get(p);
					}
				}
			}
			
			if(found_selection)
			{
				primitive_selection_rank.add(primitive_selection_rank.remove(selected_primitive_index));
			}
			*/
		}
	}
}

