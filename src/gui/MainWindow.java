package gui;

import java.awt.Frame;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import facebook.Input;

public class MainWindow extends JPanel implements Runnable {
	private static final long serialVersionUID = 1L;
	
	private static final int SLEEP_TIME = 20;

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
	}
	
	public MainWindow() {
		new Thread(this).start();
		
		Input.login("CAACEdEose0cBAFozaiyIU3sZCicBej8QqoyPYI0FxokPxkkZB0G0eibNgVHZCqR0ADDT37L6mTIv5g9HcfXoKZAIq6DZAMtXGAOGvmsMaGW4vSJ2khJKxWQFqmv8fesSW6tRu9obYrqr2biyS8h1ZCc6RKzNl4ffeNTZAZCCIf63p98kb9TLa3LlWsqBBjarTRYqOE8pjl8mvgZDZD");
		Input.initialize();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		int W = this.getWidth(), H = this.getHeight();
		g.drawImage(Input.head.picture, W/2-80, H/2-80, 150, 150, null);
	}
	
	@Override
	public void run() {
		long start;
		
		while (true) {
			start = System.currentTimeMillis();
			
			try {
				Thread.sleep(Math.max(0, start + SLEEP_TIME - System.currentTimeMillis()));
			} catch (Exception e) {
			}
		}
	}
}
