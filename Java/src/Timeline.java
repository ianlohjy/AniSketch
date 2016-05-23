import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Timeline extends Element{
	Style default_style;
	AnimationController a;
	
	TimelineBar timeline_bar;
	CursorHandle cursor_handle;
	
	Timeline(int x, int y, int w, int h, AniSketch p, AnimationController a)
	{
		super(x,y,w,h,p);
		this.a = a;
		default_style = new Style(p);
		default_style.fill(30,30,30,255);
		timeline_bar = new TimelineBar(5, this, p);
		cursor_handle = new CursorHandle(timeline_bar, p);
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
	}
	
	void checkMouseEvent(MouseEvent e)
	{
		boolean within_bounds = withinBounds(e.getX(), e.getY());	
		
		timeline_bar.checkMouseEvent(e);
		cursor_handle.checkMouseEvent(e);
		
		if(!cursor_handle.hover)
		{
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
				if(pressed && within_bounds)
				{
					a.togglePlayback();
				}
				pressed = false;
			}
			else if(e.getAction() == 3) // When mouse is clicked (down then up)
			{
				//a.togglePlayback();
			}
			else if(e.getAction() == 4) // When mouse is dragged
			{
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
			
		//if(e.getAction() =)
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
			this.y = t.y+(int)(t.h*0.35f)-(int)(h*0.35f);
			this.w = t.w-(side_margin*2);
			
			base_style.apply();
			p.rect(x, y, w, h);
			
			progression_style.apply();
			p.rect(x, y, w*progression, h);
			
			p.fill(255);
			p.textFont(p.default_font);
			p.textSize(14);
			
			p.textAlign(PApplet.LEFT, PApplet.BOTTOM);
			p.text("F0", x, y-5);
			
			p.textAlign(PApplet.RIGHT, PApplet.BOTTOM);
			if(a.recording_stroke && a.frame_range[1] < a.current_frame)
			{p.text("F"+a.current_frame, x+w, y-5);}
			else 
			{p.text("F"+a.frame_range[1], x+w, y-5);}
			
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
			super(0,0,0,26,p);
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
			p.rect(b.x+(progression*(b.w-5)), b.y, 5, b.h);
			
			p.textSize(14);
			//p.textFont(p.consolas_b);
			
			p.fill(255);
			p.textAlign(PApplet.CENTER, PApplet.BOTTOM);

			p.text(label, x+(w/2), y+h-5);
			
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
	
}
