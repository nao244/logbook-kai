package logbook.internal.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import logbook.bean.Ship;
import logbook.bean.ShipMst;
import logbook.bean.SlotItem;
import logbook.bean.SlotItemCollection;
import logbook.bean.SlotitemMst;
import logbook.internal.Items;
import logbook.internal.Ships;

/**
 * 所有艦娘一覧のテーブル
 *
 */
public class ShipTablePane extends VBox {

    /** テーブル */
    @FXML
    private TableView<ShipItem> table;

    /** ID */
    @FXML
    private TableColumn<ShipItem, Integer> id;

    /** 艦娘 */
    @FXML
    private TableColumn<ShipItem, Ship> ship;

    /** 艦種 */
    @FXML
    private TableColumn<ShipItem, String> type;

    /** Lv */
    @FXML
    private TableColumn<ShipItem, Integer> lv;

    /** cond */
    @FXML
    private TableColumn<ShipItem, Integer> cond;

    /** 海域 */
    @FXML
    private TableColumn<ShipItem, String> area;

    /** 制空 */
    @FXML
    private TableColumn<ShipItem, Integer> seiku;

    /** 砲戦火力 */
    @FXML
    private TableColumn<ShipItem, Integer> hPower;

    /** 雷戦火力 */
    @FXML
    private TableColumn<ShipItem, Integer> rPower;

    /** 夜戦火力 */
    @FXML
    private TableColumn<ShipItem, Integer> yPower;

    /** 対潜火力 */
    @FXML
    private TableColumn<ShipItem, Integer> tPower;

    /** 装備1 */
    @FXML
    private TableColumn<ShipItem, Integer> slot1;

    /** 装備2 */
    @FXML
    private TableColumn<ShipItem, Integer> slot2;

    /** 装備3 */
    @FXML
    private TableColumn<ShipItem, Integer> slot3;

    /** 装備4 */
    @FXML
    private TableColumn<ShipItem, Integer> slot4;

    /** 補強 */
    @FXML
    private TableColumn<ShipItem, Integer> slotEx;

    /** 艦娘達 */
    private final List<Ship> ships;

    /** 艦娘達 */
    private final ObservableList<ShipItem> shipItems = FXCollections.observableArrayList();

    /**
     * 所有艦娘一覧のテーブルのコンストラクタ
     *
     * @param ships 艦娘達
     */
    public ShipTablePane(Collection<Ship> ships) {
        this.ships = new ArrayList<>(ships);
        try {
            FXMLLoader loader = InternalFXMLLoader.load("logbook/gui/ship_table.fxml");
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            LogManager.getLogger(ShipTablePane.class)
                    .error("FXMLのロードに失敗しました", e);
        }
    }

    @FXML
    void initialize() {
        try {
            // カラムとオブジェクトのバインド
            this.id.setCellValueFactory(new PropertyValueFactory<>("id"));
            this.ship.setCellValueFactory(new PropertyValueFactory<>("ship"));
            this.ship.setCellFactory(p -> new ShipImageCell());
            this.type.setCellValueFactory(new PropertyValueFactory<>("type"));
            this.lv.setCellValueFactory(new PropertyValueFactory<>("lv"));
            this.cond.setCellValueFactory(new PropertyValueFactory<>("cond"));
            this.area.setCellValueFactory(new PropertyValueFactory<>("area"));
            this.seiku.setCellValueFactory(new PropertyValueFactory<>("seiku"));
            this.hPower.setCellValueFactory(new PropertyValueFactory<>("hPower"));
            this.rPower.setCellValueFactory(new PropertyValueFactory<>("rPower"));
            this.yPower.setCellValueFactory(new PropertyValueFactory<>("yPower"));
            this.tPower.setCellValueFactory(new PropertyValueFactory<>("tPower"));
            this.slot1.setCellValueFactory(new PropertyValueFactory<>("slot1"));
            this.slot1.setCellFactory(p -> new ItemImageCell());
            this.slot2.setCellValueFactory(new PropertyValueFactory<>("slot2"));
            this.slot2.setCellFactory(p -> new ItemImageCell());
            this.slot3.setCellValueFactory(new PropertyValueFactory<>("slot3"));
            this.slot3.setCellFactory(p -> new ItemImageCell());
            this.slot4.setCellValueFactory(new PropertyValueFactory<>("slot4"));
            this.slot4.setCellFactory(p -> new ItemImageCell());
            this.slotEx.setCellValueFactory(new PropertyValueFactory<>("slotEx"));
            this.slotEx.setCellFactory(p -> new ItemImageCell());

            this.shipItems.addAll(this.ships.stream()
                    .map(ShipItem::toShipItem)
                    .sorted(Comparator.comparing(ShipItem::getType))
                    .sorted(Comparator.comparing(ShipItem::getLv).reversed())
                    .collect(Collectors.toList()));

            this.table.setItems(this.shipItems);

        } catch (Exception e) {
            LogManager.getLogger(NdockPane.class)
                    .error("FXMLの初期化に失敗しました", e);
        }
    }

    /**
     * 艦娘画像のセル
     *
     */
    private static class ShipImageCell extends TableCell<ShipItem, Ship> {
        @Override
        protected void updateItem(Ship ship, boolean empty) {
            super.updateItem(ship, empty);

            if (!empty) {
                this.setGraphic(new ImageView(Ships.shipWithItemImage(ship)));
                this.setText(Ships.shipMst(ship)
                        .map(ShipMst::getName)
                        .orElse(""));
            } else {
                this.setGraphic(null);
                this.setText(null);
            }
        }
    }

    /**
     * 装備画像のセル
     *
     */
    private static class ItemImageCell extends TableCell<ShipItem, Integer> {
        @Override
        protected void updateItem(Integer itemId, boolean empty) {
            super.updateItem(itemId, empty);

            if (!empty) {
                SlotItem item = SlotItemCollection.get()
                        .getSlotitemMap()
                        .get(itemId);
                Optional<SlotitemMst> mst = Items.slotitemMst(item);

                if (mst.isPresent()) {
                    StringBuilder text = new StringBuilder(mst.get().getName());

                    if (item.getAlv() != null && item.getAlv() > 0) {
                        text.append("☆+" + item.getAlv());
                    }
                    if (item.getLevel() > 0) {
                        text.append("★+" + item.getLevel());
                    }
                    this.setGraphic(new ImageView(Items.itemImage(mst.get())));
                    this.setText(text.toString());
                }
            } else {
                this.setGraphic(null);
                this.setText(null);
            }
        }
    }
}