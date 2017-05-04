package Builder;

/**
 * @author Arif Hosan
 **/

import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.*;

public class DFABuilder extends Application {
    ArrayList<StateCircle>statesList;
    TreeSet<Character>alphabetSet;
    String simulateString;

    static Status currentMode=Status.DEFAULT;

    StateCircle firstCircle;
    Font seoge16;
    Button playSimButton,finishButton,formalDefButton;
    FlowPane flowPane,flowPane2;
    Group root;
    AnchorPane anchorPane;
    Scene scene;

    public void start(Stage stage){
        stage.setWidth(800); stage.setHeight(700);
        mainMenu(stage);
        stage.setTitle("DFA Builder");
        stage.centerOnScreen();
        stage.setResizable(false);
        stage.setMaximized(false);
        stage.show();
    }

    /**
     * Creates a new Scene and displays the Formal Definition of the DFA.
     * The DFA must be finished for this method
     * **/
    private void showDef(Stage stage) {
        VBox vBox=new VBox();
        vBox.setPadding(new Insets(30,30,30,30));
        vBox.setAlignment(Pos.CENTER);

        //Getting the list of all states
        String Q="{"+statesList.get(0).getCircleName();
        for(int i=1;i<statesList.size();i++) {
            Q+=","+statesList.get(i).getCircleName();
        }
        Q+="}";

        //Getting list of All Alphabets
        String E="{"+alphabetSet.first();
        for(Character c:alphabetSet) {
            if(c==alphabetSet.first()) continue;
            E+=","+c;
        }
        E+="}";

        //Getting Final States
        String F="{";
        for(StateCircle sc:statesList) {
            if(sc.isFinal) F+=sc.getCircleName()+",";
        }
        F+="}";

        //Creating Table to show the Transition Functions
        TableView tableView=new TableView();
        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(300);

        //"\u03B4" represents Small Delta
        TableColumn tt=new TableColumn<>("\u03B4");
        tt.setCellValueFactory(new MapValueFactory('D'));
        tableView.getColumns().addAll(tt);

        for(Character a:alphabetSet) {
            TableColumn tc=new TableColumn<>(a.toString());
            tc.setCellValueFactory(new MapValueFactory(a));
            tableView.getColumns().addAll(tc);
        }

        ObservableList<Map> dataList= FXCollections.observableArrayList();
        for(StateCircle s:statesList) {
            Map<Character,Character> map=new HashMap<>();
            for(Character ch:s.Table.keySet()) {
                map.put('D',s.getCircleName());
                map.put(ch,s.Table.get(ch));
            }
            dataList.add(map);
        }
        tableView.setItems(dataList);

        Label detailsLabel1=new Label("Q = "+Q+"\n"+"\u03A3 = "+E);
        Label detailsLabel2=new Label("Q0: "+String.valueOf(statesList.get(0).getCircleName())+"\nF = "+F);
        Label backLabel=new Label("Go Back");

        detailsLabel1.setFont(new Font("seoge",22));
        detailsLabel2.setFont(new Font("seoge",22));
        backLabel.setFont(new Font("seoge",22));

        backLabel.setOnMouseClicked(event -> {
            stage.setScene(scene);
        });

        vBox.getChildren().addAll(detailsLabel1,tableView,detailsLabel2,backLabel);
        vBox.setSpacing(30);
        Scene definitionScene=new Scene(vBox,stage.getWidth(),stage.getHeight());
        stage.setScene(definitionScene);
    }

