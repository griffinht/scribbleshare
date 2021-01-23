package net.stzups.board.data;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataAccessObject<K extends Serializable, V extends Serializable> extends HashMap<K, V> {
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

    @SuppressWarnings("unchecked")
    public void load() throws IOException {
        System.out.println("LOADING");
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(getFile()))) {
            System.out.println(objectInputStream.available());
            while (true) {
                Object key;
                Object value;
                try {
                    key = objectInputStream.readObject();
                    value = objectInputStream.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    continue;
                } catch (EOFException e) {
                    System.out.println("eof exception");
                    break;
                }
                put((K) key, (V) value);//unchecked
            }

        }
    }

    public void save() throws IOException {
        System.out.println("SAVIGIN " + size());
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(getFile()))) {
            for (Map.Entry<K, V> entry : entrySet()) {
                objectOutputStream.writeObject(entry.getKey());
                objectOutputStream.writeObject(entry.getValue());
            }
        }
    }
}
