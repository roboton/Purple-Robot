package edu.northwestern.cbits.purple_robot_manager.widget;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import edu.northwestern.cbits.purple_robot_manager.BootUpReceiver;
import edu.northwestern.cbits.purple_robot_manager.ManagerService;
import edu.northwestern.cbits.purple_robot_manager.config.LegacyJSONConfigFile;
import edu.northwestern.cbits.purple_robot_manager.logging.LogManager;
import edu.northwestern.cbits.purple_robot_manager.scripting.BaseScriptEngine;
import edu.northwestern.cbits.purple_robot_manager.scripting.JavaScriptEngine;
import edu.northwestern.cbits.purple_robot_manager.triggers.TriggerManager;

public class PurpleRobotAppWidgetProvider extends AppWidgetProvider
{

    public void onReceive(Context context, Intent intent)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Log.i("Status", "Broadcast send from " + intent.getAction());

        if ("android.appwidget.action.APPWIDGET_UPDATE".equals(intent.getAction()))
        {
            Log.i("Status:", "Boot action detected");
            long now = System.currentTimeMillis();

            SharedPreferences.Editor e = prefs.edit();

            e.putLong(BootUpReceiver.BOOT_KEY, now);
            e.putBoolean(BootUpReceiver.BOOT_STATUS, true);

            e.commit();

            LegacyJSONConfigFile.getSharedFile(context.getApplicationContext());

            TriggerManager.getInstance(context).fireMissedTriggers(context, now);

            if (prefs.contains(BaseScriptEngine.STICKY_NOTIFICATION_PARAMS)) {
                try {
                    JSONObject json = new JSONObject(prefs.getString(BaseScriptEngine.STICKY_NOTIFICATION_PARAMS, "{}"));

                    JavaScriptEngine engine = new JavaScriptEngine(context);

                    engine.showScriptNotification(json.getString("title"), json.getString("message"),
                            json.getBoolean("persistent"), json.getBoolean("sticky"), json.getString("script"));
                } catch (JSONException ex) {
                    LogManager.getInstance(context).logException(ex);
                }
            }
        }

        ManagerService.setupPeriodicCheck(context);
    }

}
