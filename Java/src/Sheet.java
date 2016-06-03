import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Sheet extends Element{

	Style default_style;
	//ArrayList<PVector> drawn_points;
	PVector camera;
	boolean drawing;
	//long time_since_last_point = System.currentTimeMillis();
	
	static final int UP = 0;
	static final int DOWN = 1;
	static final int DRAW = 0;
	static final int COMPOSITION = 1;
	
	
	//int framerate = 25; // The framerate sets limits the number of points per second that you are able to draw
	//int millis_per_frame = 1000/25;
	
	int mouse_state = UP;
	AnimationController a;
	
	// SELECTION //
	Key active_key_selection; // Currently selected key (There can only be one key selected at a time)
	Stroke active_stroke_selection; 
	
	int number_keys_under_mouse = 0;
	ArrayList<Key> possible_selections;
	
	int animation_mode = COMPOSITION;
	ButtonToggleMode button_toggle_mode = new ButtonToggleMode(80, 25, p);
	ButtonKeyColour button_key_colour = new ButtonKeyColour(80, 25, p);
	
	Sheet(int x, int y, int w, int h, AniSketch p, AnimationController a)
	{
		super(x,y,w,h,p);
		default_style = new Style(p);
		default_style.fill(230,230,230,255);
		//drawn_points = new ArrayList<PVector>();
		drawing = false;
		this.a = a;
		possible_selections = new ArrayList<Key>();
	}
	
	void draw()
	{
		p.clip(x, y, w, h);
		default_style.apply();
		p.rect(x, y, w, h);
		
		p.blendMode(p.MULTIPLY);
		drawKeys();
		drawKeyShapes();
		p.blendMode(p.NORMAL);
		
		drawStrokes();
		p.blendMode(p.NORMAL);
		
		p.noClip();
		p.fill(0);
		
		/*
		if(animation_mode == COMPOSITION)
		{
			p.text("COMPOSITION MODE", 5, this.h - 20);
		}
		else if(animation_mode == DRAW)
		{
			p.text("DRAWING MODE", 5, this.h - 20);
		}
		*/	
		//p.text("Number of keys under mouse: " + possible_selections.size(), 5, this.h - 10);
		//p.text("Frame " + a.current_frame, 5, this.h - 30);
		
		update();
		
		updateAndDrawButtons();
	}
	
	void drawKeyShapes()
	{
		a.keyshapes.draw();
	}
	
	void drawKeys()
	{
		for(Key key: a.delta_keys)
		{
			key.draw();
		}
	}
	
	void drawStrokes()
	{
		for(Stroke stroke: a.strokes)
		{
			stroke.draw();
		}
		
		if(a.recorded_stroke != null)
		{
			a.recorded_stroke.draw();
		}
	}
	
	void update()
	{
		if(drawing)
		{
			a.recordStroke(p.mouseX, p.mouseY);
		}
		else
		{
			a.stopStroke();
		}
		//drawMotionLine();
	}
	
	void switchToDrawingMode()
	{
		Utilities.printAlert("Switched to drawing mode");
		p.main_windows.stage.exitActiveKey();
		animation_mode = DRAW;
		//p.main_windows.stage.startCompiledKeys();
	}
	
	void switchToCompositionMode()
	{
		Utilities.printAlert("Switched to composition mode");
		
		p.main_windows.stage.setAllPrimitivesToDefaultKey();
		
		animation_mode = COMPOSITION;
		//p.main_windows.stage.stopCompiledKeys();
		if(active_key_selection != null && active_key_selection.key_opened)
		{
			p.main_windows.stage.goToActiveKey(active_key_selection);
		}
	}
	
	boolean isCompositionMode()
	{
		if(animation_mode == COMPOSITION)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	boolean isDrawMode()
	{
		if(animation_mode == DRAW)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	boolean hasKeySelected()
	{
		if(active_key_selection != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	// If an object is selected, we wont select anything new UNLESS it is a click
	// Mouse event checking needs to return a true 
	void checkMouseEvent(MouseEvent e)
	{
		// Check event, if it is a click, we know to change selection, 
		// 
		boolean within_bounds = withinBounds(e.getX(), e.getY());
		boolean buttons_in_use = checkButtonMouseEvent(e);
		
		//a.keyshapes.getWeights(e.getX(), e.getY());
		if(within_bounds && !buttons_in_use)
		{
			if(animation_mode == COMPOSITION)
			{
				boolean allow_selection_switch = false;
				ArrayList<Key> t_possible_selections = new ArrayList<Key>();
				int[] mouse_status;
				
				boolean selection_has_switched = false;
				
				Key last_active_key = active_key_selection; 
				
				// Check key mouse selection incrementally
				// If there is an active key selection, DO NOT switch selection unless:
				// 1. A mouse click event is detected
				// 2. Or the active key selection is NOT in the selectable object list
				// If there is no active selection:
				// 1. The key with the oldest last selected time becomes the new selected key
				
				
				for(Key key: a.delta_keys)
				{	
					mouse_status = key.checkMouseEvent(e, active_key_selection, possible_selections, !selection_has_switched);	
					
					if(mouse_status[0] == 1)
					{
						t_possible_selections.add(key);
					}
					
					if(mouse_status[1] == 1)
					{
						if(!selection_has_switched)
						{
							p.println("SWITCHING ACTIVE KEY");
							//found_selection = true;
							active_key_selection = key;
						}
						selection_has_switched = true;
					}	
					else if(mouse_status[1] == -1 && active_key_selection == key)
					{
						active_key_selection = null;
					}
				}
				possible_selections = t_possible_selections;
				if(last_active_key != active_key_selection)
				{
					if(active_key_selection != null)
					{
						// If there is a key selected, pass the key to the 'open key' button for handling.
						p.main_windows.stage.button_goto_key.checkKeyOpenStatus(active_key_selection);
					}
					else if(active_key_selection == null)
					{
						p.main_windows.stage.exitActiveKey();
					}
				}
				
				// Stroke selection
				active_stroke_selection = null;
				for(Stroke stroke: a.strokes)
				{
					stroke.checkMouseEvent(e);
					if(stroke.selected)
					{
						active_stroke_selection = stroke;
					}
				}	
			}	
			else if(animation_mode == DRAW)
			{
				if(within_bounds)
				{
					//p.main_windows.stage.showCompiledKeys(e.getX(), e.getY());
					
					if(e.getButton() == 37)
					{ // If its a left click
						if(e.getAction() == 1)// Mouse Pressed
						{
							drawing = true;
						}
						else if(e.getAction() == 2)// Mouse Released
						{
							//a.stopStroke();
							drawing = false;
						}
						else if(e.getAction() == 3) // Mouse Clicked
						{
						}
						else if(e.getAction() == 4) // Mouse Dragged
						{
							//a.recordStroke(e.getX(), e.getY());
						}
						else if(e.getAction() == 5)
						{
						}
					}
				}
			}
		}
	}
	
	/*
	void drawMotionLine()
	{
		p.stroke(0);
		p.strokeWeight(10);
		
		if(drawn_points != null && drawn_points.size()>1)
		{
			for(int l=0; l<drawn_points.size()-1; l++)
			{
				p.line(drawn_points.get(l).x, drawn_points.get(l).y, drawn_points.get(l+1).x, drawn_points.get(l+1).y);
			}
		}
	}
	*/
	
	public void updateAndDrawButtons()
	{
		button_toggle_mode.x = this.x + 10;
		button_toggle_mode.y = this.y + 10;
		button_toggle_mode.draw();
		
		if(active_key_selection != null)
		{
			button_key_colour.x = this.x + 10;
			button_key_colour.y = button_toggle_mode.y + button_toggle_mode.h + 2;
			button_key_colour.draw();
			button_key_colour.drawPalette();
		}
	}
	
	public boolean checkButtonMouseEvent(MouseEvent e)
	{
		button_toggle_mode.checkMouseEvent(e);
		
		if(active_key_selection != null)
		{
			button_key_colour.checkMouseEvent(e);
		}
		
		if(button_toggle_mode.hover || button_key_colour.hover || button_key_colour.palette.hover)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public class ButtonToggleMode extends Button{

		ButtonToggleMode(int w, int h, AniSketch p) 
		{
			super(0, 0, w, h, p);
			setToToggle();
			setLabel("COMP");
		}
		
		@Override
		void update()
		{
			this.x = p.main_windows.sheet.x + 10;
			this.y = p.main_windows.sheet.y + 10;
		}
		
		@Override
		void toggleOnAction()
		{
			setLabel("DRAWING");
			switchToDrawingMode();
		}
		
		@Override
		void toggleOffAction()
		{
			setLabel("COMP");
			switchToCompositionMode();
		}
	}
	
	public class ButtonKeyColour extends Button
	{
		ColourPalette palette;
		boolean show_palette = false;
		
		ButtonKeyColour(int w, int h, AniSketch p) 
		{
			super(0, 0, w, h, p);
			setToPress();
			setLabel("COLOUR");
			palette = new ColourPalette((h*3)+2, h*3, p); // We add a little more width so that we can have a small space
		}
		
		@Override
		void mouseEventCallback(MouseEvent e)
		{
			//if(this.hover)
			//{
			palette.checkMouseEvent(e);
			//}
			
			if(palette.hover)
			{
				show_palette = true;
			}
			else
			{
				show_palette = false;
			}
		}
		
		void drawPalette()
		{
			if(show_palette)
			{
				palette.x = (int)(ButtonKeyColour.this.x + ButtonKeyColour.this.w);
				palette.y = (int)(ButtonKeyColour.this.y);
				palette.draw();
			}
		}
		
		class ColourPalette extends Element
		{
			int[][][] colours;
			int[] selection = {0,0};
			int[] active_selection = {0,0};
			ColourPalette(int w, int h, AniSketch p) 
			{
				super(0, 0, w, h, p);
				setupColours();
			}
			boolean button_last_hover = false;
			boolean allow_open = false;
			
			void setupColours()
			{
				// Colours have been taken from the Google Material Design Specification
				// @ https://www.google.com/design/spec/style/color.html#color-color-palette
				
				int[] red     = {244,67,54 };
				int[] teal    = {0,150,136 };
				int[] blue    = {30,136,229};
				int[] orange  = {255,87,34 };
				int[] green   = {76,175,80 };
				int[] l_blue  = {41,182,246};
				int[] amber   = {255,193,7 };
				int[] l_green = {139,195,74};
				int[] b_grey  = {96,125,139};
				
				colours = new int[3][3][3];
				colours[0][0] = red;
				colours[1][0] = teal;
				colours[2][0] = blue;
				colours[0][1] = orange;
				colours[1][1] = green;
				colours[2][1] = l_blue;
				colours[0][2] = amber;
				colours[1][2] = l_green;
				colours[2][2] = b_grey;
			}
			
			void mouseInputResponse(MouseEvent e)
			{
				// Checking what colour the mouse is over
				
				if(ButtonKeyColour.this.withinBounds(e.getX(),e.getY()))
				{
					//hover = true;
				}
				else
				{
					//hover = false;
				}
				
				if(withinBounds(e.getX(), e.getY()))
				{
					ButtonKeyColour.this.hover = true;
					
					if(active_key_selection != null)
					{
						active_selection = findKeyColourPosition(active_key_selection);
					}
					
					int pos_x = (int)(((e.getX()-this.x)/(float)this.w)*3);
					int pos_y = (int)(((e.getY()-this.y)/(float)this.h)*3);
					
					//drawBoundingBox();
							
					selection[0] = pos_x;
					selection[1] = pos_y;
					
					if(pressed && active_key_selection != null)
					{
						active_key_selection.updateColour(colours[pos_x][pos_y]);
					}
				}
				
				
				//ButtonKeyColour.this.hover
			}
			
			int[] findKeyColourPosition(Key key)
			{
				int[] position = {-1, -1};
				
				for(int cx=0; cx<colours.length; cx++)
				{
					for(int cy=0; cy<colours[cx].length; cy++)
					{
						if(key.colour[0] == colours[cx][cy][0]
						&& key.colour[1] == colours[cx][cy][1]
						&& key.colour[2] == colours[cx][cy][2])
						{
							position[0] = cx;
							position[1] = cy;
							return position;
						}
					}		
				}
				return position;
			}
			
			void draw()
			{
				//p.println(colours[0][0]);
				float colour_box_width = this.w/3f;
				
				for(int cx=0; cx<colours.length; cx++)
				{
					for(int cy=0; cy<colours[cx].length; cy++)
					{
						int opacity = 125;
						if(cx == selection[0] && cy == selection[1])
						{
							if(hover)
							{
								opacity = 200;
							}
							if(pressed)
							{
								opacity = 255;
							}
						}	
						
						if(cx == active_selection[0] && cy == active_selection[1])
						{
							opacity = 255;
						}	
						
						p.noStroke();
						p.fill(colours[cx][cy][0], colours[cx][cy][1], colours[cx][cy][2], opacity);
						p.rect(2+this.x+(cx*colour_box_width), this.y+(cy*colour_box_width), colour_box_width, colour_box_width);
					}
				}
			}
		}
	}
	

}