    /**
     * Plays Simulation of the current String returned by getString() method
     *  DFA must be Complete for this method
     *  **/
    private void playSimulation(Button nextStateButton) {
        nextStateButton.setText("Next State");
        //Defining i and curCircle as a final One Dimensional Array of Length One.
        //As using local variable(non final) in inner class is not allowed in java.
        final int[] i = {-1};
        final StateCircle[] curCircle = {statesList.get(0)};

        Label textLabel=new Label();
        FlowPane fp=new FlowPane();
        fp.setLayoutX(20);
        fp.setLayoutY(20);
        Label simLabels[]=new Label[simulateString.length()];
        textLabel.setVisible(false);

        //Create Labels for each of the Character of the input String
        for(int j=0;j<simulateString.length();j++) {
            simLabels[j]=new Label(String.valueOf(simulateString.charAt(j)));
            simLabels[j].setFont(new Font("Comic Sans MS",28));
            fp.getChildren().add(simLabels[j]);
        }

        textLabel.setTextFill(Color.RED);
        textLabel.setBackground(new Background(new BackgroundFill(Color.WHITE,CornerRadii.EMPTY,Insets.EMPTY)));
        textLabel.setBorder(new Border(new BorderStroke(
                Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,BorderWidths.DEFAULT)));

        textLabel.setFont(new Font("Comic Sans MS",26));
        textLabel.setAlignment(Pos.CENTER);
        textLabel.setMinSize(25,25);
        textLabel.toFront();

        //Press Next State to Continue Simulation
        nextStateButton.setOnAction(event -> {
            if(!textLabel.isVisible()) textLabel.setVisible(true);
            if(i[0]+1<simulateString.length()) i[0]++;
            else {
                Alert a=new Alert(Alert.AlertType.INFORMATION,"",ButtonType.YES,ButtonType.NO);
                if(curCircle[0].isFinal) a.setContentText("The String is Acepted!!\n"+
                                        "Do You want To Try another String?");
                else a.setContentText("The String is Rejected!!\n"+
                        "Do You want To Try another String?");
                a.showAndWait();
                if(a.getResult()==ButtonType.YES) {

                    //Getting another string to simulate in the DFA
                    root.getChildren().removeAll(fp,textLabel);
                    getString();
                    playSimulation(nextStateButton);
                    return;
                }
                else {
                    //Returning to default Status
                    root.getChildren().removeAll(fp,textLabel);
                    nextStateButton.setText("Play Simulation");
                    //anchorPane.getChildren().addAll(flowPane);
                    flowPane.setVisible(true);
                    currentMode=Status.DEFAULT;
                    finishButton.setDisable(false);
                    playSimButton.setDisable(true);
                    formalDefButton.setDisable(true);
                    return;
                }
            }

            if(i[0]>0)simLabels[i[0]-1].setTextFill(Color.BLACK);
            simLabels[i[0]].setTextFill(Color.RED);
            textLabel.setText(String.valueOf(simulateString.charAt(i[0])));

            //Sending the Label and the path the Current Character of the Input String leads to
            pathTransit(textLabel, curCircle[0].PathMap.get(curCircle[0].Table.get(simulateString.charAt(i[0]))));

            //Getting the next Circle Name
            Character c= curCircle[0].Table.get(simulateString.charAt(i[0]));

            //Finding next Circle by Circle Name and Storing it in curCircle[0]
            for(StateCircle sc:statesList){
                if(sc.getCircleName()==c) {
                    curCircle[0] =sc;
                }
            }
        });

        //flowPane.getChildren().addAll();
        root.getChildren().addAll(textLabel,fp);
    }

    /**
     * Gets the String that is to be Simulated by the DFA and stores in the Global variable simulateString
     * The DFA must be Completed
     */
    private boolean getString() {
        TextInputDialog t=new TextInputDialog();
        t.setTitle("String for the Transition");
        t.setHeaderText(null);

        Optional<String> res=t.showAndWait();
        if(res.isPresent()) {
            if(res.get().isEmpty()) return getString();
            else {
                boolean flag=true;
                String s=res.get();
                for(int i=0;i<s.length();i++) {
                    if(!alphabetSet.contains(s.charAt(i))) {
                        flag=false;
                        break;
                    }
                }
                if(flag) {
                    simulateString=s;
                    return true;
                }
                else {
                    Alert b=new Alert(Alert.AlertType.ERROR,"Invalid Alphabet in the String", ButtonType.CLOSE);
                    b.setHeaderText(null);
                    b.showAndWait();
                    return getString();
                }
            }
        }
        else {
            //If Cancel was Pressed
            currentMode=Status.FINISHED;
            return false;
        }
    }

    /**
     * @return whether if DFA is Complete or not
     */
    private boolean isFinished() {
        int SZ=alphabetSet.size();
        if(SZ==0) return false;
        for(StateCircle sc:statesList) {
            if(sc.Table.size()!=SZ) return false;
        }
        return true;
    }

