package cn.ucai.superwechat.utils;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by 11039 on 2016/11/4.
 */
public class ExitActivityUtils {
    private List<Activity> activityList = new LinkedList<>();
    private static ExitActivityUtils instance = new ExitActivityUtils();
    private ExitActivityUtils(){}
    public static  ExitActivityUtils getInstance(){
        return instance;
    }
    public void addActivity(Activity activity){
        activityList.add(activity);
    }
    public void delActivity(Activity activity){
        activityList.remove(activity);
    }
    public void exit(){
        for(Activity activity:activityList){
            activity.finish();
        }
    }
}
