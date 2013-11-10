package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import facebook.Input;
import facebook.Person;

public class MainWindow extends JPanel implements MouseListener, MouseMotionListener, Runnable {
	private static final long serialVersionUID = 1L;

	private static final int SLEEP_TIME = 20;
	public static final int LINE_LENGTH = 300;

	public static int width, height;

	private boolean mousePressed = false;
	private int mouseX, mouseY;
	private int tx, ty;
	private double vx, vy;

	public static void main(String[] args) {
		JFrame window = new JFrame();
		MainWindow content = new MainWindow();

		window.setExtendedState(Frame.MAXIMIZED_BOTH);
		window.setUndecorated(true);

		window.setContentPane(content);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(null);
		window.setTitle("Family Tree");

		window.setVisible(true);

		content.width = window.getWidth();
		content.height = window.getHeight();
		content.init();

		window.addMouseListener(content);
		window.addMouseMotionListener(content);
	}

	public MainWindow() {
	}

	public void init() {
		new Thread(this).start();

		Input.login("CAACEdEose0cBAP6JHZCkUrUqx2iUSNYNsCAgXz3t8t3JE5nK48QQPA3bagwfmrtWyOyEJb9Mp2mRBahEQDi6T55DjtNgDhRcu9l9jocVtT69xutjfCOZB2a8BiAtJegPjaUJZBWTt9lP4nnseSHRsKdVnqd7fjSkd84y7RZBD3ZCYff99TQPWIuyvKwPe9ajlLudwOIjynQZDZD");
		Input.initialize();
	}

	public void update() {
		if (!mousePressed && Input.head != null && Input.head.children != null && Input.head.children.size() > 0) {
			tx += vx;
			ty += vy;

			final int border = 200;

			if (width - border > Person.maxX - Person.minX) {
				int dx1 = Person.minX + tx - border;
				int dx2 = width - border - (Person.maxX + tx);
				if (dx1 < 0) {
					tx += (-dx1 / 2);
					vx = 0;
				} else if (dx2 < 0) {
					tx -= (-dx2 / 2);
					vx = 0;
				}
			} else {
				//*

				// TODO: validate
				int dx1 = Person.minX + tx - border;
				int dx2 = width - border - (Person.maxX + tx);
				if (dx1 > 0) {
					tx += (-dx1 / 2);
					vx = 0;
				} else if (dx2 > 0) {
					tx -= (-dx2 / 2);
					vx = 0;
				}

				//*/
			}

			if (height - border > Person.maxY - Person.minY) {
				int dy1 = Person.minY + ty - border;
				int dy2 = height - border - (Person.maxY + ty);
				if (dy1 < 0) {
					ty += (-dy1 / 2);
					vy = 0;
				} else if (dy2 < 0) {
					ty -= (-dy2 / 2);
					vy = 0;
				}
			} else {
				int dy1 = Person.minY + ty - border;
				int dy2 = height - border - (Person.maxY + ty);
				if (dy1 > 0) {
					ty += (-dy1 / 2);
					vy = 0;
				} else if (dy2 > 0) {
					ty -= (-dy2 / 2);
					vy = 0;
				}

			}
		}

		if (Input.head != null) {
			if (!Input.head.mouseMoved(tx, ty, mouseX, mouseY)) {
				Input.head.line(tx, ty, mouseX, mouseY);
			}
			Input.head.update();
			Person.minX = Input.head.x - 80;
			Person.minY = Input.head.y - 80;
			Person.maxX = Input.head.x + 80;
			Person.maxY = Input.head.y + 80;
			Input.head.calcBounds();
		}

		vx *= .9;
		vy *= .9;
		if (Math.abs(vx) < 1)
			vx = 0;
		if (Math.abs(vy) < 1)
			vy = 0;

	}

	@Override
	public void paintComponent(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new Color(64,128,192));
		g.fillRect(0, 0, width, height);

		// calculate the dimensions of the entire thing
		if (Input.head != null) {
			Input.head.paintLines(g, tx, ty, width, height);
			Input.head.paint(g, tx, ty, width, height);
		}

		// draw the legend
		g.setColor(new Color(255, 255, 255, 128));
		int hh = 168, ww = 208;
		int y0 = height - hh - 16;
		int xt = 16 + 32;
		g.fillRect(16, y0, ww, hh);

		g.translate(0, 4);
		g.setColor(Person.COLORS[0]);
		g.fillRect(24, y0+8, 16, 16);

		g.setColor(Person.COLORS[1]);
		g.fillRect(24, y0+8+32, 16, 16);

		g.setColor(Person.COLORS[2]);
		g.fillRect(24, y0+8+64, 16, 16);

		g.setColor(Person.COLORS[3]);
		g.fillRect(24, y0+8+96, 16, 16);

		g.setColor(Person.COLORS[4]);
		g.fillRect(24, y0+8+128, 16, 16);
		g.translate(0,-4);

		g.translate(0, -8);
		g.setFont(new Font("Verdana", Font.PLAIN, 12));
		g.setColor(Color.black);
		g.drawString("Sibling", xt, y0 + 32);
		g.drawString("Parent/Child", xt, y0 + 64);
		g.drawString("Grandparent/Grandchild", xt, y0 + 96);
		g.drawString("Cousin", xt, y0 + 128);
		g.drawString("Aunt/Uncle/Nephew/Niece", xt, y0 + 160);
		g.translate(0, 8);
		
		/*
		 * 
	public static final int SIBLING = 0;
	public static final int PARENT_CHILD = 1;
	public static final int GRANDPARENT_GRANDCHILD = 2;
	public static final int COUSIN = 3;
	public static final int OTHER = 4;
		 */
	}

	@Override
	public void run() {
		long start;

		while (true) {
			start = System.currentTimeMillis();

			update();
			repaint();

			try {
				Thread.sleep(Math.max(0, start + SLEEP_TIME - System.currentTimeMillis()));
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		int dx = arg0.getX() - mouseX;
		int dy = arg0.getY() - mouseY;

		tx += dx;
		ty += dy;

		vx = dx;
		vy = dy;

		mouseX = arg0.getX();
		mouseY = arg0.getY();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		mouseX = arg0.getX();
		mouseY = arg0.getY();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (Input.head != null)
			Input.head.click(tx, ty, mouseX, mouseY);
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		mousePressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		mousePressed = false;
	}

	// unused listeners
	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}
}
