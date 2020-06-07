package file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class DuplicateDetector {

    private static final int HASH_SIZE = 16;
    private static final int KB = 1024;
    private static final int MB = 1024 * KB;
    private static final NumberFormat FORMATTER = NumberFormat.getInstance(new Locale("en_US"));

    private static MessageDigest messageDigest;
    private static long totalLength = 0L;

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        if (!checkArgs(args)) {
            return;
        }

        final File dir = new File(args[0]);
        final Map<String, List<File>> files = new LinkedHashMap<>();

        findDuplicates(files, dir);

        System.out.println("Total read megabytes: " + FORMATTER.format(totalLength / MB));

        deleteDuplicates(files);
    }

    private static boolean checkArgs(String[] args) {
        if (args == null || args.length < 1) {
            System.out.println(("Input path must be specified"));
            return false;
        }
        return true;
    }

    private static void findDuplicates(Map<String, List<File>> files, File dir) throws IOException {
        final File[] listFiles = Optional.ofNullable(dir.listFiles()).orElse(new File[0]);

        for (File file : listFiles) {
            if (file.isDirectory()) {
                findDuplicates(files, file);
            } else {
                byte[] data;
                try (InputStream in = new FileInputStream(file)) {
                    long length = file.length() / 10;
                    totalLength += length;
                    data = new byte[(int) length];
                    in.read(data);
                }

                final String hash = new BigInteger(1, messageDigest.digest(data)).toString(HASH_SIZE);
                final List<File> list = files.getOrDefault(hash, new LinkedList<>());
                list.add(file);
                files.put(hash, list);
            }
        }
    }

    private static void deleteDuplicates(Map<String, List<File>> duplicates) {
        for (Map.Entry<String, List<File>> entry : duplicates.entrySet()) {
            final List<File> files = entry.getValue();
            for (int i = 1; i < files.size(); i++) {
                final File file = files.get(i);
                final boolean deleted = file.delete();

                if (deleted) {
                System.out.println("Deleted: " + file);
                }
            }
        }
    }
}