    /**
     * Removes a transition by clicking on the label of the transition
     * @param event MouseEvent
     */
    private void removeTransit(MouseEvent event) {
        boolean isDone=false;
        //Iterating through all the paths to find a path which is empty
        for(StateCircle sc:statesList) {
            for(CustomCurve cv:sc.PathMap.values()) {
                if(cv.getElements().isEmpty()) {
                        isDone=true;
                       for(Character cx:sc.PathMap.keySet()) {
                           if(sc.PathMap.get(cx)==cv) {
                               for(Character cc:sc.Table.keySet()) {
                                   if(sc.Table.get(cc)==cx) sc.Table.remove(cc);
                               }
                               sc.PathMap.remove(cx);
                           }
                       }
                }
            }
        }
        if(isDone) {
            currentMode=Status.DEFAULT;
        }
    }

    /**
     * Changes Cursor to a Circle for addState method
     */
    private void changeCursor() {
        Circle circle=new Circle(40,null);
        circle.setStroke(Color.BLACK);
        SnapshotParameters sp=new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        Image image=circle.snapshot(sp,null);
        scene.setCursor(new ImageCursor(image,512,512));
    }

    /**
     * Adds new State on the scene
     * @param event
     */
    private void addNewState(MouseEvent event) {
        if(event.getSceneX()<40 || event.getSceneY()>scene.getWidth()-140
           || event.getSceneY()<140 || event.getSceneY()>scene.getHeight()-140){
                currentMode=Status.DEFAULT;
                scene.setCursor(Cursor.DEFAULT);
                return;
        }

        StateCircle circle=new StateCircle(event.getSceneX()-40,event.getSceneY()-40,40);
        circle.setStroke(Color.BLACK);
        circle.setFill(Color.WHITE);

        if(statesList.isEmpty())
            circle.circleIndex=0;
        else
            circle.circleIndex=statesList.get(statesList.size()-1).circleIndex+1;

        circle.setCircleName((char)(circle.circleIndex+65));
        circle.showLabel();

        circle.setOnMouseDragged(event1 -> {
            if(currentMode!=Status.FINISHED) {
                circle.setCenterX(event1.getSceneX());
                circle.setCenterY(event1.getSceneY());
            }
        });

        circle.setOnMouseClicked(event1 -> {
            if(currentMode==Status.FIRST || currentMode==Status.SECOND) addNewTransit(circle);
            if(currentMode==Status.REMOVESTATE) removeState(circle);
            if(currentMode==Status.LOOP) createNewLoop(circle);
            if(currentMode==Status.MAKEFINAL) {
                circle.finalCircle=new StateCircle(circle.getCenterX(),circle.getCenterY(),30);
                circle.finalCircle.centerXProperty().bindBidirectional(circle.centerXProperty());
                circle.finalCircle.centerYProperty().bindBidirectional(circle.centerYProperty());
                circle.finalCircle.setStroke(Color.BLACK);
                circle.finalCircle.setFill(Color.WHITE);
                circle.isFinal=true;

                circle.finalCircle.setOnMouseDragged(event2 -> {
                    circle.finalCircle.setCenterX(event2.getSceneX());
                    circle.finalCircle.setCenterY(event2.getSceneY());
                });

                root.getChildren().addAll(circle.finalCircle);
                circle.nameLabel.toFront();

                currentMode=Status.DEFAULT;
            }
        });

        currentMode=Status.DEFAULT;
        scene.setCursor(Cursor.DEFAULT);
        statesList.add(circle);

        root.getChildren().addAll(circle,circle.nameLabel);
    }

    /**
     * Adds a self Transition to a state
     * @param circle
     */
    private void createNewLoop(StateCircle circle) {
        //Finding if loop already exists or not
        if(circle.PathMap.containsKey(circle.getCircleName())) {
            currentMode=Status.DEFAULT;
            scene.setCursor(Cursor.DEFAULT);
            return;
        }

        String trasitString=askAlphabet();
        if(trasitString.isEmpty()) return;

        //Generating the alphabets from user input
        //Storing alphabets in alphabetSet and in the Table of the firstCircle
        String[] sb=trasitString.split(",");
        boolean flag=false;
        for(String s:sb) {
            if(circle.Table.containsKey(s.charAt(0))) {
                flag=true;
            }
        }

        if(flag) {
            Alert b=new Alert(Alert.AlertType.ERROR,"One or More of the Transitions are already defined!"+
                    "\nRemove them First!", ButtonType.CLOSE);
            b.setHeaderText(null);
            b.showAndWait();
            currentMode=Status.DEFAULT;
            scene.setCursor(Cursor.DEFAULT);
            return;
        }

        else {
            for(String s:sb) {
                circle.Table.put(s.charAt(0), circle.getCircleName());
                alphabetSet.add(s.charAt(0));
            }
        }

        CustomCurve p;

        p=new CustomCurve(circle, trasitString);

        circle.PathMap.put(circle.getCircleName(), p);
        root.getChildren().addAll(p,p.nameLabel);
        p.toBack();

        scene.setCursor(Cursor.DEFAULT);
        currentMode=Status.DEFAULT;



    }

