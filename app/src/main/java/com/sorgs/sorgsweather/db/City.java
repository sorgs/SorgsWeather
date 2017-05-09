package com.sorgs.sorgsweather.db;

import org.litepal.crud.DataSupport;

/**
 * 存放市级数据
 * <p>
 * Created by Sorgs
 * on 2017/5/9.
 */

public class City extends DataSupport {
    public int id;
    /**
     * 市的名字
     */
    public String cityName;
    /**
     * 市的代号
     */
    public int cityCode;
    /**
     * 当前市对应所属省的id
     */
    public int provinceId;
}
