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
	
	boolean showing_compiled_keys = false;
	
	// Buttons
	ButtonGoToKey button_goto_key = new ButtonGoToKey(x, y, 80, 25, p);
	
	Stage(int x, int y, int w, int h, AniSketch p)
	{
		super(x,y,w,h,p);
		
		this.camera = new PVector(x,y);
		
		default_style = new Style(p);
		//default_style.fill(255,70,0,255); // Orange
		//default_style.fill(250,228,78,255); // Mellow Yellow
		
		default_style.fill(150,150,150,255);
		
		primitives = new ArrayList<Primitive>();
		
		addPrimitive(100, 500, 100, 100, this, p.animation, p);
		addPrimitive(100, 400, 100, 100, this, p.animation, p);
		//addPrimitive(100, 300, 100, 100, this, p.animation, p);
		primitives.get(1).setParent(primitives.get(0));
		//primitives.get(2).setParent(primitives.get(1));
		
	}
	
	void exitActiveKey()
	{
		if(active_key != null)
		{
			for(Primitive primitive: primitives)
			{
				active_key.mergeDataFromPrimitive(primitive);
				primitive.endDeltaRecording();
			}	
			active_key.printDeltaData();
		}
		
		// For each primitive, reset its properties with the default key
		// The default key stores ABSOLUTE properties of the primitives
		// so we need to disble parent controls so that any primitives with children
		// do not accidentally affect their position when reset
		for(Primitive primitive: primitives)
		{
			primitive.disableParentControl();
		}
		
		// Next we apply properties from the default key
		for(Primitive primitive: primitives)
		{
			primitive.setPropertiesFromKey(p.animation.default_key);
		}
		
		// Then, if the primitives have a parent, re-enable parent control
		for(Primitive primitive: primitives)
		{
			primitive.enableParentControl();
		}
		
		// Set active key to null
		active_key = null;		
	}

	void startCompiledKeys()
	{
		showing_compiled_keys = true;
	}
	
	void stopCompiledKeys()
	{
		showing_compiled_keys = false;
		
		for(Primitive all_primitives: primitives)
		{
			all_primitives.disableParentControl();
		}
		
		p.println("Applying default key to all primitives");
		for(Primitive all_primitives: primitives)
		{
			all_primitives.setPropertiesFromKey(p.animation.default_key);
		}
	
		for(Primitive all_primitives: primitives)
		{
			all_primitives.resetLastParentOffset();
			all_primitives.enableParentControl();
		}
	}
	
	void setAllPrimitivesToDefaultKey()
	{
		for(Primitive all_primitives: primitives)
		{
			all_primitives.disableParentControl();
		}
		
		p.println("Applying default key to all primitives");
		for(Primitive all_primitives: primitives)
		{
			all_primitives.setPropertiesFromKey(p.animation.default_key);
		}
	
		for(Primitive all_primitives: primitives)
		{
			all_primitives.resetLastParentOffset();
			all_primitives.enableParentControl();
		}
	}
	
	// Overrides primitive properties with the values of a key
	void applyDeltaKeyToAllPrimitivesInOrder(Key default_key, Key delta_key)
	{
		// For all primitives, apply the default key to start off with
		// Find a list of primitives that DO NOT have parents. These top-level primitives are the objects that we will apply the key first
		// Step through the top-level primitives, applying deltas to the children. At each stage of the delta application, recusively apply parentControl() to update the children positions
		// It is useful to think about the primitive parent-child organisation as a tree - starting from the "roots" we iterate through the branches until we do not have anything left to iterate
		//resetLastParentOffset
		
		for(Primitive all_primitives: primitives)
		{
			all_primitives.disableParentControl();
		}
		
		//p.println("Applying default key to all primitives");
		for(Primitive all_primitives: primitives)
		{
			all_primitives.setPropertiesFromKey(default_key);
		}
	
		for(Primitive all_primitives: primitives)
		{
			all_primitives.resetLastParentOffset();
			all_primitives.enableParentControl();
		}
		
		ArrayList<Primitive> delta_update_order = new ArrayList<Primitive>();
		ArrayList<Primitive> current_primitive_branch = new ArrayList<Primitive>();
		
		for(Primitive those_primitives: primitives)
		{
			if(those_primitives.parent == null)
			{
				current_primitive_branch.add(those_primitives);
			}
		}
		
		//p.println("Creating ordered delta update list");
		while(current_primitive_branch.size() > 0)
		{
			ArrayList<Primitive> next_primitive_branch = new ArrayList<Primitive>();
			
			for(Primitive current_primitive: current_primitive_branch)
			{
				delta_update_order.add(current_primitive);
				
				if(current_primitive.hasChildren())
				{
					for(Primitive subchildren: current_primitive.children)
					{
						next_primitive_branch.add(subchildren);
					}
				}
			}
			current_primitive_branch = next_primitive_branch;
		}
		
		//p.println("Applying deltas in order");
		for(Primitive primitive_to_update: delta_update_order)
		{	
			primitive_to_update.addAllPropertiesFromKey(delta_key);
			primitive_to_update.resetLastParentOffset();
		}
	}
	
	void goToActiveKey(Key key)
	{
		if(p.main_windows.sheet.animation_mode == p.main_windows.sheet.COMPOSITION)
		{
			// If the key we are going to is NOT already the active key
			if(active_key != key)
			{
				// If there is no active key, set the new key. This will only be triggered once per key switch
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
				// For each primitive, override its property values to the default_key + active_key
				for(Primitive primitive: primitives)
				{
					applyDeltaKeyToAllPrimitivesInOrder(p.animation.default_key, active_key);
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
		
		updateButtons();
		drawButtons();
	}

	void handlePrimitiveDeletion()
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
		handlePrimitiveDeletion();
		
		for(int p=0; p<primitives.size(); p++)
		{
			primitives.get(p).update();
		}
	}
	
	Primitive addPrimitive(float x, float y, float w, float h, Stage stage, AnimationController a, AniSketch p)
	{
		Primitive new_primitive = new Primitive(x, y, w, h, this, a, p);
		primitives.add(new_primitive);
		buildPrimitiveSelectionRank();
		return new_primitive;
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
			checkButtonMouseEvent(e);
			
			for(int p=0; p<primitive_selection_rank.size(); p++)
			{
				primitives.get(p).checkMouseEvent(e, false);
			}
		}
	}

	public void drawButtons()
	{
		if(p.main_windows.sheet.active_key_selection != null)
		{
			button_goto_key.draw();
		}
	}
	
	public void checkButtonMouseEvent(MouseEvent e)
	{
		if(p.main_windows.sheet.active_key_selection != null)
		{
			button_goto_key.checkMouseEvent(e);
		}
	}
	
	public void updateButtons()
	{
		if(p.main_windows.sheet.active_key_selection != null)
		{
			button_goto_key.update();
		}
	}
	
	public class ButtonGoToKey extends Button{

		ButtonGoToKey(int x, int y, int w, int h, AniSketch p) 
		{
			super(x, y, w, h, p);
			setToToggle();
			setLabel("OPEN KEY");
		}
		
		@Override
		void update()
		{
			this.x = p.main_windows.stage.x + 10;
			this.y = p.main_windows.stage.y + 10;
		}
		
		@Override
		void toggleOnAction()
		{
			setLabel("EXIT KEY");
		}
		
		@Override
		void toggleOffAction()
		{
			setLabel("OPEN KEY");
		}
	}
}

