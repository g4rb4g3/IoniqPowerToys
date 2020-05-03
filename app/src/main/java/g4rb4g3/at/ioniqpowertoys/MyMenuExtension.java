package g4rb4g3.at.ioniqpowertoys;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class MyMenuExtension implements IXposedHookLoadPackage, IXposedHookInitPackageResources {
  public static final String PKG_NAME_MY_MENU = "com.lge.ivi.launcher";
  public static final String PKG_NAME_ABRP = "com.iternio.abrpapp";

  public static final String CLASS_LAUNCHER_APPDATAENUM = "com.lge.ivi.launcher.item.MyMenuAppDataEnum";
  public static final String CLASS_LAUNCHER_MYMENUACTIVITY = "com.lge.ivi.launcher.activity.MyMenuActivity";
  public static final String CLASS_LAUNCHER_MAINACTIVITY = "com.lge.ivi.launcher.activity.MainActivity";

  public static final int CLASS_LAUNCHER_RESID_MIRRORLINKTITLE = 2131165292;
  public static final int CLASS_LAUNCHER_RESID_ICON_NAVI = 2130837811;
  public static final int CLASS_LAUNCHER_RESID_ICON_NAVINOR = 2130837713;
  public static final int MIN_DISTANCE = 50;

  private Class<?> mClassMyMenuActivity;
  private float mDownY, mUpY;

  @Override
  public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
    if (!PKG_NAME_MY_MENU.equals(lpparam.packageName)) {
      return;
    }

    //add abrp to app data enum for my menu
    Class<?> classAppDataEnum = findClass(CLASS_LAUNCHER_APPDATAENUM, lpparam.classLoader);
    findAndHookMethod(classAppDataEnum, "values", new XC_MethodHook() {
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        Object[] enumItems = (Object[]) param.getResult();
        Object[] newEnumItems = Arrays.copyOf(enumItems, enumItems.length + 1);
        final Class<?> classAppDataEnum = findClass(CLASS_LAUNCHER_APPDATAENUM, lpparam.classLoader);
        Object abrp = XposedHelpers.newInstance(classAppDataEnum, "ABRP", newEnumItems.length, CLASS_LAUNCHER_RESID_ICON_NAVI, CLASS_LAUNCHER_RESID_ICON_NAVINOR, CLASS_LAUNCHER_RESID_MIRRORLINKTITLE, PKG_NAME_ABRP, true, 0, 0);
        newEnumItems[newEnumItems.length - 1] = abrp;
        param.setResult(newEnumItems);
      }
    });

    //override onclick method in my menu activity so our added apps can be started
    mClassMyMenuActivity = findClass(CLASS_LAUNCHER_MYMENUACTIVITY, lpparam.classLoader);
    findAndHookMethod(mClassMyMenuActivity, "onClick", android.view.View.class, new XC_MethodHook() {
      @Override
      protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        View button = (View) param.args[0];
        if (button.getTag() != null) {
          ArrayList hl = (ArrayList) getObjectField(param.thisObject, "hl");
          int index = Integer.valueOf(button.getTag().toString());
          Object item = hl.get(index);
          String pkgName = (String) getObjectField(item, "pakageName");
          if (PKG_NAME_ABRP.equals(pkgName)) {
            param.setResult(null);
            Context context = AndroidAppHelper.currentApplication().getApplicationContext();
            context.startActivity(context.getPackageManager().getLaunchIntentForPackage(PKG_NAME_ABRP));
          }
        }
      }
    });

    //add ontouch listener to all menu button so a swipe from bottom to top will open my menu app
    Class<?> classMainActivity = findClass(CLASS_LAUNCHER_MAINACTIVITY, lpparam.classLoader);
    findAndHookMethod(classMainActivity, "onCreate", Bundle.class, new XC_MethodHook() {
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        LinearLayout allMenus = (LinearLayout) getObjectField(param.thisObject, "gY");
        allMenus.setOnTouchListener(new View.OnTouchListener() {
          @Override
          public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
              case MotionEvent.ACTION_DOWN: {
                mDownY = event.getY();
              }
              case MotionEvent.ACTION_UP: {
                mUpY = event.getY();
                float deltaY = mDownY - mUpY;
                if (Math.abs(deltaY) > MIN_DISTANCE && deltaY > 0) {
                  //BottomToTopSwipe
                  launchMyMenu();
                  return true;
                }
              }
            }

            return false;
          }
        });
      }
    });
  }

  @Override
  public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
    if (!PKG_NAME_MY_MENU.equals(resparam.packageName)) {
      return;
    }
    resparam.res.setReplacement(CLASS_LAUNCHER_RESID_MIRRORLINKTITLE, "ABRP");
  }

  private void launchMyMenu() {
    Context context = AndroidAppHelper.currentApplication().getApplicationContext();
    Intent intent = new Intent(context, mClassMyMenuActivity);
    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra("fromMenu", "none");
    context.startActivity(intent);
  }
}
