package Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class Listener extends ListenerAdapter {

    private final Logger log = LoggerFactory.getLogger(Listener.class);
    private final String COMMAND_SIGN = "!";

    private TextChannel logChannel;

    @Override
    public void onReady(@NotNull ReadyEvent e) {
        //#log | Text Channel
        logChannel = Main.getDiscordServer().getTextChannelById(1007282869699367024L);
        //#bot_testing | Text Channel//
        //logChannel = Main.getDiscordServer().getTextChannelById(1022509987907502140L);
        log.info("Listener is ready!");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        switch (e.getName()) {
            case "here" -> {
                List<Meeting> activeMeetings = Main.getActiveMeetings();
                if (activeMeetings.size() == 0)
                    e.reply("There doesn't seem to be a meeting... Ask an officer to start one.").setEphemeral(true).queue();
                else {
                    for (Meeting meeting : activeMeetings) {
                        if (meeting.getPassword().equalsIgnoreCase(e.getOption("password").getAsString())) {
                            Member member = e.getMember();
                            log.info(member.getEffectiveName() + " attended meeting #" + meeting.getPassword());
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setAuthor(member.getEffectiveName(),member.getEffectiveAvatarUrl(),member.getEffectiveAvatarUrl());
                            embed.setImage(member.getEffectiveAvatarUrl());
                            embed.setTitle(member.getEffectiveName() + " joined meeting #" + meeting.getPassword());
                            logChannel.sendMessageEmbeds(embed.build()).queue();
                            e.reply(meeting.memberAttend(e.getMember())).setEphemeral(true).queue();
                        } else e.reply("Sorry that password must be incorrect.").setEphemeral(true).queue();
                    }
                }
            }
            case "meeting" -> {
                switch (e.getSubcommandName()) {
                    case "start" -> {
                        Meeting meeting = new Meeting();
                        Main.getActiveMeetings().add(meeting);
                        e.reply("**Meeting started**\nPassword : " + meeting.getPassword()).setEphemeral(true).queue();
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setTitle("Meeting Started").addField("Password",meeting.getPassword(),true);
                        embed.setTimestamp(LocalDateTime.now());
                        logChannel.sendMessageEmbeds(embed.build()).queue();
                        if (e.getOption("attending").getAsBoolean())
                            meeting.memberAttend(e.getMember());
                    }
                    case "end" -> {
                        List<Meeting> activeMeetings = Main.getActiveMeetings();
                        switch (activeMeetings.size()) {
                            case 0 -> e.reply("There are no active meetings...").setEphemeral(true).queue();
                            case 1 -> {
                                EmbedBuilder embed = new EmbedBuilder();
                                embed.setTitle("Meeting Ended").addField("Password",activeMeetings.get(0).getPassword(),true);
                                embed.addField("Members Attended", String.valueOf(activeMeetings.get(0).getAttendees().size()),true);
                                embed.setTimestamp(LocalDateTime.now());
                                logChannel.sendMessageEmbeds(embed.build()).queue();
                                e.reply("Meeting #" + activeMeetings.get(0).getPassword() + " successfully ended.").setEphemeral(true).queue();
                                activeMeetings.get(0).endMeeting();
                            }
                            default -> {
                                for (Meeting meeting : activeMeetings) {
                                    if (meeting.getPassword().equalsIgnoreCase(e.getOption("password").getAsString())) {
                                        EmbedBuilder embed = new EmbedBuilder();
                                        embed.setTitle("Meeting Ended").addField("Password",meeting.getPassword(),true);
                                        embed.addField("Members Attended", String.valueOf(meeting.getAttendees().size()),true);
                                        embed.setTimestamp(LocalDateTime.now());
                                        logChannel.sendMessageEmbeds(embed.build()).queue();
                                        e.reply("Meeting #" + meeting.getPassword() + " successfully ended.").setEphemeral(true).queue();
                                        meeting.endMeeting();
                                    } else {
                                        e.reply("Sorry that password must be incorrect.").queue();
                                    }
                                }
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
