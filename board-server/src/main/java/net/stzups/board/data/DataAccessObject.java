package net.stzups.board.data;

import net.stzups.board.Board;

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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataAccessObject<K extends Serializable, V extends Serializable> extends HashMap<K, V> {
    private static final int SAVE_INTERVAL = Integer.parseInt(Board.getConfig().get("autosave.interval", "-1"));//in seconds, -1 to disable
    private static final String FILE_EXTENSION = "data";

    private File file;

    public DataAccessObject(String name) throws IOException {
        File directory = new File(Board.getConfig().get("data.root.path", "data"));
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
        if (SAVE_INTERVAL > 0) {
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                try {
                    save();
                } catch (IOException e) {
                    Board.getLogger().warning(new IOException("Autosave: Failed to save to disk (ruh roh raggy!)", e).toString());
                }
            }, 10, SAVE_INTERVAL, TimeUnit.SECONDS);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                save();
            } catch (IOException e) {
                Board.getLogger().warning(new IOException("Failed to save to disk (ruh roh raggy!)", e).toString());
            }
        }));
    }

    protected File getFile() {
        return file;
    }

    @SuppressWarnings("unchecked")
    public void load() throws IOException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(getFile()))) {
            while (true) {
                Object key;
                Object value;
                try {
                    key = objectInputStream.readObject();
                    value = objectInputStream.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    continue;//maybe just one bad entry, so keep going
                } catch (EOFException e) {
                    break;//indicates end of file
                }
                put((K) key, (V) value);//todo unchecked
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