    /**
     * Removes a state from the scene
     * @param circle
     */
    private void removeState(StateCircle circle) {
        currentMode=Status.DEFAULT;
        scene.setCursor(Cursor.DEFAULT);

        root.getChildren().removeAll(circle,circle.nameLabel);
        statesList.remove(circle);

        //Removing all the transitions associated with the state
        for(Character c:circle.PathMap.keySet())
            root.getChildren().removeAll(circle.PathMap.get(c),circle.PathMap.get(c).nameLabel);
        for(StateCircle sc:statesList) {
            if(sc.PathMap.containsKey(circle.getCircleName())) {
                root.getChildren().removeAll(sc.PathMap.get(circle.getCircleName()),sc.PathMap.get(circle.getCircleName()).nameLabel);
                sc.PathMap.remove(circle.getCircleName());
            }
            for(Character c:sc.Table.values()) {
                if(c.equals(circle.getCircleName()))
                    sc.Table.remove(c,sc.Table.get(c));
            }
        }
        if(circle.isFinal) root.getChildren().removeAll(circle.finalCircle);
    }

    /**
     * Adds a new Transtion to a state
     * @param circle
     */
    private void addNewTransit(StateCircle circle) {
        if(currentMode==Status.FIRST) {
            firstCircle=circle;
            currentMode=Status.SECOND;
            scene.setCursor(Cursor.CROSSHAIR);
        }

        else if(currentMode==Status.SECOND) {
            if(firstCircle==circle) return;
            if(firstCircle.PathMap.containsKey(circle.getCircleName())) {
                currentMode=Status.DEFAULT;
                scene.setCursor(Cursor.DEFAULT);
                return;
            }

            String trasitString=askAlphabet();
            if(trasitString.isEmpty()) return;

            //Generating the alphabets from user input
            //Storing alphabets in alphabetSet and in the Table of the firstCircle
            String[] sb=trasitString.split(",");
            boolean flag=false;
            for(String s:sb) {
                if(firstCircle.Table.containsKey(s.charAt(0))) {
                    flag=true;
                }
            }
            if(flag) {
                Alert a=new Alert(Alert.AlertType.ERROR,"One or More of the Transitions are already defined!"+
                        "\nRemove them First!", ButtonType.CLOSE);
                a.setHeaderText(null);
                a.showAndWait();
                currentMode=Status.DEFAULT;
                scene.setCursor(Cursor.DEFAULT);
                return;
            }
            else {
                for(String s:sb) {
                    firstCircle.Table.put(s.charAt(0), circle.getCircleName());
                    alphabetSet.add(s.charAt(0));
                }
            }

            CustomCurve p;

            //if(circle.PathMap.containsKey(firstCircle.getCircleName())) {
                p=new CustomCurve(firstCircle, circle, trasitString);
                //p=c.getPath();
            //}
            /*else {
                CustomLine c = new CustomLine(firstCircle, circle);
                p=c.getPath();
                l=c.nameLabel;
            }*/

            firstCircle.PathMap.put(circle.getCircleName(), p);
            root.getChildren().addAll(p,p.nameLabel);
            p.toBack();

            scene.setCursor(Cursor.DEFAULT);
            currentMode=Status.DEFAULT;
        }
    }

    /**
     * Asks the alphabet(s) after a Transition is created
     * @return String alphabet(s)
     */
    private String askAlphabet() {
        TextInputDialog t=new TextInputDialog();
        t.setTitle("Alphabet for the Transition");
        t.setHeaderText(null);

        Optional<String> res=t.showAndWait();
        if(res.isPresent()) {
            if(res.get().isEmpty()) return askAlphabet();
            else return res.get();
        }
        else return "";
    }

