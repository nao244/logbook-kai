package logbook.internal;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import logbook.Messages;

/**
 * アプリケーションの設定を読み書きします
 *
 */
public final class Config {

    private static final Path CONFIG_DIR = Paths.get("./config"); //$NON-NLS-1$

    private static final Config DEFAULT = new Config(CONFIG_DIR);

    private final Path dir;

    private final Map<Class<?>, Object> map = new ConcurrentHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * アプリケーション設定の読み書きを指定のディレクトリで行います
     *
     * @param dir アプリケーション設定ディレクトリ
     */
    public Config(Path dir) {
        this.dir = dir;
    }

    /**
     * clazzで指定された型からインスタンスを復元します
     *
     * @param <T> Bean型
     * @param clazz Bean型 Classオブジェクト
     * @param def デフォルト値を供給するSupplier
     * @return 設定
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz, Supplier<T> def) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(def);

        T instance = (T) this.map.computeIfAbsent(clazz, key -> {
            T v = this.read((Class<T>) key);
            if (v == null) {
                v = def.get();
            }
            return v;
        });
        return instance;
    }

    /**
     * 読み込まれたすべてのインスタンスをファイルに書き込みます
     */
    public void store() {
        this.map.entrySet()
                .forEach(this::store);
    }

    private void store(Entry<Class<?>, ?> entry) {
        this.write(entry.getKey(), entry.getValue());
    }

    @SuppressWarnings("unchecked")
    private <T> T read(Class<T> clazz) {
        T instance = null;
        Path filepath;

        // try read from JSON
        filepath = this.jsonPath(clazz);
        try {
            if (!Files.isReadable(filepath) || (Files.size(filepath) <= 0)) {
                // ファイルが読み込めないまたはサイズがゼロの場合バックアップファイルを読み込む
                filepath = this.backupPath(filepath);
            }
            if (Files.isReadable(filepath)) {
                try (Reader reader = Files.newBufferedReader(filepath)) {
                    instance = this.mapper.readValue(reader, clazz);
                }
            }
        } catch (Exception e) {
            instance = null;
            this.getListener().exceptionThrown(e);
        }

        // try read from XML
        if (instance == null) {
            filepath = this.xmlPath(clazz);
            try {
                if (!Files.isReadable(filepath) || (Files.size(filepath) <= 0)) {
                    // ファイルが読み込めないまたはサイズがゼロの場合バックアップファイルを読み込む
                    filepath = this.backupPath(filepath);
                }
                if (Files.isReadable(filepath)) {
                    try (InputStream in = new BufferedInputStream(Files.newInputStream(filepath))) {
                        try (XMLDecoder encoder = new XMLDecoder(in, this.getListener())) {
                            instance = (T) encoder.readObject();
                        }
                    }
                }
            } catch (Exception e) {
                instance = null;
                this.getListener().exceptionThrown(e);
            }
        }
        return instance;
    }

    private void write(Class<?> clazz, Object instance) {
        try {
            Path filepath = this.jsonPath(clazz);

            // create parent directory
            if (!Files.exists(filepath)) {
                Path parent = filepath.getParent();
                if (parent != null) {
                    if (!Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }
                }
            }

            // write JSON
            if (Files.exists(filepath) && (Files.size(filepath) > 0)) {
                Path backup = this.backupPath(filepath);
                // ファイルが存在してかつサイズが0を超える場合、ファイルをバックアップにリネームする
                Files.move(filepath, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            try (Writer writer = Files.newBufferedWriter(filepath, StandardOpenOption.CREATE)) {
                this.mapper.writeValue(writer, instance);
            }

            // XML形式の古い設定ファイルを削除する
            // .backup ファイルを削除して、.xml ファイルを .backupにリネームする
            Path xml = this.xmlPath(clazz);
            Path xmlbackup = this.backupPath(xml);
            if (Files.exists(xmlbackup)) {
                Files.deleteIfExists(xmlbackup);
            }
            if (Files.exists(xml)) {
                Files.move(xml, xmlbackup, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            this.getListener().exceptionThrown(e);
        }
    }

    private Path xmlPath(Class<?> clazz) {
        return this.dir.resolve(clazz.getCanonicalName() + ".xml");
    }

    private Path jsonPath(Class<?> clazz) {
        return this.dir.resolve(clazz.getCanonicalName() + ".json");
    }

    private Path backupPath(Path filepath) {
        return filepath.resolveSibling(filepath.getFileName() + ".backup"); //$NON-NLS-1$
    }

    private ExceptionListener getListener() {
        return e -> LoggerHolder.LOG.warn(Messages.getString("ConfigReader.1"), e); //$NON-NLS-1$
    }

    /**
     * アプリケーションのデフォルト設定ディレクトリから設定を取得します
     *
     * @return アプリケーションのデフォルト設定ディレクトリ
     */
    public static Config getDefault() {
        return DEFAULT;
    }

    private static class LoggerHolder {
        /** ロガー */
        private static final Logger LOG = LogManager.getLogger(Config.class);
    }
}
