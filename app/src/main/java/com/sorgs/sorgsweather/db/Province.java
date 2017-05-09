package com.sorgs.sorgsweather.db;

import org.litepal.crud.DataSupport;

/**
 * 存放省级数据
 * <p>
 * Created by Sorgs
 * on 2017/5/9.
 */

public class Province extends DataSupport {
    public int id;
    /**
     * 省级名字
     */
    public String provinceName;
    /**
     * 省的代号
     */
    public int provinceCode;
}
