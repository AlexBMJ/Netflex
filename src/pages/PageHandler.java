package pages;

import javafx.stage.Stage;
import java.util.LinkedList;

public class PageHandler {
    private static PageHandler pageHandler;
    private LinkedList<Page> pageCache;
    private Stage stage;
    private int cacheSize;
    private Page currentPage;


    private PageHandler(Stage stage, int cacheSize) {
        this.cacheSize = cacheSize;
        this.stage = stage;
        this.pageCache = new LinkedList();
    }

    public void addPage(Page page) {
        if (pageCache.size() >= cacheSize) {
            pageCache.remove(pageCache.size()-1);
        }
        if (this.currentPage != null)
            pageCache.add(0, this.currentPage);
        this.currentPage = page;
        stage.setScene(this.currentPage.getScene());
        stage.show();
    }

    public void prevPage() throws PageCacheException {
        if (pageCache.size() > 0) {
            this.currentPage = pageCache.pop();
            stage.setScene(this.currentPage.getScene());
            stage.show();
        } else {
            throw new PageCacheException("No more pages");
        }
    }

    public static PageHandler get() {
        return pageHandler;
    }

    public static PageHandler create(Stage stage, int cacheSize) {
        pageHandler = new PageHandler(stage, cacheSize);
        return pageHandler;
    }

}
