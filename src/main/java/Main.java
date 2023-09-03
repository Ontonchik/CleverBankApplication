public class Main {
    public static void main(String[] args) {
        Controller controller = new Controller();
        Thread thread = new Thread(controller);
        thread.start();
        controller.begin();
    }
}
