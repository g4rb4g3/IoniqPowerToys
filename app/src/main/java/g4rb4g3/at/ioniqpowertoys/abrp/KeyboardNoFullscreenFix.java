package g4rb4g3.at.ioniqpowertoys.abrp;

import android.view.inputmethod.EditorInfo;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class KeyboardNoFullscreenFix implements IXposedHookLoadPackage {
  public static final String PKG_NAME_ABRP = "com.iternio.abrpapp";
  public static final String CLASS_NAME_EDITTEXT = "com.facebook.react.views.textinput.ReactEditText";

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
    if (!PKG_NAME_ABRP.equals(lpparam.packageName)) {
      return;
    }

    Class<?> classReactEditText = findClass(CLASS_NAME_EDITTEXT, lpparam.classLoader);
    findAndHookMethod(classReactEditText, "onCreateInputConnection", EditorInfo.class, new XC_MethodHook() {
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        EditorInfo editorInfo = (EditorInfo) param.args[0];
        editorInfo.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN;
      }
    });
  }
}
