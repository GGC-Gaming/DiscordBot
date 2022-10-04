package Bot;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Listener extends ListenerAdapter {

    private final Logger log = LoggerFactory.getLogger(Listener.class);
    private final String COMMAND_SIGN = "!";

    @Override
    public void onReady(@NotNull ReadyEvent e) {
        log.info("Listener is ready!");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        switch (e.getName()) {
            case "test" -> e.reply("Testing works as intended.").setEphemeral(true).queue();
            case "here" -> {
                List<Meeting> activeMeetings = Main.getActiveMeetings();
                if (activeMeetings.size() == 0) e.reply("There doesn't seem to be a meeting... Ask an officer to start one.").setEphemeral(true).queue();
                for (Meeting meeting : activeMeetings) {
                    if (meeting.getPassword().equalsIgnoreCase(e.getOption("password").getAsString())) {
                        e.reply(meeting.memberAttend(e.getMember())).setEphemeral(true).queue();
                    } else {
                        e.reply("Sorry that password must be incorrect.").setEphemeral(true).queue();
                    }
                }
            }
            case "meeting" -> {
                switch (e.getSubcommandName()) {
                    case "start" -> {
                        Meeting meeting = new Meeting();
                        Main.activeMeetings.add(meeting);
                        e.reply("**Meeting started**\nPassword : " + meeting.getPassword()).queue();
                    }
                    case "end" -> {
                        for (Meeting meeting : Main.getActiveMeetings()) {
                            if (meeting.getPassword().equalsIgnoreCase(e.getOption("password").getAsString())) {
                                StringBuilder sb = new StringBuilder();
                                for (Member attendee : meeting.getAttendees()) {
                                    sb.append(attendee.getEffectiveName()).append("\n");
                                }
                                e.reply("**Meeting " + meeting.getPassword() + " ended.**\nList of members attended.\n" + sb).queue();
                            } else {
                                e.reply("Sorry that password must be incorrect.").queue();
                            }
                        }
                    }
                    case "list" -> {
                        StringBuilder sb = new StringBuilder();
                        List<Meeting> meetings = Main.getActiveMeetings();
                        if (meetings.size() == 0) {
                            e.reply("No active meetings...").queue();
                        } else {
                            sb.append("**List of active meetings**\n");
                            for (Meeting meeting : meetings) {
                                sb.append(meeting.getPassword()).append(" | Members Attended : ").append(meeting.getAttendees().size()).append("\n");
                            }
                            e.reply(sb.toString()).queue();
                        }
                    }
                }
            }
        }
    }

}
