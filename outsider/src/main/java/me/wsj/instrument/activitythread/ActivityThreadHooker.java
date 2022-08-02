package me.wsj.instrument.activitythread;


import me.wsj.core.utils.Looger;

/**
 * @author neighbWang
 */
public class ActivityThreadHooker {

    private volatile static boolean hooked;

    /**
     * @param ignorePackages comma-separated list
     */
    public static void hook(final String ignorePackages) {
        if (hooked) {
            return;
        }

        try {
            final String pkgs = null == ignorePackages ? "" : ignorePackages.trim();
            final ActivityThreadCallback callback = new ActivityThreadCallback(pkgs.split("\\s*,\\s*"));
            if (!(hooked = callback.hook())) {
                Looger.i("Hook ActivityThread.mH.mCallback failed");
            }
        } catch (final Throwable t) {
            Looger.i("Hook ActivityThread.mH.mCallback failed" + t);
        }

        if (hooked) {
            Looger.i("Hook ActivityThread.mH.mCallback success!");
        }
    }

}
