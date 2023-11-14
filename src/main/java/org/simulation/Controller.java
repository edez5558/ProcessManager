package org.simulation;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {
    @FXML
    private Button btnNumberSet;

    @FXML
    private Button btnProcessSet;

    @FXML
    private Label lbCurrentLote;

    @FXML
    private TextField txtID;

    @FXML
    private TextField txtLeftNumber;


    @FXML
    private TextField txtNumberProcess;

    @FXML
    private TextField txtQuantum;

    @FXML
    private TextField txtRightNumber;

    @FXML
    private TextField txtTime; 

    @FXML
    private FlowPane plProcess;

    @FXML
    private ChoiceBox<String> cbOperation; 

    @FXML
    private Button btnExecute;
    
    @FXML
    private Label lbGlobalTime;

    @FXML
    private Label lbTimeRest;

    @FXML
    private Label lbBatchRest;

    @FXML
    private Label lbRunTime;

    @FXML
    private TableColumn<Result, Integer> tcID;


    @FXML
    private TableColumn<Result, String> tcResult;

    @FXML
    private TableView<Result> tvFinished;

    @FXML
    private ProgressBar pbProcess;

    @FXML
    private TableView<Process> tvNew;

    @FXML
    private TableColumn<Process,Integer> tvNewID;

    @FXML
    private TableView<Process> tvWaiting;
    @FXML
    private TableColumn<Process,Integer> tvWaitingID;
    @FXML
    private TableColumn<Process,Double> tvWaitingTime;

    @FXML
    private Label lbProcessRemaining;
    @FXML
    private TableColumn<Process,String> tvNewStatus;

    private Manager _manager;

    private boolean _isSelectNumber;

    private ObservableList<Result> _results;
    private ObservableList<Process> _all;
    private ObservableList<Process> _news;
    private ObservableList<Process> _ready;
    private ObservableList<Process> _waiting;
    ControllerTime controllerTime = null;
    Stage tableStage = null;
    private void disableProcessInfo(){
        this.txtLeftNumber.setDisable(true);
        this.txtRightNumber.setDisable(true);
        this.txtID.setDisable(true);
        this.txtTime.setDisable(true);
        this.cbOperation.setDisable(true);
    }

    private void enableProcessInfo(){
        this.txtLeftNumber.setDisable(false);
        this.txtRightNumber.setDisable(false);
        this.txtID.setDisable(false);
        this.txtTime.setDisable(false);
        this.cbOperation.setDisable(false);
    }
    private void simpleAlertIncorrectInput(String msg){
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("Dato incorrecto");
        alert.setContentText(msg);
        alert.showAndWait();
    }
    

    private void cleanProcessInfo(){
        this.txtLeftNumber.setText("");
        this.txtRightNumber.setText("");
        this.txtID.setText("");
        this.txtID.setDisable(false);
        this.txtTime.setText("");
        this.cbOperation.setValue(null);
    }

    private void updateProcessInfo(Pane pl,Process process){
        Label lbID = (Label)pl.getChildren().get(1);
        Label lbEditable = (Label)pl.getChildren().get(3);
        Label lbStatus = (Label)pl.getChildren().get(4);
        Label lbTME = (Label)pl.getChildren().get(5);
        Label lbTR = (Label)pl.getChildren().get(6);

        //Process current = _manager.getProcessAt(index);

        if(process == null){
            lbID.setText("");
            lbStatus.setText("");

            lbEditable.setText("Sin proceso");

            lbTME.setText("00:00");
            lbTR.setText("00:00");
            return;
        }

        lbID.setText(Integer.toString(process.getID()));

        lbEditable.setText("");
        lbStatus.setText(process.getStatus());
        lbTME.setText(millisecondsToTimeString(process.getTimeMilli()));
        lbTR.setText(millisecondsToTimeString(process.getRemainingTIme()));
    }

    public void updateProcessInfoList(){
        int index = 0;
        ObservableList<Node> nodes = plProcess.getChildren();

        synchronized (RR.class){
            try{
                for(Process process : _ready){
                    updateProcessInfo((Pane)nodes.get(index),process);
                    index++;
                }

                for(Process process : _waiting){
                    updateProcessInfo((Pane)nodes.get(index),process);
                    index++;
                }
            }catch (java.lang.IndexOutOfBoundsException ex){

            }
        }

        for(;index < 3; index++){
            updateProcessInfo((Pane)nodes.get(index),null);
        }
    }

    private void fillProcessData(Process process){

        if(process == null){
            cleanProcessInfo();
            return;
        }

        
        txtID.setText(Integer.toString(process.getID()));
        txtID.setDisable(true);

        txtTime.setText(Integer.toString(process.getTime()));
        txtLeftNumber.setText(Integer.toString(process.getLeftNumber()));
        txtRightNumber.setText(Integer.toString(process.getRightNumber()));
        cbOperation.setValue(Character.toString(process.getOperation()));
    }

    private static String millisecondsToTimeString(Long milliseconds){
        return  String.format("%1$02d:%2$02d", 
                    milliseconds/1000,
                    (int)Math.round((milliseconds%1000)/1000.0 * 60)
                );
    }
    public void updateGlobalTime(Long milliseconds){
        /*
         * Javafx no permite modificar componentes de la GUI con threads externos
         */
        Platform.runLater(() -> {
            lbGlobalTime.setText(millisecondsToTimeString(milliseconds));
        });
    }

    public void updateRemainingProcess(){
        Platform.runLater(() -> {
            lbProcessRemaining.setText(Integer.toString(_news.size()));
        });
    }

    public void updateTimeRemaining(Long milliseconds){
        Platform.runLater(() -> {
            lbTimeRest.setText(millisecondsToTimeString(milliseconds));
        });
    }

    public void updateRunTime(Long milliseconds){
        Platform.runLater(() -> {
            lbRunTime.setText(millisecondsToTimeString(milliseconds));
        });
    }

    public void updateProcessInfoListThread(){
        Platform.runLater(() -> {
            updateProcessInfoList();
        });
    }

    public void refreshTableBlock(){
        Platform.runLater(() -> {
            tvWaiting.refresh();
        });
    }
    private boolean _isTableTimeShowing;
    public void refreshTableTime(){
        if(controllerTime != null && _isTableTimeShowing){
            controllerTime.refrestTable();
        }
    }


    private void createTableWindow(){
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("TableTimeProcessInterface.fxml"));
            Parent root = loader.load();

            controllerTime = loader.getController();
            controllerTime.setResults(_all);

            tableStage = new Stage();
            tableStage.setTitle("Resultados");
            tableStage.setResizable(false);
            tableStage.setScene(new Scene(root, 800,400));
            tableStage.initModality(Modality.APPLICATION_MODAL);
            tableStage.setOnHidden(event -> {
                _isTableTimeShowing = false;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
    public void showtable(){
        Platform.runLater(() -> {
            if(tableStage == null) createTableWindow();

            tableStage.show();
            controllerTime.refrestTable();

            _isTableTimeShowing = true;
        });
    }

    public void notifyEnded(){
        showtable();
    }

    public void updateBatchRemaining(int value){
        Platform.runLater(() -> {
            lbBatchRest.setText(Integer.toString(value));
        });
    }

    public void updateProcessTime(double progress){
        Platform.runLater(() -> {
            pbProcess.setProgress(progress);
        });
    }

    public void receiveResult(Result result){
        _results.add(result);
    }

    public void receiveProcessInfo(Process process){
        Platform.runLater(() -> {
            fillProcessData(process);
        });
    }

    private EventHandler<KeyEvent> createKeyPressHandler(){
        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(_isSelectNumber) return;

                switch (event.getCode()){
                    case E : _manager.sendInterruption();
                        break;
                    case W : _manager.sendError(); //Interrupcion
                        break;
                    case P : _manager.sendPause();
                        break;
                    case C : _manager.sendContinue();
                        break;
                    case N : _manager.newProcess();
                        break;
                    case B : showtable();
                        break;
                }
            }
        };
    }

    @FXML
    private void initialize(){
        txtQuantum.setText("1");

        tcID.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcResult.setCellValueFactory(new PropertyValueFactory<>("resultado"));

        disableProcessInfo();
        btnProcessSet.setDisable(true);

        _results = FXCollections.observableArrayList();

        tvFinished.setItems(_results);

        //Iniciar tabla de nuevos
        tvNewID.setCellValueFactory(new PropertyValueFactory<>("ID"));
        tvNewStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        _news = FXCollections.observableArrayList();

        tvNew.setItems(_news);

        //Iniciar tabla de bloqueados
        tvWaitingID.setCellValueFactory(new PropertyValueFactory<>("ID"));
        tvWaitingTime.setCellValueFactory(new PropertyValueFactory<>("waitingTime"));

        _ready = FXCollections.observableArrayList();
        _waiting = FXCollections.observableArrayList();
        _all = FXCollections.observableArrayList();

        tvWaiting.setItems(_waiting);

        _manager = new Manager(this);
        _isSelectNumber = false;

        btnNumberSet.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
            @Override
            public void handle(Event event){
                try{
                    int number = Integer.parseInt(txtNumberProcess.getText());
                    int quantum = Integer.parseInt(txtQuantum.getText());

                    if(number <= 0 || quantum <= 0){
                        simpleAlertIncorrectInput("Ingresa un numero mayor a 0");
                        return;
                    }

                    btnNumberSet.setDisable(true);
                    txtNumberProcess.setDisable(true);
                    txtQuantum.setDisable(true);

                    _manager.fillProcess(number,_news,_ready,_waiting,_all);
                    _manager.setQuantum(quantum);
                    _isSelectNumber = true;

                    btnExecute.setDisable(false);

                    //enableProcessInfo();
                    //btnProcessSet.setDisable(false);

                    updateRemainingProcess();
                    ObservableList<String> aux = cbOperation.getItems();

                    aux.add("+");
                    aux.add("-");
                    aux.add("*");
                    aux.add("/");
                    aux.add("%");
                    //aux.add("^");

                    Scene scene = btnProcessSet.getScene();

                    scene.addEventHandler(KeyEvent.KEY_PRESSED,createKeyPressHandler());
                }catch(NumberFormatException e){
                    simpleAlertIncorrectInput("Ingresa un numero");
                }
            }
        });

        btnExecute.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
            @Override
            public void handle(Event event){
                btnProcessSet.setDisable(true);
                btnProcessSet.setVisible(false);
                btnExecute.setDisable(true);


                disableProcessInfo();
                _isSelectNumber = false;

                _manager.start();
            }
        });
    }
}
