package com.limingz.mymod.util;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SQLiteTempData {
    public static ConcurrentLinkedQueue sqliteAddQueue = new ConcurrentLinkedQueue();
    public static ConcurrentLinkedQueue sqliteDeleteQueue = new ConcurrentLinkedQueue();
}
