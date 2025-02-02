package com.shomazzapp.vavilonWalls.Requests;

import com.shomazzapp.vavilonWalls.Utils.Constants;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;

import org.json.JSONObject;

import java.util.ArrayList;

public class PhotosRequest {

    private ArrayList<VKApiPhoto> photos;

    private int albumSize;
    private int offset;
    private int album_id;
    private int count;

    private final String log = "PhotoRequest";

    public PhotosRequest(int album_id, int offset, int count) {
        this.offset = offset;
        this.album_id = album_id;
        this.count = count;
        this.photos = new ArrayList<>();
        loadPhotos();
    }

    public PhotosRequest(int album_id, int offset) {
        this.offset = offset;
        this.album_id = album_id;
        this.count = -1; //load full album
        this.photos = new ArrayList<>();
        loadPhotos();
    }

    public void loadPhotos() {
        VKRequest request = new VKRequest("photos.get", VKParameters.from(
                VKApiConst.OWNER_ID, Constants.COMMUNITY_ID,
                VKApiConst.ALBUM_ID, album_id,
                "extended", 1,
                "rev", 1,
                VKApiConst.OFFSET, offset));
        if (count != -1) request.addExtraParameter(VKApiConst.COUNT, count);
        request.executeSyncWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    //Log.d(log, response.json.toString());
                    albumSize = response.json.getJSONObject("response").getInt("count");
                    if (count == -1) count = albumSize;
                    for (int i = 0; i < count; i++)
                        photos.add(new VKApiPhoto((JSONObject) response.json.getJSONObject("response")
                                .getJSONArray("items").get(i)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                System.out.println(error);
            }
        });
    }

    public ArrayList<VKApiPhoto> getPhotos() {
        return photos;
    }

    public int getAlbumSize() {
        return albumSize;
    }
}
