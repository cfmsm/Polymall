package commons.polymall.api;

public class CommonUtil {
    public static void print(String msg) {
        System.out.println(msg);
    }

    public static void finish(int result) {
        if (result == 0) {
            String green = "\u001B[32m";
            String reset = "\u001B[0m";
            print(green + "==> DOWNLOAD SUCCESS" + reset);
        }
        else {
            System.err.println("==> DOWNLOAD FAILED");
        }
    }
    public static void logDownload(String message) {
        String purple = "\u001B[35m";
        String reset = "\u001B[0m";
        print(purple + "==> DOWNLOADING FROM: " + reset + message);
    }
}