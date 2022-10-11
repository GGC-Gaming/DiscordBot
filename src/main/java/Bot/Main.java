package Bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private final static Logger log = LoggerFactory.getLogger(Main.class);
    private static JDA jda;

    public static List<Meeting> activeMeetings = new ArrayList<>();

    public static List<Member> voters = new ArrayList<>();
    public static int vote_left = 0;
    public static int vote_right = 0;

    public static void main(String[] args) {

        JDABuilder builder = JDABuilder.create(
                getToken(),
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS
        ).setMemberCachePolicy(MemberCachePolicy.ALL).disableCache(
                CacheFlag.ACTIVITY,
                CacheFlag.VOICE_STATE,
                CacheFlag.CLIENT_STATUS,
                CacheFlag.ONLINE_STATUS
        );

        builder.addEventListeners( new Listener() );

        try {
            jda = builder.build();
            jda.awaitReady();

            //Slash Commands
            Guild discordServer = getDiscordServer();
            //Here
            discordServer.upsertCommand("here","Let's you mark yourself as here, you only need the password.").addOptions(
                    new OptionData(OptionType.STRING,"password","Ask an officer for a password, if you don't know it.")
                            .setRequired(true).setRequiredLength(5,5)
            ).queue();

            //Meeting | Start - End - List
            discordServer.upsertCommand("meeting","Starts, ends, or list active meetings.").addSubcommands(
                    new SubcommandData("start","Starts a meeting, and will give you a password you can give out.").addOptions(
                            new OptionData(OptionType.BOOLEAN,"attending","Set this to false if you are starting the meeting, but are not attending.")
                                    .setRequired(true)),
                    new SubcommandData("end", "Ends a meeting, but you may need to tell me which meeting via the password").addOptions(
                            new OptionData(OptionType.STRING,"password","Scroll up in this chat, if you can't find the password...")
                                    .setRequired(false).setRequiredLength(5,5)),
                    new SubcommandData("list","Gives you a list of active meetings.")
            ).queue();

            discordServer.upsertCommand("vote","Places a vote.").addSubcommands(
                    new SubcommandData("left","Votes for the left option."),
                    new SubcommandData("right","Votes for the right option.")
            ).queue();

            discordServer.upsertCommand("end-voting","Ends the voting period and send the results.").queue();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static String getToken() {
        try {
            return Files.readAllLines(Paths.get("bot.token")).get(0);
        } catch (NoSuchFileException e) {
            log.error("Could not find the bot.token file!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(1);
        return "";
    }

    public static Guild getDiscordServer() {
        return jda.getGuildById(1007265345444270090L);
    }

    public static List<Meeting> getActiveMeetings() {
        return activeMeetings;
    }
}
