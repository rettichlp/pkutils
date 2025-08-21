package de.rettichlp.commands;

import com.mojang.brigadier.CommandDispatcher;
import de.rettichlp.common.listener.MessageListener;
import de.rettichlp.common.manager.BaseManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.PKUtilsClient.networkHandler;
import static de.rettichlp.PKUtilsClient.player;
import static java.util.regex.Pattern.compile;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class RichTaxesCommand extends BaseManager implements MessageListener {

    private final Pattern PLAYER_MONEY_BANK_AMOUNT = compile("^Ihr Bankguthaben beträgt: (?<moneyBankAmount>([+-])\\d+)\\$$");
    private final Pattern MONEY_ATM_AMOUNT = compile("ATM \\d+: (?<atmMoneyAmount>\\d+)/100000\\$");

    private int moneyBankAmount = 0;
    private int atmMoneyAmount = 0;

    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("reichensteuer")
                        .executes(context -> {
                            networkHandler.sendChatCommand("bank info");

                            delayedAction(() -> {
                                networkHandler.sendChatCommand("atminfo");
                                int amount = 0;

                                if (moneyBankAmount < 100000) {
                                    player.sendMessage(Text.of("Du hast nicht genug Geld auf der Bank."), false);
                                    return;
                                }

                                if (atmMoneyAmount < moneyBankAmount) {
                                    amount = moneyBankAmount - atmMoneyAmount;
                                    networkHandler.sendChatCommand("bank abbuchen " + amount);
                                    player.sendMessage(Text.of("Du musst noch " + amount + "$ abbuchen."), false);
                                    return;
                                }

                                amount = moneyBankAmount - 100000;
                                networkHandler.sendChatCommand("bank abbuchen " + amount);
                            }, 1000);

                            return 1;
                        })
        );
    }

    @Override
    public void onMessage(String message) {
        Matcher matcher = PLAYER_MONEY_BANK_AMOUNT.matcher(message);
        if (matcher.find()) {
            String moneyBankAmountString = matcher.group("moneyBankAmount");
            try {
                this.moneyBankAmount = Integer.parseInt(moneyBankAmountString);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return;
        }

        Matcher atmMatcher = MONEY_ATM_AMOUNT.matcher(message);
        if (atmMatcher.find()) {
            String atmMoneyAmountString = atmMatcher.group("atmMoneyAmount");
            try {
                this.atmMoneyAmount = Integer.parseInt(atmMoneyAmountString);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
}
