package logbook.internal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * アプリケーションの設定を読み書きします
 *
 */
public final class Config {

    /** 設定ファイル デフォルトディレクトリ */
    private static final String CONFIG_DIR = "./config"; //$NON-NLS-1$

    /** 設定ファイル 拡張子 */
    private static final String CONFIG_EXT = ".xml"; //$NON-NLS-1$

    private static final Config DEFAULT = new Config(Paths.get(CONFIG_DIR));

    private final Path dir;

    private final Map<Class<?>, Object> map = new IdentityHashMap<>();

    /**
     * アプリケーション設定の読み書きを指定のディレクトリで行います
     *
     * @param dir アプリケーション設定ディレクトリ
     */
    public Config(Path dir) {
        this.dir = dir;
    }

    /**
     * clazzで指定された型からXMLEncoderテキスト表現を復元します
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

        T instance;
        synchronized (this.map) {
            instance = (T) this.map.get(clazz);
            if (instance == null) {
                ConfigReader<T> reader = new ConfigReader<>(this.getPath(clazz));
                instance = reader.read(clazz);
            }
            if (instance == null) {
                instance = def.get();
            }
            this.map.put(clazz, instance);
        }
        return instance;
    }

    /**
     * 読み込まれたすべてのBeanインスタンスをXMLEncoderテキスト表現でファイルに書き込みます
     */
    public void store() {
        synchronized (this.map) {
            this.map.entrySet()
                .parallelStream()
                .forEach(this::store);
        }
    }

    /**
     * BeanインスタンスをXMLEncoderテキスト表現でファイルに書き込みます
     *
     * @param entry Classオブジェクトとオブジェクトのペア
     */
    private void store(Entry<Class<?>, ?> entry) {
        ConfigWriter writer = new ConfigWriter(this.getPath(entry.getKey()));
        writer.write(entry.getValue());
    }

    private <T> Path getPath(Class<T> clazz) {
        return this.dir.resolve(clazz.getCanonicalName() + CONFIG_EXT);
    }

    /**
     * アプリケーションのデフォルト設定ディレクトリから設定を取得します
     *
     * @return アプリケーションのデフォルト設定ディレクトリ
     */
    public static Config getDefault() {
        return DEFAULT;
    }
}