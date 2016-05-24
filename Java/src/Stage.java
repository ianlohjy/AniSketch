import java.io.File;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PImage;
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
	//ArrayList<Primitive> primitive_selection_rank; // Selection rank determines which primitive to select when the mouse is over multiple primitives
	Primitive last_selected_primitive;
	
	PVector camera;
	Sheet sheet;
	Key opened_key; // Currently selected key
	PImage background;
	float background_x = 0;
	float background_y = 0;
	
	// Selection handling
	ArrayList<Primitive> selectable_primitives;
	Primitive active_primitve_selection = null;
	//boolean showing_compiled_keys = false;
	
	// Buttons
	ButtonGoToKey button_goto_key = new ButtonGoToKey(x, y, 80, 25, p);
	ButtonLoadImage button_load_image = new ButtonLoadImage(x, y, 80, 25, p);
	ButtonMakeFish button_make_fish = new ButtonMakeFish(x, y, 80, 25, p);
	ButtonMakeBall button_make_ball = new ButtonMakeBall(x, y, 80, 25, p);
	ButtonMakeElephant button_make_elephant = new ButtonMakeElephant(x, y, 80, 25, p);
	ButtonLoadBackground button_load_background = new ButtonLoadBackground(x, y, 80, 25, p);
	
	Stage(int x, int y, int w, int h, Sheet sheet, AniSketch p)
	{
		super(x,y,w,h,p);
		
		this.camera = new PVector(x,y);
		this.sheet = sheet;
		
		default_style = new Style(p);
		//default_style.fill(255,70,0,255); // Orange
		//default_style.fill(250,228,78,255); // Mellow Yellow
		
		default_style.fill(150,150,150,255);
		
		primitives = new ArrayList<Primitive>();
		
		//addPrimitive(100, 500, 100, 100, this, sheet, p.animation, p);
		//addPrimitive(100, 400, 100, 100, this, sheet, p.animation, p);
		//addPrimitive(100, 300, 100, 100, this, sheet, p.animation, p);
		//primitives.get(1).setParent(primitives.get(0));
		//primitives.get(1).loadSprite("./resources/sprites/ball.svg");
		//primitives.get(2).setParent(primitives.get(1));
		
		//exampleMakeElephant();
		//exampleMakeBall();
		//exampleMakeFish();
	}
	
	// Adds existing delta recording from primitives to the currently active key
	// Resets the primitive properties with the default key
	void exitActiveKey()
	{
		if(opened_key != null)
		{
			for(Primitive primitive: primitives)
			{
				opened_key.mergeDataFromPrimitive(primitive);
				primitive.endDeltaRecording();
			}	
			opened_key.printDeltaData();
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
		default_style.fill(150,150,150,255); // Reset background color
		opened_key = null;		
	}

	// Overrides current primitives with the values of a delta key
	// Enables delta (key) recording of primitives
	// Sets the 'opened_key' value
	void goToActiveKey(Key key)
	{
		Utilities.printAlert("GOING TO ACTIVE KEY");
		if(sheet.animation_mode == p.main_windows.sheet.COMPOSITION)
		{
			// If the key we are going to is NOT already the active key
			if(opened_key != key)
			{
				// If there is no active key, set the new key. This will only be triggered once per key switch
				if(opened_key == null) 
				{
					opened_key = key;
				}
				// If there is an active key open, close the active key and set the new one
				else if(opened_key != null) 
				{
					exitActiveKey();
					opened_key = key;
				}
				// For each primitive, override its property values to the default_key + active_key
				for(Primitive primitive: primitives)
				{
					applyDeltaKeyToAllPrimitivesInOrder(p.animation.default_key, opened_key);
				}
			}
			
			if(opened_key != null)
			{
				setBackgroundColour(opened_key.colour[0],opened_key.colour[1],opened_key.colour[2]); // Override background color
				
				// Reset and begin a new delta recording for all primitives
				for(Primitive primitive: primitives)
				{
					primitive.startDeltaRecording();
				}
			}
		}
	}
	
	void setBackgroundColour(int r, int g, int b)
	{
		default_style.fill(r, g, b, 255);  // Override background color
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
	
	void deletePrimitive(Primitive to_delete)
	{
		int index_to_delete = primitives.indexOf(to_delete);
		
		if(index_to_delete != -1)
		{
			// Clear active primitive selection
			if(active_primitve_selection == primitives.get(index_to_delete))
			{
				active_primitve_selection = null;
			}
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
		
		p.rect(x, y, w, h);
		if(background != null)
		{
			if(p.animation.isPlaying() && opened_key == null){p.tint(255,255);}
			else{p.tint(255,100);}
			
			p.image(background, background_x, background_y, background.width, background.height);
			p.tint(255,255);
		}
		updatePrimitives();
		p.noClip();
	
		updateAndDrawButtons();
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
	}
	
	void updatePrimitives()
	{
		handlePrimitiveDeletion();
		
		for(int p=0; p<primitives.size(); p++)
		{
			primitives.get(p).update();
		}
	}
	
	Primitive addPrimitive(float x, float y, float w, float h, Stage stage, Sheet sheet, AnimationController a, AniSketch p)
	{
		Primitive new_primitive = new Primitive(x, y, w, h, this, sheet, a, p);
		primitives.add(new_primitive);
		//buildPrimitiveSelectionRank();
		return new_primitive;
	}
	
	/*
	void buildPrimitiveSelectionRank()
	{
		primitive_selection_rank = (ArrayList<Primitive>) primitives.clone();
		//last_selected_primitive = null;
	}
	*/
	
	boolean hasPrimitiveSelected()
	{
		if(active_primitve_selection != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	boolean not(Boolean value)
	{
		if(value == true)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	void checkMouseEvent(MouseEvent e)
	{
		if(withinBounds(e.getX(), e.getY()))
		{
			boolean buttons_in_use = checkButtonMouseEvent(e);
			
			if(!buttons_in_use)
			{
				ArrayList<Primitive> new_selectables = new ArrayList<Primitive>();
				Primitive last_active_selection = active_primitve_selection;
				boolean allowed_to_select = true;
				
				if(active_primitve_selection != null)
				{
					if(active_primitve_selection.checkMouseEventHandles(e))
					{
						allowed_to_select = false;
					}
				}
				
				for(Primitive cur_primitive: primitives)
				{
					int[] response = cur_primitive.checkMouseEvent(e, active_primitve_selection, selectable_primitives, allowed_to_select);
					
					if(response[0] == 1) // If the object was within bounds
					{
						new_selectables.add(cur_primitive);
					}	
					if(response[1] == 1) // If the object was selected
					{
						if(allowed_to_select)
						{
							active_primitve_selection = cur_primitive;
							allowed_to_select = false;
						}
					}
					if(response[1] == -1 && active_primitve_selection == cur_primitive) // If the object was deselected
					{
						active_primitve_selection = null;
					}
				}
				selectable_primitives = new_selectables;
			}
		}
	}

	void exampleMakeElephant()
	{
		String elephant_body_url = p.getResource("/resources/sprites/elephant_body.png");
		String elephant_leg_url = p.getResource("/resources/sprites/elephant_leg.png");
		
		Primitive elephant_leg1 = addPrimitive((w/2)-50, (h/2)+70, 33, 65, this, sheet, p.animation, p);
		elephant_leg1.loadSprite(elephant_leg_url);
		elephant_leg1.setPivot(0, 20);
		elephant_leg1.setPropertiesToDefaultKey();
		
		Primitive elephant_leg2 = addPrimitive((w/2)+40, (h/2)+70, 33, 65, this, sheet, p.animation, p);
		elephant_leg2.loadSprite(elephant_leg_url);
		elephant_leg2.setPivot(0, 20);
		elephant_leg2.setPropertiesToDefaultKey();
		
		Primitive elephant_body = addPrimitive(w/2, h/2, 215, 137, this, sheet, p.animation, p);
		elephant_body.loadSprite(elephant_body_url);
		elephant_body.setPropertiesToDefaultKey();
		
		Primitive elephant_leg3 = addPrimitive((w/2)-10, (h/2)+70, 33, 65, this, sheet, p.animation, p);
		elephant_leg3.loadSprite(elephant_leg_url);
		elephant_leg3.setPivot(0, 20);
		elephant_leg3.setPropertiesToDefaultKey();
		
		Primitive elephant_leg4 = addPrimitive((w/2)+80, (h/2)+70, 33, 65, this, sheet, p.animation, p);
		elephant_leg4.loadSprite(elephant_leg_url);
		elephant_leg4.setPivot(0, 20);
		elephant_leg4.setPropertiesToDefaultKey();
		
		elephant_leg1.setParent(elephant_body);
		elephant_leg2.setParent(elephant_body);
		elephant_leg3.setParent(elephant_body);
		elephant_leg4.setParent(elephant_body);
	}
	
	void exampleMakeBall()
	{
		String ball_url = p.getResource("/resources/sprites/ball.png");
		Primitive ball = addPrimitive((w/2), (h/2), 100, 100, this, sheet, p.animation, p);
		ball.loadSprite(ball_url);
		ball.setPropertiesToDefaultKey();
	}
	
	void exampleMakeFish()
	{
		String fish_url = p.getResource("/resources/sprites/fish.png");
		Primitive fish = addPrimitive((w/2), (h/2), 80, 38, this, sheet, p.animation, p);
		fish.loadSprite(fish_url);
		fish.setPropertiesToDefaultKey();
	}
	
	public void updateAndDrawButtons()
	{
		// Drawing 'open key' and 'load image' buttons 
		boolean showing_open_key_button = false;
		boolean showing_load_img_button = false;
		
		if(hasPrimitiveSelected())
		{
			showing_load_img_button = true;
		}
		if(p.main_windows.sheet.hasKeySelected() && p.main_windows.sheet.isCompositionMode())
		{
			showing_open_key_button = true;
		}
		if(showing_open_key_button && not(showing_load_img_button))
		{
			button_goto_key.x = this.x + 10;
			button_goto_key.y = this.y + 10;
			button_goto_key.draw();
		}
		else if(showing_load_img_button && not(showing_open_key_button))
		{
			button_load_image.x = this.x + 10;
			button_load_image.y = this.y + 10;
			button_load_image.updateLabelState();
			button_load_image.draw();
		}
		else if(showing_load_img_button && showing_open_key_button)
		{
			button_goto_key.x = p.main_windows.stage.x + 10;
			button_goto_key.y = p.main_windows.stage.y + 10;
			button_goto_key.draw();
			
			button_load_image.x = this.x + 10;
			button_load_image.y = button_goto_key.y + button_goto_key.h + 2;
			button_load_image.updateLabelState();
			button_load_image.draw();
		}
		
		// Drawing example object buttons
		if(!p.animation.isPlaying() && opened_key == null && sheet.isCompositionMode())
		{
			button_make_fish.update();
			button_make_ball.update();
			button_make_elephant.update();
			button_load_background.update();
			button_make_fish.draw();
			button_make_ball.draw();
			button_make_elephant.draw();
			button_load_background.draw();
		}
	}
	
	public boolean checkButtonMouseEvent(MouseEvent e)
	{
		boolean buttons_in_use = false; 
		
		if(hasPrimitiveSelected())
		{
			button_load_image.checkMouseEvent(e);
			if(button_load_image.hover) {buttons_in_use = true;}
		}
		if(p.main_windows.sheet.hasKeySelected() && p.main_windows.sheet.isCompositionMode())
		{
			button_goto_key.checkMouseEvent(e);
			if(button_goto_key.hover) {buttons_in_use = true;}
		}
		
		if(!p.animation.isPlaying() && opened_key == null && sheet.isCompositionMode())
		{
			button_make_fish.checkMouseEvent(e);
			button_make_ball.checkMouseEvent(e);
			button_make_elephant.checkMouseEvent(e);
			button_load_background.checkMouseEvent(e);
			
			if(button_make_fish.hover || button_make_ball.hover || button_make_elephant.hover || button_load_background.hover) 
			{
				buttons_in_use = true;
			}
		}
		
		return buttons_in_use;
	}
	
	public void loadBackground(String path)
	{
		background = p.loadImage(path);
		background_x = this.x + (this.w/2f) - (background.width/2f);
		background_y = this.y + (this.h/2f) - (background.height/2f);
	}
	
	public void clearBackground()
	{
		background = null;
	}
	
	//===============//
	// STAGE BUTTONS //
	//===============//
	
	public class ButtonMakeFish extends Button
	{
		ButtonMakeFish(int x, int y, int w, int h, AniSketch p) {
			super(x, y, w, h, p);
			setToPress();
			setLabel("CREATE FISH");
		}
		
		@Override
		void update()
		{
			this.x = p.main_windows.stage.x;
			this.y = p.main_windows.stage.y+p.main_windows.stage.h-h;
			this.w = p.main_windows.stage.w/4;
		}
		
		@Override
		void pressAction()
		{
			exampleMakeFish();
		}
	}
	
	public class ButtonMakeBall extends Button
	{
		ButtonMakeBall(int x, int y, int w, int h, AniSketch p) {
			super(x, y, w, h, p);
			setToPress();
			setLabel("CREATE BALL");
		}
		
		@Override
		void update()
		{
			this.x = p.main_windows.stage.x + p.main_windows.stage.w/4;
			this.y = p.main_windows.stage.y+p.main_windows.stage.h-h;
			this.w = p.main_windows.stage.w/4;
		}
		
		@Override
		void pressAction()
		{
			exampleMakeBall();
		}
	}
	
	public class ButtonMakeElephant extends Button
	{
		ButtonMakeElephant(int x, int y, int w, int h, AniSketch p) {
			super(x, y, w, h, p);
			setToPress();
			setLabel("CREATE ELEPHANT");
		}
		
		@Override
		void update()
		{
			this.x = p.main_windows.stage.x +(p.main_windows.stage.w*(2f/4f));
			this.y = p.main_windows.stage.y+p.main_windows.stage.h-h;
			this.w = p.main_windows.stage.w/4;
		}
		
		@Override
		void pressAction()
		{
			exampleMakeElephant();
		}
	}

	public class ButtonLoadBackground extends Button
	{
		ButtonLoadBackground(int x, int y, int w, int h, AniSketch p) {
			super(x, y, w, h, p);
			setToPress();
			setLabel("LOAD BACKGROUND");
		}
		
		@Override
		void update()
		{
			updateLabelState();
			this.x = p.main_windows.stage.x +(p.main_windows.stage.w*(3f/4f));
			this.y = p.main_windows.stage.y+p.main_windows.stage.h-h;
			this.w = p.main_windows.stage.w/4;
		}
		
		void updateLabelState()
		{
			if(background == null)
			{
				font_size = 12;
				setLabel("LOAD BACKGROUND");
			}
			else
			{
				if(hover)
				{
					font_size = 12;
					setLabel("CLEAR BACKGROUND");
				}
				else
				{
					font_size = 12;
					setLabel("BACKGROUND LOADED");
				}
			}
		}
		
		@Override
		void pressAction()
		{
			if(background != null)
			{
				Stage.this.clearBackground();
			}
			else
			{
				selectFile("Select an image to load");
			}
		}
		
		public void selectFile(String dialog_message)
		{
			p.selectInput(dialog_message, "selectedFileCallback", null, this);
		}
		
		public void selectedFileCallback(File selection)
		{
			if(selection != null)
			{
				String file = selection.getAbsolutePath();
				Utilities.printAlert("Selected file " + file);
				
				if(file.endsWith(".gif") 
				|| file.endsWith(".jpg") 
				|| file.endsWith(".tga") 
				|| file.endsWith(".png")
			    || file.endsWith(".GIF") 
				|| file.endsWith(".JPG") 
				|| file.endsWith(".TGA") 
				|| file.endsWith(".PNG"))
				{
					Utilities.printAlert("Loading image file");
					Stage.this.loadBackground(file);
				}
				else
				{
					Utilities.printAlert("Selected file not supported");
				}
			}
			else
			{
				Utilities.printAlert("No file selected");
			}
		}
	}
	
	public class ButtonLoadImage extends Button
	{
		ButtonLoadImage(int x, int y, int w, int h, AniSketch p) 
		{
			super(x, y, w, h, p);
			setToPress();
			setLabel("LOAD IMG");
		}
		
		@Override
		void pressAction()
		{
			if(active_primitve_selection != null)
			{
				if(active_primitve_selection.sprite != null)
				{
					active_primitve_selection.clearSprite();
				}
				else
				{
					selectFile("Select an image to load");
				}
			}	
		}
		
		void updateLabelState()
		{
			if(active_primitve_selection != null)
			{
				if(active_primitve_selection.sprite == null)
				{
					font_size = 12;
					setLabel("LOAD IMG");
				}
				else
				{
					if(hover)
					{
						font_size = 12;
						setLabel("CLEAR IMG");
					}
					else
					{
						font_size = 11;
						setLabel("IMG LOADED");
					}
				}
			}
		}
		
		public void selectFile(String dialog_message)
		{
			p.selectInput(dialog_message, "selectedFileCallback", null, this);
		}
		
		public void selectedFileCallback(File selection)
		{
			if(selection != null)
			{
				String file = selection.getAbsolutePath();
				Utilities.printAlert("Selected file " + file);
				
				if(file.endsWith(".gif") 
				|| file.endsWith(".jpg") 
				|| file.endsWith(".tga") 
				|| file.endsWith(".png")
			    || file.endsWith(".GIF") 
				|| file.endsWith(".JPG") 
				|| file.endsWith(".TGA") 
				|| file.endsWith(".PNG"))
				{
					if(active_primitve_selection != null)
					{
						Utilities.printAlert("Loading image file");
						active_primitve_selection.loadSprite(file);
					}
				}
				else
				{
					Utilities.printAlert("Selected file not supported");
				}
			}
			else
			{
				Utilities.printAlert("No file selected");
			}
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
		void toggleOnAction()
		{
			openKey();
		}
		
		@Override
		void toggleOffAction()
		{
			closeKey();
		}
		
		void openKey()
		{
			if(p.main_windows.sheet.active_key_selection != null)
			{
				goToActiveKey(p.main_windows.sheet.active_key_selection);
				setLabel("CLOSE KEY");
				if(!p.main_windows.sheet.active_key_selection.key_opened)
				{
					p.main_windows.sheet.active_key_selection.openKey();
				}
			}
		}
		
		void closeKey()
		{
			if(p.main_windows.sheet.active_key_selection != null)
			{
				exitActiveKey();
				setLabel("OPEN KEY");
				if(p.main_windows.sheet.active_key_selection.key_opened)
				{
					p.main_windows.sheet.active_key_selection.closeKey();
				}
			}
		}
		
		void checkKeyOpenStatus(Key key)
		{
			if(key != null)
			{
				if(key.key_opened)
				{
					openKey();
					this.pressed = true;
					//setLabel("CLOSE KEY");
				}
				else
				{
					closeKey();
					this.pressed = false;
					//this.pressed = false;
					//setLabel("OPEN KEY");
				}
			}
		}
	}
}

