package btw.community.chronicboy.controller;

import api.AddonHandler;
import api.BTWAddon;
import btw.community.chronicboy.controller.config.ControllerConfig;

public class ControllerAddon extends BTWAddon {
    private static ControllerAddon instance;

    public ControllerAddon() {
        super();
    }

    public static ControllerAddon getInstance() {
        return instance;
    }

    @Override
    public void preInitialize() {
        this.shouldVersionCheck = false;
        this.isRequiredClientAndServer = false;

        // Clean up legacy BTW config migration artifacts to prevent Windows FileAlreadyExistsException
        java.io.File oldConfig = new java.io.File("config/controller-ce.properties.old");
        if (oldConfig.exists()) {
            oldConfig.delete();
        }
        java.io.File legacyProp = new java.io.File("config/controller-ce.properties");
        if (legacyProp.exists()) {
            legacyProp.delete();
        }

        ControllerConfig.load();
    }

    @Override
    public void initialize() {
        instance = this;
        AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initialized.");
    }
}
