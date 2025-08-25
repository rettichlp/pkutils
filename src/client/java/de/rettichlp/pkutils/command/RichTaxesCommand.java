package de.rettichlp.pkutils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.pkutils.common.manager.CommandBase;
import de.rettichlp.pkutils.common.registry.PKUtilsCommand;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static java.util.regex.Pattern.compile;

@PKUtilsCommand(label = "reichensteuer")
public class RichTaxesCommand extends CommandBase implements IMessageReceiveListener {

    private static final Pattern PLAYER_MONEY_BANK_AMOUNT = compile("^Ihr Bankguthaben beträgt: (?<moneyBankAmount>([+-])\\d+)\\$$");
    private static final Pattern MONEY_ATM_AMOUNT = compile("ATM \\d+: (?<moneyAtmAmount>\\d+)/100000\\$");

    private int moneyBankAmount = 0;
    private int atmMoneyAmount = 0;

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> execute(@NotNull LiteralArgumentBuilder<FabricClientCommandSource> node) {
        return node
                .executes(context -> {
                    // execute command to check money on the bank of player
                    networkHandler.sendChatCommand("bank info");

                    // execute command to check money in atm
                    delayedAction(() -> networkHandler.sendChatCommand("atm info"), 1000);

                    // handle money withdraw
                    delayedAction(() -> {
                        // check player has rich taxes
                        if (this.moneyBankAmount <= 100000) {
                            sendModMessage("Du hast nicht ausreichend Geld auf der Bank.", false);
                            return;
                        }

                        int moneyThatNeedsToBeWithdrawn = this.moneyBankAmount - 100000;

                        if (this.atmMoneyAmount >= moneyThatNeedsToBeWithdrawn) {
                            networkHandler.sendChatCommand("bank abbuchen " + moneyThatNeedsToBeWithdrawn);
                        } else {
                            networkHandler.sendChatCommand("bank abbuchen " + this.atmMoneyAmount);
                            sendModMessage("Du musst noch " + (moneyThatNeedsToBeWithdrawn - this.atmMoneyAmount) + "$ abbuchen.", false);
                        }
                    }, 2000);

                    return 1;
                });
    }

    @Override
    public boolean onMessageReceive(String message) {
        Matcher playerMoneyBankAmountMatcher = PLAYER_MONEY_BANK_AMOUNT.matcher(message);
        if (playerMoneyBankAmountMatcher.find()) {
            this.moneyBankAmount = Integer.parseInt(playerMoneyBankAmountMatcher.group("moneyBankAmount"));
            return true;
        }

        Matcher moneyAtmAmountMatcher = MONEY_ATM_AMOUNT.matcher(message);
        if (moneyAtmAmountMatcher.find()) {
            this.atmMoneyAmount = Integer.parseInt(moneyAtmAmountMatcher.group("moneyAtmAmount"));
            return true;
        }

        return true;
    }
}
