package de.uib.configed.dashboard.view;

import java.util.*;

public class ViewManager
{
    private static Map<String, View> views = new HashMap<>();

    public static void addView(String name, View view)
    {
        views.put(name, view);
    }

    public static void removeView(String name)
    {
        views.remove(name);
    }

    public static void displayView(String name)
    {
        views.get(name).display();
    }
}
