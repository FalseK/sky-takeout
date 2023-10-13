package com.sky.utils;

public class AdminHolder {
    public static final ThreadLocal<Long> tl = new ThreadLocal<>();

    public static void saveAdminId(Long adminId){tl.set(adminId);}

    public static Long getAdminId(){return tl.get();}

    public static void removeAdminId(){tl.remove();}

}
