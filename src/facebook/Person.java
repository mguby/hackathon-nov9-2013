package facebook;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import facebook.Input.FullData;
import facebook.Input.RelationshipData;
import gui.MainWindow;

// TODO:
// hover on line = hover
// fix 3+ generations

// make it look better (threaded)

// exit functionality (button + esc)
// 

public class Person implements Comparable<Person>, Runnable {
	public static int minX, minY, maxX, maxY;

	public final ArrayList<Person> children = new ArrayList<Person>();
	public Person parent = null;

	public static boolean getting = false;

	public static final int SIBLING = 0;
	public static final int PARENT_CHILD = 1;
	public static final int GRANDPARENT_GRANDCHILD = 2;
	public static final int COUSIN = 3;
	public static final int OTHER = 4;

	public static final String[] SIBLING_QUERY = {"sister", "brother"};
	public static final String[] PARENT_CHILD_QUERY = {"mother", "father", "daughter", "son"};

	public static final Font font8 = new Font("Verdana", Font.PLAIN, 16);
	public static final FontMetrics metrics8;
	public static final Font font16 = new Font("Verdana", Font.PLAIN, 32);
	public static final FontMetrics metrics16;

	static {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		metrics16 = g.getFontMetrics(font16);
		metrics8 = g.getFontMetrics(font8);
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

	public int alpha = 0, beta = 360, angle;

	// runtype = 1
	int nextGen;

	public void gen2(int gen, int cur, Person parent, final int xx, final int yy) {
		this.parent = parent;
		if (gen == 0)
			return;

		List<RelationshipData> d22 = Input.getRelationshipData(uid);
		ArrayList<RelationshipData> d = new ArrayList<RelationshipData>();
		d.addAll(d22);

		// remove extra useless crap
		List<Person> parents = new ArrayList<Person>();
		parents.add(this);
		Person pr = parent;
		while (pr != null) {
			parents.add(pr);
			if (pr == Input.head)
				break;
			pr = pr.parent;
		}
		for (int i = 0; i < d.size(); i++) {

			boolean rem = false;
			RelationshipData pp = d.get(i);
			for (int j = 0; j < parents.size(); j++) {
				if (parents.get(j).uid.equals(pp.uid)) {
					rem = true;
					break;
				} else {
					//System.out.println(pp.name + "pp" + " " + pp.uid + " -> " + pp);
				}
			}
			if (rem) {
				d.remove(i);
				--i;
			}
		}

		int num = d.size();
		if (num == 0)
			return;
		int delta = (beta - alpha) / num;
		int theta = alpha;

		for (int i = 0; i < d.size(); i++) {
			RelationshipData r = d.get(i);
			FullData f;
			try {
				f = Input.getFullData(r.uid);
			} catch (Exception e) {
				System.out.println("!");
				continue;
			}
			Person p = new Person(f.first_name, f.last_name, f.uid, r.relationship);
			p.setPicture(f.pic_big);
			p.loadPicture();
			children.add(p);

			p.alpha = theta;
			p.beta = theta + delta;
			p.width = Math.max(60, 7 * width / 10);
			p.height = Math.max(60, 7 * height / 10);

			p.angle = (p.alpha + p.beta) / 2;
			//p.angle=p.alpha;
			if (gen == 2) {
				//System.out.println((Input.head==this)+" "+angle+" -- "+xx + ","+yy);
			} else if (Input.head == this) {
				//System.out.println("@@@@@@@@@@@@@@@@@@@");
			}

			//p.x = xx + (int) (MainWindow.LINE_LENGTH * Math.sin(Math.toRadians(p.angle)));
			//p.y = yy + (int) (MainWindow.LINE_LENGTH * Math.cos(Math.toRadians(p.angle)));

			//	int LEN = MainWindow.LINE_LENGTH;
			int len = MainWindow.LINE_LENGTH * (1 + cur);
			double rx = len * Math.sin(Math.toRadians(p.angle));
			double ry = len * Math.cos(Math.toRadians(p.angle));
			//double r_norm = Math.sqrt(rx*rx+ry*ry);
			p.x = Input.head.x + (int) rx;
			p.y = Input.head.y + (int) ry;

			/*
			// make the length = 200
			int dx = p.x-Input.head.x, dy=p.y-Input.head.y;
			double d_norm_sq = dx*dx+dy*dy;
			
			int alpha = angle - p.angle;
			int beta = 180 - 2 * alpha;
			
			double c_norm = Math.sqrt(d_norm_sq + LEN*LEN - 2*Math.sqrt(d_norm_sq)*LEN*Math.cos(Math.toRadians(beta)));
			double mult = c_norm;
			rx*=mult;
			ry*=mult;
			p.x = Input.head.x + (int) rx;
			p.y = Input.head.y + (int) ry;
			
			//*/

			/*
			double dist = Math.sqrt(dx*dx+dy*dy);
			double mult = MainWindow.LINE_LENGTH/dist;
			dx*=mult;
			dy*=mult;
			p.x=xx+dx;
			p.y=yy+dy;
			//*/

			p.gen2(gen - 1, cur + 1, this, p.x, p.y);

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
		if (x - width / 2 < minX)
			minX = x - width / 2;
		if (x + width / 2 > maxX)
			maxX = x + width / 2;
		if (y - height / 2 < minY)
			minY = y - height / 2;
		if (y + height / 2 > maxY)
			maxY = y + height / 2;

		for (int i = 0; i < children.size(); i++)
			if (children.get(i)!=null)
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

	public static final Color[] COLORS = {new Color(251, 255, 126), new Color(126, 255, 130), new Color(255, 130, 126),
			new Color(130, 126, 255), new Color(255, 126, 251)};

	public void paintLines(Graphics g, int tx, int ty, int w, int h) {
		/*
		if (picture == null)
			return;
		int fx = x + tx;
		int fy = y + ty;

		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).picture == null)
				continue;

			int cfx = children.get(i).x + tx;
			int cfy = children.get(i).y + ty;

			switch (children.get(i).relationship) {
				case SIBLING :
					g.setColor(Color.red);
					g.setColor(COLORS[0]);
					break;
				case PARENT_CHILD :
					g.setColor(Color.white);
					g.setColor(COLORS[1]);
					break;
				case GRANDPARENT_GRANDCHILD :
					g.setColor(Color.yellow);
					g.setColor(COLORS[2]);
					break;
				case COUSIN :
					g.setColor(Color.black);
					g.setColor(COLORS[3]);
					break;
				case OTHER :
					g.setColor(Color.green);
					g.setColor(COLORS[4]);
					break;
			}

			Graphics2D g2d = (Graphics2D) g;
			g2d.setStroke(new BasicStroke(4));

			if (showIdx != SHOW_MAX) {
				int dx = cfx - fx;
				int dy = cfy - fy;
				double mult = (double) showIdx / SHOW_MAX;
				
				cfx = fx + (int) (dx * mult);
				cfy = fy + (int) (dy * mult);
				
				//g.drawLine(fx, fy, cfx, cfy);
			} else if (picture != null) {
				//g.drawLine(fx, fy, cfx, cfy);
				//
			}

			children.get(i).paintLines(g, tx, ty, w, h);
		}*/
	}

	public void paint(Graphics g, int tx, int ty, int w, int h) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(4));

		for (int i = 0; i < children.size(); i++)
			children.get(i).paint(g, tx, ty, w, h);

		int fx = x + tx - width / 2;
		int fy = y + ty - height / 2;
		{
			if (this == Input.head) {
				g.setColor(new Color(135,206,250));
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
					g.drawRect(fx-2, fy-2, ww+3, hh+3);
					g.drawImage(picture, fx, fy, ww, hh, null);
				} else {
					g.drawRect(fx-2, fy-2, width+3, height+3);
					g.drawImage(picture, fx, fy, width, height, null);
				}
			} else {

				switch (relationship) {
					case SIBLING :
						g.setColor(Color.red);
						g.setColor(COLORS[0]);
						break;
					case PARENT_CHILD :
						g.setColor(Color.white);
						g.setColor(COLORS[1]);
						break;
					case GRANDPARENT_GRANDCHILD :
						g.setColor(Color.yellow);
						g.setColor(COLORS[2]);
						break;
					case COUSIN :
						g.setColor(Color.black);
						g.setColor(COLORS[3]);
						break;
					case OTHER :
						g.setColor(Color.green);
						g.setColor(COLORS[4]);
						break;
				}
				
				if (picture == null) {
				} else if (showIdx != SHOW_MAX) {
					int dx = x - parent.x;
					int dy = y - parent.y;
					int ix = parent.x + dx * showIdx / SHOW_MAX;
					int iy = parent.y + dy * showIdx / SHOW_MAX;
					ix += (tx - width / 2);
					iy += (ty - height / 2);
					g.drawLine(parent.x+tx, parent.y+ty, ix+width/2, iy+height/2);

					if (fx >= w || fy >= h || fx <= -w || fy <= -h)
						;else{
							g.drawRect(ix-2, iy-2, width+3, height+3);
					g.drawImage(picture, ix, iy, width, height, null);
						}
				} else {
					g.drawLine(parent.x+tx, parent.y+ty, fx+width/2, fy+height/2);
					if (fx >= w || fy >= h || fx <= -w || fy <= -h)
						;else{

							g.drawRect(fx-2, fy-2, width+3, height+3);
							g.drawImage(picture, fx, fy, width, height, null);
						}
					
				}
			}

			if (showIdx == SHOW_MAX && hoverIdx != 0) {
				g.setColor(new Color(128, 128, 128, 20 * hoverIdx));
				g.fillRect(fx, fy, width, height);

				g.setColor(new Color(255, 255, 255, 25 * hoverIdx + 5));
				int mh = metrics16.getHeight();
				int asc = metrics16.getAscent();

				int w1 = (int) metrics16.stringWidth(firstName);
				int w2 = (int) metrics16.stringWidth(lastName);

				g.setFont(font16);
				if (width < 80) {
					String name = firstName + " " + lastName;
					g.setFont(font8);
					int ww = (int) metrics8.stringWidth(name);
					int as2c=metrics8.getAscent();

					int ww1 = (int) metrics8.stringWidth(firstName);
					int ww2 = (int) metrics8.stringWidth(lastName);
					int m2h = metrics8.getHeight();
					
					g.drawString(firstName, fx + (width - ww1) / 2, fy + height / 2 - m2h + as2c);
					g.drawString(lastName, fx + (width - ww2) / 2, fy + height / 2 + as2c);
					
					g.drawString(name, MainWindow.width-ww-16, MainWindow.height-8);
				} else {
					g.drawString(firstName, fx + (width - w1) / 2, fy + height / 2 - mh + asc);
					g.drawString(lastName, fx + (width - w2) / 2, fy + height / 2 + asc);
				}
			}
		}
	}

