package model.tidal;

import com.google.gson.annotations.SerializedName;

public record Me(
        @SerializedName("id")
        String id,

        @SerializedName("type")
        String type,

        @SerializedName("attributes")
        Attributes attributes

) {
    public record Attributes(
            @SerializedName("username")
            String username
    ) {
    }
}

