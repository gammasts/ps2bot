import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import java.util.HashMap;
import java.util.Scanner;
import org.pircbotx.Colors;


public class PresenceHandler extends ListenerAdapter {


	private PermissionsManager pm;
	private Scanner scanner;
	private PresenceEngine pe;
	private Thread peThread;
	private String soeapikey;

	public PresenceHandler(PresenceEngine pe, Thread peThread, String soeapikey) {
		super();
		this.pe = pe;
		this.peThread = peThread;
		this.pm = PermissionsManager.getInstance();
		this.soeapikey = soeapikey;
		System.out.println("PresenceHandler Initialized.");
	}

	public void onMessage(MessageEvent event) {
		String rawcommand = event.getMessage();
		String command = rawcommand.toLowerCase();
		
		if (command.startsWith("!presence ")) {
			User user = event.getUser();
			//check permissions
			//if (!pm.isAllowed("!announcements",user.getNick())) {
			if (!pm.isAllowed("!presence",event.getUser(),event.getChannel())) {
				event.respond("Sorry, you are not in the access list for presence checking.");
				return;
			}
			scanner = new Scanner(command);
			String token = scanner.next();
			
			if (scanner.hasNext()){
				token = scanner.next().toLowerCase();
				switch (token) {
					//
					case "interval": if (!scanner.hasNextLong()){
						event.respond("Setting the interval requires a number, expressed in minutes.  Max 60.");
					} else {
						long rawlong = scanner.nextLong();
						if (rawlong > 60) {
							event.respond("Setting the interval requires a number, expressed in minutes.  Max 60.");
						} else {
							event.respond("Interval set to "+rawlong+" minutes.");
							pe.setInterval(rawlong*1000*60);
							peThread.interrupt();
						}
					}
					break;
					//
					//
					case "timeout": if (!scanner.hasNextInt()){
						event.respond("Setting the timeout requires a number, expressed in seconds.  Max 20.");
					} else {
						int rawint = scanner.nextInt();
						if (rawint > 20) {
							event.respond("Setting the timeout requires a number, expressed in seconds.  Max 20.");
						} else {
							event.respond("Timeout set to "+rawint+" seconds.");
							pe.setTimeout(rawint*1000);
						}
					}
					break;
					//
					//
					case "outfit": if (!scanner.hasNext()){
						event.respond("Setting the outfit requires an outfit alias, such as \"FKPK\".");
					} else {
						String outfit = scanner.next();
						if (outfit.length() > 4) {
							event.respond("Outfit aliases tend to be four characters or less.");
						} else {
							event.respond("Outfit set to "+outfit+".");
							pe.setOutfit(outfit);
						}
					}
					break;
					//
					case "on": pe.turnOn();
					event.respond("Auto-presence turned ON.");
					break;
					//
					case "off": pe.turnOff();
					event.respond("Auto-presence turned OFF.");
					break;
					//
					default: event.respond("I'm not sure what you asked me.  Valid commands are \"on\", \"off\", \"timeout\", and \"interval\".");
					break;

				}
			}
		}
		
		if (command.equals("!presence")) {
			
			HashMap<String,Boolean> hm = null;
			try {
				hm = PresenceChecker.getPresence(pe.getOutfit(), pe.getTimeout(), soeapikey);
			} catch (Exception e) {
				event.respond("Something real bad wrong happened when I checked in with SOE.  So, no.");
				return;
			}
			
			String onlinenames = "";
			int count = 0;
			for (String key : hm.keySet()) {
				if (hm.get(key)) {
					//System.out.println("concat'ing key: "+key);
					onlinenames=onlinenames.concat(key+" ");
					count++;
					//System.out.println("onlinenames is now "+onlinenames);
				}
			}
			if (onlinenames.equals("")) {
				event.respond("No one is online.  Not a sausage.");
			} else if (count > 20) {
				event.respond("There are "+count+" players online.  I shan't bore you with the details.");
			} else {
				event.respond(onlinenames + Colors.GREEN +"online");
			}
		}
	}
}


