package Builder;

import javafx.scene.control.Label;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.text.Font;

/**
 * Creates a path based on the source and destination states
 */
public class CustomCurve extends Path{
    Label nameLabel;
    QuadCurveTo quadCurveTo;

    CustomCurve(StateCircle circle1,StateCircle circle2,String title){
        super();
        MoveTo moveTo=new MoveTo();
        moveTo.xProperty().bind(circle1.centerXProperty());
        moveTo.yProperty().bind(circle1.centerYProperty());

        quadCurveTo=new QuadCurveTo();
        quadCurveTo.xProperty().bind(circle2.centerXProperty());
        quadCurveTo.yProperty().bind(circle2.centerYProperty());

        quadCurveTo.setControlX((circle1.getCenterX()+circle2.getCenterX())/2);
        quadCurveTo.setControlY((circle1.getCenterY()+circle2.getCenterY())/2);

        nameLabel=new Label(circle1.getCircleName()+">"+circle2.getCircleName()+":"+title);
        nameLabel.setFont(new Font("seoge",18));
        nameLabel.layoutXProperty().bindBidirectional(quadCurveTo.controlXProperty());
        nameLabel.layoutYProperty().bindBidirectional(quadCurveTo.controlYProperty());
        nameLabel.setOnMouseDragged(event -> {
            nameLabel.setLayoutX(event.getSceneX());
            nameLabel.setLayoutY(event.getSceneY());
        });

        nameLabel.setOnMouseClicked(event -> {
            if(DFABuilder.currentMode==Status.REMOVETRANSIT) {
                this.getElements().clear();
                this.nameLabel.setVisible(false);
            }
        });

        //path=new Path();
        this.getElements().addAll(moveTo,quadCurveTo);
    }

    CustomCurve(StateCircle circle,String title) {
        super();
        MoveTo moveTo=new MoveTo();
        moveTo.xProperty().bind(circle.centerXProperty());
        moveTo.yProperty().bind(circle.centerYProperty());

        CubicCurveTo cubicCurveTo=new CubicCurveTo();
        cubicCurveTo.xProperty().bind(circle.centerXProperty());
        cubicCurveTo.yProperty().bind(circle.centerYProperty());

        nameLabel=new Label(circle.getCircleName()+">"+circle.getCircleName()+":"+title);
        nameLabel.setFont(new Font("seoge",18));
        nameLabel.layoutXProperty().bindBidirectional(cubicCurveTo.controlX1Property());
        nameLabel.layoutYProperty().bindBidirectional(cubicCurveTo.controlY1Property());

        cubicCurveTo.setControlX1(circle.getCenterX()-40);
        cubicCurveTo.setControlY1(circle.getCenterY()-100);
        cubicCurveTo.setControlX2(circle.getCenterX()+100);
        cubicCurveTo.setControlY2(circle.getCenterY()-80);

        cubicCurveTo.xProperty().addListener((observable, oldValue, newValue) -> {
            cubicCurveTo.setControlX1(circle.getCenterX()-40);
            cubicCurveTo.setControlY1(circle.getCenterY()-100);
            cubicCurveTo.setControlX2(circle.getCenterX()+100);
            cubicCurveTo.setControlY2(circle.getCenterY()-80);
        });


        nameLabel.setOnMouseDragged(event -> {
            nameLabel.setLayoutX(event.getSceneX());
            nameLabel.setLayoutY(event.getSceneY());
        });

        nameLabel.setOnMouseClicked(event -> {
            if(DFABuilder.currentMode==Status.REMOVETRANSIT) {
                this.getElements().clear();
                this.nameLabel.setVisible(false);
            }
        });
        this.getElements().addAll(moveTo,cubicCurveTo);
    }

    Path getPath(){return this;}
}
