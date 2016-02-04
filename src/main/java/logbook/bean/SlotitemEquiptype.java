package logbook.bean;

import javax.json.JsonObject;

import logbook.internal.JsonHelper;

/**
 * api_mst_slotitem_equiptype
 *
 */
public class SlotitemEquiptype {

    /** api_id */
    private Integer id;

    /** api_name */
    private String name;

    /** api_show_flg */
    private Integer showFlg;

    /**
     * api_idを取得します。
     * @return api_id
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * api_idを設定します。
     * @param id api_id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * api_nameを取得します。
     * @return api_name
     */
    public String getName() {
        return this.name;
    }

    /**
     * api_nameを設定します。
     * @param name api_name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * api_show_flgを取得します。
     * @return api_show_flg
     */
    public Integer getShowFlg() {
        return this.showFlg;
    }

    /**
     * api_show_flgを設定します。
     * @param showFlg api_show_flg
     */
    public void setShowFlg(Integer showFlg) {
        this.showFlg = showFlg;
    }

    /**
     * JsonObjectから{@link SlotitemEquiptype}を構築します
     *
     * @param json JsonObject
     * @return {@link SlotitemEquiptype}
     */
    public static SlotitemEquiptype toSlotitemEquiptype(JsonObject json) {
        SlotitemEquiptype bean = new SlotitemEquiptype();
        JsonHelper.bind(json)
                .set("api_id", bean::setId, JsonHelper::toInteger)
                .set("api_name", bean::setName, JsonHelper::toString)
                .set("api_show_flg", bean::setShowFlg, JsonHelper::toInteger);
        return bean;
    }
}
