import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Timeline extends Element{
	Style default_style;
	AnimationController a;
	
	TimelineBar timeline_bar;
	CursorHandle cursor_handle;
	StrokeHandle stroke_handle;
	ButtonPlay button_play;
	ButtonStop button_stop;
	ButtonLoop button_loop;
	
	Timeline(int x, int y, int w, int h, AniSketch p, AnimationController a)
	{
		super(x,y,w,h,p);
		this.a = a;
		default_style = new Style(p);
		default_style.fill(30,30,30,255);
		timeline_bar = new TimelineBar(5, this, p);
		cursor_handle = new CursorHandle(timeline_bar, p);
		stroke_handle = new StrokeHandle(timeline_bar, p);
		button_play = new ButtonPlay(50, 25, timeline_bar, p);
		button_stop = new ButtonStop(50, 25, timeline_bar, p);
		button_loop = new ButtonLoop(50, 25, timeline_bar, p);
	}
	
	void draw()
	{
		if(hover && pressed)
		{
			default_style.fill(25,25,25,255);
		}
		else if(hover)
		{
			default_style.fill(35,35,35,255);
		}
		else
		{
			default_style.fill(30,30,30,255);
		}
		
		default_style.apply();
		p.rect(x, y, w, h);

		timeline_bar.draw();
		cursor_handle.draw();
		stroke_handle.draw();
		
		button_play.update();
		button_play.draw();
		button_stop.update();
		button_stop.draw();
		button_loop.update();
		button_loop.draw();
	}
	
	void checkMouseEvent(MouseEvent e)
	{
		boolean within_bounds = withinBounds(e.getX(), e.getY());	
		
		timeline_bar.checkMouseEvent(e);
		cursor_handle.checkMouseEvent(e);
		stroke_handle.checkMouseEvent(e);
		button_play.checkMouseEvent(e);
		button_stop.checkMouseEvent(e);
		button_loop.checkMouseEvent(e);
	}
	
	///////////////////////////////////
	
	class TimelineBar extends Element
	{
		Style base_style;
		Style progression_style;
		Timeline t;
		int font_size = 14;
		int side_margin = 50;
		
		public TimelineBar(int h, Timeline t, AniSketch p) 
		{
			super(0,0,0,h,p);
			this.t = t;
			setupStyles();
		}
		
		void setupStyles()
		{
			base_style = new Style(p);
			base_style.noStroke();
			base_style.fill(255,255,255,255);
			progression_style = new Style(p);
			progression_style.noStroke();
			//progression_style.fill(150,150,150,255);
			progression_style.fill(194,53,51,255);
			//p.fill();
		}
		
		void draw()
		{
			float progression = (float)a.current_frame/a.frame_range[1];
			if(progression > 1) {progression = 1;}
			
			this.x = t.x+side_margin;
			this.y = t.y+(int)(t.h*0.30f)-(int)(h*0.30f);
			this.w = t.w-(side_margin*2);
			
			base_style.apply();
			p.rect(x, y, w, h);
			
			progression_style.apply();
			p.rect(x, y, w*progression, h);
			
			p.fill(255);
			p.textFont(p.default_font);
			p.textSize(14);
			
			p.textAlign(PApplet.LEFT, PApplet.BOTTOM);
			p.text("F0", x, y-3);
			
			p.textAlign(PApplet.RIGHT, PApplet.BOTTOM);
			if(a.recording_stroke && a.frame_range[1] < a.current_frame)
			{p.text("F"+a.current_frame, x+w, y-3);}
			else 
			{p.text("F"+a.frame_range[1], x+w, y-3);}
			
			p.textAlign(PApplet.LEFT, PApplet.BOTTOM);
		}
	}
	
	class CursorHandle extends Element
	{
		boolean locked_to_animation = true;
		int min_width = 30;
		int width_buffer = 5;
		int font_size = 14;
		TimelineBar b;
		String label = "";
		float progression = 0.5f;
		
		boolean moving = false;
		float init_progression = 0;
		float init_x_pos = 0;
		
		public CursorHandle(TimelineBar b, AniSketch p) 
		{
			super(0,0,0,25,p);
			this.b = b;
		} 	
		
		void updateWidth()
		{
			this.w = (int)findWidth(label);
		}
		
		float findWidth(String cursor_label)
		{
			p.textFont(p.default_font);
			p.textSize(font_size);
			
			int new_width = (int)p.textWidth(cursor_label) + width_buffer;
			
			if(new_width+width_buffer > min_width)
			{
				return new_width+width_buffer;
			}
			else
			{
				return min_width;
			}
		}
		
		void updatePosition()
		{
			int left_limit = (Timeline.this.w - b.w)/2;
			int right_limit = Timeline.this.w - left_limit;
			
			if(progression > 1)
			{
				progression = 1;
			}
			this.x = left_limit+(int)(progression*(b.w-this.w));
			this.y = b.y-h;
		}
		
		float findProgressionForFrame(int frame)
		{
			if(frame < 0)
			{
				return 0;
			}
			else if(frame > a.frame_range[1])
			{
				return 1;
			}
			else
			{
				return (float)frame/a.frame_range[1];
			}
		}
		
		int findFrameForProgression(float progression)
		{
			if(progression < 0)
			{
				return 0;
			}
			else if(progression > 1)
			{
				return (int)a.frame_range[1];
			}
			else
			{
				return (int)(progression*(float)a.frame_range[1]);
			}
		}
		
		void draw()
		{
			if(!moving)
			{
				progression = findProgressionForFrame((int)a.current_frame);
			}
			
			this.label = "F" + Long.toString(a.current_frame);
			updateWidth();
			updatePosition();
			
			if(hover)
			{
			p.fill(239,59,57);
			}
			else
			{
			p.fill(194,53,51);
			}
			p.rect(x, y, w, h);
			p.rect((int)(b.x+(progression*(b.w-5))), b.y, 5, b.h);
			
			p.textSize(14);
			//p.textFont(p.consolas_b);
			
			p.fill(255);
			p.textAlign(PApplet.CENTER, PApplet.BOTTOM);

			p.text(label, x+(w/2), y+h-3);
			p.textAlign(PApplet.LEFT, PApplet.BOTTOM);
		}
		
		void checkMouseEvent(MouseEvent e)
		{
			boolean within_bounds = withinBounds(e.getX(), e.getY());	
			
			if(e.getAction() == 1) // When mouse is pressed (down)
			{
				if(within_bounds)
				{
					pressed = true;
				}
				else
				{
					pressed = false;
				}
			}
			else if(e.getAction() == 2) // When mouse is released
			{
				pressed = false;
				endTranslate();
			}
			else if(e.getAction() == 3) // When mouse is clicked (down then up)
			{
			}
			else if(e.getAction() == 4) // When mouse is dragged
			{
				if(pressed)
				{
					doTranslate(e.getX(), e.getY());
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
		}

		float findProgression(float x_input)
		{
			float progression = (x_input-b.x)/b.w;
			
			if(progression < 0)
			{
				progression = 0;
			}
			else if(progression > 1)
			{
				progression = 1;
			}
				
			return progression;
		}
		
		public void doTranslate(float x_input, float y_input)
		{ 
			// Does translation
			if(!moving) // If translate has not been started, initialise it
			{
				init_progression  = this.progression;
				init_x_pos = x_input;
				moving = true;
				
				PApplet.println("Started translate");
			}
			if(moving)
			{
				float amount_x = init_x_pos-x_input;
				progression = findProgression(x_input);
				a.current_frame = findFrameForProgression(progression);
			}
		}
		
		public void endTranslate()
		{ 
			// Ends translation
			moving = false;
		}	
	}
	
	class StrokeHandle extends Element
	{
		TimelineBar t;
		int min_width = 30;
		int width_buffer = 5;
		String label_start = "";
		String label_end = "";
		int font_size = 14;
		
		HandleSubButton button_loop = new HandleSubButton(80, 25, p);
		
		StrokeHandle(TimelineBar t, AniSketch p) 
		{
			super(0,0,0,25,p);
			this.t = t;
		} 	
		
		void draw()
		{
			if(p.main_windows.sheet.active_stroke_selection != null)
			{
				Stroke active_stroke = p.main_windows.sheet.active_stroke_selection;
				
				float stroke_start = (float)active_stroke.start_frame/(float)a.frame_range[1];
				float stroke_end = (float)(active_stroke.start_frame+active_stroke.points.size())/(float)a.frame_range[1];
				
				p.textFont(p.default_font);
				p.textSize(14);
				
				if(active_stroke.play_mode == active_stroke.PLAY_ONCE)
				{
					button_loop.setLabel("PLAY ONCE");
				}
				else if(active_stroke.play_mode == active_stroke.LOOP)
				{
					button_loop.setLabel("LOOP");
				}
				if(active_stroke.play_mode == active_stroke.HOLD)
				{
					button_loop.setLabel("HOLD");
				}

				label_start = "F" + Long.toString(active_stroke.start_frame);
				label_end = "F" + (active_stroke.start_frame+active_stroke.points.size());
				float handle_start_width = width_buffer + p.textWidth(label_start) + width_buffer;
				float handle_end_width = width_buffer + p.textWidth(label_end) + width_buffer;
				
				if(stroke_end > 1) {stroke_end = 1f;}
				if(stroke_start < 0) {stroke_start = 0f;}
				
				int handle_start_mark = (int)(stroke_start*(t.w-5)) + (t.x);
				int handle_end_mark = (int)(stroke_end*(t.w-5)) + (t.x);
				int handle_width = handle_end_mark - handle_start_mark + 5;
				
				// Draw the 'ticks'
				p.fill(150,200);
				p.rect(handle_start_mark, t.y, 5, t.h);
				p.rect(handle_end_mark, t.y, 5, t.h);
				
				// Draw the handle
				// If the handle width is less than half the timeline, then we can place the sub button outside the handle

				// If the handle width is too short, just show the start handle and the sub button
				if(handle_width < (handle_start_width+handle_start_width))
				{
					this.x = handle_start_mark;
					this.y = t.y+t.h;
					
					// If the handle width is even shorter than the start handle
					if(handle_width < handle_start_width)
					{	
						this.w = (int)handle_start_width;
					}
					else
					{
						this.w = handle_width;
					}
					p.rect(this.x, this.y, this.w, this.h); // Draw the start handle
					p.fill(30);
					p.textSize(font_size);
					//p.textFont(p.default_font);
					p.textAlign(p.CENTER, p.TOP);
					p.text(label_start, this.x+(this.w/2f), this.y+2);
				
				}
				else
				{
					this.x = handle_start_mark;
					this.y = t.y+t.h;
					this.w = handle_width;
					p.rect(this.x, this.y, this.w, this.h);
					p.fill(30);
					p.textSize(font_size);
					//p.textFont(p.default_font);
					p.textAlign(p.CENTER, p.TOP);
					p.text(label_start, this.x+(handle_start_width/2f), this.y+2);
					p.textAlign(p.CENTER, p.TOP);
					p.text(label_end, this.x+handle_width-(handle_end_width/2f), this.y+2);
				}
				
				// Figure out the sub button placement
				// Ideally the button should be placed on the right hand side of the handle
				if((t.w+t.x)-handle_end_mark < button_loop.w+5)
				{
					if((this.w - handle_start_width - handle_end_width) >= button_loop.w+5)
					{
						button_loop.x = this.x + this.w - handle_end_width - button_loop.w ;
						button_loop.y = this.y;
					}
					else
					{
						button_loop.x = this.x - button_loop.w;
						button_loop.y = this.y;
					}
				}
				else
				{
					button_loop.x = this.x + this.w;
					button_loop.y = this.y;	
				}
				
				button_loop.draw();
			}	
			p.textAlign(p.LEFT, p.BOTTOM); // Reset text alignment
		}
		
		void checkMouseEvent(MouseEvent e)
		{
			if(p.main_windows.sheet.active_stroke_selection != null)
			{
				button_loop.checkMouseEvent(e);
			}
		}
		
		class HandleSubButton extends Button
		{
			HandleSubButton(int w, int h, AniSketch p) 
			{
				super(0, 0, w, h, p);
			}	
			
			@Override
			void pressAction()
			{
				if(p.main_windows.sheet.active_stroke_selection != null)
				{
					p.main_windows.sheet.active_stroke_selection.cyclePlayMode();
				}
			}
			
		}
		
		
	}
	
	class ButtonPlay extends Button
	{
		TimelineBar t;
		
		ButtonPlay(int w, int h, TimelineBar t, AniSketch p) 
		{
			super(0, 0, w, h, p);
			this.t = t;
			setToToggle();
			setOffImage(p.getResource("/resources/icons/play.png"), 11, 14);
			setOnImage(p.getResource("/resources/icons/pause.png"), 14, 14);
		}
		
		@Override
		void update()
		{
			this.x = (t.x) + (t.w/2f) - (this.w/2);
			this.y = t.y + 35;
		}			
		
		void toggleOnAction()
		{
			a.play();
		}
		
		void toggleOffAction()
		{
			a.pause();
		}
	}
	
	class ButtonStop extends Button
	{
		TimelineBar t;
		
		ButtonStop(int w, int h, TimelineBar t, AniSketch p) 
		{
			super(0, 0, w, h, p);
			this.t = t;
			setToPress();
			setOffImage(p.getResource("/resources/icons/stop.png"), 14, 14);
			setOnImage(p.getResource("/resources/icons/stop.png"), 14, 14);
		}
		
		@Override
		void update()
		{
			this.x = (t.x) + (t.w/2f) - (this.w/2) - this.w;
			this.y = t.y + 35;
		}		
		
		void pressAction()
		{
			a.stop();
		}
	}
	
	class ButtonLoop extends Button
	{
		TimelineBar t;
		
		ButtonLoop(int w, int h, TimelineBar t, AniSketch p) 
		{
			super(0, 0, w, h, p);
			this.t = t;
			setToToggle();
			setOffImage(p.getResource("/resources/icons/back.png"), 14, 14);
			setOnImage(p.getResource("/resources/icons/loop.png"), 14, 14);
		}
		
		@Override
		void update()
		{
			this.x = (t.x) + (t.w/2f) - (this.w/2) + this.w;
			this.y = t.y + 35;
		}			
		
		void toggleOnAction()
		{
			a.setToLoop();
		}
		
		void toggleOffAction()
		{
			a.setToPlayOnce();
		}
	}
	
}
