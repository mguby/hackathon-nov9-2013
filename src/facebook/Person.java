package facebook;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import facebook.Input.FullData;
import facebook.Input.RelationshipData;
import gui.MainWindow;

public class Person implements Comparable<Person>, Runnable {
	public class Location
	{
		private int xLoc;
		private int yLoc;
		public Location(int x, int y)
		{
			xLoc=x;
			yLoc=y;
		}
		public void add(Location parent)
		{
			xLoc += parent.xLoc;
			yLoc += parent.yLoc;
		}
		public Location calcLoc(Location parentLoc, int parentAngle, int totalAngle /*degrees*/,int numInGen, int myId)
		{
			int angleBetween = (int) totalAngle/numInGen;
			int myAngle = (int) (.5*parentAngle + angleBetween * (.5+myId));
			int myXLoc = (int) (200*Math.PI*Math.sin(Math.toRadians(myAngle)));
			int myYLoc = (int) (200*Math.PI*Math.cos(Math.toRadians(myAngle)));
			Location relLoc = new Location(myXLoc, myYLoc);
			relLoc.add(parentLoc);
			return relLoc;
		}
	}
	
	public static int minX, minY, maxX, maxY;
	
	private final ArrayList<Person> children = new ArrayList<Person>();
	
	public static boolean getting = false;
	
	public static final int SIBLING = 0;
	public static final int PARENT_CHILD = 1;
	public static final int GRANDPARENT_GRANDCHILD = 2;
	public static final int COUSIN = 3;
	public static final int OTHER = 4;
	
	public static final String[] SIBLING_QUERY = {
		"sister", "brother"
	};
	public static final String[] PARENT_CHILD_QUERY = {
		"mother", "father", "daughter", "son"
	};
	
	public static final Font font16 = new Font("Verdana", Font.PLAIN, 32);
	public static final FontMetrics metrics16;
	
	static {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		metrics16 = g.getFontMetrics(font16);
	}
	
	private boolean started = false;
	private int runType = 0;
	
	public String firstName;
	public String lastName;
	public String uid;
	public int relationship;
	public String pictureLink;
	public BufferedImage picture;
	
	public boolean hover = false;
	public int hoverIdx = 0;
	public static final int HOVER_MAX = 10;
	public int x, y;
	public int width, height;
	
	public int showIdx = 0;
	public static final int SHOW_MAX = 15;
	
	public int alpha=0,beta=360,angle;
	
	// runtype = 1
	int nextGen;
	
	public void gen2(int gen, Person p2, final int xx, final int yy) {
		if(gen==0)return;
		
		List<RelationshipData> d = Input.getRelationshipData(uid);
		int num = d.size();
		if (num == 0) return;
		int delta = (p2.beta - p2.alpha) / num;
		int theta = p2.alpha;
		
		for (int i = 0; i < d.size(); i++) {
			RelationshipData r = d.get(i);
			FullData f;
			try {
			f = Input.getFullData(r.uid);
			}catch(Exception e){ 
				System.out.println("!");
				continue;
			}
			Person p = new Person(f.first_name, f.last_name, f.uid, r.relationship);
			p.setPicture(f.pic_big);
			p.loadPicture();
			children.add(p);
			
			p.alpha = theta;
			p.beta = theta + delta;
			p.width = Math.max(60, 7 * p2.width / 10);
			p.height = Math.max(60,  7 * p2.height / 10);

			p.angle = (p.alpha+p.beta)/2;
			if(gen==2){
				System.out.println((Input.head==p2)+" "+angle+" -- "+xx + ","+yy);
			} else if (Input.head==p2) {
				System.out.println("@@@@@@@@@@@@@@@@@@@");
			}
			p.x = xx + (int) (MainWindow.LINE_LENGTH * Math.sin(Math.toRadians(p.angle)));
			p.y = yy + (int) (MainWindow.LINE_LENGTH * Math.cos(Math.toRadians(p.angle)));

			
			p.gen2(gen-1,p,p.x,p.y);
			
			
			
			// TODO: set x y width height etc
			
			theta += delta;
			
		}
	}
	
	public void generate(int gen) {
		
		nextGen = gen;
		
		runType = 1;
		started = true;
		new Thread(this).start();
	}
	
	public void calcBounds() {
		if (x - width/2 < minX)
			minX = x-width/2;
		if (x + width/2 > maxX)
			maxX = x+width/2;
		if (y - height/2 < minY)
			minY = y - height/2;
		if(y+height/2>maxY)
			maxY =y+height/2;

		for (int i = 0; i < children.size(); i++)
			children.get(i).calcBounds();
	}
	
	public void update() {
		if (hover) {
			++hoverIdx;
		} else {
			--hoverIdx;
		}
		
		if (hoverIdx < 0) {
			hoverIdx = 0;
		} else if (hoverIdx > HOVER_MAX) {
			hoverIdx = HOVER_MAX;
		}

		for (int i = 0; i < children.size(); i++)
			children.get(i).update();
		
		if (this == Input.head && picture != null && showIdx == 0) {
			/*
			List<RelationshipData> data = Input.getRelationshipData(uid);
			for (int i = 0; i < data.size(); i++) {
				RelationshipData d = data.get(i);
				Person p = new Person("","",d.uid,d.relationship);
				children.add(p);
				p.generate(2);
			}
			//*/
		}
		
		if (picture != null && showIdx != SHOW_MAX)
			++showIdx;
	}
	
