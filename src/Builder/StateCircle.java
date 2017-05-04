package Builder;

import javafx.scene.control.Label;
import javafx.scene.shape.Circle;

import java.util.HashMap;

public class StateCircle extends Circle{
    private char circleName;
    public int circleIndex=0;
    public boolean isFinal=false;
    public StateCircle finalCircle;

    //Contains Alphabet and the State that the alphabet Leads to
    HashMap<Character,Character> Table=new HashMap<>();

    //Contains Next State Name and A Unique Path to that State
    HashMap<Character,CustomCurve>PathMap=new HashMap<>();

    Label nameLabel;

    public char getCircleName(){return circleName;}
    public void setCircleName(char name){circleName=name;}

    StateCircle(double x, double y,double r) {
        super(x,y,r);
        nameLabel=new Label("");

    }
    StateCircle() {
        super();
        nameLabel=new Label("");
    }

    public void showLabel(){
        nameLabel.setText(String.valueOf(circleName));
        nameLabel.layoutXProperty().bind(this.centerXProperty());
        nameLabel.layoutYProperty().bind(this.centerYProperty());
        nameLabel.setVisible(true);
    }

    public void hideLabel(){
        nameLabel.setVisible(false);
    }
}
