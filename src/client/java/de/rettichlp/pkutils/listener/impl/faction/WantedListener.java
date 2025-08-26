package de.rettichlp.pkutils.listener.impl.faction;

import de.rettichlp.pkutils.common.manager.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.common.storage.schema.WantedEntry;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.*;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static java.util.regex.Pattern.compile;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.*;

@PKUtilsListener
public class WantedListener extends PKUtilsBase implements IMessageReceiveListener {

    private static final Pattern WANTED_GIVEN_POINTS_PATTERN = compile("^HQ: ([a-zA-Z0-9_]+)'s momentanes WantedLevel: (\\d+)$");
    private static final Pattern WANTED_GIVEN_REASON_PATTERN = compile("^HQ: Gesuchter: (?<playerName>[a-zA-Z0-9_]+)\\. Grund: (?<reason>.+)$");
    private static final Pattern WANTED_REASON_PATTERN = compile("^HQ: Fahndungsgrund: (?<reason>.+) \\| Fahndungszeit: (?<time>.+)$");
    private static final Pattern WANTED_DELETE_PATTERN = compile("^HQ: (?<playerName>[a-zA-Z0-9_]+) hat (?<targetName>[a-zA-Z0-9_]+)(?:'s)* Akten gelöscht, over\\.$");
    private static final Pattern WANTED_KILL_PATTERN = compile("^HQ: (?<targetName>[a-zA-Z0-9_]+) wurde von (?<playerName>[a-zA-Z0-9_]+) getötet\\.$");
    private static final Pattern WANTED_ARREST_PATTERN = compile("^HQ: (?<targetName>[a-zA-Z0-9_]+) wurde von (?<playerName>[a-zA-Z0-9_]+) eingesperrt\\.$");
    private static final Pattern STRAFZETTEL_PATTERN = compile("^HQ: (?<playerName>[a-zA-Z0-9_]+) hat ein Strafzettel an das Fahrzeug [A-Z0-9-]+ vergeben\\.$");
    private static final Pattern WANTED_UNARREST_PATTERN = compile("^HQ: (?<playerName>[a-zA-Z0-9_]+) hat (?<targetName>[a-zA-Z0-9_]+) aus dem Gefängnis entlassen\\.$");
    private static final Pattern WANTED_LIST_HEADER_PATTERN = compile("Online Spieler mit WantedPunkten:");
    private static final Pattern WANTED_LIST_ENTRY_PATTERN = compile("- (?<playerName>[a-zA-Z0-9_]+) \\| (?<wantedPointAmount>\\d+) \\| (?<reason>.+)(?<afk> \\| AFK|)");
    private static final Pattern GIVE_DRIVING_LICENSE_PATTERN = compile("^(Agent|Beamter) (?<playerName>[a-zA-Z0-9_]+) hat (?<targetName>[a-zA-Z0-9_]+)(?:'s)* Führerschein zurückgegeben\\.$");
    private static final Pattern TAKE_DRIVING_LICENSE_PATTERN = compile("^(Agent|Beamter) (?<playerName>[a-zA-Z0-9_]+) hat (?<targetName>[a-zA-Z0-9_]+)(?:'s)* Führerschein abgenommen\\.$");
    private static final Pattern GIVE_GUN_LICENSE_PATTERN = compile("^(Agent|Beamter) (?<playerName>[a-zA-Z0-9_]+) hat (?<targetName>[a-zA-Z0-9_]+)(?:'s)* Waffenschein zurückgegeben\\.$");
    private static final Pattern TAKE_GUN_LICENSE_PATTERN = compile("^(Agent|Beamter) (?<playerName>[a-zA-Z0-9_]+) hat (?<targetName>[a-zA-Z0-9_]+)(?:'s)* Waffenschein abgenommen\\.$");
    private static final Pattern TAKE_GUNS_PATTERN = compile("^(Beamtin|Beamter) (?<playerName>[a-zA-Z0-9_]+) hat (?<targetName>[a-zA-Z0-9_]+) die Waffen abgenommen\\.$");
    private static final Pattern TAKE_DRUGS_PATTERN = compile("^(Beamtin|Beamter) (?<playerName>[a-zA-Z0-9_]+) hat (?<targetName>[a-zA-Z0-9_]+) (seine|ihre) Drogen abgenommen!$");
    private static final Pattern TRACKER_AGENT_PATTERN = compile("^HQ: Agent (?<playerName>[a-zA-Z0-9_]+) hat ein Peilsender an (?<targetName>[a-zA-Z0-9_]+) befestigt, over\\.$");

