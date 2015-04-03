package ua.kpi.kbis.campus;

import android.support.v4.app.Fragment;

/**
 * Created by Stanislav on 17.02.2015.
 */
class NavMenuItem {
    String name;
    int icon;
    Fragment menuFragment;

    NavMenuItem(String name, int icon, Fragment menuFragment) {
        this.name = name;
        this.icon = icon;
        this.menuFragment = menuFragment;
    }
}
