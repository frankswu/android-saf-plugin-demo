
package com.example.plugin1;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 自定义类,实现Parcelable接口,用于DL插件的Activity之间传�?Parcelable数据示例.
 * 
 * @author mrsimple
 */
public class Person implements Parcelable {

    String mName;
    int mAge;

    public Person(String name, int age) {
        mName = name;
        mAge = age;
    }

    protected Person(Parcel in) {
        mName = in.readString();
        mAge = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeInt(mAge);
    }

    public static final Parcelable.Creator<Person> CREATOR = new Creator<Person>() {

        @Override
        public Person createFromParcel(Parcel source) {
            return new Person(source);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }

    };

    @Override
    public String toString() {
        return "Person [mName=" + mName + ", mAge=" + mAge + "]";
    }

}
