package com.sorgs.sorgsweather.db;

import org.litepal.crud.DataSupport;

import android.util.Log;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * description: 从数据库查询city.
 *
 * @author Sorgs.
 * @date 2018/3/7.
 */

public class QueryCityFromDb {
    /**
     * 省级
     */
    public static final int LEVEL_PROVINCE = 0;

    /**
     * 市级
     */
    public static final int LEVEL_CITY = 1;

    /**
     * 县级
     */
    public static final int LEVEL_COUNTY = 2;

    /**
     * 从数据库获取城市数据
     *
     * @param cityId       查询的城市id
     * @param currentLevel 当前需要查询的级别
     */
    public static Observable queryCity(String cityId, int currentLevel) {
        Log.i("ChooseAreaFragment", "cityId: " + cityId + "currentLevel: " + currentLevel);
        return Observable.create(emitter -> {
            switch (currentLevel) {
                case LEVEL_PROVINCE:
                    //省级
                    //查询所有省份
                    List<Province> provinces = DataSupport.findAll(Province.class);
                    if (provinces.isEmpty()) {
                        emitter.onError(new RuntimeException(String.valueOf(LEVEL_PROVINCE)));
                    } else {
                        emitter.onNext(provinces);
                    }
                    break;
                case LEVEL_CITY:
                    //市级
                    List<City> cities = DataSupport.where("provinceid = ?", cityId).find(City.class);
                    if (cities.isEmpty()) {
                        emitter.onError(new RuntimeException(String.valueOf(LEVEL_CITY)));
                    } else {
                        emitter.onNext(cities);
                    }
                    break;
                case LEVEL_COUNTY:
                    //县级
                    List<County> counties = DataSupport.where("cityid = ?", cityId).find(County.class);
                    if (counties.isEmpty()) {
                        emitter.onError(new RuntimeException(String.valueOf(LEVEL_COUNTY)));
                    } else {
                        emitter.onNext(counties);
                    }
                    break;
                default:
                    emitter.onError(new Throwable());
                    break;
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
