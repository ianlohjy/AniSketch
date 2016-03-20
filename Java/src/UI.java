import processing.core.PApplet;

public class UI extends Element{

	// Able to be responsive
	
	final static int RECT = 1;
	final static int ELLI = 2;
	final static int IMAG = 3;
	
	int draw_mode = RECT;
	
	Style default_style;
	
	Element parent;
	
	UI(PApplet p)
	{
		super(0,0,0,0,p);
		default_style = new Style(p);
	}
	
	void init()
	{
		
	}
	
	void draw()
	{
		switch (draw_mode) 
		{
		case RECT:
			default_style.apply();
			p.rect(x, y, w, h);
			break;

		default:
			break;
		}
		
		debug();
	}
	
	void behaviour()
	{
		
	}
}
