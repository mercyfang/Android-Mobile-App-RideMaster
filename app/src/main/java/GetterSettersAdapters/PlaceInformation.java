package GetterSettersAdapters;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;



/**
 * Created by shreyasingh on 4/28/18.
 */

public class PlaceInformation {

    private String name;
    private String address;
    private String phoneNumber;
    private String id;
    private Uri website;
    private LatLng latlng;
    private float rating;
    private String attribution;


    public PlaceInformation(String name, String address, String phoneNumber, String id, Uri website, LatLng latlng, float rating, String attribution) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.id = id;
        this.website = website;
        this.latlng = latlng;
        this.rating = rating;
        this.attribution = attribution;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Uri getWebsite() {
        return website;
    }

    public void setWebsite(Uri website) {
        this.website = website;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public PlaceInformation() {


    }
}
