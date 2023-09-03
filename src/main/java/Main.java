import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        Controller controller = new Controller();
        Thread thread = new Thread(controller);
        thread.start();
        controller.begin();
        thread.interrupt();
    }
}