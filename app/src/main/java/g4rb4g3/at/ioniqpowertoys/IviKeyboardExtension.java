package g4rb4g3.at.ioniqpowertoys;

import android.inputmethodservice.Keyboard;

import com.lge.ivi.media.ExtMediaManager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class IviKeyboardExtension implements IXposedHookLoadPackage {
  public static final String PKG_NAME_KEYBOARD = "com.lge.app.keyboard";
  public static final String CLASS_NAME_LATINKEYBOARDVIEW = "com.lge.app.keyboard.LatinKeyboardView";
  public static final int KEYCODE_OK = 10;

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
    if (!PKG_NAME_KEYBOARD.equals(lpparam.packageName)) {
      return;
    }

    Class<?> classKeyboard = findClass(CLASS_NAME_LATINKEYBOARDVIEW, lpparam.classLoader);
    findAndHookMethod(classKeyboard, "onLongPress", Keyboard.Key.class, new XC_MethodHook() {
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        Keyboard.Key key = (Keyboard.Key) param.args[0];
        if (key.codes[0] == KEYCODE_OK) {
          Runtime.getRuntime().exec(new String[]{"su", "-c", "input keyevent 4"});
          param.setResult(true);
        }
      }
    });
  }
}
