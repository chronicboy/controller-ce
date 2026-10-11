package btw.community.chronicboy.controller.input;

public enum ControllerButton {
    A("A / Cross"),
    B("B / Circle"),
    X("X / Square"),
    Y("Y / Triangle"),
    LB("LB / L1"),
    RB("RB / R1"),
    LT("LT / L2"),
    RT("RT / R2"),
    L3("Left Stick Click"),
    R3("Right Stick Click"),
    START("Start / Options"),
    BACK("Back / Select / Share"),
    GUIDE("Guide / Home"),
    DPAD_UP("D-Pad Up"),
    DPAD_DOWN("D-Pad Down"),
    DPAD_LEFT("D-Pad Left"),
    DPAD_RIGHT("D-Pad Right");

    private final String displayName;

    ControllerButton(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
