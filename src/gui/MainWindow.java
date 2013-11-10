package gui;

import java.awt.Color;
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
		
		Input.login("CAACEdEose0cBAAtTbt1lX6YEcl1kZCNUIuLIIL11pHXZAdRLZBgiecMtZAAm2hFxGy6G0HHq7HppINXtNvFz3XONrOhnnU9gMuurDNmO6Cszo8lXMfLBmvhQ8pwBsSuhOCnD5JT41ERt6B8o6nUC5g4FKFMkTbOElMB2hD6ns1U8zmY9xnP965mqHp24g9lxWIvZBFUKBJgZDZD");
		Input.initialize();
	}
	
	public void update() {
		if (!mousePressed && Input.head != null && Input.head.children != null && Input.head.children.size() > 0) {
			tx += vx;
			ty += vy;
			
			final int border = 80;

			if (width - border > Person.maxX - Person.minX) {
				int dx1 = Person.minX + tx - border;
				int dx2 = width - border - (Person.maxX + tx);
				if (dx1 < 0) {
					tx += (-dx1/2);
					vx = 0;
				}
				else if (dx2 < 0) {
					tx -= (-dx2/2);
					vx = 0;
				}
			} else {
				//*
				
				// TODO: validate
				int dx1 = Person.minX + tx - border;
				int dx2 = width - border - (Person.maxX + tx);
				if (dx1 > 0) {
					tx += (-dx1/2);
					vx = 0;
				}
				else if (dx2 > 0) {
					tx -= (-dx2/2);
					vx = 0;
				}
				
				//*/
			}
			
			if (height -border> Person.maxY - Person.minY){
				int dy1 = Person.minY + ty - border;
				int dy2 = height - border - (Person.maxY + ty);
				if (dy1 < 0) {
					ty += (-dy1/2);
					vy = 0;
				}
				else if (dy2 < 0) {
					ty -= (-dy2/2);
					vy = 0;
				}
			} else {
				int dy1 = Person.minY + ty - border;
				int dy2 = height - border - (Person.maxY + ty);
				if (dy1 > 0) {
					ty += (-dy1/2);
					vy = 0;
				}
				else if (dy2 > 0) {
					ty -= (-dy2/2);
					vy = 0;
				}
				
			}
		}
		
		if (Input.head != null) {
			if (!Input.head.mouseMoved(tx, ty, mouseX, mouseY)) {
				Input.head.line(tx, ty, mouseX, mouseY);
			}
			Input.head.update();
			Person.minX=Input.head.x-80;
			Person.minY=Input.head.y-80;
			Person.maxX=Input.head.x+80;
			Person.maxY=Input.head.y+80;
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
		g.setColor(new Color(135, 206, 250));
		g.fillRect(0, 0, width, height);
		
		// calculate the dimensions of the entire thing
		if (Input.head != null) {
		Input.head.paintLines(g, tx, ty, width, height);
			Input.head.paint(g, tx, ty, width, height);
		}
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
