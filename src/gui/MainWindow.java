package gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.Facebook;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookException;
import com.restfb.types.Page;
import com.restfb.types.Post;
import com.restfb.types.User;

public class MainWindow extends JFrame {
	/**
	   * RestFB Graph API client.
	   */
	private final FacebookClient facebookClient;

	/**
	 * Entry point. You must provide a single argument on the command line: a
	 * valid Graph API access token.
	 * 
	 * @param args
	 *          Command-line arguments.
	 * @throws FacebookException
	 *           If an error occurs while talking to the Facebook Graph API.
	 */
	public static void main(String[] args) throws FacebookException {
		Scanner in = new Scanner(System.in);
		new MainWindow(in.nextLine()).runEverything();
	}

	MainWindow(String accessToken) {
		facebookClient = new DefaultFacebookClient(accessToken);
	}

	void runEverything() throws FacebookException {
		fetchConnections();
	}

	void fetchConnections() throws FacebookException {
		for (int i = 0; i < 4; i++)
			names.add(new ArrayList<String>());
		List<FqlUser> users = facebookClient.executeFqlQuery(
				"SELECT uid, name, relationship FROM family WHERE profile_id = me()", FqlUser.class);

		recurse("base", users, 3);

		for (int i = 0; i < names.size(); i++) {
			System.out.println("GENERATION " + i);
			for (int j = 0; j < names.get(i).size(); j++) {
				System.out.println(names.get(i).get(j));
			}
			System.out.println();
		}
	}

	ArrayList<ArrayList<String>> names = new ArrayList<ArrayList<String>>();

	void recurse(String pre, List<FqlUser> u, int gen) {
		if (gen == 0)
			return;

		for (FqlUser us : u) {
			if (gen == 3)
				System.out.println(us.name + "-" + us.uid +"\n"+"\n"+"\n"+"\n");
			names.get(3 - gen).add(us.name);
			try {
				List<FqlUser> users = facebookClient.executeFqlQuery(
						"SELECT uid, name, relationship FROM family WHERE profile_id = " + us.uid, FqlUser.class);
				if (us.name.contains("ichelle") && gen == 3) {
					System.out.println("!!!!!!!");
					System.out.println(users.size() + " " + users.get(0).toString());
					System.out.println("!!!!!!!");
				}
				recurse(pre + " " + us.name, users, gen - 1);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	public static class FqlUser {
		@Facebook
		String uid;

		@Facebook
		String name;

		@Facebook
		String relationship;

		@Facebook
		String pic_square;

		@Override
		public String toString() {
			return String.format("%s %s (%s) %s", name, relationship, uid, pic_square);
		}
	}

}
