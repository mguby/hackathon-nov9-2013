package facebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.restfb.DefaultFacebookClient;
import com.restfb.Facebook;
import com.restfb.FacebookClient;

public class Input {
	private static FacebookClient fb;

	public static Person head;

	public static void login(String accessToken) {
		fb = new DefaultFacebookClient(accessToken);
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		login(in.nextLine());
		initialize();
		gatherData(head, 4);
		in.close();
	}

	// generates initial person
	public static void initialize() {
		// get my data
		List<FullData> data = fb
				.executeFqlQuery("SELECT uid, name, pic_big FROM user WHERE uid = me()", FullData.class);
		head = new Person(data.get(0).name, data.get(0).uid, "");
		head.setPicture(data.get(0).pic_big);
	}

	// helper classes
	public static class RelationshipData {
		@Facebook
		String uid;

		@Facebook
		String name;

		@Facebook
		String relationship;
	}

	public static class FullData {
		@Facebook
		String uid;

		@Facebook
		String name;

		@Facebook
		String pic_big;
	}

	// helper methods
	/**
	 * gets the uid, name, and picture of a person
	 */
	public static FullData getFullData(String uid) {
		return fb.executeFqlQuery("SELECT uid, name, pic_big FROM user WHERE uid = " + uid, FullData.class).get(0);
	}

	/**
	 * gets a list of all family members
	 */
	public static List<RelationshipData> getRelationshipData(String uid) {
		return fb.executeFqlQuery("SELECT uid, name, relationship FROM family WHERE profile_id = " + uid,
				RelationshipData.class);
	}

	// TODO: fill in these methods (use the helper methods)
	/**
	 * the data in person is already filled
	 * add to person's arraylist children such that:
	 *     the children of person will be one generation greater than person
	 * repeat this for the number of generations specified
	 * 
	 * also, make sure that each "line" in the tree does not contain two of the same person
	 *     (if you do find a match, then the "deeper" person is obviously deleted)
	 */
	public static void gatherData(Person person, int generations) {
		
	}
}
