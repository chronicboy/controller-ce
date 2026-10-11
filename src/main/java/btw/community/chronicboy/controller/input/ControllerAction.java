package btw.community.chronicboy.controller.input;

public enum ControllerAction {
    ATTACK("Attack / Destroy"),
    USE("Use / Place Block"),
    JUMP("Jump / Ascend"),
    SNEAK("Sneak / Descend"),
    INVENTORY("Inventory"),
    DROP("Drop Item"),
    HOTBAR_PREV("Previous Item"),
    HOTBAR_NEXT("Next Item"),
    SPRINT("Toggle Sprint"),
    PERSPECTIVE("Change View (F5)"),
    PAUSE("Pause Menu"),
    PICK_BLOCK("Pick Block"),
    PLAYER_LIST("Player List (Tab)"),
    CHAT("Open Chat"),
    SPECIAL("Special / Sprint Boost"),
    NONE("Unbound");

    private final String displayName;

    ControllerAction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
