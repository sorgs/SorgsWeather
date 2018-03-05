package com.sorgs.sorgsweather.domian;

/**
 * description: xxx.
 *
 * @author Sorgs.
 * @date 2018/2/28.
 */

public class BrokenLineBean {
    /**
     * 天气，取值为上面6种
     */
    public String weather;

    /**
     * 温度值
     */
    public int temperature;

    /**
     * 温度的描述
     */
    public String temperatureStr;

    /**
     * 时间值
     */
    public String time;

    public BrokenLineBean(String weather, int temperature, String time) {
        this.weather = weather;
        this.temperature = temperature;
        this.time = time;
        this.temperatureStr = temperature + "°";
    }

    public BrokenLineBean(String weather, int temperature, String temperatureStr, String time) {
        this.weather = weather;
        this.temperature = temperature;
        this.temperatureStr = temperatureStr;
        this.time = time;
    }

}
