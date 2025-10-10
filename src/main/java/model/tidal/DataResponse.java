package model.tidal;

import com.google.gson.annotations.SerializedName;

public record DataResponse<T>(
        @SerializedName("data")
        T data,

        @SerializedName("links")
        Links links
) {
    public record Links(
            @SerializedName("self")
            String self
    ) {}
}