    /**
     * Creates and plays the transition along the path p using Node Label l
     * @param l the TextLabel
     * @param p the Path
     */
    private void pathTransit(Label l,Path p) {
        PathTransition pathTransition=new PathTransition();
        pathTransition.setDuration(Duration.seconds(4));
        pathTransition.setDelay(Duration.seconds(0.5));
        pathTransition.setOrientation(PathTransition.OrientationType.NONE);
        pathTransition.setPath(p);
        pathTransition.setNode(l);
        pathTransition.play();
    }

    /**
     * Takes a snapshot of the current screen, and saves as a PNG format image
     * @param stage
     */
    private void takeSnap(Stage stage) {
        WritableImage image=root.snapshot(new SnapshotParameters(),null);
        try{
            //ImageIO.write(SwingFXUtils.fromFXImage(image,null),"png",file);
            FileChooser fileChooser=new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files(*.png)","*.png"));
            fileChooser.setInitialFileName("Snap.PNG");
            File saveFile=fileChooser.showSaveDialog(stage);
            if(saveFile!=null) {
                ImageIO.write(SwingFXUtils.fromFXImage(image,null),"png",saveFile);
            }

        }catch (Exception e) {}
    }

    /**
     * Launches the Main Menu
     * @param stage Stage
     */
    private void mainMenu(Stage stage){
        BorderPane root=new BorderPane();
        root.setPadding(new Insets(100,20,20,20));
        VBox vBox=new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);

        Font seoge20=new Font("seoge",20);

        Label titleLabel=new Label("Deterministic Finite Automata Simulator");
        Button dfaButton=new Button("Create New");
        Button exitButton=new Button("Exit");

        dfaButton.setMinWidth(130);
        exitButton.setMinWidth(130);


        dfaButton.setFont(seoge20);
        titleLabel.setFont(new Font("Comic Sans MS",35));
        exitButton.setFont(seoge20);

        dfaButton.setTooltip(new Tooltip("Build and Simulate your DFA"));

        dfaButton.setOnAction(event -> init(stage));
        exitButton.setOnAction(event -> Platform.exit());

        vBox.getChildren().addAll(dfaButton,exitButton);
        BorderPane.setAlignment(vBox,Pos.CENTER);
        BorderPane.setAlignment(titleLabel,Pos.CENTER);

        root.setTop(titleLabel);
        root.setCenter(vBox);

        Scene menuScene=new Scene(root,800,800);

