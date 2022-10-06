package Bot;

import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Meeting {

    private final Logger log = LoggerFactory.getLogger(Meeting.class);

    private final List<Member> ATTENDEES = new ArrayList<>();
    private final String PASSWORD;

    public Meeting() {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        PASSWORD = sb.toString();
        log.info("Meeting #" + PASSWORD + " started.");
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
        log.info(member.getEffectiveName() + " attended meeting #" + getPassword());
        return "Sign in successful.";
    }

    public String endMeeting() {
        StringBuilder sb = new StringBuilder();
        sb.append("**Meeting ").append(getPassword()).append(" ended.**\n");
        if (getAttendees().size() == 0)
            sb.append("No one attended the meeting...");
        else {
            sb.append("List of members attended.\n");
            for (Member attendee : getAttendees()) {
                sb.append(attendee.getEffectiveName()).append("\n");
            }
        }
        Main.getActiveMeetings().remove(this);
        return sb.toString();
    }
}
