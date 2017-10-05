package com.zolipe.communitycensus.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SupervisorObj implements Parcelable{
    private String id;
    private String first_name;
    private String last_name;
    private String gender;
    private String age;
    private String phone_number;
    private String aadhaar;
    private String email;
    private String address;
    private String image_url;
    private String zipcode;
    private String member_count;
    private String dob;
    private String isSynced;

    public SupervisorObj (String id, String first_name, String last_name,
                          String phone, String aadhar, String email,
                          String address, String age, String gender,
                          String url, String zipcode, String member_count,
                          String dob, String isSynced){
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.gender = gender;
        this.age = age;
        this.phone_number = phone;
        this.aadhaar = aadhar;
        this.email = email;
        this.address = address;
        this.image_url = url;
        this.zipcode = zipcode;
        this.member_count = member_count;
        this.dob = dob;

        this.isSynced = isSynced;
    }

    protected SupervisorObj(Parcel in) {
        id = in.readString();
        first_name = in.readString();
        last_name = in.readString();
        gender = in.readString();
        age = in.readString();
        phone_number = in.readString();
        aadhaar = in.readString();
        email = in.readString();
        address = in.readString();
        image_url = in.readString();
        zipcode = in.readString();
        member_count = in.readString();
        dob = in.readString();
        isSynced = in.readString();
    }

    public static final Creator<SupervisorObj> CREATOR = new Creator<SupervisorObj>() {
        @Override
        public SupervisorObj createFromParcel(Parcel in) {
            return new SupervisorObj(in);
        }

        @Override
        public SupervisorObj[] newArray(int size) {
            return new SupervisorObj[size];
        }
    };

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAadhaar() {
        return aadhaar;
    }

    public void setAadhaar(String aadhaar) {
        this.aadhaar = aadhaar;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getMember_count() {
        return member_count;
    }

    public void setMember_count(String member_count) {
        this.member_count = member_count;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(String isSynced) {
        this.isSynced = isSynced;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(first_name);
        dest.writeString(last_name);
        dest.writeString(gender);
        dest.writeString(age);
        dest.writeString(phone_number);
        dest.writeString(aadhaar);
        dest.writeString(email);
        dest.writeString(address);
        dest.writeString(image_url);
        dest.writeString(zipcode);
        dest.writeString(member_count);
        dest.writeString(dob);
        dest.writeString(isSynced);
    }
}