    private long activeCheck = 0;

    @Override
    public boolean onMessageReceive(String message) {
        Matcher wantedGivenPointsMatcher = WANTED_GIVEN_POINTS_PATTERN.matcher(message);
        if (wantedGivenPointsMatcher.find()) {
            String playerName = wantedGivenPointsMatcher.group(1);
            int wantedPoints = parseInt(wantedGivenPointsMatcher.group(2));

            storage.getWantedEntries().stream()
                    .filter(wantedEntry -> wantedEntry.getPlayerName().equals(playerName))
                    .findFirst()
                    .ifPresentOrElse(wantedEntry -> wantedEntry.setWantedPointAmount(wantedPoints), () -> {
                        WantedEntry wantedEntry = new WantedEntry(playerName, wantedPoints, "");
                        storage.addWantedEntry(wantedEntry);
                    });

            Text modifiedMessage = empty()
                    .append(of("➥").copy().formatted(DARK_GRAY)).append(" ")
                    .append(of(wantedGivenPointsMatcher.group(2)).copy().formatted(BLUE)).append(" ")
                    .append(of("Wanteds").copy().formatted(BLUE));

            player.sendMessage(modifiedMessage, false);

            return false;
        }

        Matcher wantedGivenReasonMatcher = WANTED_GIVEN_REASON_PATTERN.matcher(message);
        if (wantedGivenReasonMatcher.find()) {
            String playerName = wantedGivenReasonMatcher.group("playerName");
            String reason = wantedGivenReasonMatcher.group("reason");

            storage.getWantedEntries().stream()
                    .filter(wantedEntry -> wantedEntry.getPlayerName().equals(playerName))
                    .findFirst()
                    .ifPresent(wantedEntry -> wantedEntry.setReason(reason));

            Text modifiedMessage = empty()
                    .append(of("Gesucht").copy().formatted(RED)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(wantedGivenReasonMatcher.group(1)).copy().formatted(BLUE)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(wantedGivenReasonMatcher.group(2)).copy().formatted(BLUE));

            player.sendMessage(modifiedMessage, false);

            return false;
        }

        Matcher wantedReasonMatcher = WANTED_REASON_PATTERN.matcher(message);
        if (wantedReasonMatcher.find()) {
            String reason = wantedReasonMatcher.group("reason");
            String time = wantedReasonMatcher.group("time");

            Text modifiedMessage = empty()
                    .append(of("➥").copy().formatted(DARK_GRAY)).append(" ")
                    .append(of(reason).copy().formatted(BLUE)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(time).copy().formatted(BLUE));

            player.sendMessage(modifiedMessage, false);

            return false;
        }

        Matcher wantedDeleteMatcher = WANTED_DELETE_PATTERN.matcher(message);
        if (wantedDeleteMatcher.find()) {
            String playerName = wantedDeleteMatcher.group("playerName");
            String targetName = wantedDeleteMatcher.group("targetName");

            int wpAmount = getWpAmountAndDelete(targetName);

            Text modifiedMessage = empty()
                    .append(of("Gelöscht").copy().formatted(RED)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(targetName).copy().formatted(BLUE)).append(" ")
                    .append(of("(").copy().formatted(GRAY)).append(" ")
                    .append(of(valueOf(wpAmount)).copy().formatted(RED)).append(" ")
                    .append(of(")").copy().formatted(GRAY)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(playerName).copy().formatted(BLUE));

            player.sendMessage(modifiedMessage, false);

            return false;
        }

        Matcher wantedKillMatcher = WANTED_KILL_PATTERN.matcher(message);
        if (wantedKillMatcher.find()) {
            String targetName = wantedKillMatcher.group("targetName");
            String killerName = wantedKillMatcher.group("playerName");

            int wpAmount = getWpAmountAndDelete(targetName);

            if (player != null && player.getName().getString().equals(killerName)) {
                activityService.trackActivity("arrest", "Aktivität 'Verhaftung' +1");
            }

            Text modifiedMessage = empty()
                    .append(of("Getötet").copy().formatted(RED)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(targetName).copy().formatted(BLUE)).append(" ")
                    .append(of("(").copy().formatted(GRAY)).append(" ")
                    .append(of(valueOf(wpAmount)).copy().formatted(RED)).append(" ")
                    .append(of(")").copy().formatted(GRAY)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(killerName).copy().formatted(BLUE));

            player.sendMessage(modifiedMessage, false);

            return false;
        }

        Matcher wantedJailMatcher = WANTED_ARREST_PATTERN.matcher(message);
        if (wantedJailMatcher.find()) {
            String targetName = wantedJailMatcher.group("targetName");
            String officerName = wantedJailMatcher.group("playerName");

            int wpAmount = getWpAmountAndDelete(targetName);

            if (player != null && player.getName().getString().equals(officerName)) {
                activityService.trackActivity("arrest", "Aktivität 'Verhaftung' +1");
            }

            Text modifiedMessage = empty()
                    .append(of("Eingesperrt").copy().formatted(RED)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(targetName).copy().formatted(BLUE)).append(" ")
                    .append(of("(").copy().formatted(GRAY)).append(" ")
                    .append(of(valueOf(wpAmount)).copy().formatted(RED)).append(" ")
                    .append(of(")").copy().formatted(GRAY)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(officerName).copy().formatted(BLUE));

            player.sendMessage(modifiedMessage, false);

            return false;
        }

        Matcher strafzettelMatcher = STRAFZETTEL_PATTERN.matcher(message);
        if (strafzettelMatcher.find()) {
            String officerName = strafzettelMatcher.group("playerName");

            if (player != null && player.getName().getString().equals(officerName)) {
                activityService.trackActivity("ticket", "Aktivität 'Strafzettel' +1");
            }

            // Wir verhindern, dass die Originalnachricht angezeigt wird, da sie redundant ist.
            return false;
        }

        Matcher wantedUnarrestMatcher = WANTED_UNARREST_PATTERN.matcher(message);
        if (wantedUnarrestMatcher.find()) {
            String playerName = wantedUnarrestMatcher.group("playerName");
            String targetName = wantedUnarrestMatcher.group("targetName");

            Text modifiedMessage = empty()
                    .append(of("Entlassen").copy().formatted(RED)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(targetName).copy().formatted(BLUE)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(playerName).copy().formatted(BLUE));

            player.sendMessage(modifiedMessage, false);

            return false;
        }

        Matcher wantedListHeaderMatcher = WANTED_LIST_HEADER_PATTERN.matcher(message);
        if (wantedListHeaderMatcher.find()) {
            this.activeCheck = currentTimeMillis();
            storage.resetWantedEntries();
            return !syncService.isGameSyncProcessActive();
        }

        Matcher wantedListEntryMatcher = WANTED_LIST_ENTRY_PATTERN.matcher(message);
        if (wantedListEntryMatcher.find() && (currentTimeMillis() - this.activeCheck < 100)) {
            String playerName = wantedListEntryMatcher.group("playerName");
            int wantedPointAmount = parseInt(wantedListEntryMatcher.group("wantedPointAmount"));
            String reason = wantedListEntryMatcher.group("reason");
            boolean isAfk = wantedListEntryMatcher.group("afk").contains("AFK");

            WantedEntry wantedEntry = new WantedEntry(playerName, wantedPointAmount, reason);
            storage.addWantedEntry(wantedEntry);

            Formatting color = factionService.getWantedPointColor(wantedPointAmount);

            if (!syncService.isGameSyncProcessActive()) {
                Text modifiedMessage = empty()
                        .append(of("➥").copy().formatted(DARK_GRAY)).append(" ")
                        .append(of(playerName).copy().formatted(color)).append(" ")
                        .append(of("-").copy().formatted(GRAY)).append(" ")
                        .append(of(reason).copy().formatted(color)).append(" ")
                        .append(of("(").copy().formatted(GRAY))
                        .append(of(valueOf(wantedPointAmount)).copy().formatted(BLUE))
                        .append(of(")").copy().formatted(GRAY)).append(" ")
                        .append(of(isAfk ? "|" : "").copy().formatted(DARK_GRAY)).append(" ")
                        .append(of(isAfk ? "AFK" : "").copy().formatted(GRAY));

                player.sendMessage(modifiedMessage, false);
            }

            return false;
        }

        Matcher giveDrivingLicenseMatcher = GIVE_DRIVING_LICENSE_PATTERN.matcher(message);
        if (giveDrivingLicenseMatcher.find()) {
            String playerName = giveDrivingLicenseMatcher.group("playerName");
            String targetName = giveDrivingLicenseMatcher.group("targetName");

            Text modifiedMessage = empty()
                    .append(of("Führerscheinrückgabe").copy().formatted(RED)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(targetName).copy().formatted(BLUE)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(playerName).copy().formatted(BLUE));

            player.sendMessage(modifiedMessage, false);

            return false;
        }

        Matcher takeDrivingLicenseMatcher = TAKE_DRIVING_LICENSE_PATTERN.matcher(message);
        if (takeDrivingLicenseMatcher.find()) {
            String playerName = takeDrivingLicenseMatcher.group("playerName");
            String targetName = takeDrivingLicenseMatcher.group("targetName");

            Text modifiedMessage = empty()
                    .append(of("Führerscheinabnahme").copy().formatted(RED)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(targetName).copy().formatted(BLUE)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(playerName).copy().formatted(BLUE));

            player.sendMessage(modifiedMessage, false);

            return false;
        }

        Matcher giveGunLicenseMatcher = GIVE_GUN_LICENSE_PATTERN.matcher(message);
        if (giveGunLicenseMatcher.find()) {
            String playerName = giveGunLicenseMatcher.group("playerName");
            String targetName = giveGunLicenseMatcher.group("targetName");

            Text modifiedMessage = empty()
                    .append(of("Waffenscheinrückgabe").copy().formatted(RED)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(targetName).copy().formatted(BLUE)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(playerName).copy().formatted(BLUE));

            player.sendMessage(modifiedMessage, false);

            return false;
        }

        Matcher takeGunLicenseMatcher = TAKE_GUN_LICENSE_PATTERN.matcher(message);
        if (takeGunLicenseMatcher.find()) {
            String playerName = takeGunLicenseMatcher.group("playerName");
            String targetName = takeGunLicenseMatcher.group("targetName");

            Text modifiedMessage = empty()
                    .append(of("Waffenscheinabnahme").copy().formatted(RED)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(targetName).copy().formatted(BLUE)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(playerName).copy().formatted(BLUE));

            player.sendMessage(modifiedMessage, false);

            return false;
        }

        Matcher takeGunsMatcher = TAKE_GUNS_PATTERN.matcher(message);
        if (takeGunsMatcher.find()) {
            String playerName = takeGunsMatcher.group("playerName");
            String targetName = takeGunsMatcher.group("targetName");

            Text modifiedMessage = empty()
                    .append(of("Waffenabnahme").copy().formatted(RED)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(targetName).copy().formatted(BLUE)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(playerName).copy().formatted(BLUE));

            player.sendMessage(modifiedMessage, false);

            return false;
        }

        Matcher takeDrugsMatcher = TAKE_DRUGS_PATTERN.matcher(message);
        if (takeDrugsMatcher.find()) {
            String playerName = takeDrugsMatcher.group("playerName");
            String targetName = takeDrugsMatcher.group("targetName");

            Text modifiedMessage = empty()
                    .append(of("Drogenabnahme").copy().formatted(RED)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(targetName).copy().formatted(BLUE)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(playerName).copy().formatted(BLUE));

            player.sendMessage(modifiedMessage, false);

            return false;
        }

        Matcher trackerMatcher = TRACKER_AGENT_PATTERN.matcher(message);
        if (trackerMatcher.find()) {
            String playerName = trackerMatcher.group("playerName");
            String targetName = trackerMatcher.group("targetName");

            Text modifiedMessage = empty()
                    .append(of("Peilsender").copy().formatted(RED)).append(" ")
                    .append(of("-").copy().formatted(GRAY)).append(" ")
                    .append(of(playerName).copy().formatted(DARK_AQUA)).append(" ")
                    .append(of("»").copy().formatted(GRAY)).append(" ")
                    .append(of(targetName).copy().formatted(GOLD));

            player.sendMessage(modifiedMessage, false);

            return false;
        }

        return true;
    }

    private int getWpAmountAndDelete(String targetName) {
        Predicate<WantedEntry> predicate = wantedEntry -> wantedEntry.getPlayerName().equals(targetName);
        int wantedPointAmount = storage.getWantedEntries().stream()
                .filter(predicate)
                .findAny()
                .map(WantedEntry::getWantedPointAmount)
                .orElse(0);

        storage.getWantedEntries().removeIf(predicate);
        return wantedPointAmount;
    }
}