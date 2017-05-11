package com.sorgs.sorgsweather.db;

import org.litepal.crud.DataSupport;

/**
 * 存放县级数据
 * <p>
 * Created by Sorgs
 * on 2017/5/9.
 */

public class County extends DataSupport {

    public int id;
    /**
     * 县的名字
     */
    public String countyName;
    /**
     * 对应的天气的id
     */
    public String weatherId;
    /**
     * 县对应所属的市的id
     */
    public int cityId;

}