	public static void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void openWebpage(URL url) {
		try {
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public void click(int tx, int ty, int mx, int my) {
		int fx = x + tx - width / 2;
		int fy = y + ty - height / 2;

		if (mx >= fx && mx < fx + width && my >= fy && my < fy + height) {
			try {
				openWebpage(new URL("http://www.facebook.com/people/@/" + uid));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {

			for (int i = 0; i < children.size(); i++)
				children.get(i).click(tx, ty, mx, my);
		}
	}

	public boolean line(int tx, int ty, int mx, int my) {
		
		//*
		int fx = mx - tx;
		int fy = my - ty;
		
		if (parent != null) {
		
			Line2D.Double l = new Line2D.Double(parent.x, parent.y, x, y);
			int ddd = (beta - alpha);
			ddd = 20;
			if (width < 80) {
				ddd = 10;
			}
			if (!hover && l.ptSegDist(fx, fy) < ddd) {

				hover = true;
				return true;
			} else {
				hover = false;
			}
		}

		for (int i = children.size()-1; i >=0; i--)
			if (children.get(i).line(tx, ty, mx ,my)) {
				Person p = children.remove(i);
				children.add(p);
				return true;
			}
		
		return false;
		//*/
	}
	
	public void reset() {
		hover = false;

		for (int i = 0; i < children.size(); i++)
			children.get(i).reset();
	}

	public boolean mouseMoved(int tx, int ty, int mx, int my) {
		int fx = x + tx - width / 2;
		int fy = y + ty - height / 2;
		hover = (mx >= fx && mx < fx + width && my >= fy && my < fy + height);
		if (hover)
			return true;

		for (int i = children.size() - 1; i >= 0; i--)
			if (children.get(i).mouseMoved(tx, ty, mx, my)) {
				// 
				Person p = children.remove(i);
				children.add(p);
				
				return true;
			}

		return false;
	}

	public Person(String first, String last, String uid, String relationship) {
		if (uid.length() == 0)
			throw new RuntimeException("FML:");
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
