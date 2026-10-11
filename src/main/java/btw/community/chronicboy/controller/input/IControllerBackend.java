package btw.community.chronicboy.controller.input;

public interface IControllerBackend {
    boolean init();
    void update(ControllerSnapshot snapshot, ControllerSnapshot prev);
    boolean isAvailable();
    String getName();
    void cleanup();
}
