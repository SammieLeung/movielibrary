package com.hphtv.movielibrary.util;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * author: Sam Leung
 * date:  2022/4/21
 */
public class ViewHolderCreator {


    public static <VH extends RecyclerView.ViewHolder> Constructor<VH>[] getViewHolderConstructors(Class clazz) {
        Class Class_VH = getViewHolderClass(clazz);
        if (Class_VH != null) {
            Constructor[] constructors = (Constructor[]) Class_VH.getDeclaredConstructors();
            return constructors;
        }
        return null;
    }

    /**
     * 获取ViewHolder具体类
     *
     * @param clazz
     * @return
     */
    private static Class getViewHolderClass(Class clazz) {
        Class modelClass = null;
        Type type = clazz.getGenericSuperclass();
        if (type == null)
            return null;
        if (type instanceof ParameterizedType) {
            ParameterizedType tmpType = (ParameterizedType) type;
            for (Type t : tmpType.getActualTypeArguments()) {
                if (t instanceof Class && instanceOfViewHolder((Class) t))
                    modelClass = (Class) t;
            }
            if (modelClass == null)
                modelClass = getViewHolderClass(clazz.getSuperclass());
        } else {
            modelClass = getViewHolderClass(clazz.getSuperclass());
        }
        return modelClass;
    }

    /**
     * 判断是否继承于ViewHolder
     *
     * @param clazz
     * @return
     */
    private static boolean instanceOfViewHolder(Class clazz) {
        if (clazz == null)
            return false;
        if (!RecyclerView.ViewHolder.class.isAssignableFrom(clazz))
            return instanceOfViewHolder(clazz.getSuperclass());
        else
            return true;
    }
}
