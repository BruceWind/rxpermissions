package com.androidyuan.rxpermissions.component;

/**
 * Created by wei on 16-9-17.
 * 封装了  permission制定的 name 和 是否授权
 */
public class Permission {

    public final String name;
    public final boolean granted;//是否授权

    public Permission(String name, boolean granted) {
        this.name = name;
        this.granted = granted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;

        if (granted != that.granted) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (granted ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "name='" + name + '\'' +
                ", granted=" + granted +
                '}';
    }
}
