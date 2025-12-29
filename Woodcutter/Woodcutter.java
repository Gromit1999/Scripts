import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.PaintListener;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.Player;

import java.awt.*;

@ScriptManifest(name = "woodcutter", description = "we woodcut", author = "Gromit",
        version = 1.0, category = Category.WOODCUTTING, image = "")
public class Woodcutter extends AbstractScript implements PaintListener {
    State state;

    Area bankArea = new Area(3180, 3447, 3185, 3433);
    Area normalTreeArea = new Area(3170, 3423, 3158, 3397);
    Area oakTreArea = new Area(3134, 3414, 3147, 3392);

    @Override
    public void onPaint(Graphics g) {
        String bestTree = null;
        if (Skills.getRealLevel(Skill.WOODCUTTING) >= 15) {
            bestTree = "Oak tree";
        }
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Currect State: " + state, 10, 50);
        g.drawString("Best Tree:" + bestTree, 10, 75);
    }


    @Override
    public int onLoop() {
        state = getState();
        // Now we use the variable 'state' for the switch
        switch (state) {
            case FINDING_TREE:
                GameObject tree = GameObjects.closest(t -> t.getName().equalsIgnoreCase("tree") && normalTreeArea.contains(t.getTile()));
                if (normalTreeArea.contains(Players.getLocal())) {
                    if (tree != null && tree.interact("Chop down")) {
                        Logger.log("Attempting to chop tree");
                        Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), 5000);
                    }
                } else {
                    Walking.walk(normalTreeArea.getRandomTile());
                    Sleep.sleepUntil(() -> normalTreeArea.contains(Players.getLocal()), 2500);
                }
                break;
            case WALKING_TO_BANK:
                if (!bankArea.contains(Players.getLocal())) {
                    Logger.log("Walking to bank");
                    Walking.walk(bankArea.getRandomTile());
                    Sleep.sleepUntil(() -> bankArea.contains(Players.getLocal()), 2500);
                }
                break;
            case BANKING:
                if (Inventory.isFull()) {
                    if (Bank.open()) {
                        Logger.log("Banking all logs");
                        Bank.depositAll("Logs");
                    } else {
                        Logger.log("opening bank");
                        Bank.open();
                    }
                }
                break;
            case CHOPPING_TREE:
                if (Players.getLocal().isAnimating()) {
                    Logger.log("Waiting for tree to chop");
                }
                break;
        }
    return 1000;
    }


    private State getState() {
        if (Inventory.isFull() && !bankArea.contains(Players.getLocal())) {
            return State.WALKING_TO_BANK; 
        }
        if (!Inventory.isFull() && !Players.getLocal().isAnimating()) {
            return State.FINDING_TREE;
        }
        if (Players.getLocal().isAnimating()) {
            return State.CHOPPING_TREE;
        }
        if (Inventory.isFull()
                && bankArea.contains(Players.getLocal())){
            return State.BANKING;
        }
        
        // we will return nothing for now and see what happens in the future
        return null;
    }


}
