package pages.components;

import content.Content;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CoverImage extends ImageView {

    private Content MovieInfo;

    public CoverImage(Image img, Content result) {
        super(img);
        MovieInfo = result;
    }

    public Content getInfo() {return MovieInfo;}

    public Runnable fetchImage = () -> {
        Image image = MovieInfo.getImage();
        Platform.runLater(() -> this.setImage(image));
    };

}
