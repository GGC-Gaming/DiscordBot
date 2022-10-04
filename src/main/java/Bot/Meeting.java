package Bot;

import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Meeting {

    private final List<Member> ATTENDEES = new ArrayList<>();
    private final String PASSWORD;

    public Meeting() {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        PASSWORD = sb.toString();
    }

    public String getPassword() {
        return PASSWORD;
    }

    public List<Member> getAttendees() {
        return ATTENDEES;
    }

    public String memberAttend(Member member) {
        if (ATTENDEES.contains(member)) return "Sorry you seem to already be on the list.";
        ATTENDEES.add(member);
        return "Sign in successful.";
    }
}
