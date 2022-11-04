package Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
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
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent e) {
        EmbedBuilder embed = new EmbedBuilder();
        User joinUser = e.getUser();
        embed.setAuthor(joinUser.getName(),joinUser.getEffectiveAvatarUrl());
        embed.setDescription(joinUser.getAsMention() + " joined the server.");
        embed.addField("Account creation", String.valueOf(joinUser.getTimeCreated().getYear()),true);
        embed.setFooter("ID:" + joinUser.getId());
        embed.setTimestamp(LocalDateTime.now());
        embed.setColor(Color.GREEN);
        logChannel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent e) {
        EmbedBuilder embed = new EmbedBuilder();
        User leftUser = e.getUser();
        embed.setAuthor(leftUser.getName(),leftUser.getEffectiveAvatarUrl());
        embed.setDescription(leftUser.getAsMention() + " left the server.");
        embed.setFooter("ID:" + leftUser.getId());
        embed.setTimestamp(LocalDateTime.now());
        embed.setColor(Color.RED);
        logChannel.sendMessageEmbeds(embed.build()).queue();
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
            case "vote" -> {
                if (Main.voters.contains(e.getMember())) {
                    e.reply("It appears you already voted for this one.").setEphemeral(true).queue();
                } else {
                    switch (e.getSubcommandName()) {
                        case "left" -> {
                            Main.vote_left++;
                            Main.voters.add(e.getMember());
                            e.reply("You have voted for left.").setEphemeral(true).queue();
                        }
                        case "right" -> {
                            Main.vote_right++;
                            Main.voters.add(e.getMember());
                            e.reply("You have voted for right.").setEphemeral(true).queue();
                        }
                        default -> e.reply("An error has occurred.").setEphemeral(true).queue();
                    }
                }
            }
            case "end-voting" -> {
                e.reply("Number of voters: " + Main.voters.size() +
                        "\nNumber that choose Left: " + Main.vote_left +
                        "\nNumber that choose Right: " + Main.vote_right
                ).queue();
                Main.vote_left = 0; Main.vote_right = 0;
                Main.voters.clear();
            }
        }
    }
}
