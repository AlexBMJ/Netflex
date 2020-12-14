package database;

import com.algolia.search.DefaultSearchClient;
import com.algolia.search.SearchClient;
import com.algolia.search.SearchIndex;
import com.algolia.search.models.indexing.Query;
import content.TVDBResult;

import java.util.Arrays;
import java.util.List;

public class AlgoliaAPI {
    private static final SearchClient client = DefaultSearchClient.create("tvshowtime", "c9d5ec1316cec12f093754c69dd879d3");
    private static final SearchIndex<TVDBResult> index = client.initIndex("TVDB", TVDBResult.class);

    public static List<TVDBResult> getSeriesHits(String seriesName, String type, int hitsPerPage){
        return index.search(new Query(seriesName).setFacets(Arrays.asList("type")).setFacetFilters(Arrays.asList(Arrays.asList("type:"+type))).setHitsPerPage(hitsPerPage)).getHits();
    }
}
