package net.stzups.board.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataAccessObject<K, V> extends HashMap<K, V> {
    private static final String FILE_EXTENSION = "data";

    private File file;

    public DataAccessObject(String name) throws IOException {
        File directory = new File(Optional.ofNullable(System.getenv("DATA_ROOT")).orElse("data"));
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Error while making directory at " + directory.getPath());
        }
        file = new File(directory, name + "." + FILE_EXTENSION);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Error while making file at " + file.getPath());
            }
            save();
        }
        load();
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    protected File getFile() {
        return file;
    }

    public void load() throws IOException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(getFile()))) {
            System.out.println(objectInputStream.available());
            while (objectInputStream.available() > 0) {
                Object object;
                try {
                    object = objectInputStream.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }
                System.out.println(object.getClass() + ", " + object);
            }

        }
    }

    public void save() throws IOException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(getFile()))) {
            for (Map.Entry<K, V> entry : entrySet()) {
                objectOutputStream.writeObject(entry.getKey());
                objectOutputStream.writeObject(entry.getValue());
            }
        }
    }
}