	public void paintLines(Graphics g, int tx, int ty, int w, int h) {
		int fx = x + tx;
		int fy = y + ty;
		

		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).picture == null) continue;
			
			int cfx = children.get(i).x + tx;
			int cfy = children.get(i).y + ty;
			
			switch (children.get(i).relationship) {
				case SIBLING:
					g.setColor(Color.red);
					break;
				case PARENT_CHILD:
					g.setColor(Color.white);
					break;
				case GRANDPARENT_GRANDCHILD:
					g.setColor(Color.yellow);
					break;
				case COUSIN:
					g.setColor(Color.black);
					break;
				case OTHER:
					g.setColor(Color.green);
					break;
			}
			
			g.drawLine(fx, fy, cfx, cfy);
			
			children.get(i).paintLines(g, tx, ty, w, h);
		}
	}
	
	public void paint(Graphics g, int tx, int ty, int w, int h) {
		int fx = x + tx - width / 2;
		int fy = y + ty - height / 2;
		if (fx >= w || fy >= h || fx <= -w || fy <= -h);
		else {
		if (this == Input.head) {
			if (picture == null && !started) {
				started = true;
				new Thread(this).start();
			} else if (showIdx != SHOW_MAX) {
				int ww = 0;
				int hh = 0;
				
				for (int i = 0; i < showIdx; i++) {
					ww = (ww + width) / 2;
					hh = (hh + height) / 2;
				}
				
				fx = x + tx - ww / 2;
				fy = y + ty - hh / 2;
				g.drawImage(picture, fx, fy, ww, hh, null);
			} else {
				g.drawImage(picture, fx, fy, width, height, null);
			}
		} else {
			if (picture == null) {
			} else if (showIdx != SHOW_MAX) {
			} else {
				g.drawImage(picture, fx, fy, width,height,null);
			}
		}
		
		if (showIdx == SHOW_MAX && hoverIdx != 0) {
			g.setColor(new Color(128, 128, 128, 20 * hoverIdx));
			g.fillRect(fx, fy, width, height);
			
			g.setColor(new Color(255, 255, 255, 25 * hoverIdx + 5));
			int mh = metrics16.getHeight();
			int asc =  metrics16.getAscent();

			int w1 = (int) metrics16.stringWidth(firstName);
			int w2 = (int) metrics16.stringWidth(lastName);
			
			g.setFont(font16);
			g.drawString(firstName, fx + (width - w1)/2, fy + height/2-mh+asc);
			g.drawString(lastName, fx + (width - w2)/2, fy + height/2+asc);
		}
		}
		
		for (int i = 0; i < children.size(); i++)
			children.get(i).paint(g, tx, ty, w, h);
	}
	
	public void mouseMoved(int tx, int ty, int mx, int my) {
		int fx = x + tx - width / 2;
		int fy = y + ty - height / 2;
		hover = (mx >= fx && mx < fx + width && my >= fy && my < fy + height);

		for (int i = 0; i < children.size(); i++)
			children.get(i).mouseMoved(tx, ty, mx ,my);
	}
	
	public Person(String first, String last, String uid, String relationship) {
		if (uid.length()==0)throw new RuntimeException("FML:");
		this.firstName = first;
		this.lastName = last;
		this.uid = uid;

		// convert relationship to parent to an integer
		this.relationship = OTHER;
		for (int i = 0; i < SIBLING_QUERY.length; i++) {
			if (relationship.contains(SIBLING_QUERY[i])) {
				this.relationship = SIBLING;
			}
		}
		for (int i = 0; i < PARENT_CHILD_QUERY.length; i++) {
			if (relationship.contains(PARENT_CHILD_QUERY[i])) {
				this.relationship = PARENT_CHILD;
			}
		}
		if (relationship.contains("cousin")) {
			this.relationship = COUSIN;
		} else if (relationship.contains("grand")) {
			this.relationship = GRANDPARENT_GRANDCHILD;
		}
	}
	
	public void setPicture(String link) {
		pictureLink = link;
	}
	
	public void addChild(Person person) {
		children.add(person);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Person))
			return false;
		
		return uid.equals(((Person) o).uid);
	}

	@Override
	public int compareTo(Person p) {
		return uid.compareTo(p.uid);
	}
	
	public void loadPicture() {
		try {
		    URL url = new URL(pictureLink);
			picture = ImageIO.read(url);
			
			int width = picture.getWidth();
			int height = picture.getHeight();
			
			int size = Math.min(width, height);
			
			picture = picture.getSubimage((width - size) / 2, (height - size) / 2, size, size);
		} catch (Exception e) {
		}
	}

	@Override
	public void run() {
		if (runType == 0) {
			try {
			    URL url = new URL(pictureLink);
				picture = ImageIO.read(url);
				
				int width = picture.getWidth();
				int height = picture.getHeight();
				
				int size = Math.min(width, height);
				
				picture = picture.getSubimage((width - size) / 2, (height - size) / 2, size, size);
			} catch (Exception e) {
			}
		} else if (runType == 1) {
			/*
			FullData fd;
			try {
			fd = Input.getFullData(this.uid);
			}catch(Exception e) {
				System.out.println("-"+this.uid+"-");
				return;
			}
			this.firstName = fd.first_name;
			this.lastName = fd.last_name;
			this.pictureLink = fd.pic_big;
			
			try {
			    URL url = new URL(pictureLink);
				picture = ImageIO.read(url);
				
				int width = picture.getWidth();
				int height = picture.getHeight();
				
				int size = Math.min(width, height);
				
				picture = picture.getSubimage((width - size) / 2, (height - size) / 2, size, size);
				
				if (nextGen != 0) {
					while (getting) {
						Thread.sleep(20);
					}
					getting = true;
					List<RelationshipData> data = Input.getRelationshipData(uid);
					getting = false;
					for (int i = 0; i < data.size(); i++) {
						RelationshipData d = data.get(i);
						if (d.uid.length() == 0) {
							System.out.println(uid);
							continue;
						}
						Person p = new Person("","",d.uid,d.relationship);
						children.add(p);
						p.generate(nextGen - 1);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			//*/
		}
	}
}
