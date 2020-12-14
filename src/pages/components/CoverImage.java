package pages.components;

import content.TVDBResult;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CoverImage extends ImageView {

    private TVDBResult MovieInfo;

    public CoverImage(Image img, TVDBResult result) {
        super(img);
        MovieInfo = result;
    }

    public TVDBResult getInfo() {return MovieInfo;}

    public Runnable fetchImage = () -> {
        Image img = new Image(MovieInfo.getImage());
        Platform.runLater(() -> this.setImage(img));
    };

}
