package com.mehtank.androminion.server;

import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;

import com.vdom.api.Card;

public class Strings {
    static HashMap<Card, String> nameCache = new HashMap<Card, String>();
    static HashMap<Card, String> descriptionCache = new HashMap<Card, String>();
    static HashMap<String, String> expansionCache = new HashMap<String, String>();
	public static Context context;
    
    public static String getCardName(Card c) {
        String name = nameCache.get(c);
        if(name == null) {
           try {
               Resources r = context.getResources();
               int id = r.getIdentifier(c.getSafeName() + "_name", "string", context.getPackageName());
               name = r.getString(id);
           }
           catch(Exception e) {
               e.printStackTrace();
           }
           if(name == null) {
               name = c.getName();
           }
           
           nameCache.put(c, name);
        }
        return name;
    }
    
    public static String getCardDescription(Card c) {
        String description = descriptionCache.get(c);
        if(description == null) {
            try {
                Resources r = context.getResources();
                int id = r.getIdentifier(c.getSafeName() + "_desc", "string", context.getPackageName());
                description = r.getString(id);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            if(description == null) {
                description = c.getDescription();
            }
            
            descriptionCache.put(c, description);
        }
        return description;
    }
    
    public static String getCardExpansion(Card c) {
        String expansion = expansionCache.get(c.getExpansion());
        if(expansion == null) {
            try {
                Resources r = context.getResources();
                int id = r.getIdentifier(c.getExpansion(), "string", context.getPackageName());
                expansion = r.getString(id);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            if(expansion == null) {
                expansion = c.getExpansion();
            }
            
           expansionCache.put(c.getExpansion(), expansion);
        }
        return expansion;
    }
    
    public static String format(String str, Object... args) {
        return String.format(str, args);
    }

    public static String format(int resId, Object... args) {
        return String.format(context.getString(resId), args);
    }
    
    public static String getString(int resId) {
        return context.getString(resId);
    }
}