        stage.setScene(menuScene);
    }

    private void init(Stage stage) {
        MenuBar menuBar=new MenuBar();
        root=new Group();
        flowPane=new FlowPane();
        flowPane2=new FlowPane();
        anchorPane=new AnchorPane(flowPane,flowPane2,root,menuBar);
        scene=new Scene(anchorPane,stage.getWidth(),stage.getHeight());

        seoge16=new Font("seoge",14);

        flowPane.setMinSize(stage.getWidth()-50,100);
        flowPane2.setMinSize(stage.getWidth()-50,100);
        flowPane.setAlignment(Pos.CENTER);
        flowPane2.setAlignment(Pos.CENTER);
        flowPane.setHgap(20);  flowPane.setVgap(20);
        flowPane2.setHgap(20); flowPane2.setVgap(20);
        flowPane.setPadding(new Insets(10,20,10,20));
        flowPane2.setPadding(new Insets(10,20,10,20));
        flowPane.setBorder(new Border(new BorderStroke(
                Color.BLACK, BorderStrokeStyle.DASHED, CornerRadii.EMPTY,BorderWidths.DEFAULT)));
        flowPane2.setBorder(new Border(new BorderStroke(
                Color.BLACK, BorderStrokeStyle.DASHED, CornerRadii.EMPTY,BorderWidths.DEFAULT)));

        statesList=new ArrayList<>();
        alphabetSet=new TreeSet<>();

        menuBar.prefWidthProperty().bind(scene.widthProperty());
        Menu fileMenu=new Menu("File");
        MenuItem newMenuItem = new MenuItem("New");
        MenuItem saveMenuItem = new MenuItem("Save");
        MenuItem exitMenuItem = new MenuItem("Exit");

        fileMenu.getItems().addAll(newMenuItem,saveMenuItem,exitMenuItem);
        menuBar.getMenus().addAll(fileMenu);

        exitMenuItem.setOnAction(event -> Platform.exit());
        saveMenuItem.setOnAction(event -> takeSnap(stage));
        newMenuItem.setOnAction(event -> init(stage));

        Button addStateButton=new Button("Add a State");
        Button addTransitButton=new Button("Add a Transition");
        Button addLoopButton=new Button("Add a self Transition");
        Button removeStateButton=new Button("Remove a State");
        Button removeTransitButton=new Button("Remove a Transition");
        Button makeFinalButton=new Button("Make Final State");
        formalDefButton=new Button("Show Formal Definition");

        finishButton=new Button("Finish");
        playSimButton=new Button("Play Simulation");
        playSimButton.setDisable(true);
        formalDefButton.setDisable(true);

        addStateButton.setFont(seoge16);
        addTransitButton.setFont(seoge16);
        addLoopButton.setFont(seoge16);
        removeStateButton.setFont(seoge16);
        removeTransitButton.setFont(seoge16);
        makeFinalButton.setFont(seoge16);
        formalDefButton.setFont(seoge16);
        finishButton.setFont(seoge16);
        playSimButton.setFont(seoge16);

        scene.setOnMouseClicked(event -> {
            //Right Click to Cancel current event
            if(event.getButton()== MouseButton.SECONDARY&& currentMode!=Status.FINISHED) {
                scene.setCursor(Cursor.DEFAULT);
                currentMode=Status.DEFAULT;
            }

            if(currentMode==Status.ADDSTATE) addNewState(event);
            if(currentMode==Status.REMOVETRANSIT) {
                removeTransit(event);
            }
        });
        addStateButton.setOnAction(event -> {
            currentMode=Status.ADDSTATE;
            changeCursor();
        });
        addTransitButton.setOnAction(event -> {
            currentMode=Status.FIRST;
            scene.setCursor(Cursor.DEFAULT);
        });
        addLoopButton.setOnAction(event -> {
            currentMode=Status.LOOP;
            scene.setCursor(Cursor.DEFAULT);
        });
        removeStateButton.setOnAction(event -> {
            scene.setCursor(Cursor.CLOSED_HAND);
            currentMode=Status.REMOVESTATE;
        });
        removeTransitButton.setOnAction(event -> {
            scene.setCursor(Cursor.DEFAULT);
            currentMode=Status.REMOVETRANSIT;
        });
        makeFinalButton.setOnAction(event -> {
            currentMode=Status.MAKEFINAL;
            scene.setCursor(Cursor.DEFAULT);
        });
        finishButton.setOnAction(event -> {
            if(isFinished()){
                flowPane.setVisible(false);
                //anchorPane.getChildren().removeAll(flowPane);
                playSimButton.setDisable(false);
                finishButton.setDisable(true);
                formalDefButton.setDisable(false);
                currentMode=Status.FINISHED;
            }
            else {
                Alert a=new Alert(Alert.AlertType.ERROR,"Not all Transitions are defined!",ButtonType.CLOSE);
                a.showAndWait();
                //currentMode=Status.DEFAULT;
            }
        });
        playSimButton.setOnAction(event -> {
            if(currentMode==Status.FINISHED)
                if(getString())
                    playSimulation(playSimButton);
        });
        formalDefButton.setOnAction(event -> {
            if(currentMode==Status.FINISHED) {
                showDef(stage);
            }
        });

        flowPane.getChildren().addAll(addStateButton,addTransitButton,addLoopButton,
                removeStateButton,removeTransitButton,makeFinalButton);
        flowPane2.getChildren().addAll(formalDefButton,finishButton,playSimButton);

        AnchorPane.setTopAnchor(menuBar,0.0);
        AnchorPane.setTopAnchor(flowPane,30.0);
        AnchorPane.setLeftAnchor(flowPane,20.0);
        AnchorPane.setBottomAnchor(flowPane2,0.0);
        AnchorPane.setLeftAnchor(flowPane2,20.0);

        stage.setScene(scene);
    }

    public static void main(String[] args) {
		launch(args);}
}
