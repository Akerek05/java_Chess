public interface MoveCommand {
    void execute();
    void undo();
}