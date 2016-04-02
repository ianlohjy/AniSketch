import processing.core.PApplet;

public class Primitive extends Element
{
	PApplet p;
	
	Primitive(PApplet p)
	{
		super(0,0,0,0,p);
	}
	
	public void update()
	{
		
	}
	
	public void drawShape()
	{
		p.ellipse(x, y, 50, 50);
	}
	
}
