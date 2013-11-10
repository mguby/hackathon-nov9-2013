package facebook;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Person implements Comparable<Person> {
	private final ArrayList<Person> children = new ArrayList<Person>();
	
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
	
	public String name;
	public String uid;
	public int relationship;
	public BufferedImage picture;
	
	public Person(String name, String uid, String relationship) {
		this.name = name;
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
		try {
		    URL url = new URL(link);
			picture = ImageIO.read(url);
			
			int width = picture.getWidth();
			int height = picture.getHeight();
			
			int size = Math.min(width, height);
			
			picture = picture.getSubimage((width - size) / 2, (height - size) / 2, size, size);
		} catch (Exception e) {
		}
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
}
