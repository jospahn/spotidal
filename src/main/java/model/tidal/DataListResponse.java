package model.tidal;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record DataListResponse<T>(
        @SerializedName("data")
        List<T> data,

        @SerializedName("links")
        Links links
) {
    public record Links(
            @SerializedName("self")
            String self
    ) {}
}
