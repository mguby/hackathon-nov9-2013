package facebook;

import gui.MainWindow;

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

	// generates initial person
	public static void initialize() {
		// get my data
		List<FullData> data = fb
				.executeFqlQuery("SELECT uid, first_name, last_name, pic_big FROM user WHERE uid = me()", FullData.class);
		head = new Person(data.get(0).first_name, data.get(0).last_name, data.get(0).uid, "");
		head.setPicture(data.get(0).pic_big);
		
		// GET ALL THE DATA HERE
		head.alpha=0;
		head.beta=360;
		head.width=head.height=160;

		Input.head.x = MainWindow.width / 2;
		Input.head.y = MainWindow.height / 2;
		Input.head.width = 160;
		Input.head.height = 160;
		
		head.gen2(2,null,head.x, head.y);
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
		String first_name;
		
		@Facebook
		String last_name;

		@Facebook
		String pic_big;
	}

	// helper methods
	/**
	 * gets the uid, name, and picture of a person
	 */
	public static FullData getFullData(String uid) {
		return fb.executeFqlQuery("SELECT uid, first_name, last_name, pic_big FROM user WHERE uid = " + uid, FullData.class).get(0);
	}

	/**
	 * gets a list of all family members
	 */
	public static List<RelationshipData> getRelationshipData(String uid) {
		return fb.executeFqlQuery("SELECT uid, name, relationship FROM family WHERE profile_id = " + uid,
				RelationshipData.class);
	}
}
