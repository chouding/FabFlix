import java.io.*;

public class log_processing {

    private static void process_tj(String tj_location) throws IOException {
        try (BufferedReader file = new BufferedReader(new FileReader(tj_location))){
            String line = file.readLine();
            float total = 0;
            int count = 0;
            while (line != null){
                total += Float.parseFloat(line);
                count += 1;
                line = file.readLine();
            }

            String time = Double.toString(total / (count * 1e6));
            System.out.println("TJ average time in ms: " + time);
            System.out.println();
        }

    }

    private static void process_ts(String ts_location) throws IOException {
        try (BufferedReader file = new BufferedReader(new FileReader(ts_location))){
            String line = file.readLine();
            float total = 0;
            int count = 0;
            while (line != null){
                total += Float.parseFloat(line);
                count += 1;
                line = file.readLine();
            }

            String time = Double.toString(total / (count * 1e6));
            System.out.println("TS average time in ms: " + time);
        }

    }

    public static void main(String[] args) throws IOException {
        String tj_location = "./log/TJ1.txt";
        String ts_location = "./log/TS1.txt";

        if (args.length >= 1){
            tj_location = args[0];
        }
        if (args.length >= 2){
            ts_location = args[1];
        }
        process_tj(tj_location);
        process_ts(ts_location);
    }
}
