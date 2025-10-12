package service;

import dao.IsrcTidalIdDAO;
import http.ApiException;
import model.IsrcTidalId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TidalService {
    private static final Logger log = LoggerFactory.getLogger(TidalService.class);
    private final TidalApiService apiService;
    private final IsrcTidalIdDAO isrcTidalIdDAO;

    public TidalService(
            TidalApiService apiService,
            IsrcTidalIdDAO isrcTidalIdDAO
    ) {
        this.apiService = apiService;
        this.isrcTidalIdDAO = isrcTidalIdDAO;

        isrcTidalIdDAO.createTable();
    }

    public void addIsrcToIsrcTidalIds(List<String> isrcs) {
        isrcTidalIdDAO.addAllIsrc(isrcs);
    }

    public void updateIsrcTidalIds() {
        var updateNeeded = isrcTidalIdDAO.findUpdateNeeded(30);
        log.info("updateNeeded: {}", updateNeeded.size());

        updateNeeded.forEach(this::handleUpdateNeeded);

    }

    private void handleUpdateNeeded(IsrcTidalId t) {
        try {
            var id = apiService.getTrackIdByIsrc(t.isrc().toUpperCase());
            id.ifPresentOrElse(
                    i -> {
                            isrcTidalIdDAO.update(t.setTidalId(i));
                            log.info("Updated id for {}: {}", t.isrc(), i);
                        } ,
                    () -> {
                            isrcTidalIdDAO.update(t.setFailed());
                            log.warn("Failed to update id for {}", t.isrc());
                    });
        } catch (ApiException e) {
            if (e.getStatusCode() == 429) {
                log.warn("Rate limit exceeded! Aborting");
                throw e;
            }
            log.error("Failed to update {}", t, e);
        }
    }


}
